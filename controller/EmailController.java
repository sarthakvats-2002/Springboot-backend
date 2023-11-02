package com.TruMIS.assetservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TruMIS.assetservice.entity.transaction.AssetMailConfig;
import com.TruMIS.assetservice.service.EmailService;
import com.TruMIS.assetservice.service.impl.EmailServiceImpl;

@RestController
@RequestMapping(value = "/api/v1/assets")
public class EmailController {
	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
	private final EmailService emailService;

	public EmailController(EmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping("/email")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> saveMailObject(@RequestBody AssetMailConfig assetMailConfig) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Inside the saveMailconfig controller and saving the mail object. AssetMailObject is : "
					+ assetMailConfig);
			AssetMailConfig savedMailObject = emailService.saveMailObject(assetMailConfig);
			response.put("Success", savedMailObject);
		} catch (Exception e) {
			log.info("Exception Occurred  " + e.getMessage());
			response.put("Exception", "Error while creating the Mail Object: " + e.getMessage());
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/email")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getAllMailConfig(@RequestParam(required = false) String location,
			@RequestParam(required = false) String type) {
		Map<String, Object> response = new HashMap<>();
		try {
			if (Objects.isNull(location)) {
				log.info("Inside the getAllMailConfig Controller and Fetching all the mail body objects from Data");
				List<AssetMailConfig> mailBodyObjects = emailService.findAllMailObjects();
				log.info("success", "Mail body objects fetched successfully");
				response.put("mailBodyObjects", mailBodyObjects);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				if (type.equalsIgnoreCase("CC")) {
					log.info(
							"Inside the getAllMailConfig Controller and Fetching all the CC mail body objects from Data Based On Location");
					List<AssetMailConfig> mailBodyObjects = emailService.findCCMailWithGivenLocation(location);
					log.info("success", "CC Mail body objects fetched successfully");
					response.put("mailBodyObjects", mailBodyObjects);
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else if (type.equalsIgnoreCase("BCC")) {
					log.info(
							"Inside the getAllMailConfig Controller and Fetching all the BCC mail body objects from Data Based On Location");
					List<AssetMailConfig> mailBodyObjects = emailService.findBCCMailWithGivenLocation(location);
					log.info("success", "BCC Mail body objects fetched successfully");
					response.put("mailBodyObjects", mailBodyObjects);
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else {
					log.info(
							"Inside the getAllMailConfig Controller and Fetching all the CC & BCC mail body objects from Data Based On Location");
					List<AssetMailConfig> mailCCandBCCBodyObjects = emailService
							.findCCandBCCMailWithGivenLocation(location);
					response.put("mailBodyObjects", mailCCandBCCBodyObjects);
					return new ResponseEntity<>(response, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			response.put("error", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/email/{id}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> updateMailConfig(@PathVariable("id") Long Objectid,
			@RequestBody AssetMailConfig assetMailConfig) {
		log.info("Inside the put mapping: /api/v1/assets/email/" + Objectid);
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Indise the try-catch of PutMapping, updating the current emailObject with id " + Objectid);
			AssetMailConfig updatedMailConfig = emailService.updateMailObject(Objectid, assetMailConfig);
			response.put("updatedMailConfig", updatedMailConfig);
		} catch (Exception e) {
			response.put("Exception", e.getMessage());
			log.info("Exception while updating the mail Object" + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/email/{id}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> deleteMailConfigById(@PathVariable("id") Long ObjectId) {
		log.info("Indise the delete mapping: /api/v1/assets/email/" + ObjectId);
		Map<String, Object> response = new HashMap<>();
		try {
			emailService.deleteById(ObjectId);
			String message = "Email Object with Id as " + ObjectId + " Deleted Successfully";
			response.put("Success", message);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception while Deleting the Mail Object " + e.getMessage());
			response.put("Exception", e.getLocalizedMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
