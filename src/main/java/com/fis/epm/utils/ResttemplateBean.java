package com.fis.epm.utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fis.epm.repo.LogApiRepo;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.epm.entity.LogAPI;
import com.fis.epm.entity.LogApiResult;

public class ResttemplateBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String errorMessage = "";

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Autowired
	LogApiRepo logApiRepo;

	public Object handleHttpsRequest(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, Class c) {
		Object objResponse = null;

		try {
			TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			};
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			requestFactory.setReadTimeout(iTimeout);
			requestFactory.setConnectTimeout(iTimeout);

			RestTemplate restTemplate = new RestTemplate(requestFactory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			HttpEntity<String> result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
		} catch (Exception exp) {
			exp.printStackTrace();
			this.errorMessage = exp.getMessage();
		}
		return objResponse;
	}

	public Object handleHttpsRequestWithProxy(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, Class c, String hostName, int Port) {
		Object objResponse = null;

		try {
			HttpHost proxy = new HttpHost(hostName, Port);
			TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			};
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf)
					.setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
						@Override
						protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context)
								throws HttpException {
							return super.determineProxy(target, request, context);
						}
					}).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			requestFactory.setReadTimeout(iTimeout);
			requestFactory.setConnectTimeout(iTimeout);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			HttpEntity<String> result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
		} catch (Exception exp) {
			exp.printStackTrace();
			this.errorMessage = exp.getMessage();
		}
		return objResponse;
	}

	public Object handleHttpsRequest(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, ParameterizedTypeReference c) {
		Object objResponse = null;

		try {
			TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					return true;
				}
			};
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			requestFactory.setReadTimeout(iTimeout);
			requestFactory.setConnectTimeout(iTimeout);

			RestTemplate restTemplate = new RestTemplate(requestFactory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			HttpEntity result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return objResponse;
	}

	public Object handleHttpRequest(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, Class c) {
		LogApiResult logAPI = new LogApiResult();

		Object objResponse = null;

		try {
			logAPI.setUri(url);
			logAPI.setRequestHeader(requestHeaders.toString());
			logAPI.setMethod(method.name());
			logAPI.setCreateTime(new Date());

			iTimeout = iTimeout * 1000;
			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			factory.setConnectTimeout(iTimeout);
			factory.setReadTimeout(iTimeout);

			RestTemplate restTemplate = new RestTemplate(factory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				logAPI.setRequestBody(Tools.convertModeltoJSON(data));
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
//			HttpEntity result = restTemplate.exchange(uri, method, request, c);
			ResponseEntity<Object> result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
			logAPI.setResponseStatus(result.getStatusCode().toString());
			logAPI.setResponseBody(Tools.convertModeltoJSON(objResponse));

		} catch (Exception exp) {
			logAPI.setResponseBody(exp.getMessage());
			exp.printStackTrace();
		} finally {
			logAPI.setEndTime(new Date());
			Long processTime = logAPI.getEndTime().getTime() - logAPI.getCreateTime().getTime();
			logAPI.setProcessTime(processTime.toString());
			EPMQueueManager.QUEUE_LOG_API_SEND.enqueueNotify(logAPI);
		}
		return objResponse;
	}

	public Object handleHttpRequest(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, Class c, String transId) {
		LogApiResult logAPI = new LogApiResult();
		Object objResponse = null;

		try {
			if (!StringUtils.isNullOrEmpty(transId)) {
				logAPI.setTranId(transId);
			}

			logAPI.setUri(url);
			logAPI.setRequestHeader(requestHeaders.toString());
			logAPI.setMethod(method.name());
			logAPI.setCreateTime(new Date());

			iTimeout = iTimeout * 1000;
			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			factory.setConnectTimeout(iTimeout);
			factory.setReadTimeout(iTimeout);

			RestTemplate restTemplate = new RestTemplate(factory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				logAPI.setRequestBody(Tools.convertModeltoJSON(data));
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
//			HttpEntity result = restTemplate.exchange(uri, method, request, c);
			ResponseEntity<Object> result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
			logAPI.setResponseStatus(result.getStatusCode().toString());
			logAPI.setResponseBody(Tools.convertModeltoJSON(objResponse));

		} catch (Exception exp) {
			logAPI.setResponseBody(exp.getMessage());
			exp.printStackTrace();
		} finally {
			logAPI.setEndTime(new Date());
			Long processTime = logAPI.getEndTime().getTime() - logAPI.getCreateTime().getTime();
			logAPI.setProcessTime(processTime.toString());
			EPMQueueManager.QUEUE_LOG_API_SEND.enqueueNotify(logAPI);
		}
		return objResponse;
	}

	/// comment
	public Object handleHttpRequestNoReadTimeOut(String url, int iTimeout, HttpMethod method,
			HttpHeaders requestHeaders, Object data, Class c, String transId) {
		LogApiResult logAPI = new LogApiResult();
		Long startTime = System.currentTimeMillis();

		Object objResponse = null;

		try {
			if (!StringUtils.isNullOrEmpty(transId)) {
				logAPI.setTranId(transId);
			}

			logAPI.setUri(url);
			logAPI.setRequestHeader(requestHeaders.toString());
			logAPI.setMethod(method.name());
			logAPI.setCreateTime(new Date());

			iTimeout = iTimeout * 1000;
			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			factory.setConnectTimeout(iTimeout);
			factory.setReadTimeout(0);

			RestTemplate restTemplate = new RestTemplate(factory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				logAPI.setRequestBody(Tools.convertModeltoJSON(data));
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			HttpEntity result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
			
		} catch (Exception exp) {
			logAPI.setResponseBody(exp.getMessage());
			exp.printStackTrace();
		} finally {
			Long endTime = System.currentTimeMillis();
			Long processTime = endTime - startTime;
			logAPI.setProcessTime(processTime.toString());
			EPMQueueManager.QUEUE_LOG_API_SEND.enqueueNotify(logAPI);
		}
		return objResponse;
	}

	public Object handleHttpRequestWithProxy(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, Class c, String hostName, int port) {
		Object objResponse = null;

		try {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			factory.setConnectTimeout(iTimeout);
			factory.setReadTimeout(iTimeout);
			factory.setProxy(proxy);
			RestTemplate restTemplate = new RestTemplate(factory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			HttpEntity result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return objResponse;
	}

	public Object handleHttpRequest(String url, int iTimeout, HttpMethod method, HttpHeaders requestHeaders,
			Object data, ParameterizedTypeReference c) {
		Object objResponse = null;

		LogAPI logAPI = new LogAPI();
		Long startTime = System.currentTimeMillis();

		try {

			logAPI.setUri(url);
			logAPI.setRequestHeader(requestHeaders.toString());
			logAPI.setMethod(method.name());
			logAPI.setCreateTime(new Date());

			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			factory.setConnectTimeout(iTimeout);
			factory.setReadTimeout(iTimeout);

			RestTemplate restTemplate = new RestTemplate(factory);
			URI uri = new URI(url);
			HttpEntity<Object> request = null;

			if (data != null) {
				logAPI.setRequestBody(Tools.convertModeltoJSON(data));
				request = new HttpEntity<>(data, requestHeaders);
			} else {
				request = new HttpEntity<>(requestHeaders);
			}
			ResponseEntity<Object> result = restTemplate.exchange(uri, method, request, c);
			objResponse = result.getBody();
			logAPI.setResponseStatus(result.getStatusCode().toString());
			logAPI.setResponseBody(Tools.convertModeltoJSON(result.getBody()));
			Long endTime = System.currentTimeMillis();
			Long processTime = endTime - startTime;
			logAPI.setProcessTime(processTime.toString());
		} catch (Exception exp) {
			logAPI.setResponseBody(exp.getMessage());
			logger.error("EPM handleHttpRequest Exp:" + exp.getMessage());
			exp.printStackTrace();
		}
		logApiRepo.save(logAPI);
		return objResponse;
	}
}
