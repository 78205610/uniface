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
import univideo.UniVideoFrame;
import univideo.UniVideoSource;

/**
 * @author rechard
 *
 */
public class UniFaceRecognitionService implements UniVideoSource.GrabCallback {
	public interface RecognitionCallback {
		void onRecognized(UniVideoSource source, UniVideoFrame frame, UniFaceFeature[] features);
	}
	private class RecognitionVideoFrame {
		public UniVideoSource source;
		public UniVideoFrame frame;
		public UniFaceFeature[] features;
		public boolean recognized;
		public RecognitionVideoFrame(UniVideoSource source, UniVideoFrame frame) {
			this.source = source;
			this.frame = frame;
		}
	}

	/**
	 * 
	 */
	public UniFaceRecognitionService() {
		// TODO Auto-generated constructor stub
	}

	private final List<RecognitionVideoFrame> frameQueue = new LinkedList<RecognitionVideoFrame>();
	private long lastGrabbedTime;
	private long lastGrabIntervalTime;
	private boolean grabbedNew;
	@Override
	public void onVideoFrameGrabbed(UniVideoSource source, UniVideoFrame frame) {
		if (this.lastGrabbedTime > 0) {
			this.lastGrabIntervalTime = System.currentTimeMillis() - this.lastGrabbedTime;
			if (this.lastGrabIntervalTime > 200) {
				this.lastGrabIntervalTime = 200;
			}
		}
		this.lastGrabbedTime = System.currentTimeMillis();
		int maxQueue = 30;
		if (this.skipFrames >= 0) {
			maxQueue = this.skipFrames + 1;
		}
		synchronized (this.frameQueue) {
			while (this.frameQueue.size() >= maxQueue) {
				try {
					this.frameQueue.wait(10);
				} catch (InterruptedException e) {
					return;
				}
			}
			this.frameQueue.add(new RecognitionVideoFrame(source, frame));
			this.grabbedNew = true;
			this.buffedCount = this.frameQueue.size();
			this.frameQueue.notifyAll(); // 通知识别线程有新帧可识别
		}
	}
	private RecognitionCallback recognitionCallback;

	private long lastCallbackTime;
	protected void callbackThreadLoop() {
		RecognitionVideoFrame recognizedFrame = null;
		synchronized (this.frameQueue) {
			if (this.frameQueue.size() > 0 && this.frameQueue.get(0).recognized) {
				// 队列中的第一帧已经识别过了
				long callbackIntervalTime = System.currentTimeMillis() - this.lastCallbackTime;
				if (callbackIntervalTime >= this.lastGrabIntervalTime || this.grabbedNew) {
					this.grabbedNew = false;
					// 距上次回调的间隔时间已经超过了抓帧间隔时间
					// 取出第一帧
					recognizedFrame = this.frameQueue.remove(0);
					this.buffedCount = this.frameQueue.size();
				} else {
					// 计算一个合理的等待时间，等待队列中出现已识别帧
					long waitTime = 0;
					if (this.lastGrabIntervalTime > 0) {
						waitTime = this.lastGrabIntervalTime - callbackIntervalTime;
						if (waitTime > 50) {
							waitTime = 50;
						}
						waitTime /= 4;
					}
					if (waitTime < 1) {
						waitTime = 1;
					}
					try {
						this.frameQueue.wait(waitTime);
					} catch (InterruptedException e) {
					}
					return;
				}
			} else {
				try {
					this.frameQueue.wait(10);
				} catch (InterruptedException e) {
				}
				return;
			}
		}
		if (recognizedFrame != null) {
			this.lastCallbackTime = System.currentTimeMillis();
			RecognitionCallback callback = this.recognitionCallback;
			if (callback != null) {
				// 回调
				callback.onRecognized(recognizedFrame.source, recognizedFrame.frame, recognizedFrame.features);
			}
		}
	}
	private boolean enableRecognize = true;
	public boolean isEnableRecognize() {
		return enableRecognize;
	}
	public void setEnableRecognize(boolean enableRecognize) {
		this.enableRecognize = enableRecognize;
	}

	private long recognizeCount;
	private int skipFrames = -1;
	protected void recognitionThreadLoop(UniFaceEngine engine) {
		RecognitionVideoFrame target = null;
		synchronized (this.frameQueue) {
			if (this.frameQueue.size() > 0) {
				if (this.skipFrames < 0) {
					target = this.frameQueue.get(this.frameQueue.size() - 1);
				} else {
					int count = 0;
					Iterator<RecognitionVideoFrame> it = this.frameQueue.iterator();
					while (it.hasNext()) {
						RecognitionVideoFrame next = it.next();
						if (next.recognized) {
							count = 0;
						} else {
							if (count == this.skipFrames) {
								target = next;
								break;
							}
							count++;
						}
					}
				}
				if (target == null || target.recognized) {
					// 暂时无帧可识别
					try {
						this.frameQueue.wait(10);
					} catch (InterruptedException e) {
					}
					return;
				}
			} else {
				try {
					this.frameQueue.wait(10);
				} catch (InterruptedException e) {
				}
				return;
			}
		}
		try {
			// 识别最后一帧
			if (this.enableRecognize) {
				target.features = engine.analyse(target.frame.getImage());
			}
			this.recognizeCount++;
		} catch (Exception e) {
		} finally {
			synchronized (this.frameQueue) {
				// 无论结果如何都表示last帧已经识别过了
				target.recognized = true;
				Iterator<RecognitionVideoFrame> it = this.frameQueue.iterator();
				while (it.hasNext()) {
					// last之前的帧都标记为已识别
					RecognitionVideoFrame next = it.next();
					if (next == target) {
						break;
					}
					if (!next.recognized) {
						next.recognized = true;
						if (target.frame.getTimestamp() - next.frame.getTimestamp() < 300) {
							next.features = target.features;
						}
					}
				}
				this.frameQueue.notifyAll(); // 通知回调线程有新的已识别帧可供推送
			}
		}
	}
	
	private UniFaceEngineFactory faceEngineFactory;

	private final WhileThread callbackThread = new WhileThread() {
		@Override
		public void run() {
			while (super.running()) {
				callbackThreadLoop();
			}
		}
		@Override
		protected void stopping() {
		}
		@Override
		protected void pausing() {
		}};
	private final WhileThread recognitionThread = new WhileThread() {
		@Override
		public void run() {
			UniFaceEngine engine = null;
			UniFaceEngineFactory factory = null;
			try {
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
			}
		}
		@Override
		protected void stopping() {
		}
		@Override
		protected void pausing() {
		}};
	public UniFaceRecognitionService start() {
		synchronized (this.frameQueue) {
			this.frameQueue.clear();
		}
		this.callbackThread.start();
		this.recognitionThread.start();
		return this;
	}
	public UniFaceRecognitionService stop() {
		this.callbackThread.stop();
		this.recognitionThread.stop();
		synchronized (this.frameQueue) {
			this.frameQueue.clear();
			this.frameQueue.notifyAll();
		}
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
	private int buffedCount;
	public int getBuffedCount() {
		return buffedCount;
	}
}
