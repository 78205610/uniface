package uniface.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import thread.WhileThread;
import uniface.UniFaceAnalyzer;
import uniface.UniFaceEngine;
import uniface.UniFaceEngineFactory;
import uniface.UniFaceFeature;
import uniimage.UniImage;

/**
 * 人脸特征并行分析器
 * @author rechard
 *
 */
public class UniFaceParallelAnalyzer implements UniFaceAnalyzer {
	private class AnalyseTask {
		public UniImage image;
		public Object custom;
		public AnalysedCallback callback;
		public AnalyseTask(UniImage image, Object custom, AnalysedCallback callback) {
			this.image = image;
			this.custom = custom;
			this.callback = callback;
		}
	}
	private class AnalyseThread extends WhileThread {
		private final UniFaceEngine[] engine = new UniFaceEngine[1];
		public AnalyseThread(UniFaceEngine engine) {
			this.engine[0] = engine;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("AnalyseThread_" + Thread.currentThread().getId());
			AnalyseTask task = null;
			while (super.running()) {
				task = null;
				synchronized (UniFaceParallelAnalyzer.this.tasks) {
					if (UniFaceParallelAnalyzer.this.tasks.size() < 1) {
						try {
							// 等待新任务
							UniFaceParallelAnalyzer.this.tasks.wait(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					if (UniFaceParallelAnalyzer.this.tasks.size() > 0) {
						// 取走第一个任务
						task = UniFaceParallelAnalyzer.this.tasks.remove(0);
						UniFaceParallelAnalyzer.this.tasks.notifyAll();
					}
				}
				if (task != null) {
					UniFaceFeature[] features = null;
					Exception exception = null;
					try {
						synchronized (this.engine) {
							features = this.engine[0].analyse(task.image);
						}
					} catch (Exception e) {
						exception = e;
					} finally {
						task.callback.onCompleted(task.custom, features, exception);
						task.image = null;
						task = null;
					}
				}
			}
		}

		@Override
		protected void stopping() {
			synchronized (this.engine) {
				if (this.engine[0] != null) {
					UniFaceParallelAnalyzer.this.factory.destoryEngine(this.engine[0]);
				}
				this.engine[0] = null;
			}
		}

		@Override
		protected void pausing() {
		}
	}
	private final List<AnalyseTask> tasks = new LinkedList<AnalyseTask>();
	private AnalyseThread[] analyseThreads;
	private final int[] parallelNum = new int[1];
	private int maxWaitings;
	private UniFaceEngineFactory factory;
	private boolean closed;

	public int getWaitings() {
		synchronized (this.tasks) {
			return this.tasks.size();
		}
	}
	public UniFaceAnalyzer close() {
		this.closed = true;
		synchronized (this.tasks) {
			if (UniFaceParallelAnalyzer.this.tasks.size() > 0) {
				Iterator<AnalyseTask> it = UniFaceParallelAnalyzer.this.tasks.iterator();
				while (it.hasNext()) {
					AnalyseTask next = it.next();
					next.callback.onCompleted(next.custom, null, null);
				}
				UniFaceParallelAnalyzer.this.tasks.clear();
			}
		}
		synchronized (this.parallelNum) {
			if (this.analyseThreads != null) {
				for (AnalyseThread at : this.analyseThreads) {
					if (at != null) {
						at.stop();
					}
				}
				this.analyseThreads = null;
			}
		}
		return this;
	}
	private void createSearchThreads() throws Exception {
		this.analyseThreads = new AnalyseThread[this.parallelNum[0]];
		try {
			for (int i = 0; i < this.analyseThreads.length; i++) {
				this.analyseThreads[i] = new AnalyseThread(this.factory.createEngine(true));
				this.analyseThreads[i].start();
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
	public UniFaceParallelAnalyzer(int parallelNum, UniFaceEngineFactory factory, int maxWaitings) {
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
	public UniFaceAnalyzer analyse(UniImage image, Object custom, AnalysedCallback callback) throws Exception {
		synchronized (this.parallelNum) {
			if (this.closed) {
				callback.onCompleted(custom, null, null);
				return this;
			}
			if (this.analyseThreads == null) {
				this.createSearchThreads();
			}
		}
		synchronized (UniFaceParallelAnalyzer.this.tasks) {
			if (this.maxWaitings > 0) {
				while (this.tasks.size() >= this.maxWaitings && !this.closed) {
					this.tasks.wait(100);
				}
			}
			if (!this.closed) {
				UniFaceParallelAnalyzer.this.tasks.add(new AnalyseTask(image, custom, callback));
				UniFaceParallelAnalyzer.this.tasks.notifyAll();
			}
		}
		if (this.closed) {
			callback.onCompleted(custom, null, null);
		}
		return this;
	}
}
