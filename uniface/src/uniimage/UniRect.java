/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniRect extends UniSize {
	
	public int x;
    public int y;
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	/**
	 * 
	 */
	public UniRect() {
	}
	public UniRect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	public UniPoint leftTop() {
		return new UniPoint(this.x, this.y);
	}
	public UniPoint rightTop() {
		return new UniPoint(this.x + this.width - 1, this.y);
	}
	public UniPoint rightBottom() {
		return new UniPoint(this.x + this.width - 1, this.y + this.height - 1);
	}
	public UniPoint leftBottom() {
		return new UniPoint(this.x, this.y + this.height - 1);
	}
	public UniPoint origin() {
		return new UniPoint(this.x + (this.width >> 1), this.y + (this.height >> 1));
	}
	public UniSize size() {
		return this;
	}
	public UniRect switchWH() {
		super.switchWH();
		return this;
	}
	public UniRect clone() {
		return new UniRect(this.x, this.y, this.width, this.height);
	}
	public boolean in(int left, int top, int right, int bottom) {
		if (!UniPoint.in(this.x, this.y, left, top, right, bottom)) {
			return false;
		}
		if (!UniPoint.in(this.x + this.width - 1, this.y, left, top, right, bottom)) {
			return false;
		}
		if (!UniPoint.in(this.x + this.width - 1, this.y + this.height - 1, left, top, right, bottom)) {
			return false;
		}
		if (!UniPoint.in(this.x, this.y + this.height - 1, left, top, right, bottom)) {
			return false;
		}
		return true;
	}
	public boolean in(UniRect rect) {
		return this.in(rect.x, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);
	}
}
