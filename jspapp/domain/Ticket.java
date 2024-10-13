package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.TicketMapper;

/**
 * Represents a ticket associated with an RSVP, a student, and an event.
 */
public class Ticket extends DomainObject {
    /**
     * The RSVP associated with this ticket.
     */
    private Rsvp rsvp;

    /**
     * The student who holds this ticket.
     */
    private Student student;

    /**
     * The event associated with this ticket.
     */
    private Event event;

    /**
     * Default constructor for Ticket.
     */
    public Ticket() {
    }

    /**
     * Constructs a Ticket with the specified id.
     *
     * @param id the unique identifier for the ticket
     */
    public Ticket(Long id) {
        super(id);
    }

    /**
     * Constructs a Ticket with the specified id, RSVP, and student.
     *
     * @param id      the unique identifier for the ticket
     * @param rsvp    the RSVP associated with the ticket
     * @param student the student who holds the ticket
     */
    public Ticket(Long id, Rsvp rsvp, Student student) {
        super(id);
        this.rsvp = rsvp;
        this.student = student;
    }

    /**
     * Returns the RSVP associated with this ticket.
     * Loads the RSVP from the database if it has not been loaded yet.
     *
     * @return the RSVP associated with this ticket
     */
    public Rsvp getRsvp() {
        if (rsvp == null) {
            ((TicketMapper) DataMapper.getMapper(Ticket.class))
                    .findRsvp(getId())
                    .ifPresent(rsvp -> this.rsvp = rsvp);
        }
        return rsvp;
    }

    /**
     * Sets the RSVP associated with this ticket.
     *
     * @param rsvp the RSVP to associate with this ticket
     */
    public void setRsvp(Rsvp rsvp) {
        this.rsvp = rsvp;
    }

    /**
     * Returns the student who holds this ticket.
     * Loads the student from the database if it has not been loaded yet.
     *
     * @return the student who holds this ticket
     */
    public Student getStudent() {
        if (student == null) {
            ((TicketMapper) DataMapper.getMapper(Ticket.class))
                    .findStudent(getId())
                    .ifPresent(student -> this.student = student);
        }
        return student;
    }

    /**
     * Sets the student who holds this ticket.
     *
     * @param student the student to associate with this ticket
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Returns the event associated with this ticket.
     * Loads the event from the database if it has not been loaded yet.
     *
     * @return the event associated with this ticket
     */
    public Event getEvent() {
        if (event == null) {
            ((TicketMapper) DataMapper.getMapper(Ticket.class))
                    .findEvent(getId())
                    .ifPresent(event -> this.event = event);
        }
        return event;
    }

    /**
     * Sets the event associated with this ticket.
     *
     * @param event the event to associate with this ticket
     */
    public void setEvent(Event event) {
        this.event = event;
    }
}
