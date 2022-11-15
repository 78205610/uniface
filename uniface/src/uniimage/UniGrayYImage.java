/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniGrayYImage extends UniRGBImage {
	private static final long serialVersionUID = -1842302897418530930L;

	/**
	 * 
	 */
	public UniGrayYImage() {
	}

	/**
	 * @param width
	 * @param height
	 */
	public UniGrayYImage(int width, int height) {
		super(GRAY, width, height);
	}

	/**
	 * @param width
	 * @param height
	 * @param imageData
	 */
	public UniGrayYImage(int width, int height, byte[] imageData) {
		super(GRAY, width, height, imageData);
	}

	@Override
	public UniImage create(boolean copyImageData) {
		UniGrayYImage img = new UniGrayYImage(this.getWidth(), this.getHeight());
		if (copyImageData) {
			img.setInsideGeometryGraph(UniGeometryGraph.clone(this.getInsideGeometryGraph()));
			img.setDegree(this.getDegree());
			img.setOrientation(this.getOrientation());
			img.setMirror(this.isMirror());
			System.arraycopy(this.getImageData(), 0, img.getImageData(), 0, this.getImageData().length);
		}
		return img;
	}

	@Override
	public UniImage create(int width, int height) {
		return new UniGrayYImage(width, height);
	}

}
