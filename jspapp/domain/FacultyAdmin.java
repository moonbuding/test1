package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FacultyAdminMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FundingApplicationMapper;

import java.util.List;

/**
 * Represents a faculty administrator who manages funding applications.
 */
public class FacultyAdmin extends Person {

    /**
     * The list of funding applications reviewed by the faculty administrator.
     */
    private List<FundingApplication> reviewedApplications;

    /**
     * Constructs a new FacultyAdmin with the specified identifier.
     *
     * @param id the unique identifier for this faculty administrator
     */
    public FacultyAdmin(Long id) {
        super(id);
    }

    /**
     * Constructs a new FacultyAdmin with the specified values.
     *
     * @param id       the unique identifier for this faculty
     *                 administrator
     * @param name     the name of the faculty administrator
     * @param email    the email of the faculty administrator
     * @param password the password of the faculty administrator
     */
    public FacultyAdmin(Long id, String name, String email, String password) {
        super(id, name, email, password);
    }

    /**
     * Returns the name of the faculty administrator. Loads data if necessary.
     *
     * @return the name of the faculty administrator
     */
    @Override
    public String getName() {
        if (name == null) {
            ((FacultyAdminMapper) DataMapper.getMapper(FacultyAdmin.class))
                    .findName(getId())
                    .ifPresent(name -> this.name = name);
        }
        return super.getName();
    }

    /**
     * Returns the email of the faculty administrator. Loads data if necessary.
     *
     * @return the email of the faculty administrator
     */
    @Override
    public String getEmail() {
        if (email == null) {
            ((FacultyAdminMapper) DataMapper.getMapper(FacultyAdmin.class))
                    .findEmail(getId())
                    .ifPresent(email -> this.email = email);
        }
        return super.getEmail();
    }

    /**
     * Returns the password of the faculty administrator. Loads data if
     * necessary.
     *
     * @return the password of the faculty administrator
     */
    @Override
    public String getPassword() {
        if (password == null) {
            ((FacultyAdminMapper) DataMapper.getMapper(FacultyAdmin.class))
                    .findPassword(getId())
                    .ifPresent(value -> this.password = value);
        }
        return super.getPassword();
    }

    /**
     * Returns the list of funding applications reviewed by the faculty
     * administrator. Loads data if necessary.
     *
     * @return the list of reviewed funding applications
     */
    public List<FundingApplication> getReviewedApplications() {
        if (reviewedApplications == null) {
            reviewedApplications = ((FundingApplicationMapper)
                    DataMapper.getMapper(FundingApplication.class))
                    .findByReviewer(getId());
        }
        return reviewedApplications;
    }

    /**
     * Adds a funding application to the list of reviewed applications.
     *
     * @param application the funding application to add
     */
    public void addReviewedApplication(FundingApplication application) {
        getReviewedApplications().add(application);
    }
}
