package com.TruMIS.assetservice.service.impl;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.TruMIS.assetservice.entity.transaction.AssetMailConfig;
import com.TruMIS.assetservice.exception.ResourceNotFoundException;
import com.TruMIS.assetservice.repository.EmailRepository;
import com.TruMIS.assetservice.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
	private final EmailRepository emailRepository;

	@Autowired
	private RestTemplate restTemplate = new RestTemplate();

	public EmailServiceImpl(EmailRepository emailRepository) {
		this.emailRepository = emailRepository;
	}

	@Override
	public void sendEmailInBackground(String from, String to, String reportingManagerId, List<AssetMailConfig> cc,
			List<AssetMailConfig> bcc, String subject, String body) {
		Thread sendEmailNotif = new Thread(
				new EmailNotificationThread(from, to, reportingManagerId, cc, bcc, subject, body, restTemplate));
		sendEmailNotif.start();
	}

	@Override
	public AssetMailConfig saveMailObject(AssetMailConfig assetMailConfig) throws Exception {
		AssetMailConfig mailInstancefromDB = emailRepository.findByMailId(assetMailConfig.getMailId());
		if (Objects.nonNull(mailInstancefromDB)
				&& mailInstancefromDB.getOwnerService().equals(assetMailConfig.getOwnerService())) {
			log.info("Email id with same service already exist");
			throw new Exception("Email id already exists");
		} else {
			log.info("Saving the email id in db");
			emailRepository.save(assetMailConfig);
		}
		return assetMailConfig;
	}

	@Override
	public List<AssetMailConfig> findAllMailObjects() {
		return emailRepository.findAll();
	}

	@Override
	public AssetMailConfig updateMailObject(Long objectid, AssetMailConfig assetMailConfig) {
		log.info("Finding the mailObject from DB using the ObjectId");
		AssetMailConfig mailObjectfromDB = emailRepository.findById(objectid)
				.orElseThrow(() -> new ResourceNotFoundException("Mail Object Not Found For Id: " + objectid));

		// Checking for type (CC or BCC)
		if (Objects.nonNull(assetMailConfig.getType()) && !"".equalsIgnoreCase(assetMailConfig.getType())) {
			mailObjectfromDB.setType(assetMailConfig.getType());
		}
		// Checking for Mail ID
		if (Objects.nonNull(assetMailConfig.getMailId()) && !"".equalsIgnoreCase(assetMailConfig.getMailId())) {
			mailObjectfromDB.setMailId(assetMailConfig.getMailId());
		}
		// Checking for Location
		if (Objects.nonNull(assetMailConfig.getLocation()) && !"".equalsIgnoreCase(assetMailConfig.getLocation())) {
			mailObjectfromDB.setLocation(assetMailConfig.getLocation());
		}
		// Checking for Owner Service
		if (Objects.nonNull(assetMailConfig.getOwnerService())
				&& !"".equalsIgnoreCase(assetMailConfig.getOwnerService())) {
			mailObjectfromDB.setOwnerService(assetMailConfig.getOwnerService());
		}
		return emailRepository.save(mailObjectfromDB);
	}

	@Override
	public void deleteById(Long objectId) {
		emailRepository.deleteById(objectId);
	}

	@Override
	public List<AssetMailConfig> findCCMailWithGivenLocation(String location) {
		return emailRepository.findCCIdsByLocation(location);
	}

	@Override
	public List<AssetMailConfig> findBCCMailWithGivenLocation(String location) {
		return emailRepository.findBCCIdsByLocation(location);
	}

	@Override
	public List<AssetMailConfig> findCCandBCCMailWithGivenLocation(String location) {
		return emailRepository.findCCandBCCIdsByLocation(location);
	}
}
