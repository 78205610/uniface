/**
 * 
 */
package uniface.util;

import java.time.Duration;
import java.util.Properties;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import uniface.UniFaceAnalysisItem;
import uniface.UniFaceEngine;
import uniface.UniFaceEngineFactory;
import uniface.UniFaceFeature;
import uniface.UniFaceSearchResult;
import uniimage.UniImage;

/**
 * 内部实现了池化管理的人脸算法引擎工厂
 * @author rechard
 *
 */
public class UniFaceEnginePoolFactory implements UniFaceEngineFactory {
	private final static GenericObjectPoolConfig<ReusableFaceEngine> poolConfig = new GenericObjectPoolConfig<ReusableFaceEngine>();
	static {
		try{
            Class.forName("java.lang.management.ManagementFactory");
        }catch(ClassNotFoundException e){
        	poolConfig.setJmxEnabled(false);
        }
	}
	/**
	 * 人脸算法引擎的重用包装类
	 *
	 */
	private class ReusableFaceEngine implements UniFaceEngine {
		/**
		 * 真实的引擎
		 */
		private UniFaceEngine engine;
		/**
		 * 由open引擎时指定的配置参数计算出来的key值
		 */
		private String openKey;
		/**
		 * 生成引擎open相关参数的key值
		 * @param items 分析项目
		 * @param properties 配置参数
		 * @return key值
		 */
		private String buildOpenKey(UniFaceAnalysisItem[] items, Properties properties) {
			String key = "";
			if (items != null || properties != null) {
				if (items == null) {
					key = "null";
				} else {
					for (UniFaceAnalysisItem item : items) {
						key += item + " ";
					}
				}
				key += ",";
				if (properties == null) {
					key += "null";
				} else {
					key += properties.hashCode();
				}
			}
			return key;
		}
		public ReusableFaceEngine(UniFaceEngine engine) {
			this.engine = engine;
		}
		@Override
		public UniFaceEngine open(Properties properties) throws Exception {
			return this;
		}
		/**
		 * 当前真实引擎open时使用的配置参数
		 */
		private Properties properties;
		/**
		 * 由池管理调用的引擎open方法
		 * @return 当前引擎对象
		 * @throws Exception
		 */
		public UniFaceEngine insideOpen() throws Exception {
			if (this.openKey == null) {
				// 初次open
				this.properties = (Properties) UniFaceEnginePoolFactory.this.properties.clone();
				UniFaceAnalysisItem[] items = UniFaceEnginePoolFactory.this.analysisItems.clone();
				this.engine.setAnalysisItems(items);
				this.engine.open(this.properties);
				// open成功后记录下分析项和配置参数的key值
				this.openKey = this.buildOpenKey(items, this.properties);
				//System.out.println(String.format("open(%s)=>%s", this.properties.toString(), this.openKey));
			} else if (UniFaceEnginePoolFactory.this.checkPropertiesOnReopen){
				// 重复open
				String nowKey = this.buildOpenKey(this.engine.getAnalysisItems(), this.properties);
				String factoryKey = this.buildOpenKey(UniFaceEnginePoolFactory.this.analysisItems, UniFaceEnginePoolFactory.this.properties);
				if (!nowKey.equals(this.openKey) || !factoryKey.equals(this.openKey)) {
					// 引擎的分析项目或配置参数发生了变化需要重新open
					// 这个检查确保被重新从池中分配出去的引擎各项配置与工厂配置一致
					// 同时也会使得被分配出去的引擎配置只是临时有效
					System.out.println("reopen");
					this.insideClose();
					this.insideOpen();
				}
			}
			return this;
		}
		@Override
		public UniFaceEngine close() {
			return this;
		}
		/**
		 * 由池管理调用的引擎close方法
		 * @return 当前引擎对象
		 */
		public UniFaceEngine insideClose() {
			if (this.openKey != null) {
				//System.out.println("close");
				this.openKey = null;
				this.properties = null;
				this.engine.close();
			}
			return this;
		}
		@Override
		public UniFaceAnalysisItem[] getAnalysisItems() {
			return this.engine.getAnalysisItems();
		}
		@Override
		public UniFaceAnalysisItem[] setAnalysisItems(UniFaceAnalysisItem[] items) throws Exception {
			return this.engine.setAnalysisItems(items);
		}
		@Override
		public UniFaceFeature[] analyse(UniImage image) throws Exception {
			return this.engine.analyse(image);
		}
		@Override
		public Float compare(UniFaceFeature feature1, UniFaceFeature feature2) throws Exception {
			return this.engine.compare(feature1, feature2);
		}
		@Override
		public UniFaceSearchResult search(UniFaceFeature feature, UniFaceSearchResult searchResult) throws Exception {
			return this.engine.search(feature, searchResult);
		}
		@Override
		public UniFaceEngine registerFaceFeature(UniFaceFeature feature) throws Exception {
			this.engine.registerFaceFeature(feature);
			return this;
		}
		@Override
		public UniFaceEngine updateFaceFeature(UniFaceFeature feature) throws Exception {
			this.engine.updateFaceFeature(feature);
			return this;
		}
		@Override
		public UniFaceFeature removeFaceFeature(Object keyId) throws Exception {
			return this.engine.removeFaceFeature(keyId);
		}
		@Override
		public int getFaceFeatureCount() throws Exception {
			return this.engine.getFaceFeatureCount();
		}
	}
	/**
	 * 引擎对象池
	 */
	final GenericObjectPool<ReusableFaceEngine> pool = new GenericObjectPool<ReusableFaceEngine>(new BasePooledObjectFactory<ReusableFaceEngine>() {
//		private final int[] dbg_engine_count = new int[1];
		@Override
		public ReusableFaceEngine create() throws Exception {
//			synchronized (this.dbg_engine_count) {
//				this.dbg_engine_count[0]++;
//		    	System.out.println("创建" + this.dbg_engine_count[0]);
//			}
			return new ReusableFaceEngine(UniFaceEnginePoolFactory.this.rawEngineFactory.createEngine(false));
		}
		@Override
		public PooledObject<ReusableFaceEngine> wrap(ReusableFaceEngine obj) {
			return new DefaultPooledObject<ReusableFaceEngine>(obj);
		}
	    @Override
	    public void destroyObject(final PooledObject<ReusableFaceEngine> p)
	        throws Exception  {
//			synchronized (this.dbg_engine_count) {
//		    	System.out.println("销毁" + this.dbg_engine_count[0]);
//				this.dbg_engine_count[0]--;
//			}
	    	p.getObject().insideClose();
	    }
	    @Override
	    public void activateObject(final PooledObject<ReusableFaceEngine> p) throws Exception {
//	    	System.out.println("激活");
	    	ReusableFaceEngine rawEngine = p.getObject();
	    	rawEngine.insideOpen();
	    }
	}, poolConfig);

	/**
	 * 创建后自动open引擎时采用的配置
	 */
	private Properties properties;
	/**
	 * 创建后自动open引擎时设置的分析项目
	 */
	private UniFaceAnalysisItem[] analysisItems;
	private UniFaceEngineFactory rawEngineFactory;
	private int poolMaxTotal = Runtime.getRuntime().availableProcessors() - 1;
	private long poolMaxWait = 1000 * 30;
	private boolean checkPropertiesOnReopen;
	
	public boolean isCheckPropertiesOnReopen() {
		return this.checkPropertiesOnReopen;
	}

	public void setCheckPropertiesOnReopen(boolean checkPropertiesOnReopen) {
		this.checkPropertiesOnReopen = checkPropertiesOnReopen;
	}
	public long getPoolMaxWait() {
		return this.poolMaxWait;
	}

	public void setPoolMaxWait(long poolMaxWait) {
		this.poolMaxWait = poolMaxWait;
		// 当对象池资源耗尽时,调用者最大阻塞的时间,超时时抛出异常
		this.pool.setMaxWait(Duration.ofMillis(this.poolMaxWait));
	}

	public UniFaceEngineFactory getRawEngineFactory() {
		return this.rawEngineFactory;
	}

	public void setRawEngineFactory(UniFaceEngineFactory rawEngineFactory) {
		this.rawEngineFactory = rawEngineFactory;
		if (this.properties == null) {
			this.properties = (Properties) this.rawEngineFactory.getPropertiesForAutoOpen().clone();
		}
		if (this.analysisItems == null) {
			this.analysisItems = this.rawEngineFactory.getAnalysisItemsForAutoOpen().clone();
		}
	}

	public int getPoolMaxTotal() {
		return this.poolMaxTotal;
	}

	public void setPoolMaxTotal(int poolMaxTotal) {
		if (poolMaxTotal < 1) {
			int cpus = Runtime.getRuntime().availableProcessors();
			this.poolMaxTotal = cpus > 1 ? cpus - 1 : 1;
		} else {
			this.poolMaxTotal = poolMaxTotal;
		}
		// 池中最少的空闲对象数
		this.pool.setMinIdle(0);
		// 池中最大的空闲对象数
		this.pool.setMaxIdle(this.poolMaxTotal);
		// 池中最大的对象数
		this.pool.setMaxTotal(this.poolMaxTotal);
	}

	/**
	 * 
	 */
	public UniFaceEnginePoolFactory() {
		// 连对象池尽时是否阻塞,默认为true
		this.pool.setBlockWhenExhausted(true);
		this.setPoolMaxWait(this.poolMaxWait);
		this.setPoolMaxTotal(this.poolMaxTotal);
	}
	public UniFaceEnginePoolFactory(UniFaceEngineFactory rawEngineFactory) {
		// 连对象池尽时是否阻塞,默认为true
		this.pool.setBlockWhenExhausted(true);
		this.setPoolMaxWait(this.poolMaxWait);
		this.setPoolMaxTotal(this.poolMaxTotal);
		this.setRawEngineFactory(rawEngineFactory);
	}

	@Override
	public Properties getPropertiesForAutoOpen() {
		return this.properties;
	}

	@Override
	public UniFaceEngineFactory setPropertiesForAutoOpen(Properties properties) {
		this.properties = properties;
		return this;
	}

	@Override
	public UniFaceAnalysisItem[] getAnalysisItemsForAutoOpen() {
		return this.analysisItems;
	}

	@Override
	public UniFaceEngineFactory setAnalysisItemsForAutoOpen(UniFaceAnalysisItem[] items) throws Exception {
		this.analysisItems = items;
		return this;
	}

	/**
	 * 创建并自动open一个引擎对象
	 * 由于采用了池化处理，创建的引擎对象不支持外部open和close。
	 */
	@Override
	public UniFaceEngine createEngine(boolean autoOpen) throws Exception {
		UniFaceEngine engine = null;
		engine = this.pool.borrowObject();
		return engine;
	}

	@Override
	public UniFaceEngineFactory destoryEngine(UniFaceEngine engine) {
		if (engine instanceof ReusableFaceEngine) {
			this.pool.returnObject((ReusableFaceEngine)engine);
		}
		return this;
	}
	
	public UniFaceEnginePoolFactory clearPool() {
		this.pool.clear();
		return this;
	}
}
