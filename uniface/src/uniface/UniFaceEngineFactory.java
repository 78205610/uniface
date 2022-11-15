/**
 * 
 */
package uniface;

import java.util.Properties;

/**
 * 人脸算法引擎工厂
 * @author rechard
 *
 */
public interface UniFaceEngineFactory {
	/**
	 * 如果创建引擎时指定了autoOpen=true，则使用该属性作为引擎open参数
	 * @return 引擎open参数
	 */
	Properties getPropertiesForAutoOpen();
	/**
	 * 如果创建引擎时指定了autoOpen=true，则使用该属性作为引擎open参数
	 * @param properties 引擎open参数
	 * @return 当前工厂对象
	 */
	UniFaceEngineFactory setPropertiesForAutoOpen(Properties properties);
	/**
	 * 如果创建引擎时指定了autoOpen=true，则open之前将该属性赋值给新建引擎
	 * @return 引擎的分析项目
	 */
	UniFaceAnalysisItem[] getAnalysisItemsForAutoOpen();
	/**
	 * 如果创建引擎时指定了autoOpen=true，则open之前将该属性赋值给新建引擎
	 * @param items 引擎的分析项目
	 * @return 当前工厂对象
	 * @throws Exception
	 */
	UniFaceEngineFactory setAnalysisItemsForAutoOpen(UniFaceAnalysisItem[] items) throws Exception;
	/**
	 * 创建一个引擎对象
	 * @param autoOpen 创建后是否执行open操作
	 * @return 新创建的引擎
	 * @throws Exception
	 */
	UniFaceEngine createEngine(boolean autoOpen) throws Exception;
	/**
	 * 销毁一个引擎对象
	 * @param engine 待销毁的引擎
	 * @return 当前工厂对象
	 */
	UniFaceEngineFactory destoryEngine(UniFaceEngine engine);
}
