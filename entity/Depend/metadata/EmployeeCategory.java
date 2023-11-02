package com.TruMIS.assetservice.entity.Depend.metadata;

import jakarta.persistence.*;

import java.util.List;

import com.TruMIS.assetservice.entity.Depend.Employee;

@Entity
@Table(name = "emp_cat_metadata")
public class EmployeeCategory  {
    /*
    Below Class is responsible for Creating a project Domain table with below code in the DB.
    _____________________________
   | id    | code   | name       |
   -------------------------------
   |     1 | EC001 | Billable    |
   |     2 | EC002 | Bench       |
   |     3 | EC003 | Support_fn  |
   |     4 | EC003 | Investment  |
   -------------------------------
    */

    //@Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", insertable = false, updatable = false, nullable = false, columnDefinition = "VARCHAR(255)")
//    private UUID uuid;

    //@Id
    @Column(name = "id")
    @GeneratedValue()
    private Long id;


    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "field_name")
    private String name;

    @OneToMany(mappedBy = "employeeCategory")
   private List<Employee> employee;



    public EmployeeCategory() {
    }

    public EmployeeCategory(Long id, String code, String name, Employee employee) {
        this.id = id;
        this.code = code;
        this.name = name;
        //this.employee = employee;
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
