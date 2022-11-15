/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniRectPoints implements UniGeometry {
	private static final long serialVersionUID = 5702153154596841447L;
	
	private final UniPoint[] points = new UniPoint[4];

	public UniPoint[] getPoints() {
		return this.points;
	}

	public UniPoint getLeftTop() {
		return this.points[0];
	}

	public UniPoint getRightTop() {
		return this.points[1];
	}

	public UniPoint getLeftBottom() {
		return this.points[2];
	}

	public UniPoint getRightBottom() {
		return this.points[3];
	}

	/**
	 * 
	 */
	public UniRectPoints(int x, int y, int width, int height) {
		this.points[0] = new UniPoint(x, y);
		this.points[1] = new UniPoint(x + width - 1, y);
		this.points[2] = new UniPoint(x, y + height - 1);
		this.points[3] = new UniPoint(x + width - 1, y + height - 1);
	}
}
