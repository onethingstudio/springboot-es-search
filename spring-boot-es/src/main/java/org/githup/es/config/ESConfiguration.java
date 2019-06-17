package org.githup.es.config;

import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 数据配置，进行初始化操作
 * 
 * @author sdc
 *
 */
@Configuration
public class ESConfiguration implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(ESConfiguration.class);
	
	/**
	 * es集群地址
	 */
	@Value("${elasticsearch.ip}")
	private String hostName;
	/**
	 * 端口
	 */
	@Value("${elasticsearch.port}")
	private String port;
	/**
	 * 集群名称
	 */
	@Value("${elasticsearch.cluster.name}")
	private String clusterName;
	
	/**
	 * 连接池
	 */
	@Value("${elasticsearch.pool}")
	private String poolSize;
	
	private TransportClient client;

	@Override
	public void destroy() throws Exception {
		try {
			logger.info("Closing elasticSearch client");
			if (client != null) {
				client.close();
			}
		} catch (final Exception e) {
			logger.error("Error closing ElasticSearch client: ", e);
		}
	}

	@Override
	public TransportClient getObject() throws Exception {
		return client;
	}

	@Override
	public Class<TransportClient> getObjectType() {
		return TransportClient.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			// 配置信息
			Settings esSetting = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", false)// 增加嗅探机制，找到ES集群
					.put("thread_pool.search.size", Integer.parseInt(poolSize))// 增加线程池个数，暂时设为5
					.build();

			client = new PreBuiltTransportClient(esSetting);
			TransportAddress inetSocketTransportAddress = new TransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port));
			client.addTransportAddresses(inetSocketTransportAddress);

		} catch (Exception e) {
			logger.error("elasticsearch TransportClient create error!!!", e);
		}
	}

}
