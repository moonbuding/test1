package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.RsvpMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.TicketMapper;

import java.util.List;
import java.sql.Timestamp;

/**
 * Represents a reservation for an event made by a student.
 */
public class Rsvp extends DomainObject {

    /**
     * The student who made the reservation.
     */
    private Student student;
    /**
     * The event for which the reservation is made.
     */
    private Event event;
    /**
     * The time when the operation is performed.
     */
    private Timestamp issueDate;
    /**
     * Indicates whether the reservation is cancelled.
     */
    private Boolean cancelled;

    /**
     * The list of tickets associated with this reservation.
     */
    private List<Ticket> tickets;

    /**
     * Constructs a new RSVP with default values.
     */
    public Rsvp() {
    }

    /**
     * Constructs a new RSVP with the specified identifier.
     *
     * @param id the unique identifier for this RSVP
     */
    public Rsvp(Long id) {
        super(id);
    }

    /**
     * Constructs a new RSVP with the specified identifier and student
     * identifier.
     *
     * @param id the unique identifier for this RSVP
     */
    public Rsvp(Long id, Long studentID) {
        super(id);
        student = new Student(studentID);
    }

    /**
     * Constructs a new RSVP with the specified identifier, student identifier,
     * event identifier, issue date, and cancellation status.
     *
     * @param id        the unique identifier for this RSVP
     * @param studentID the unique identifier for the student
     * @param eventID   the unique identifier for the event
     * @param issueDate the time when the operation is performed
     * @param cancelled {@code true} if the RSVP is cancelled, {@code false}
     *                  otherwise
     */
    public Rsvp(Long id, Long studentID, Long eventID, Timestamp issueDate, Boolean cancelled) {
        super(id);
        this.student = new Student(studentID);
        this.event = new Event(eventID);
        this.issueDate = issueDate;
        this.cancelled = cancelled;
    }

    /**
     * Returns the student associated with this RSVP. Loads data if necessary.
     *
     * @return the student
     */
    public Student getStudent() {
        if (student == null) {
            ((RsvpMapper) DataMapper.getMapper(Rsvp.class))
                    .findStudent(getId())
                    .ifPresent(student -> this.student = student);
        }
        return student;
    }

    /**
     * Sets the student associated with this RSVP.
     *
     * @param student the student to set
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Returns the event associated with this RSVP. Loads data if necessary.
     *
     * @return the event
     */
    public Event getEvent() {
        if (event == null) {
            ((RsvpMapper) DataMapper.getMapper(Rsvp.class))
                    .findEvent(getId())
                    .ifPresent(event -> this.event = event);
        }
        return event;
    }

    /**
     * Sets the event associated with this RSVP.
     *
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Returns whether this RSVP is cancelled. Loads data if necessary.
     *
     * @return {@code true} if the RSVP is cancelled, {@code false} otherwise
     */
    public Boolean getCancelled() {
        if (cancelled == null) {
            ((RsvpMapper) DataMapper.getMapper(Rsvp.class))
                    .findCancelled(getId())
                    .ifPresent(cancelled -> this.cancelled = cancelled);
        }
        return cancelled;
    }

    /**
     * Sets whether this RSVP is cancelled.
     *
     * @param cancelled {@code true} if the RSVP should be marked as cancelled,
     *                  {@code false} otherwise
     */
    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Returns the time when the operation is performed.
     *
     * @return the time when the operation is performed
     */
    public Timestamp getIssueDate() {
        return issueDate;
    }

    /**
     * Sets the time when the operation is performed.
     *
     * @param issueDate the time when the operation is performed
     */
    public void setIssueDate(Timestamp issueDate) {
        this.issueDate = issueDate;
    }

    /**
     * Returns the list of tickets associated with this RSVP. Loads data if
     * necessary.
     *
     * @return the list of tickets
     */
    public List<Ticket> getTickets() {
        if (tickets == null) {
            tickets = ((TicketMapper) DataMapper.getMapper(Ticket.class))
                    .findByRsvp(getId());
        }
        return tickets;
    }

    /**
     * Adds a ticket to this RSVP.
     *
     * @param ticket the ticket to add
     */
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }
}
