/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniGrayImage extends UniRGBImage {
	private static final long serialVersionUID = -5411650085030872442L;

	/**
	 * 
	 */
	public UniGrayImage() {
	}

	/**
	 * @param width
	 * @param height
	 */
	public UniGrayImage(int width, int height) {
		super(GRAY, width, height);
	}

	/**
	 * @param width
	 * @param height
	 * @param imageData
	 */
	public UniGrayImage(int width, int height, byte[] imageData) {
		super(GRAY, width, height, imageData);
	}

	@Override
	public UniImage create(boolean copyImageData) {
		UniGrayImage img = new UniGrayImage(this.getWidth(), this.getHeight());
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
		return new UniGrayImage(width, height);
	}
}
