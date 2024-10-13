package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.RsvpMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentClubMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentMapper;

import java.util.List;

/**
 * Represents a student who is a member of and also an administrator for
 * student clubs, and can have RSVPs for events.
 */
public class Student extends Person {
    /**
     * List of student clubs that the student is an administrator of.
     */
    private List<StudentClub> clubs;

    /**
     * List of RSVPs for events made by the student.
     */
    private List<Rsvp> rsvps;

    /**
     * Default constructor.
     */
    public Student() {
    }

    /**
     * Constructs a student with the specified name, email, and password.
     *
     * @param name     the name of the student
     * @param email    the email of the student
     * @param password the password of the student
     */
    public Student(String name, String email, String password) {
        super(name, email, password);
    }

    /**
     * Constructs a student with the specified ID.
     *
     * @param id the ID of the student
     */
    public Student(Long id) {
        super(id);
    }

    /**
     * Constructs a student with the specified details.
     *
     * @param id       the ID of the student
     * @param name     the name of the student
     * @param email    the email of the student
     * @param password the password of the student
     */
    public Student(Long id, String name, String email, String password) {
        super(id, name, email, password);
    }

    /**
     * Gets the name of the student, loading it from the database if necessary.
     *
     * @return the name of the student
     */
    @Override
    public String getName() {
        if (name == null) {
            ((StudentMapper) DataMapper.getMapper(Student.class))
                    .findName(getId())
                    .ifPresent(name -> this.name = name);
        }
        return name;
    }

    /**
     * Gets the email of the student, loading it from the database if
     * necessary.
     *
     * @return the email of the student
     */
    @Override
    public String getEmail() {
        if (email == null) {
            ((StudentMapper) DataMapper.getMapper(getClass()))
                    .findEmail(getId())
                    .ifPresent(email -> this.email = email);
        }
        return email;
    }

    /**
     * Gets the password of the student, loading it from the database if
     * necessary.
     *
     * @return the password of the student
     */
    @Override
    public String getPassword() {
        if (password == null) {
            ((StudentMapper) DataMapper.getMapper(StudentClub.class))
                    .findPassword(getId())
                    .ifPresent(password -> this.password = password);
        }
        return password;
    }

    /**
     * Gets the list of clubs that the student is an administrator of.
     *
     * @return the list of student clubs
     */
    public List<StudentClub> getClubs() {
        if (clubs == null) {
            clubs = ((StudentClubMapper)
                    DataMapper.getMapper(StudentClub.class))
                    .findByStudent(getId());
        }
        return clubs;
    }

    /**
     * Adds a club to the list of clubs that the student is an administrator
     * of.
     *
     * @param club the student club to add
     */
    public void addClubs(StudentClub club) {
        getClubs().add(club);
    }

    /**
     * Gets the list of RSVPs for events made by the student.
     *
     * @return the list of RSVPs
     */
    public List<Rsvp> getRsvps() {
        if (rsvps == null) {
            rsvps = ((RsvpMapper) DataMapper.getMapper(Rsvp.class))
                    .findByStudent(getId());
        }
        return rsvps;
    }

    /**
     * Adds an RSVP to the list of RSVPs for events made by the student.
     *
     * @param rsvp the RSVP to add
     */
    public void addRsvp(Rsvp rsvp) {
        getRsvps().add(rsvp);
    }

    /**
     * Compares this Student to another object for equality based on ID.
     *
     * @param o the object to compare with.
     * @return true if this Student and the given object are the same instance
     * or have the same ID, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId().equals(((Student) o).getId());
    }

    /**
     * Returns a hash code value for this Student based on ID.
     *
     * @return the hash code value of this Student.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
