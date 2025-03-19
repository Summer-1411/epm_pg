package com.fis.epm.threads;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fis.epm.prop.EPMApiConstant;
import com.fis.pg.common.utils.LinkQueue;

@Component
@Scope("singleton")
public class AcceptPaymentRequestThread extends AbstractThread {

	private static final Logger log = LoggerFactory.getLogger(AcceptPaymentRequestThread.class);

	@Autowired
	@Qualifier(EPMApiConstant.ACCEPT_PAYMENT_REQUEST_BEAN)
	private LinkQueue<Map> acceptPaymentRequestQueue = null;

	@PostConstruct
	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	protected void init() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void process() throws Exception {
		// TODO Auto-generated method stub
		while (isRunningThread()) {
			try {
				Map req =  this.acceptPaymentRequestQueue.dequeueWait(1);
				
				if(req != null) {
					continue;
				}
			} catch (Exception exp) {
				log.error("error process payment request from partner", exp);
			}
			Thread.sleep(100);
		}
	}

	@Override
	protected void end() throws Exception {
		// TODO Auto-generated method stub

	}

}
