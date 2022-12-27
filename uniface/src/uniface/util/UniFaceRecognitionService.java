/**
 * 
 */
package uniface.util;

import java.util.LinkedList;
import java.util.List;

import thread.WhileThread;
import uniface.UniFaceEngine;
import uniface.UniFaceEngineFactory;
import uniface.UniFaceFeature;
import univideo.UniVideoFrame;
import univideo.UniVideoSource;

/**
 * @author rechard
 *
 */
public class UniFaceRecognitionService implements UniVideoSource.GrabCallback {
	protected final static UniFaceFeature[] NullFeatures = new UniFaceFeature[0];
	public interface RecognitionCallback {
		void onRecognized(UniVideoSource source, UniVideoFrame frame, UniFaceFeature[] features);
	}

	/**
	 * 
	 */
	public UniFaceRecognitionService() {
		// TODO Auto-generated constructor stub
	}

	protected final List<UniVideoFrame> recognitionQueue = new LinkedList<UniVideoFrame>();
	protected int recognitionQueueSize;
	protected final List<UniVideoFrame> callbackQueue = new LinkedList<UniVideoFrame>();
	protected int callbackQueueSize;
	protected UniVideoSource currentVideoSource;
	protected int queueLimit = 30;
	@Override
	public void onVideoFrameGrabbed(UniVideoSource source, UniVideoFrame frame) {
		while (this.isRunning()) {
			synchronized (this.recognitionQueue) {
				if (this.getBuffedCount() >= this.queueLimit) {
					try {
						this.recognitionQueue.wait(10); // 等待队列可追加
					} catch (InterruptedException e) {
						break;
					}
				} else {
					this.currentVideoSource = source;
					this.recognitionQueue.add(frame);
					this.recognitionQueueSize = this.recognitionQueue.size();
					this.recognitionQueue.notifyAll(); // 通知识别线程有新帧待识别
					break;
				}
			}
		}
		this.clear();
	}
	protected RecognitionCallback recognitionCallback;

	protected void callbackThreadLoop() {
		UniVideoFrame frame = null;
		synchronized (this.callbackQueue) {
			if (this.callbackQueue.size() < 1) {
				try {
					this.callbackQueue.wait(10); // 等待新帧
				} catch (InterruptedException e) {
				}
			}
			if (this.callbackQueue.size() > 0) {
				frame = this.callbackQueue.remove(0);
				this.callbackQueueSize = this.callbackQueue.size();
				this.callbackQueue.notifyAll(); // 通知回调队列可以接收新帧
			}
		}
		if (frame != null) {
			RecognitionCallback callback = this.recognitionCallback;
			if (callback != null) {
				callback.onRecognized(this.currentVideoSource, frame, frame.getTimestamp() - this.lastRecognizeTimestamp > 500 ? NullFeatures : this.lastFeatures);
			}
		}
	}
	protected boolean enableRecognize = true;
	public boolean isEnableRecognize() {
		return enableRecognize;
	}
	public void setEnableRecognize(boolean enableRecognize) {
		this.enableRecognize = enableRecognize;
	}

	protected UniFaceFeature[] lastFeatures;
	protected long lastRecognizeTimestamp;
	protected long recognizeCount;
	protected int skipFrames = -1;
	protected int skipCount;
	protected void recognitionThreadLoop(UniFaceEngine engine) {
		UniVideoFrame frame = null;
		do {
			frame = null;
			synchronized (this.recognitionQueue) {
				if (this.recognitionQueue.size() < 1) {
					try {
						this.recognitionQueue.wait(10); // 尝试等待新帧
					} catch (InterruptedException e) {}
				}
				if (this.recognitionQueue.size() > 0) {
					frame = this.recognitionQueue.remove(0);
					this.recognitionQueueSize = this.recognitionQueue.size();
					this.recognitionQueue.notifyAll(); // 通知进队线程可以进行追加了
				}
			}
			if (frame != null) {
				// this.skipFrames>=0则按照skipFrames的规则进行跳帧识别
				if (this.enableRecognize && this.skipFrames >= 0) {
					if (this.skipCount >= this.skipFrames) {
						this.skipCount = 0;
						try {
							this.recognizeCount++;
							this.lastRecognizeTimestamp = frame.getTimestamp();
							this.lastFeatures = engine.analyse(frame.getImage());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						this.skipCount++;
					}
				}
				synchronized(this.callbackQueue) {
					this.callbackQueue.add(frame);
					this.callbackQueueSize = this.callbackQueue.size();
					this.callbackQueue.notifyAll(); // 通知回调线程有新帧
				}
				if (this.enableRecognize && this.skipFrames < 0 && this.getBuffedCount() < this.queueLimit - 2) {
					try {
						this.recognizeCount++;
						this.lastRecognizeTimestamp = frame.getTimestamp();
						this.lastFeatures = engine.analyse(frame.getImage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} while(frame != null);
	}
	
	protected UniFaceEngineFactory faceEngineFactory;

	protected final WhileThread callbackThread = new WhileThread() {
		@Override
		public void run() {
			try {
				synchronized(UniFaceRecognitionService.this.backThreadsRunning) {
					UniFaceRecognitionService.this.backThreadsRunning[1] = true;
				}
				while (super.running()) {
					callbackThreadLoop();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized(UniFaceRecognitionService.this.backThreadsRunning) {
					UniFaceRecognitionService.this.backThreadsRunning[1] = false;
					UniFaceRecognitionService.this.clear();
				}
			}
		}
		@Override
		protected void stopping() {
		}
		@Override
		protected void pausing() {
		}};
	protected final WhileThread recognitionThread = new WhileThread() {
		@Override
		public void run() {
			UniFaceEngine engine = null;
			UniFaceEngineFactory factory = null;
			try {
				synchronized(UniFaceRecognitionService.this.backThreadsRunning) {
					UniFaceRecognitionService.this.backThreadsRunning[0] = true;
				}
				UniFaceRecognitionService.this.lastFeatures = UniFaceRecognitionService.NullFeatures;
				UniFaceRecognitionService.this.skipCount = UniFaceRecognitionService.this.skipFrames;
				while (super.running()) {
					if (engine == null) {
						factory = faceEngineFactory;
						if (factory != null) {
							engine = factory.createEngine(true);
						}
					}
					if (engine == null) {
						synchronized (super.threadRunnable) {
							super.threadRunnable.wait(100);
						}
						continue;
					}
					recognitionThreadLoop(engine);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (factory != null && engine != null) {
					factory.destoryEngine(engine);
				}
				synchronized(UniFaceRecognitionService.this.backThreadsRunning) {
					UniFaceRecognitionService.this.backThreadsRunning[0] = false;
					UniFaceRecognitionService.this.clear();
				}
			}
		}
		@Override
		protected void stopping() {
		}
		@Override
		protected void pausing() {
		}};
	protected final boolean[] backThreadsRunning = new boolean[2];
	protected boolean isRunning() {
		for (boolean running : this.backThreadsRunning) {
			if (running) {
				return true;
			}
		}
		return false;
	}
	protected void clear() {
		if (this.isRunning()) {
			return;
		}
		synchronized (this.recognitionQueue) {
			this.recognitionQueue.clear();
		}
	}
	public UniFaceRecognitionService start() {
		this.callbackThread.start();
		this.recognitionThread.start();
		return this;
	}
	public UniFaceRecognitionService stop() {
		this.callbackThread.stop();
		this.recognitionThread.stop();
		return this;
	}
	public UniFaceRecognitionService setRecognitionCallback(RecognitionCallback recognitionCallback) {
		this.recognitionCallback = recognitionCallback;
		return this;
	}
	public UniFaceRecognitionService setFactory(UniFaceEngineFactory factory) {
		this.faceEngineFactory = factory;
		return this;
	}
	public int getSkipFrames() {
		return this.skipFrames;
	}
	public UniFaceRecognitionService setSkipFrames(int skipFrames) {
		this.skipFrames = skipFrames;
		return this;
	}
	public long getRecognizeCount() {
		return this.recognizeCount;
	}
	public int getBuffedCount() {
		return this.recognitionQueueSize + this.callbackQueueSize;
	}
}
