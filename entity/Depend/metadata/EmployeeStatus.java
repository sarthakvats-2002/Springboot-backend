package com.TruMIS.assetservice.entity.Depend.metadata;

import jakarta.persistence.*;

import java.util.List;

import com.TruMIS.assetservice.entity.Depend.Employee;

@Entity
@Table(name = "emp_stat_metadata")
public class EmployeeStatus {
     /*
     Below Class is responsible for Creating a Employee Status  table with below code in the DB.
     ________________________________
    | id    |code-PK| name         |
    ---------------------------------
    |     1 | ES001 | OnRoll        |
    |     2 | ES002 | Exit          |
    |     3 | ES003 | Absconding    |
	--------------------------------
     */

//    //@Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", insertable = false, updatable = false, nullable = false, columnDefinition = "VARCHAR(255)")
//    private UUID uuid;
    //@Id
    @GeneratedValue()
    @Column(name = "id")
    private Long id;

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "field_name")
    private String name;

    @OneToMany(mappedBy = "employeeStatus")
    private List<Employee> employee;

    public EmployeeStatus() {
    }

    public EmployeeStatus(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
