/**
 * 
 */
package uniface;

/**
 * 人脸图像分析项目定义
 * @author rechard
 *
 */
public enum UniFaceAnalysisItem {
	/**
	 * 分析人脸在源图中的矩形区域
	 */
	Rectangle,
	/**
	 * 提取特征码
	 */
	FeatureCode,
	/**
	 * 提取3D角度信息
	 */
	Angle3D,
	/**
	 * 提取性别信息
	 */
	Gender,
	/**
	 * 提取年龄信息
	 */
	Age,
	/**
	 * 提取活体检测信息
	 */
	Liveness,
	/**
	 * 分析人脸在源图中的平面角度
	 */
	Orient,
	/**
	 * 分析人脸图片的清晰度
	 */
	Clarity,
	/**
	 * 提取人脸小图
	 */
	FaceImage
}
