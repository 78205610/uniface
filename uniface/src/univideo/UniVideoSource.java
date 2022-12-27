/**
 * 
 */
package univideo;

import java.util.Properties;

/**
 * @author rechard
 *
 */
public interface UniVideoSource {
	/**
	 * 视频源关闭原因
	 */
	enum CloseCause {
		Close, Error, End, Reopen
	}
	/**
	 * 视频源开/关事件回调
	 */
	interface OpenCallback {
		/**
		 * 视频源打开成功时被调用
		 * @param source
		 * @param videoInfo
		 */
		void onOpened(UniVideoSource source, UniVideoInfo videoInfo);
		/**
		 * 视频源打开失败时被调用
		 * @param source
		 * @param message
		 * @param exception
		 */
		void onOpenFailed(UniVideoSource source, String message, Exception exception);
		/**
		 * 视频源关闭时被调用
		 * @param source
		 * @param videoInfo
		 * @param closeCause
		 * @param exception
		 */
		void onClose(UniVideoSource source, UniVideoInfo videoInfo, CloseCause closeCause, Exception exception);
	}
	/**
	 * 视频源抓帧回调
	 */
	interface GrabCallback {
		/**
		 * 抓取到一帧图像时被调用
		 * @param source
		 * @param frame
		 */
		void onVideoFrameGrabbed(UniVideoSource source, UniVideoFrame frame);
	}
	
	UniVideoSource setGrabCallback(GrabCallback callback);
	UniVideoSource setOpenCallback(OpenCallback callback);
	
	/**
	 * 抓帧速率<br>
	 * 指定大于0的数值表示以视频源的原始帧率为基础计算抓帧速率。例如：原始帧率30帧/秒，设置值0.5，实际抓帧速率则为15帧/秒；设置值2，实际抓帧速率为60帧/秒；<br>
	 * 指定小于0的数值表示直接指定抓帧速率。例如：设置值-15，实际抓帧速率为15帧/秒。
	 * @return
	 */
	float getGrabSpeed();
	/**
	 * 抓帧速率<br>
	 * 指定大于0的数值表示以视频源的原始帧率为基础计算抓帧速率。例如：原始帧率30帧/秒，设置值0.5，实际抓帧速率则为15帧/秒；设置值2，实际抓帧速率为60帧/秒；<br>
	 * 指定小于0的数值表示直接指定抓帧速率。例如：设置值-15，实际抓帧速率为15帧/秒。
	 * @param grabSpeed
	 * @return
	 */
	UniVideoSource setGrabSpeed(float grabSpeed);
	/**
	 * 是否对抓取的帧图进行镜像处理
	 * @return
	 */
	boolean isMirrorImage();
	/**
	 * 是否对抓取的帧图进行镜像处理
	 * @param mirrorImage
	 * @return
	 */
	UniVideoSource setMirrorImage(boolean mirrorImage);
	/**
	 * 是否对抓取的帧图进行翻转处理
	 * @return
	 */
	boolean isInvertedImage();
	/**
	 * 是否对抓取的帧图进行翻转处理
	 * @param invertedImage
	 * @return
	 */
	UniVideoSource setInvertedImage(boolean invertedImage);
	
	/**
	 * 开启视频源
	 * @param url 视频流URL、视频文件、摄像头编号
	 * @param properties 开启视频源的参数
	 * @return
	 */
	UniVideoSource open(String url, Properties properties);
	/**
	 * 当前已开启视频源的视频基本信息
	 * @return
	 */
	UniVideoInfo getVideoInfo();
	/**
	 * 运行时的实际抓帧速率
	 * @return
	 */
	double getGrabFrameRate();
	/**
	 * 当前抓取到的视频帧
	 * @return
	 */
	UniVideoFrame getVideoFrame();
	/**
	 * 关闭视频源
	 * @return
	 */
	UniVideoSource close();
}
