/**
 * 
 */
package univideo;

import uniimage.UniRGBImage;

/**
 * @author rechard
 *
 */
public class UniVideoFrame {
	private UniRGBImage image;
	private long timestamp;
	private int index;
	private Object rawFrame;

	public Object getRawFrame() {
		return rawFrame;
	}

	public void setRawFrame(Object rawFrame) {
		this.rawFrame = rawFrame;
	}

	public UniRGBImage getImage() {
		return image;
	}

	public void setImage(UniRGBImage image) {
		this.image = image;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * 
	 */
	public UniVideoFrame() {
		// TODO Auto-generated constructor stub
	}

}
