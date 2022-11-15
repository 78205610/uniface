/**
 * 
 */
package uniface;

import java.io.Serializable;
import java.util.Iterator;

import uniimage.UniPoint;
import uniimage.UniPolygon;
import uniimage.UniRGBImage;
import uniimage.UniRect;

/**
 * 人脸特征相关信息
 * @author rechard
 *
 */
public class UniFaceFeature implements Serializable {
	private static final long serialVersionUID = 6382783115846561129L;
	
	private Object keyId;
	/**
     * 人脸位置信息
     */
    private UniPolygon polygon;
	/**
     * 人脸图片区域的清晰度
     */
    private Float clarity;
    /**
     * 人脸图片区域的可信度(0-100)
     */
    private Integer score;
    /**
     * 嘴在图片中的点位
     */
    private UniPoint mouthPoint;
    /**
     * 左眼在图片中的点位
     */
    private UniPoint leftEyePoint;
    /**
     * 右眼在图片中的点位
     */
    private UniPoint rightEyePoint;
	/**
     * 人脸在识别图中的角度
     */
    private Integer orient;
    /**
     * faceId，IMAGE模式下不返回faceId
     */
    private Integer faceId;
    /**
     * 人脸特征码
     */
    byte[] featureCode;
	/**
	 * 人脸俯仰角度(不解释)
	 */
	private Float pitch;
	/**
	 * 人脸偏航角度(侧颜90度)
	 */
	private Float yaw;
	/**
	 * 人脸横滚角度(歪头看前方)
	 */
	private Float roll;
	/**
	 * 性别
	 */
	private UniFaceGender gender;
	/**
	 * 年龄
	 */
	private Integer age;
	/**
	 * 活体检测结果
	 */
	private Boolean liveness;
	/**
	 * 标签
	 */
	private String tag;
	/**
	 * 人脸小图
	 */
	private UniRGBImage faceImage;
	
	public UniRGBImage getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(UniRGBImage faceImage) {
		this.faceImage = faceImage;
	}

	public Object getKeyId() {
		return keyId;
	}

	public void setKeyId(Object keyId) {
		this.keyId = keyId;
	}
    public UniPolygon getPolygon() {
		return polygon;
	}
	public void setPolygon(UniPolygon polygon) {
		this.polygon = polygon;
	}
	public UniRect getRectangle() {
		if (this.polygon == null) {
			return null;
		}
		if (this.polygon.isRect()) {
			return this.polygon.asRect();
		}
		UniRect rect = this.polygon.asRect();
		Iterator<UniPoint> it = this.polygon.getPoints().iterator();
		UniPoint p1 = it.next();
		UniPoint p2 = it.next();
		UniPoint p3 = it.next();
		// l1、l2是矩形多边形两条邻边的边长
		double l1 = p1.distance(p2);
		double l2 = p2.distance(p3);
		double a1 = l1 * l2; // 矩形多边形的面积
		double a2 = rect.width * rect.height; // 多边形的正矩形面积
		double ad = (a2 - a1) / 1.5f; // 面积差的2/3，对于人脸矩形而言比较合适
		double adr = ad / a2; // rect边长的缩减率
        int wd =  (int)(rect.width * adr + 0.5f); 
        int hd =  (int)(rect.height * adr + 0.5f);
        // 矩形按比例缩减
        rect.x += wd >> 1;
        rect.y += wd >> 1;
        rect.width -= wd;
        rect.height -= hd;
		return rect;
	}

	public void setRectangle(UniRect rectangle) {
		this.polygon = new UniPolygon(rectangle);
	}
	
    public Float getClarity() {
		return clarity;
	}

	public void setClarity(Float clarity) {
		this.clarity = clarity;
	}
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public UniPoint getMouthPoint() {
		return mouthPoint;
	}

	public void setMouthPoint(UniPoint mouthPoint) {
		this.mouthPoint = mouthPoint;
	}

	public UniPoint getLeftEyePoint() {
		return leftEyePoint;
	}

	public void setLeftEyePoint(UniPoint leftEyePoint) {
		this.leftEyePoint = leftEyePoint;
	}

	public UniPoint getRightEyePoint() {
		return rightEyePoint;
	}

	public void setRightEyePoint(UniPoint rightEyePoint) {
		this.rightEyePoint = rightEyePoint;
	}

	public Integer getOrient() {
		return orient;
	}

	public void setOrient(Integer orient) {
		this.orient = orient;
	}

	public Integer getFaceId() {
		return faceId;
	}

	public void setFaceId(Integer faceId) {
		this.faceId = faceId;
	}

	public byte[] getFeatureCode() {
		return featureCode;
	}

	public void setFeatureCode(byte[] featureCode) {
		this.featureCode = featureCode;
	}

	public Float getPitch() {
		return pitch;
	}

	public void setPitch(Float pitch) {
		this.pitch = pitch;
	}

	public Float getYaw() {
		return yaw;
	}

	public void setYaw(Float yaw) {
		this.yaw = yaw;
	}

	public Float getRoll() {
		return roll;
	}

	public void setRoll(Float roll) {
		this.roll = roll;
	}

	public UniFaceGender getGender() {
		return gender;
	}

	public void setGender(UniFaceGender gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Boolean getLiveness() {
		return liveness;
	}

	public void setLiveness(Boolean liveness) {
		this.liveness = liveness;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * 
	 */
	public UniFaceFeature() {
	}
	public UniFaceFeature(UniFaceFeature src) {
		this.age = src.getAge();
		this.faceId = src.getFaceId();
		this.gender = src.getGender();
		this.keyId = src.getKeyId();
		this.liveness = src.getLiveness();
		this.orient = src.getOrient();
		this.pitch = src.getPitch();
		this.roll = src.getRoll();
		this.tag = src.getTag();
		this.yaw = src.getYaw();
		this.clarity = src.getClarity();
		this.score = src.getScore();
		this.faceImage = src.getFaceImage() == null ? null : (UniRGBImage)src.getFaceImage().create(true);
		this.featureCode = src.getFeatureCode() == null ? null : src.getFeatureCode().clone();
		this.polygon = src.getPolygon() == null ? null : new UniPolygon(src.getPolygon().getPoints());
		this.leftEyePoint = src.getLeftEyePoint() == null ? null : src.getLeftEyePoint().clone();
		this.rightEyePoint = src.getRightEyePoint() == null ? null : src.getRightEyePoint().clone();
		this.mouthPoint = src.getMouthPoint() == null ? null : src.getMouthPoint().clone();
	}
	public UniFaceFeature clone() {
		return new UniFaceFeature(this);
	}
}
