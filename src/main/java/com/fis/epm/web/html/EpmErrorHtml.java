package com.fis.epm.web.html;

import org.springframework.stereotype.Service;

@Service
public class EpmErrorHtml {
	
	public String loadHtml(String url, String title, String message) {
		StringBuilder html = new StringBuilder();
		html.append(buildHeader(title));
		html.append(buildBody(url, message));
		return html.toString();
	}
	
	private String buildHeader(String title) {
		StringBuilder header = new StringBuilder("<!DOCTYPE html><html lang=\"en\">");
		header.append("<head><title>" + title + "</title></head>");
		return header.toString();
	}

	
	private String buildBody(String url, String message) {
		StringBuilder body = new StringBuilder();
		body.append("<body>");
		body.append("<script> setTimeout(function(){ ");
		body.append("window.location.href = \"" + url +"\" ");
		body.append(" }, 5000);");
		body.append("</script> ");
		body.append("<p>" + message + "</p>");
		body.append("</body>");
		body.append("</html>");
		return body.toString();
	}
	
}
