/**
 * 
 */
package uniface;

/**
 * 人脸特征搜索结果
 * @author rechard
 *
 */
public class UniFaceSearchResult {

	/**
	 * 搜索到的特征
	 */
	private UniFaceFeature feature;
	/**
	 * 与目标特征的相似度
	 */
	private float similar;
	/**
	 * 是否与目标特征匹配通过
	 * null时表示尚未完成搜索；
	 * 判定阈值由搜索器实现类的配置属性决定
	 */
	private Boolean pass;
	/**
	 * 决定当前搜索结果的搜索过程是否已经完成
	 */
	private boolean completed;
	/**
	 * 决定当前搜索结果的搜索过程是否已经被取消
	 */
	private boolean cancelled;
	/**
	 * 搜索过程中出现的异常
	 */
	private Exception exception;
	/**
	 * 搜索过程中总共比对了多少个特征
	 */
	private int searchCount;
	public int getSearchCount() {
		return searchCount;
	}
	public void setSearchCount(int searchCount) {
		this.searchCount = searchCount;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public boolean isCompleted() {
		return this.completed;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public Boolean getPass() {
		return pass;
	}
	public void setPass(Boolean pass) {
		this.pass = pass;
	}
	public UniFaceFeature getFeature() {
		return feature;
	}
	public void setFeature(UniFaceFeature feature) {
		this.feature = feature;
	}
	public float getSimilar() {
		return similar;
	}
	public void setSimilar(float similar) {
		this.similar = similar;
	}
	/**
	 * 
	 */
	public UniFaceSearchResult() {
	}
	public UniFaceSearchResult(UniFaceFeature feature, float similar) {
		this.feature = feature;
		this.similar = similar;
	}
	/**
	 * 等待决定当前搜索结果的搜索过程执行完成
	 * @param timeout 等待超时毫秒值
	 * @return 当前结果对象
	 */
	public UniFaceSearchResult waitForComplete(long timeout) {
		synchronized (this) {
			if (!this.isCancelled() && !this.isCompleted()) {
				try {
					if (timeout > 0) {
						this.wait(timeout);
					} else if (timeout < 0) {
						this.wait();
					}
				} catch (InterruptedException e) {
				}
			}
		}
		return this;
	}
	/**
	 * 取消决定当前搜索结果的搜索过程
	 * @return 当前结果对象
	 */
	public UniFaceSearchResult cancel() {
		synchronized (this) {
			this.cancelled = true;
			this.notifyAll();
		}
		return this;
	}
	/**
	 * 搜索完成（由执行搜索的过程在搜索完成时调用）
	 * @return 当前结果对象
	 */
	public UniFaceSearchResult completed() {
		synchronized (this) {
			this.completed = true;
			if (this.pass == null) {
				this.pass = false;
			}
			this.notifyAll();
		}
		return this;
	}
}
