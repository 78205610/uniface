/**
 * 
 */
package uniimage;

import java.io.Serializable;

/**
 * @author rechard
 *
 */
public abstract class UniImage implements Serializable {
	private static final long serialVersionUID = -8032488429334385802L;
	public static class PixelType {
		public final int bits;
		public PixelType(int bits) {
			this.bits = bits;
		}
	};
	
	private byte[] imageData;
	private int width;
	private int height;
	private Integer orientation;
	private Integer degree;
	private boolean mirror;
	private UniGeometryGraph insideGeometryGraph;

	public boolean isMirror() {
		return mirror;
	}
	public void setMirror(boolean mirror) {
		this.mirror = mirror;
	}
	public UniGeometryGraph getInsideGeometryGraph() {
		return insideGeometryGraph;
	}

	public void setInsideGeometryGraph(UniGeometryGraph insideGeometryGraph) {
		this.insideGeometryGraph = insideGeometryGraph;
	}

	public Integer getOrientation() {
		return orientation;
	}

	public void setOrientation(Integer orientation) {
		this.orientation = orientation;
	}

	public Integer getDegree() {
		return degree;
	}

	public void setDegree(Integer degree) {
		this.degree = degree;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * 
	 */
	public UniImage() {
	}
	public UniImage(int width, int height, byte[] imageData) {
		this.width = width;
		this.height = height;
		this.imageData = imageData;
	}
	public int pixelBits() {
		return this.getPixelType().bits;
	}
	
	protected abstract PixelType getPixelType();
	public abstract UniImage create(boolean copyImageData);
	public abstract UniImage create(int width, int height);
}
