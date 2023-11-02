package com.TruMIS.assetservice.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.TruMIS.assetservice.payload.MailRequestBody;

import com.TruMIS.assetservice.entity.transaction.AssetMailConfig;

class EmailNotificationThread implements Runnable {
	private final String from;
	private final String to;
	private final String reportingManagerId;
	private final List<AssetMailConfig> cc;
	private final List<AssetMailConfig> bcc;
	private final String subject;
	private final String body;
	private final RestTemplate restTemplate;

	private static final Logger log = LoggerFactory.getLogger(EmailNotificationThread.class);

	public EmailNotificationThread(String from, String to, String reportingManagerId, List<AssetMailConfig> cc, List<AssetMailConfig> bcc,
			String subject, String body, RestTemplate restTemplate) {
		this.from = from;
		this.to = to;
		this.reportingManagerId=reportingManagerId;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.body = body;
		this.restTemplate = restTemplate;
	}

	@Override
	public void run() {
		log.info("Inside the run method of the EmailNotifThread");
		MailRequestBody mailRequestBody = new MailRequestBody();
		mailRequestBody.setTo(to);

		StringBuilder CCmailIdsBuilder = new StringBuilder();
		for (int i = 0; i < cc.size(); i++) {
			AssetMailConfig obj = cc.get(i);
			CCmailIdsBuilder.append(obj.getMailId());
		    CCmailIdsBuilder.append(";");
		}
		CCmailIdsBuilder.append(reportingManagerId);
		String CCmailIds = CCmailIdsBuilder.toString();
		mailRequestBody.setCc(CCmailIds);

		StringBuilder BCCmailIdsBuilder = new StringBuilder();
		for (int i = 0; i < bcc.size(); i++) {
			AssetMailConfig obj = bcc.get(i);
			BCCmailIdsBuilder.append(obj.getMailId());
			if (i < bcc.size() - 1) {
				BCCmailIdsBuilder.append(";");
			}
		}
		String BCCmailIds = BCCmailIdsBuilder.toString();
		mailRequestBody.setBcc(BCCmailIds);

		mailRequestBody.setSubject(subject);
		mailRequestBody.setText(body);
		log.info("MailRequestBody from asset-service before sending mail: {}.", mailRequestBody);
		ResponseEntity<Map> response = restTemplate.postForEntity("http://AUTH-SERVICE/api/v1/auth/send-mail",
				mailRequestBody, Map.class);
		log.info("Response from auth-service after sending mail: {}.", response);
	}
}
