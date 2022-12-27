/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniRGBImage extends UniImage {
	private static final long serialVersionUID = 3545049258516935169L;
	
	public static class RGBType extends PixelType {
		private static final long serialVersionUID = -208411032996400732L;
		public final int ri;
		public final int gi;
		public final int bi;
		public final int ai;
		public RGBType(int bits, int ri, int gi, int bi, int ai) {
			super(bits);
			this.ri = ri;
			this.gi = gi;
			this.bi = bi;
			this.ai = ai;
		}
		public boolean equals(RGBType type) {
			if (this.ri != type.ri || this.gi != type.gi || this.bi != type.bi || this.ai != type.ai) {
				return false;
			}
			return super.equals(type);
		}
	}
	public final static RGBType RGB = new RGBType(24, 0, 1, 2, -1);
	public final static RGBType GBR = new RGBType(24, 2, 1, 0, -1);
	public final static RGBType GRAY = new RGBType(8, 0, 0, 0, -1);
	
	private RGBType type = GBR;
	public RGBType getType() {
		return type;
	}
	public void setType(RGBType type) {
		this.type = type;
	}
	/**
	 * 
	 */
	public UniRGBImage() {
	}
	public UniRGBImage(RGBType type, int width, int height) {
		this.type = type;
		this.setWidth(width);
		this.setHeight(height);
		this.setImageData(new byte[width * height * type.bits / 8]);
	}
	public UniRGBImage(RGBType type, int width, int height, byte[] imageData) {
		super(width, height, imageData);
		this.type = type;
	}
	public UniRGBImage(int width, int height, byte[] imageData) {
		this(GBR, width, height, imageData);
	}
	public UniRGBImage(int width, int height) {
		this(GBR, width, height);
	}
	@Override
	public UniImage create(boolean copyImageData) {
		UniRGBImage img = new UniRGBImage(this.type, this.getWidth(), this.getHeight());
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
		return new UniRGBImage(this.type, width, height);
	}
	@Override
	protected PixelType getPixelType() {
		return this.type;
	}
}
