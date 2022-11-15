/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniPoint implements UniGeometry {
	private static final long serialVersionUID = 7854376064274616212L;
	
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
	public UniPoint() {
	}
	public UniPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public UniPoint switchXY() {
		int x = this.y;
		this.y = this.x;
		this.x = x;
		return this;
	}
	public UniPoint clone() {
		return new UniPoint(this.x, this.y);
	}
	public double distance(UniPoint point) {
		return UniPoint.distance(this, point);
	}
	public UniPoint rotate(int originX, int originY, int degree) {
		UniPoint.rotate(originX, originY, this, degree);
		return this;
	}
	public UniPoint rotate(UniPoint origin, int degree) {
		UniPoint.rotate(origin.x, origin.y, this, degree);
		return this;
	}
	public UniPoint translate(int xd, int yd) {
		UniPoint.translate(this, xd, yd);
		return this;
	}
	public UniPoint resize(int fromWidth, int fromHeight, int toWidth, int toHeight) {
		UniPoint.resize(this, fromWidth, fromHeight, toWidth, toHeight);
		return this;
	}
	public boolean in(int left, int top, int right, int bottom) {
		return UniPoint.in(this, left, top, right, bottom);
	}
	public boolean in(UniRect rect) {
		return UniPoint.in(this, rect.x, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);
	}
	
	/**
	 * 计算两点之间的距离
	 */
	public static double distance(int x1, int y1, int x2, int y2) {
		int xd = Math.abs(x1 - x2) + 1;
		int yd = Math.abs(y1 - y2) + 1;
		return Math.sqrt(xd * xd + yd * yd);
	}
	/**
	 * 计算两点之间的距离
	 */
	public static double distance(UniPoint point1, UniPoint point2) {
		return UniPoint.distance(point1.x, point1.y, point2.x, point2.y);
	}
	/**
	 * 旋转点位
	 * @param originX 旋转原点的x坐标
	 * @param originY 旋转原点的y坐标
	 * @param point   待旋转的点
	 * @param radians 要旋转的弧度
	 */
	public static void rotate(int originX, int originY, UniPoint point, double radians) {
		// 图片坐标系转换成笛卡尔坐标系
		point.x -= originX;
		point.y = originY - point.y;
		if (point.x != 0 || point.y != 0) {
			double r = Math.sqrt(point.x * point.x + point.y * point.y);
			
			double alpha = 0;
			if (point.x >= 0 && point.y >= 0) {
				// 第一象限
				alpha = Math.asin((double)point.y / r);
			} else if (point.x < 0 && point.y < 0) {
				// 第三象限
				alpha = Math.asin((double)Math.abs(point.y) / r);
				alpha += Math.PI;
			} else if (point.x < 0) {
				// 第二象限
				alpha = Math.asin((double)Math.abs(point.x) / r);
				alpha += Math.PI / 2;
			} else if (point.y < 0) {
				// 第四象限
				alpha = Math.asin((double)point.x / r);
				alpha += Math.PI * 3 / 2;
			}
			alpha -= radians;
			double x = Math.round(r * Math.cos(alpha));
			double y = Math.round(r * Math.sin(alpha));
			point.x = (int) (x > 0f ? x + 0.5f : x - 0.5f);
			point.y = (int) (y > 0f ? y + 0.5f : y - 0.5f);
		}
		// 笛卡尔坐标系换回图片坐标系
		point.x += originX;
		point.y = originY - point.y;
	}
	/**
	 * 旋转点位
	 * @param originX 旋转原点的x坐标
	 * @param originY 旋转原点的y坐标
	 * @param point   待旋转的点
	 * @param degree  要旋转的角度
	 */
	public static void rotate(int originX, int originY, UniPoint point, int degree) {
		degree %= 360;
		if (degree < 0) {
			degree += 360;
		}
		if (degree > 0) {
			if (degree == 90 || degree == 180 || degree == 270) {
				// 图片坐标系转换成笛卡尔坐标系
				point.x -= originX;
				point.y = originY - point.y;
				if (degree == 180) {
					point.x *= -1;
					point.y *= -1;
				} else if (point.x != 0 || point.y != 0) {
					if (point.x >= 0 && point.y >= 0) {
						// 第一象限
						point.switchXY();
						if (degree == 90) {
							point.y *= -1;
						} else {
							point.x *= -1;
						}
					} else if (point.x < 0 && point.y < 0) {
						// 第三象限
						point.switchXY();
						if (degree == 90) {
							point.y *= -1;
						} else {
							point.x *= -1;
						}
					} else if (point.x < 0) {
						// 第二象限
						point.switchXY();
						if (degree == 90) {
							point.y *= -1;
						} else {
							point.x *= -1;
						}
					} else if (point.y < 0) {
						// 第四象限
						point.switchXY();
						if (degree == 90) {
							point.y *= -1;
						} else {
							point.x *= -1;
						}
					}
				}
				// 笛卡尔坐标系换回图片坐标系
				point.x += originX;
				point.y = originY - point.y;
			} else {
				double radians = Math.toRadians(degree);
				UniPoint.rotate(originX, originY, point, radians);
			}
		}
	}
	public static void translate(UniPoint point, int xd, int yd) {
		point.x += xd;
		point.y += yd;
	}
	public static void resize(UniPoint point, int fromWidth, int fromHeight, int toWidth, int toHeight) {
		point.x = fromWidth == 0 ? 0 : (point.x * toWidth + (fromWidth >> 1)) / fromWidth;
		point.y = fromHeight == 0 ? 0 : (point.y * toHeight + (fromHeight >> 1)) / fromHeight;
	}
	public static boolean in(UniPoint point, int left, int top, int right, int bottom) {
		return UniPoint.in(point.x, point.y, left, top, right, bottom);
	}
	public static boolean in(int x, int y, int left, int top, int right, int bottom) {
		return x >= left && x <= right && y >= top && y <= bottom;
	}
}
