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
	enum CloseCause {
		Close, Error, End, Reopen
	}
	interface OpenCallback {
		void onOpened(UniVideoSource source, UniVideoInfo videoInfo);
		void onOpenFailed(UniVideoSource source, String message, Exception exception);
		void onClose(UniVideoSource source, UniVideoInfo videoInfo, CloseCause closeCause, Exception exception);
	}
	interface GrabCallback {
		void onVideoFrameGrabbed(UniVideoSource source, UniVideoFrame frame);
	}
	
	UniVideoSource setGrabCallback(GrabCallback callback);
	UniVideoSource setOpenCallback(OpenCallback callback);
	
	float getGrabSpeed();
	UniVideoSource setGrabSpeed(float grabSpeed);
	boolean isMirrorImage();
	UniVideoSource setMirrorImage(boolean mirrorImage);
	boolean isInvertedImage();
	UniVideoSource setInvertedImage(boolean invertedImage);
	
	UniVideoSource open(String url, Properties properties);
	UniVideoInfo getVideoInfo();
	double getGrabFrameRate();
	UniVideoFrame getVideoFrame();
	UniVideoSource close();
}
