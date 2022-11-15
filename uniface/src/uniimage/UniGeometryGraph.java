/**
 * 
 */
package uniimage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uniimage.util.UniImageUtil;

/**
 * @author rechard
 *
 */
public class UniGeometryGraph implements UniGeometry {
	private static final long serialVersionUID = 2615120951154428660L;
	
	public final static String Root						= "root";
	public final static String Face						= "face";
	public final static String FacePolygon				= "face.polygon";
	public final static String FacePolygonMouthPoint	= "face.polygon.mouth.point";
	public final static String FacePolygonLeftEyePoint	= "face.polygon.left_eye.point";
	public final static String FacePolygonRightEyePoint	= "face.polygon.right_eye.point";
	
	public enum GeometryType {
		Point, Line, Polygon, Circle, Container
	}
	private Integer id;
	private String name;
	private Integer score;
	private GeometryType type;
	private List<UniPoint> points;
	private List<UniGeometryGraph> insideGraphs;
	
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GeometryType getType() {
		return type;
	}
	public void setType(GeometryType type) {
		this.type = type;
	}
	public List<UniPoint> getPoints() {
		return points;
	}
	public void setPoints(List<UniPoint> points) {
		this.points = points;
	}
	public List<UniGeometryGraph> getInsideGraphs() {
		return insideGraphs;
	}
	public void setInsideGraphs(List<UniGeometryGraph> insideGraphs) {
		this.insideGraphs = insideGraphs;
	}
	/**
	 * 
	 */
	public UniGeometryGraph() {
		this.type = GeometryType.Container;
	}
	public UniGeometryGraph(UniGeometryGraph graph) {
		this.type = graph.getType();
		this.id = graph.getId();
		this.name = graph.getName();
		this.score = graph.getScore();
		if (graph.getPoints() != null) {
			this.points = new LinkedList<UniPoint>();
			Iterator<UniPoint> it = graph.getPoints().iterator();
			while (it.hasNext()) {
				UniPoint next = it.next();
				this.points.add(next.clone());
			}
		}
		if (graph.getInsideGraphs() != null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
			Iterator<UniGeometryGraph> it = graph.getInsideGraphs().iterator();
			while (it.hasNext()) {
				UniGeometryGraph next = it.next();
				this.insideGraphs.add(next.clone());
			}
		}
	}
	public UniGeometryGraph(String name) {
		this.type = GeometryType.Container;
		this.name = name;
	}
	public UniGeometryGraph(GeometryType type, List<UniPoint> points) {
		this.type = type;
		if (points != null) {
			this.points = new LinkedList<UniPoint>();
			Iterator<UniPoint> it = points.iterator();
			while (it.hasNext()) {
				this.points.add(it.next().clone());
			}
		}
	}
	public UniGeometryGraph translate(int left, int top) {
		if (this.points != null) {
			Iterator<UniPoint> it = this.points.iterator();
			while (it.hasNext()) {
				it.next().translate(left, top);
			}
		}
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				UniGeometryGraph graph = it.next();
				graph.translate(left, top);
			}
		}
		return this;
	}
	public UniGeometryGraph resize(int fromWidth, int fromHeight, int toWidth, int toHeight) {
		if (this.points != null) {
			Iterator<UniPoint> it = this.points.iterator();
			while (it.hasNext()) {
				it.next().resize(fromWidth, fromHeight, toWidth, toHeight);
			}
		}
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				UniGeometryGraph graph = it.next();
				graph.resize(fromWidth, fromHeight, toWidth, toHeight);
			}
		}
		return this;
	}
	public UniGeometryGraph rotate(int originX, int originY, int degree, int translateX, int translateY) {
		if (this.points != null) {
//			System.out.println(String.format("r=> %d,%d %d %d,%d", originX, originY, degree, translateX, translateY));
			Iterator<UniPoint> it = this.points.iterator();
			while (it.hasNext()) {
				it.next().rotate(originX, originY, degree).translate(translateX, translateY);
			}
		}
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				UniGeometryGraph graph = it.next();
				graph.rotate(originX, originY, degree, translateX, translateY);
			}
		}
		return this;
	}
	public UniGeometryGraph findInsideGeometryGraph(String name) {
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				UniGeometryGraph graph = it.next();
				if (name.equals(graph.getName())) {
					return graph;
				}
			}
		}
		return null;
	}
	public List<UniGeometryGraph> findInsideGeometryGraphs(String name) {
		List<UniGeometryGraph> graphs = new LinkedList<UniGeometryGraph>();
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				UniGeometryGraph graph = it.next();
				if (name.equals(graph.getName())) {
					graphs.add(graph);
				}
			}
		}
		return graphs;
	}
	public boolean isRect() {
		return isRect(this.points);
	}
	public UniRect asRect() {
		UniRect rect = null;
		if (this.points != null && this.points.size() > 0) {
			if (this.type != null && this.type == GeometryType.Circle) {
				// 计算圆所在的矩形
				Iterator<UniPoint> it = this.points.iterator();
				UniPoint origin = it.next();
				double maxRadius = 0f;
				while (it.hasNext()) {
					UniPoint point = it.next();
					double r = 0f;
					if (point.x == origin.x) {
						r = point.y > origin.y ? point.y - origin.y : origin.y - point.y;
					} else if (point.y == origin.y) {
						r = point.x > origin.x ? point.x - origin.x : origin.x - point.x;
					} else {
						r = Math.sqrt((point.x - origin.x) * (point.x - origin.x) + (point.y - origin.y) * (point.y - origin.y));
					}
					if (maxRadius < r) {
						maxRadius = r;
					}
				}
				rect = new UniRect();
				rect.width = rect.height = (int)(maxRadius * 2 + 0.5f);
				rect.x = origin.x - (rect.width >> 1);
				rect.y = origin.y - (rect.height >> 1);
			} else {
				rect = new UniRect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
				Iterator<UniPoint> it = this.points.iterator();
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
//				if (!UniGeometryGraph.isRect(this.points) && this.points.size() == 4) {
//					// 斜矩形或非矩形的四边形
//					it = this.points.iterator();
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
		} else {
			rect = new UniRect();
		}
		return rect;
	}
	public UniPoint asPoint() {
		UniPoint point = null;
		if (this.points != null && this.points.size() > 0) {
			if (this.points.size() == 1) {
				point = this.points.get(0);
			} else {
				UniRect rect = this.asRect();
				point = new UniPoint(rect.x + (rect.width >> 1), rect.y + (rect.height >> 1));
			}
		} else {
			point = new UniPoint();
		}
		return point;
	}
	public String toString() {
		String str = "";
		if (this.points != null) {
			Iterator<UniPoint> it = this.points.iterator();
			while (it.hasNext()) {
				UniPoint point = it.next();
				str += " " + point.x + "," + point.y;
			}
		}
		if (this.insideGraphs != null) {
			Iterator<UniGeometryGraph> it = this.insideGraphs.iterator();
			while (it.hasNext()) {
				str += it.next().toString();
			}
		}
		return str;
	}
	
	public UniGeometryGraph createInsidePoint(UniPoint point) {
		UniGeometryGraph graph = UniGeometryGraph.createPoint(point);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsidePoint(int x, int y) {
		return this.createInsidePoint(new UniPoint(x, y));
	}
	public UniGeometryGraph createInsideLine(UniPoint point1, UniPoint point2) {
		UniGeometryGraph graph = UniGeometryGraph.createLine(point1, point2);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsideRect(int left, int top, int width, int height) {
		UniGeometryGraph graph = UniGeometryGraph.createRect(left, top, width, height);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsideRect(UniRect rect) {
		UniGeometryGraph graph = UniGeometryGraph.createRect(rect);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsideNamedContainer(String name) {
		UniGeometryGraph graph = new UniGeometryGraph(name);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsideCircle(UniPoint origin, int radius) {
		UniGeometryGraph graph = createCircle(origin, radius);
		if (this.insideGraphs == null) {
			this.insideGraphs = new LinkedList<UniGeometryGraph>();
		}
		this.insideGraphs.add(graph);
		return graph;
	}
	public UniGeometryGraph createInsideCircle(int originX, int originY, int radius) {
		return this.createInsideCircle(new UniPoint(originX, originY), radius);
	}
	public UniGeometryGraph clone() {
		return new UniGeometryGraph(this);
	}
	
	public static UniGeometryGraph createPoint(UniPoint point) {
		GeometryType type = GeometryType.Point;
		List<UniPoint> points = new LinkedList<UniPoint>();
		points.add(point);
		return new UniGeometryGraph(type, points);
	}
	public static UniGeometryGraph createPoint(int x, int y) {
		return UniGeometryGraph.createPoint(new UniPoint(x, y));
	}
	public static UniGeometryGraph createLine(UniPoint point1, UniPoint point2) {
		GeometryType type = GeometryType.Line;
		List<UniPoint> points = new LinkedList<UniPoint>();
		points.add(point1);
		points.add(point2);
		return new UniGeometryGraph(type, points);
	}
	public static UniGeometryGraph createRect(int left, int top, int width, int height) {
		return UniGeometryGraph.createRect(new UniRect(left, top, width, height));
	}
	public static UniGeometryGraph createRect(UniRect rect) {
		GeometryType type = GeometryType.Polygon;
		List<UniPoint> points = new LinkedList<UniPoint>();
		points.add(rect.leftTop());
		points.add(rect.rightTop());
		points.add(rect.rightBottom());
		points.add(rect.leftBottom());
		return new UniGeometryGraph(type, points);
	}
	public static UniGeometryGraph createCircle(UniPoint origin, int radius) {
		GeometryType type = GeometryType.Circle;
		List<UniPoint> points = new LinkedList<UniPoint>();
		points.add(origin);
		points.add(new UniPoint(origin.x + radius, origin.y));
		return new UniGeometryGraph(type, points);
	}
	public static UniGeometryGraph createCircle(int originX, int originY, int radius) {
		return UniGeometryGraph.createCircle(new UniPoint(originX, originY), radius);
	}
	public static UniGeometryGraph clone(UniGeometryGraph src) {
		return src == null ? null : src.clone();
	}
	public static UniGeometryGraph translate(UniGeometryGraph graph, int left, int top) {
		return graph == null ? null : graph.translate(left, top);
	}
	public static UniGeometryGraph resize(UniGeometryGraph graph, int fromWidth, int fromHeight, int toWidth, int toHeight) {
		return graph == null ? null : graph.resize(fromWidth, fromHeight, toWidth, toHeight);
	}
	public static UniGeometryGraph rotate(UniGeometryGraph graph, int originX, int originY, int degree, int translateX, int translateY) {
		return graph == null ? null : graph.rotate(originX, originY, degree, translateX, translateY);
	}
	public static boolean isRect(List<UniPoint> points) {
		if (points == null || points.size() != 4) {
			return false;
		}
		Iterator<UniPoint> it = points.iterator();
		boolean coaxialX = false;
		boolean coaxialY = false;
		UniPoint point = it.next();
		while (it.hasNext()) {
			UniPoint next = it.next();
			if (next.x == point.x) {
				coaxialX = true;
			} else if (next.y == point.y) {
				coaxialY = true;
			}
		}
		// 如果任意顶点都可以找到X同轴点和Y同轴点，则表示当前几何图形是一个矩形
		return coaxialX && coaxialY;
	}
	public static void rotatePoint(int originX, int originY, UniPoint point, int degree, int translateX, int translateY) {
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
				point.x += translateX;
				point.y += translateY;
			} else {
				double radians = Math.toRadians(degree);
				UniGeometryGraph.rotatePoint(originX, originY, point, radians, translateX, translateY);
			}
		}
	}
	/**
	 * 旋转点位
	 * @param origin 旋转原点位置
	 * @param points 待进行旋转计算的点位
	 * @param degree 旋转角度
	 */
	public static void rotatePoint(int originX, int originY, UniPoint point, double radians, int translateX, int translateY) {
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
		point.x += translateX;
		point.y += translateY;
	}
	public static void main(String[] args) {
		int degree = 90 * 1 - 35;
		UniSize size = new UniSize(320, 240);
		asAppTest(degree, size);
		baseTest(degree, size);
	}
	private static void asAppTest(int degree, UniSize size) {
		System.out.println("模拟实际应用测试");
		int originX;
		int originY;
		int translateX;
		int translateY;
		
		UniSize rsize = new UniSize();
		UniRGBImage img = new UniRGBImage(size.width, size.height);
		UniGeometryGraph imgGraph = build();
		
		UniRGBImage image = null;
		img.setInsideGeometryGraph(imgGraph);
		System.out.println(toLogString("原始",imgGraph));
		
		image = img;
		img = UniImageUtil.rotateImage(image, degree, (byte)0, new UniSize());
		imgGraph = img.getInsideGeometryGraph();
		rsize.width = img.getWidth();
		rsize.height = img.getHeight();
		
		System.out.println(toLogString("旋转",imgGraph));
		
		degree = 360 - degree;
		originX = rsize.width / 2;
		originY = rsize.height / 2;
		translateX = (size.width - rsize.width) / 2;
		translateY = (size.height - rsize.height) / 2;
		imgGraph.rotate(originX, originY, degree, translateX, translateY);
		
		System.out.println(toLogString("逆转",imgGraph));
	}
	private static void baseTest(int degree, UniSize size) {
		System.out.println("基本测试");
		int originX;
		int originY;
		int translateX;
		int translateY;
		
		UniSize rsize = new UniSize();
		UniRGBImage img = new UniRGBImage(size.width, size.height);
		UniGeometryGraph imgGraph = build();
		UniGeometryGraph graph = build();
		
		UniRGBImage image = null;
		img.setInsideGeometryGraph(imgGraph);
		System.out.println(toLogString("原始",imgGraph));
		System.out.println(toLogString("原始",graph));
		
		image = img;
		img = UniImageUtil.rotateImage(image, degree, (byte)0, new UniSize());
		imgGraph = img.getInsideGeometryGraph();
		rsize.width = img.getWidth();
		rsize.height = img.getHeight();
		
		originX = size.width / 2;
		originY = size.height / 2;
		translateX = (rsize.width - size.width) / 2;
		translateY = (rsize.height - size.height) / 2;
		graph.rotate(originX, originY, degree, translateX, translateY);
//		graph.rotate(size.width / 2, size.height / 2, degree, 0, 0);
		
		System.out.println(toLogString("旋转",imgGraph));
		System.out.println(toLogString("旋转",graph));
		
		degree = 360 - degree;
		image = img;
		img = UniImageUtil.rotateImage(image, degree, (byte)0, size);
		imgGraph = img.getInsideGeometryGraph();
		
		originX = rsize.width / 2;
		originY = rsize.height / 2;
		translateX = (size.width - rsize.width) / 2;
		translateY = (size.height - rsize.height) / 2;
		graph.rotate(originX, originY, degree, translateX, translateY);
//		graph.rotate(size.width / 2, size.height / 2, degree, 0, 0);
		
		System.out.println(toLogString("逆转",imgGraph));
		System.out.println(toLogString("逆转",graph));
	}
	private static String toLogString(String title, UniGeometryGraph root) {
		String str = title;
		UniGeometryGraph face = root.findInsideGeometryGraph(UniGeometryGraph.Face);
		Iterator<UniGeometryGraph> it = face.getInsideGraphs().iterator();
		while (it.hasNext()) {
			UniGeometryGraph next = it.next();
			Iterator<UniPoint> it2 = next.getPoints().iterator();
			while (it2.hasNext()) {
				UniPoint next2 = it2.next();
				if (str.length() < 1) {
					str += String.format("(%d,%d)", next2.x, next2.y);
				} else {
					str += String.format(",(%d,%d)", next2.x, next2.y);
				}
			}
			UniRect rect = next.asRect();
			str += String.format(",(%d,%d)-(%dx%d=%d)", rect.x, rect.y, rect.width, rect.height, rect.width * rect.height);
		}
		return str;
	}
	private static UniGeometryGraph build() {
		UniGeometryGraph root = new UniGeometryGraph(UniGeometryGraph.Root);
		UniGeometryGraph face = root.createInsideNamedContainer(UniGeometryGraph.Face);
		face.createInsideRect(50, 40, 30, 20);
//		face.createInsidePoint(50, 40);
		return root;
	}
}
