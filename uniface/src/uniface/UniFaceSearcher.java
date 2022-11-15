/**
 * 
 */
package uniface;

/**
 * 人脸搜索器接口
 * @author rechard
 *
 */
public interface UniFaceSearcher {
	/**
	 * 搜索
	 * @param feature 目标人脸特征
	 * @param searchResult 搜索结果，可以为null，但如果创建并传入结果对象，可以在搜索完成又另外一个线程通过传入的结果对象取消搜索
	 * @return 搜索结果，如果searchResult非null则返回searchResult。
	 * @throws Exception
	 */
	UniFaceSearchResult search(UniFaceFeature feature, UniFaceSearchResult searchResult) throws Exception;
	/**
	 * 注册一个人脸特征到搜索器的特征库
	 * @param feature 人脸特征
	 * @return 当前搜索器对象
	 * @throws Exception
	 */
	UniFaceSearcher registerFaceFeature(UniFaceFeature feature) throws Exception;
	/**
	 * 更新一个在搜索器的特征库中已存在的特征
	 * @param feature 人脸特征
	 * @return 当前搜索器对象
	 * @throws Exception
	 */
	UniFaceSearcher updateFaceFeature(UniFaceFeature feature) throws Exception;
	/**
	 * 从搜索器特征库中删除一个特征
	 * @param keyId 待删除特征的keyId值
	 * @return 如果成功则返回被删除的特征对象
	 * @throws Exception
	 */
	UniFaceFeature removeFaceFeature(Object keyId) throws Exception;
	/**
	 * 获取搜索器特征库中的特征数量
	 * @return 特征数量
	 * @throws Exception
	 */
	int getFaceFeatureCount() throws Exception;
}
