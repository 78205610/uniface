/**
 * 
 */
package uniface.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import thread.WhileThread;
import uniface.UniFaceComparator;
import uniface.UniFaceEngine;
import uniface.UniFaceEngineFactory;
import uniface.UniFaceFeature;

/**
 * 人脸特征并行比对器
 * @author rechard
 *
 */
public class UniFaceParallelComparator implements UniFaceComparator {
	private class CompareTask {
		public UniFaceFeature feature1;
		public UniFaceFeature feature2;
		public Object custom;
		public ComparedCallback callback;
		public CompareTask(UniFaceFeature feature1, UniFaceFeature feature2, Object custom,
				ComparedCallback callback) {
			this.feature1 = feature1;
			this.feature2 = feature2;
			this.custom = custom;
			this.callback = callback;
		}
	}
	private class CompareThread extends WhileThread {
		private final UniFaceEngine[] engine = new UniFaceEngine[1];
		public CompareThread(UniFaceEngine engine) {
			this.engine[0] = engine;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("AnalyseThread_" + Thread.currentThread().getId());
			CompareTask task = null;
			while (super.running()) {
				task = null;
				synchronized (UniFaceParallelComparator.this.tasks) {
					if (UniFaceParallelComparator.this.tasks.size() < 1) {
						try {
							// 等待新任务
							UniFaceParallelComparator.this.tasks.wait(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					if (UniFaceParallelComparator.this.tasks.size() > 0) {
						// 取走第一个任务
						task = UniFaceParallelComparator.this.tasks.remove(0);
						UniFaceParallelComparator.this.tasks.notifyAll();
					}
				}
				if (task != null) {
					Float similar = null;
					try {
						synchronized (this.engine) {
							similar = this.engine[0].compare(task.feature1, task.feature2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						task.callback.onCompleted(task.custom, task.feature1, task.feature2, similar);
						task = null;
					}
				}
			}
		}

		@Override
		protected void stopping() {
			synchronized (this.engine) {
				if (this.engine[0] != null) {
					UniFaceParallelComparator.this.factory.destoryEngine(this.engine[0]);
				}
				this.engine[0] = null;
			}
		}

		@Override
		protected void pausing() {
		}
	}

	private final List<CompareTask> tasks = new LinkedList<CompareTask>();
	private CompareThread[] compareThreads;
	private final int[] parallelNum = new int[1];
	private int maxWaitings;
	private UniFaceEngineFactory factory;
	private boolean closed;
	public int getWaitings() {
		synchronized (this.tasks) {
			return this.tasks.size();
		}
	}
	public UniFaceComparator close() {
		this.closed = true;
		synchronized (this.tasks) {
			if (this.tasks.size() > 0) {
				Iterator<CompareTask> it = this.tasks.iterator();
				while (it.hasNext()) {
					CompareTask next = it.next();
					next.callback.onCompleted(next.custom, null, null, null);
				}
				this.tasks.clear();
			}
		}
		synchronized (this.parallelNum) {
			if (this.compareThreads != null) {
				for (CompareThread at : this.compareThreads) {
					if (at != null) {
						at.stop();
					}
				}
				this.compareThreads = null;
			}
		}
		return this;
	}
	private void createSearchThreads() throws Exception {
		this.compareThreads = new CompareThread[this.parallelNum[0]];
		try {
			for (int i = 0; i < this.compareThreads.length; i++) {
				this.compareThreads[i] = new CompareThread(this.factory.createEngine(false));
				this.compareThreads[i].start();
			}
		} catch (Exception e) {
			this.close();
			throw e;
		}
	}
	/**
	 * @param parallelNum 最大并发数量，如果<1则根据CPU核心数量自动设置
	 * @param factory 人脸引擎工厂，一般采用池化工厂UniFaceEnginePoolFactory
	 * @param maxWaitings 在队列中等待异步执行任务的最大数量，<1表示不限
	 */
	public UniFaceParallelComparator(int parallelNum, UniFaceEngineFactory factory, int maxWaitings) {
		if (parallelNum < 1) {
			int cpus = Runtime.getRuntime().availableProcessors();
			this.parallelNum[0] = cpus > 2 ? cpus - 2 : 1;
		} else {
			this.parallelNum[0] = parallelNum;
		}
		this.factory = factory;
		this.maxWaitings = maxWaitings;
	}

	@Override
	public UniFaceComparator compare(UniFaceFeature feature1, UniFaceFeature feature2, Object custom,
			ComparedCallback callback) throws Exception {
		synchronized (this.parallelNum) {
			if (this.closed) {
				callback.onCompleted(custom, null, null, null);
				return this;
			}
			if (this.compareThreads == null) {
				this.createSearchThreads();
			}
		}
		synchronized (this.tasks) {
			if (this.maxWaitings > 0) {
				while (this.tasks.size() >= this.maxWaitings && !this.closed) {
					this.tasks.wait(100);
				}
			}
			if (!this.closed) {
				this.tasks.add(new CompareTask(feature1, feature2, custom, callback));
				this.tasks.notifyAll();
			}
		}
		if (this.closed) {
			callback.onCompleted(custom, null, null, null);
		}
		return this;
	}

}
