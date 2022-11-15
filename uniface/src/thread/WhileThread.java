/**
 * 
 */
package thread;

/**
 * 服务型线程抽象类基类
 * @author rechard
 *
 */
public abstract class WhileThread implements Runnable {
	protected final Runnable threadRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				synchronized (this) {
					WhileThread.this.runningThread = Thread.currentThread();
					this.notifyAll(); // 通知start()方法线程已经启动
				}
				WhileThread.this.run();
			} finally {
				synchronized (this) {
					WhileThread.this.runningThread = null;
					this.notifyAll();
				}
			}
		}
	};
	/**
	 * 外部控制线程的状态的指令
	 */
	protected enum ControlCommand {
		Start, Pause, Stop
	}
	/**
	 * 当前线程对象
	 */
	protected Thread runningThread;
	/**
	 * 外部最后一次执行的线程状态控制指令
	 */
	protected ControlCommand lastCmd;
	protected boolean paused;
	/**
	 * 供run()方法判断当前服务线程是否已经被外部要求停止
	 * @return true:当前服务线程可以继续运行；false:当前服务线程应当停止运行
	 */
	protected boolean running() {
		boolean retval = false;
		synchronized (this.threadRunnable) {
			retval = this.runningThread == Thread.currentThread() && this.lastCmd != ControlCommand.Stop;
			if (retval && this.lastCmd == ControlCommand.Pause) {
				try {
					// 进入暂停状态
					this.paused = true;
					this.threadRunnable.wait(); // 等待start()或stop()方法通知
				} catch (InterruptedException e) {
				} finally {
					this.paused = false;
				}
			}
			retval = this.runningThread == Thread.currentThread() && this.lastCmd != ControlCommand.Stop;
		}
		return retval;
	}
	/**
	 * 在stop()方法中被调用
	 */
	abstract protected void stopping(); 
	/**
	 * 在pause()方法中被调用
	 */
	abstract protected void pausing(); 
	/**
	 * 
	 */
	public WhileThread() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 启动线程
	 * 如果当前处于暂停状态则回复运行状态
	 * @return 服务线程是否处于运行状态
	 */
	public boolean start() {
		boolean retval = false;
		synchronized (this.threadRunnable) {
			if (this.runningThread == null) {
				this.lastCmd = ControlCommand.Start;
				new Thread(this.threadRunnable).start();
				try {
					this.threadRunnable.wait(); // 等待线程启动以后发出的通知
				} catch (InterruptedException e) {
				}
			} else {
				if (this.lastCmd == ControlCommand.Pause) {
					this.threadRunnable.notifyAll(); // 通知running()不再等待
				}
				this.lastCmd = ControlCommand.Start;
			}
			retval = this.runningThread != null;
		}
		return retval;
	}
	/**
	 * 停止服务线程
	 * 不会立即强行停止服务线程，而是通知服务线程自行停止
	 * @return 是否成功
	 */
	public boolean stop() {
		boolean retval = true;
		synchronized (this.threadRunnable) {
			if (this.runningThread != null && this.lastCmd != ControlCommand.Stop) {
				this.lastCmd = ControlCommand.Stop;
				this.stopping();
				this.threadRunnable.notifyAll(); // 通知running()不再等待
			}
		}
		return retval;
	}
	/**
	 * 暂停服务线程
	 * 不会立即强行停止服务线程，而是通知服务线程自行暂停
	 * @return 是否成功
	 */
	public boolean pause() {
		boolean retval = true;
		synchronized (this.threadRunnable) {
			if (this.runningThread != null && this.lastCmd != ControlCommand.Pause) {
				this.lastCmd = ControlCommand.Pause;
				this.pausing();
			}
			retval = this.runningThread != null;
		}
		return retval;
	}
	/**
	 * 判断服务线程是否处于运行状态
	 * 暂停也属于运行状态
	 * @return
	 */
	public boolean isRunning() {
		return this.runningThread != null;
	}
	/**
	 * 判断服务线程是否已经暂停
	 * @return
	 */
	public boolean isPaused() {
		return this.paused;
	}
}
