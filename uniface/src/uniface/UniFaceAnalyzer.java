/**
 * 
 */
package uniface;

import uniimage.UniImage;

/**
 * 人脸分析器接口
 * @author rechard
 *
 */
public interface UniFaceAnalyzer {
	interface AnalysedCallback {
		void onCompleted(Object custom, UniFaceFeature[] features, Exception exception);
	}
	/**
	 * 对指定图片进行异步人脸信息分析
	 * @param image 待分析图片
	 * @param custom 自定义对象，分析完成后在AnalysedCallback.onCompleted()中原样返回
	 * @param callback 接收分析结果的回调接口
	 * @throws Exception 
	 */
	UniFaceAnalyzer analyse(UniImage image, Object custom, AnalysedCallback callback) throws Exception;
}
