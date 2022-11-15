/**
 * 
 */
package uniface.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import thread.WhileThread;
import uniface.UniFaceEngine;
import uniface.UniFaceEngineFactory;
import uniface.UniFaceFeature;
import uniface.UniFaceSearchResult;
import uniface.UniFaceSearcher;

/**
 * 人脸并发搜索器
 * @author rechard
 *
 */
public class UniFaceParallelSearcher implements UniFaceSearcher {
	private class SearchThread extends WhileThread {
		private class SearchTask {
			public UniFaceFeature feature;
			public UniFaceSearchResult result;
			public UniFaceSearchResult[] allResults;
			public UniFaceSearchResult returnResult;
			public SearchTask(UniFaceFeature feature, UniFaceSearchResult result, UniFaceSearchResult[] allResults, UniFaceSearchResult returnResult) {
				this.feature = feature;
				this.result = result;
				this.allResults = allResults;
				this.returnResult = returnResult;
			}
		}
		private final List<SearchTask> tasks = new LinkedList<SearchTask>();
		private final UniFaceEngine[] engine = new UniFaceEngine[1];
		public SearchThread(UniFaceEngine engine) {
			this.engine[0] = engine;
		}

		@Override
		public void run() {
			SearchTask task = null;
			while (super.running()) {
				task = null;
				synchronized (this.tasks) {
					if (this.tasks.size() < 1) {
						try {
							// 等待新任务
							this.tasks.wait(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					if (this.tasks.size() > 0) {
						// 取走第一个任务
						task = this.tasks.remove(0);
						this.tasks.notifyAll();
					}
				}
				if (task != null) {
					try {
						synchronized (this.engine) {
							if (!task.result.isCancelled()) { // 有可能开始搜索之前，其他线程已经完成了搜索，所以要判断一下
								this.engine[0].search(task.feature, task.result);
							}
						}
					} catch (Exception e) {
						task.returnResult.setException(e);
					} finally {
						if (task.result.getPass() != null && task.result.getPass()) {
							// 找到了搜索目标则取消其他线程的搜索
							for (UniFaceSearchResult result : task.allResults) {
								if (result != task.result) {
									result.cancel();
								}
							}
						}
						synchronized (task.allResults) {
							boolean completedAll = true;
							UniFaceSearchResult max = null;
							UniFaceSearchResult pass = null;
							Exception exception = null;
							int searchCount = 0;
							for (UniFaceSearchResult result : task.allResults) {
								if (!result.isCompleted()) {
									completedAll = false;
								}
								if (max == null || max.getSimilar() < result.getSimilar()) {
									max = result;
								}
								if (result.getPass() != null && result.getPass()) {
									pass = result;
								}
								if (result.getException() != null) {
									exception = result.getException();
								}
								searchCount += result.getSearchCount();
							}
							if (pass != null || completedAll) {
								UniFaceSearchResult result = pass == null ? max : pass;
								task.returnResult.setFeature(result.getFeature());
								task.returnResult.setPass(result.getPass());
								task.returnResult.setSimilar(result.getSimilar());
								task.returnResult.setException(exception);
								task.returnResult.setSearchCount(searchCount);
								task.returnResult.completed();
							}
						}
					}
				}
			}
		}

		@Override
		protected void stopping() {
			synchronized (this.tasks) {
				if (this.tasks.size() > 0) {
					Iterator<SearchTask> it = this.tasks.iterator();
					while (it.hasNext()) {
						SearchTask next = it.next();
						next.returnResult.cancel();
						next.returnResult.completed();
					}
					this.tasks.clear();
				}
			}
		}

		@Override
		protected void pausing() {
		}

		public UniFaceSearchResult search(UniFaceFeature feature, UniFaceSearchResult searchResult, UniFaceSearchResult[] allResults, UniFaceSearchResult returnResult) {
			synchronized (this.tasks) {
				this.tasks.add(new SearchTask(feature, searchResult, allResults, returnResult));
				if (this.tasks.size() == 1) {
					this.tasks.notifyAll();
				}
			}
			return searchResult;
		}

		public SearchThread registerFaceFeature(UniFaceFeature feature) throws Exception {
			synchronized (this.engine) {
				this.engine[0].registerFaceFeature(feature);
			}
			return this;
		}

		public SearchThread updateFaceFeature(UniFaceFeature feature) throws Exception {
			synchronized (this.engine) {
				this.engine[0].updateFaceFeature(feature);
			}
			return this;
		}

		public UniFaceFeature removeFaceFeature(Object keyId) throws Exception {
			synchronized (this.engine) {
				return this.engine[0].removeFaceFeature(keyId);
			}
		}

		public int getFaceFeatureCount() throws Exception {
			synchronized (this.engine) {
				return this.engine[0].getFaceFeatureCount();
			}
		}
	}
	private SearchThread[] searchThreads;
	private final int[] parallelNum = new int[1];
	private UniFaceEngineFactory factory;
	/**
	 * 
	 */
	public UniFaceParallelSearcher(int parallelNum, UniFaceEngineFactory factory) {
		if (parallelNum < 1) {
			int cpus = Runtime.getRuntime().availableProcessors();
			this.parallelNum[0] = cpus > 2 ? cpus - 2 : 1;
		} else {
			this.parallelNum[0] = parallelNum;
		}
		this.factory = factory;
	}
	
	/**
	 * 涉及的两个结果对象中，只要有一个取消了，就判定为取消
	 */
	private class UniFaceSearchDoubleCancelResult extends UniFaceSearchResult {
		UniFaceSearchResult result;
		public UniFaceSearchDoubleCancelResult(UniFaceSearchResult result) {
			this.result = result;
		}
		@Override
		public boolean isCancelled() {
			if (this.result == null) {
				return super.isCancelled();
			}
			return super.isCancelled() || this.result.isCancelled();
		}
	}
	
	@Override
	public UniFaceSearchResult search(UniFaceFeature feature, UniFaceSearchResult searchResult) throws Exception {
		UniFaceSearchResult returnResult = searchResult == null ? new UniFaceSearchResult() : searchResult;
		synchronized (this.parallelNum) {
			if (this.searchThreads == null) {
				this.createSearchThreads();
			}
			UniFaceSearchResult[] results = new UniFaceSearchResult[this.searchThreads.length];
			for (int i = 0; i < this.searchThreads.length; i++) {
				results[i] = new UniFaceSearchDoubleCancelResult(searchResult);
			}
			for (int i = 0; i < this.searchThreads.length; i++) {
				this.searchThreads[i].search(feature, results[i], results, returnResult);
			}
		}
		if (searchResult == null) {
			returnResult.waitForComplete(-1);
			if (returnResult.getException() != null) {
				throw returnResult.getException();
			}
		}
		return returnResult;
	}

	private int nextRegisterIndex;
	@Override
	public UniFaceSearcher registerFaceFeature(UniFaceFeature feature) throws Exception {
		synchronized (this.parallelNum) {
			if (this.searchThreads == null) {
				this.createSearchThreads();
			}
			this.searchThreads[this.nextRegisterIndex].registerFaceFeature(feature);
			this.nextRegisterIndex++;
			if (this.nextRegisterIndex >= this.searchThreads.length) {
				this.nextRegisterIndex = 0;
			}
		}
		return this;
	}

	@Override
	public UniFaceSearcher updateFaceFeature(UniFaceFeature feature) throws Exception {
		synchronized (this.parallelNum) {
			if (this.searchThreads == null) {
				this.createSearchThreads();
			}
			for (SearchThread st : this.searchThreads) {
				try {
					st.updateFaceFeature(feature);
					break;
				} catch (Exception e) {
				}
			}
		}
		return this;
	}

	@Override
	public UniFaceFeature removeFaceFeature(Object keyId) throws Exception {
		UniFaceFeature retval = null;
		synchronized (this.parallelNum) {
			if (this.searchThreads == null) {
				this.createSearchThreads();
			}
			for (SearchThread st : this.searchThreads) {
				retval = st.removeFaceFeature(keyId);
				if (retval != null) {
					break;
				}
			}
		}
		return retval;
	}

	@Override
	public int getFaceFeatureCount() throws Exception {
		int count = 0;
		synchronized (this.parallelNum) {
			if (this.searchThreads == null) {
				this.createSearchThreads();
			}
			for (SearchThread st : this.searchThreads) {
				count += st.getFaceFeatureCount();
			}
		}
		return count;
	}
	public UniFaceSearcher clear() {
		synchronized (this.parallelNum) {
			if (this.searchThreads != null) {
				for (SearchThread st : this.searchThreads) {
					if (st != null) {
						st.stop();
						this.factory.destoryEngine(st.engine[0]);
					}
				}
				this.searchThreads = null;
			}
		}
		return this;
	}
	private void createSearchThreads() throws Exception {
		this.searchThreads = new SearchThread[this.parallelNum[0]];
		try {
			for (int i = 0; i < this.searchThreads.length; i++) {
				this.searchThreads[i] = new SearchThread(this.factory.createEngine(true));
				this.searchThreads[i].start();
			}
		} catch (Exception e) {
			this.clear();
			throw e;
		}
	}
}
