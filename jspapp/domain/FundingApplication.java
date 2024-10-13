package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.EventMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FundingApplicationMapper;

/**
 * Represents a funding application submitted by a student club.
 */
public class FundingApplication extends DomainObject {

    private String description;
    private Double amount;
    private FundingStatus status;
    private StudentClub studentClub;
    private FacultyAdmin reviewer;
    private Integer semester;
    private Integer version;

    public FundingApplication(Long id) {
        super(id);
    }

    public FundingApplication(Long id, Integer version) {
        super(id);
        this.version = version;
    }

    public FundingApplication(Long id, String description, Double amount,
            FundingStatus status, Integer semester) {
        super(id);
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.semester = semester;
    }

    public FundingApplication(Long id, String description, Double amount, FundingStatus status, Integer semester,
            StudentClub club) {
        super(id);
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.semester = semester;
        this.studentClub = club;
    }

    // Getters and setters for all fields, including the new name field
    public String getDescription() {
        if (description == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findDescription(getId())
                    .ifPresent(description -> this.description = description);
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        if (amount == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findAmount(getId())
                    .ifPresent(amount -> this.amount = amount);
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public FundingStatus getStatus() {
        if (status == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findStatus(getId())
                    .ifPresent(status -> this.status = status);
        }
        return status;
    }

    public void setStatus(FundingStatus status) {
        this.status = status;
    }

    public StudentClub getClub() {
        if (studentClub == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findStudentClub(getId())
                    .ifPresent(studentClub -> this.studentClub = studentClub);
        }
        return studentClub;
    }

    public void setClub(StudentClub studentClub) {
        this.studentClub = studentClub;
    }

    public FacultyAdmin getReviewer() {
        if (reviewer == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findReviewer(getId())
                    .ifPresent(reviewer -> this.reviewer = reviewer);
        }
        return reviewer;
    }

    public void setReviewer(FacultyAdmin reviewer) {
        this.reviewer = reviewer;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getSemester() {
        if (semester == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(getClass()))
                    .findSemester(getId())
                    .ifPresent(semester -> this.semester = semester);
        }
        return semester;
    }

    /**
     * Returns the version number identifier for this funding application.
     * @return the version number identifier.
     */
    public Integer getVersion() {
        if (version == null) {
            ((FundingApplicationMapper) DataMapper.getMapper(FundingApplication.class))
                    .findVersion(getId())
                    .ifPresent(this::setVersion);
        }
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}