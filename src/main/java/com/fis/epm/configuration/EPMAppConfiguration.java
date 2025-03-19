package com.fis.epm.configuration;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.fis.fw.common.exceptions.ResponseBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fis.epm.controller.EPMDirectController;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.threads.AbstractThread;
import com.fis.epm.threads.LoggingTransactionThread;

import com.fis.epm.utils.ResttemplateBean;
import com.fis.pg.common.utils.LinkQueue;

@Configuration
public class EPMAppConfiguration implements WebMvcConfigurer {
	private static final Logger log = LoggerFactory.getLogger(EPMDirectController.class);

	@Bean(EPMApiConstant.APP_MESSAGE_DICTIONARY_BEAN)
	public ConcurrentHashMap<String, String> errorDictionary(
			@Value(EPMApiConstant.MESSAGE_DICTIONARY_FILE_PATH) String messageDictionaryPath) throws Exception {
		ConcurrentHashMap<String, String> messageData = new ConcurrentHashMap<String, String>();

		try {
			log.info("=>>>>>>>>>messageDicPath  "+messageDictionaryPath);
			File file = new File(messageDictionaryPath);
			if(file != null){
				log.info("=>>>>>>>>>file "+file.getAbsolutePath());
			}
			List<String> lines = Files.readAllLines(file.toPath());
			log.info("=>>>>>>>>>line"+lines.toString());
			for (String l : lines) {
				if ("".equals(l)) {
					continue;
				}
				if (l.startsWith("#")) {
					continue;
				}
				int index = l.indexOf("=");
				messageData.put(l.substring(0, index), l.substring(index + 1));
			}
			
		} catch (Exception exp) {
			throw exp;
		}
		return messageData;
	}

	@Bean(name = EPMApiConstant.ASYN_EXECUTOR_BEAN)
	public Executor asyncExecutor(@Value(EPMApiConstant.CORE_POOL_NUMBER_PROP) int corePool,
			@Value(EPMApiConstant.MAX_POOL_NUMBER_PROP) int maxPool,
			@Value(EPMApiConstant.QUEUE_CAPACITY_NUMBER_PROP) int queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePool);
		executor.setMaxPoolSize(maxPool);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("EPM-");
		executor.initialize();
		return executor;
	}

	@Bean(EPMApiConstant.APP_CACHE_MANAGER_BEAN)
	public ConcurrentHashMap<String, Object> appCacheManager() throws Exception {
		return new ConcurrentHashMap<String, Object>();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ResponseBodyHandler()).addPathPatterns("/**");
	}

	public AbstractThread loggingThread() {
		LoggingTransactionThread thr = new LoggingTransactionThread();
		thr.start();
		return thr;
	}

	@Bean
	@Scope("prototype")
	public ResttemplateBean resttemplateBean() {
		return new ResttemplateBean();
	}

	@Bean(EPMApiConstant.ACCEPT_PAYMENT_REQUEST_BEAN)
	public LinkQueue<Map> acceptPaymentRequestQueue() {
		return new LinkQueue<>(500000, "Accept payment request queue");
	}
}
