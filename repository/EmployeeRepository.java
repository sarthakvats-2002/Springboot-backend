package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TruMIS.assetservice.entity.Depend.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	@Query("SELECT e FROM Employee e WHERE LOWER(e.mailId) LIKE LOWER(concat('%', :email, '%'))")
	Employee findByMailIdIgnoreCase(@Param("email") String email);
}
