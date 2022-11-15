/**
 * 
 */
package uniface;

/**
 * 人脸特征比对器
 * @author rechard
 *
 */
public interface UniFaceComparator {
	interface ComparedCallback {
		void onCompleted(Object custom, UniFaceFeature feature1, UniFaceFeature feature2, Float similar);
	}
	/**
	 * 比对
	 * @param feature1 待比对特征
	 * @param feature2 待比对特征
	 * @param custom 自定义对象，分析完成后在ComparedCallback.onCompleted()中原样返回
	 * @param callback 接收比对结果的回调接口
	 * @return 当前比对器
	 * @throws Exception
	 */
	UniFaceComparator compare(UniFaceFeature feature1, UniFaceFeature feature2, Object custom, ComparedCallback callback) throws Exception;
}
