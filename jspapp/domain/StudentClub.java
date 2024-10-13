package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.EventMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentClubMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a club with members, events, and funding applications.
 */
public class StudentClub extends DomainObject {

    /**
     * The name of the club.
     */
    private String name;

    /**
     * The description of the club.
     */
    private String description;

    /**
     * The list of students who are members of the club.
     */
    private List<Student> admins = new ArrayList<>();

    /**
     * The list of events organized by the club.
     */
    private List<Event> events = new ArrayList<>();

    /**
     * The list of funding applications submitted by the club.
     */
    private List<FundingApplication> fundingApplications = new ArrayList<>();

    public StudentClub() {
    }

    public StudentClub(Long id) {
        super(id);
    }

    public StudentClub(Long id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the club. Loads data if necessary.
     *
     * @return the name of the club
     */
    public String getName() {
        if (name == null) {
            ((StudentClubMapper) DataMapper.getMapper(StudentClub.class))
                    .findName(getId())
                    .ifPresent(name -> this.name = name);
        }
        return name;
    }

    /**
     * Sets the name of the club.
     *
     * @param name the new name of the club
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the club. Loads data if necessary.
     *
     * @return the description of the club
     */
    public String getDescription() {
        if (description == null) {
            ((StudentClubMapper) DataMapper.getMapper(StudentClub.class))
                    .findDescription(getId())
                    .ifPresent(description -> this.description = description);
        }
        return description;
    }

    /**
     * Sets the description of the club.
     *
     * @param description the new description of the club
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the list of members of the club. Loads data if necessary.
     *
     * @return the list of members
     */
    public List<Student> getAdmins() {
        if (admins == null || admins.isEmpty()) {
            admins = ((StudentMapper) DataMapper.getMapper(Student.class))
                    .findByClub(getId());
        }
        return admins;
    }

    /**
     * Adds a member to the club.
     *
     * @param student the student to add as a member
     */
    public void addMember(Student student) {
        getAdmins().add(student);
    }

    /**
     * Returns the list of events organized by the club. Loads data if
     * necessary.
     *
     * @param student the student to remove as a member
     */
    public void removeMember(Student student) {
        admins.remove(student);
    }

    /**
     * Returns the list of events organized by the club. Loads data if necessary.
     *
     * @return the list of events
     */
    public List<Event> getEvents() {
        if (events == null || events.isEmpty()) {
            events = ((EventMapper) DataMapper.getMapper(Event.class))
                    .findByClub(getId());
        }
        return events;
    }

    /**
     * Adds an event to the club's list of events.
     *
     * @param event the event to add
     */
    public void addEvent(Event event) {
        getEvents().add(event);
    }

    /**
     * Returns the list of funding applications submitted by the club. Loads
     * data if necessary.
     *
     * @return the list of funding applications
     */
    public List<FundingApplication> getFundingApplications() {
        if (fundingApplications == null) {
            fundingApplications = ((StudentClubMapper) DataMapper.getMapper(getClass()))
                    .findFundingApplications(getId());
        }
        return fundingApplications;
    }

    /**
     * Adds a funding application to the club's list.
     *
     * @param fundingApplication the funding application to add
     */
    public void addFundingApplication(FundingApplication fundingApplication) {
        fundingApplications.add(fundingApplication);
    }
}
