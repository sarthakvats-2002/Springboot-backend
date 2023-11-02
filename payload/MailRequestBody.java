
package com.TruMIS.assetservice.payload;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MailRequestBody {
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String text;
}
