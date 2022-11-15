/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniSize {
    public int width;
    public int height;

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
	public UniSize() {
	}
	public UniSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	public UniSize switchWH() {
		int w = this.height;
		this.height = this.width;
		this.width = w;
		return this;
	}
	public UniSize clone() {
		return new UniSize(this.width, this.height);
	}
}
