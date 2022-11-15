/**
 * 
 */
package uniimage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author rechard
 *
 */
public class UniPolygon implements UniGeometry {
	private static final long serialVersionUID = -8314061067884220614L;
	public static UniPoint asPoint(UniPolygon polygon) {
		UniRect rect = asRect(polygon);
		return rect == null ? null : rect.origin();
	}
	public static UniRect asRect(UniPolygon polygon) {
		UniRect rect = polygon == null ? null : new UniRect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		if (rect != null) {
			if (polygon.getPoints() != null && polygon.getPoints().size() > 0) {
				List<UniPoint> points = polygon.getPoints();
				Iterator<UniPoint> it = points.iterator();
				while (it.hasNext()) {
					UniPoint point = it.next();
					if (rect.x > point.x) {
						rect.x = point.x;
					}
					if (rect.y > point.y) {
						rect.y = point.y;
					}
					if (rect.width < point.x) {
						rect.width = point.x;
					}
					if (rect.height < point.y) {
						rect.height = point.y;
					}
				}
				rect.width -= rect.x - 1;
				rect.height -= rect.y - 1;
//				if (!UniGeometryGraph.isRect(points) && points.size() == 4) {
//					// 斜矩形或非矩形的四边形
//					it = points.iterator();
//					UniPoint lt = it.next();
//					UniPoint rt = it.next();
//					UniPoint rb = it.next();
//					UniPoint lb = it.next();
//					double tl = lt.distance(rt);
//					double rl = rt.distance(rb);
//					double bl = lb.distance(rb);
//					double ll = lt.distance(lb);
//					if (Math.abs(tl - bl) / ((tl + bl) / 2f) < 0.1f && Math.abs(ll - rl) / ((ll + rl) / 2f) < 0.1f) {
//						// 斜矩形
//						int ox = rect.x + (rect.width >> 1);
//						int oy = rect.y + (rect.height >> 1);
//						rect.width = (int)(tl + 0.5f);
//						rect.height = (int)(ll + 0.5f);
//						rect.x = ox - (rect.width >> 1);
//						rect.y = oy - (rect.width >> 1);
//					}
//				}
			}
			if (rect.width < 0) {
				rect.x = rect.y = rect.width = rect.height = 0;
			}
		}
		return rect;
	}
	
	private List<UniPoint> points;
	public List<UniPoint> getPoints() {
		return points;
	}
	public void setPoints(List<UniPoint> points) {
		this.points = points;
	}
	/**
	 * 
	 */
	public UniPolygon() {
	}
	public UniPolygon(List<UniPoint> points) {
		if (points != null) {
			this.points = new LinkedList<UniPoint>();
			Iterator<UniPoint> it = points.iterator();
			while (it.hasNext()) {
				this.points.add(it.next().clone());
			}
		}
	}
	public UniPolygon(UniRect rect) {
		this.points = new LinkedList<UniPoint>();
		this.points.add(new UniPoint(rect.x, rect.y));
		this.points.add(new UniPoint(rect.x + rect.width - 1, rect.y));
		this.points.add(new UniPoint(rect.x + rect.width - 1, rect.y + rect.height - 1));
		this.points.add(new UniPoint(rect.x, rect.y + rect.height - 1));
	}
	public UniRect asRect() {
		return UniPolygon.asRect(this);
	}
	public UniPoint asPoint() {
		return UniPolygon.asPoint(this);
	}
	public UniPolygon rotate(int degree) {
		if (this.points != null && this.points.size() > 1) {
			UniPoint origin = this.asPoint();
			Iterator<UniPoint> it = this.points.iterator();
			while (it.hasNext()) {
				it.next().rotate(origin.x, origin.y, degree);
//				UniGeometryGraph.rotatePoint(origin.x, origin.y, it.next(), degree, 0, 0);
			}
		}
		return this;
	}
	public boolean isRect() {
		return UniGeometryGraph.isRect(this.points);
	}
}
