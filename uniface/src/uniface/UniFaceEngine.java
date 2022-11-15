/**
 * 
 */
package uniface;

import java.awt.image.BufferedImage;
import java.util.Properties;

import uniimage.UniImage;

/**
 * 人脸分析算法引擎
 * @author rechard
 *
 */
public interface UniFaceEngine extends UniFaceSearcher {
	/**
	 * 打开引擎
	 * @param properties 引擎配置参数
	 * @return 成功打开的引擎对象
	 * @throws Exception
	 */
	UniFaceEngine open(Properties properties) throws Exception;
	/**
	 * 关闭引擎
	 * @return
	 */
	UniFaceEngine close();
	/**
	 * 决定{@link UniFaceEngine#analyse(BufferedImage, AnalysingCallback)}针对那些项目进行分析
	 * @return 人脸图像分析项目
	 */
	UniFaceAnalysisItem[] getAnalysisItems();
	/**
	 * 决定{@link UniFaceEngine#analyse(BufferedImage, AnalysingCallback)}针对那些项目进行分析
	 * @param items 人脸图像分析项目
	 * @return 不支持的分析项目，如果没有不支持的分析项目则返回null。
	 * @throws Exception
	 */
	UniFaceAnalysisItem[] setAnalysisItems(UniFaceAnalysisItem[] items) throws Exception;
	
	/**
	 * 对包含人脸的图像进行分析
	 * @param image 待分析图像
	 * @return 分析获得的人脸特征，如果image中没有发现人脸信息则返回的数组长度为0
	 * @throws Exception
	 */
	UniFaceFeature[] analyse(UniImage image) throws Exception;
	/**
	 * 比较两个人脸特征的相似度
	 * @param feature1 人脸特征
	 * @param feature2 人脸特征
	 * @return 相似度
	 * @throws Exception
	 */
	Float compare(UniFaceFeature feature1, UniFaceFeature feature2) throws Exception;
}
