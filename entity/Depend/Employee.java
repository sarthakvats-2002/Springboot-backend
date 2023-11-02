package com.TruMIS.assetservice.entity.Depend;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.TruMIS.assetservice.entity.Depend.metadata.EmployeeCategory;
import com.TruMIS.assetservice.entity.Depend.metadata.EmployeeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "emp_code", unique = true)
	@NotNull
	private String employeeCode;

	@Column(name = "base_loc")
	private String baseLocation;

	@Column(name = "native_loc")
	private String nativeLocation;

	@Column(name = "role")
	private String designation;

	@Column(name = "mail_id", unique = true)
	@NotNull
	private String mailId;

	@Column(name = "skill_domain")
	private String skillsDomain;

	@Column(name = "key_skills")
	private String keySkills;

	@Column(name = "comments")
	private String additionalComments;

	@Column(name = "number")
	private String mobileNumber;

	@Column(name = "status")
	private Boolean status;

	@ManyToOne
	@JoinColumn(name = "emp_status_code")
	private EmployeeStatus employeeStatus;

	@ManyToOne
	@JoinColumn(name = "emp_cat_code")
	private EmployeeCategory employeeCategory;

	@Column(name = "rep_manager")
	private String reportingManager;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createDateTime;
	@UpdateTimestamp
	@Column(name = "updated_at", updatable = true)
	private LocalDateTime updateDateTime;

	@Column(name = "exit_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate exitDate;

	@Column(name = "onboard_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate onboardDate;

	@Column(name = "exp_on_onboard")
	private String experienceOnOnboard;

	@Column(name = "curr_exp")
	private String currentExperience;
}
