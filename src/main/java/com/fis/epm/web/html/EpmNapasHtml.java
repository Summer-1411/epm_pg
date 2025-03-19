package com.fis.epm.web.html;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fis.epm.models.HtmlModel;
import com.fis.epm.prop.EPMApplicationProp;

@Service
public class EpmNapasHtml {

	/**
	 * 
	 * Value
	 */
	@Value(EPMApplicationProp.NAPAS_CLIENT_ID_PROP)
	private String clientId = "";
	@Value(EPMApplicationProp.NAPAS_CLIENT_IP_PROP)
	private String clientIP = "";
	@Value(EPMApplicationProp.NAPAS_DEVICE_ID_PROP)
	private String deviceId = "";
	@Value(EPMApplicationProp.NAPAS_EVNR_PROP)
	private String evnr = "";
	@Value(EPMApplicationProp.NAPAS_ROOT_URL_PROP)
	private String napasRootUrl = "";

	public String loadHtml(HtmlModel htmlModel) {
		StringBuilder html = new StringBuilder();

		html.append(buildHeader(htmlModel.getTitle()));
		html.append(buildBody(htmlModel));
		return html.toString();
	}

	/**
	 * html
	 */
	private String buildHeader(String title) {
		StringBuilder header = new StringBuilder("<!DOCTYPE html><html lang=\"en\">");
		header.append("<head><meta charset=\"UTF-8\">");
		header.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
		header.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		header.append("<title>" + title + "</title></head>");
		return header.toString();
	}

	private String buildBody(HtmlModel htmlModel) {
		StringBuilder body = new StringBuilder();

		body.append("<body>");
		body.append("<form id=\"merchant-form\" action=\""+ htmlModel.getUrlReturn() +"\" method=\"POST\"></form>");
		body.append("<div id=\"napas-widget-container\"></div>");
		body.append("<script  type=\"text/javascript\" id=\"napas-widget-script\" ");
		body.append("src=\""+this.napasRootUrl+"/restjs/resources/js/napas.hostedform.min.js\" ");
		body.append("merchantId=\"" + this.clientId + "\" ");
		body.append("clientIP=\"" + htmlModel.getClientIP() + "\" ");
		body.append("deviceId=\"" + this.deviceId + "\" ");
		body.append("environment=\"" + this.evnr + "\" ");
		body.append("cardScheme=\"" + htmlModel.getCardScheme() + "\" ");
		body.append("enable3DSecure=\"" + htmlModel.getEnable3DSecure() + "\" ");
		body.append("orderId=\"" + htmlModel.getOrderId() + "\" ");
		body.append("dataKey=\"" + htmlModel.getDataKey() + "\" ");
		body.append("napasKey=\"" + htmlModel.getNapasKey() + "\" ");
		if(htmlModel.getApiOperation()!=null) {
			body.append("apiOperation=\""+ htmlModel.getApiOperation() + "\" ");
		}
		body.append("orderAmount=\"" + htmlModel.getOrderAmount() + "\" ");
		body.append("orderCurrency=\"" + htmlModel.getOrderCurrency() + "\" ");
		body.append("orderReference=\"" + htmlModel.getOrderReference() + "\" ");
		if(htmlModel.getAgreementType() != null) {
			body.append("agreementType=\"" + htmlModel.getAgreementType() + "\" ");
			body.append("agreementId=\"" + htmlModel.getAgreementId() + "\" ");
			body.append("agreementExpiryDate=\"" + htmlModel.getAgreementExpiryDate() + "\" ");
			body.append("agreementDaysBetweenPayments=\"" + "" + "\" ");
		}
		body.append("channel=\"" + htmlModel.getChannel() + "\" ");
		body.append("language=\"" + htmlModel.getLanguage() + "\" ");
		body.append("sourceOfFundsType=\"" + htmlModel.getSourceOfFundsType() + "\" >");
		body.append("</script></form></body></html>");
		return body.toString();
	}
}
