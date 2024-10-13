package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.EventMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.RsvpMapper;

import java.util.List;

/**
 * Represents an event with details, venue, and RSVP information.
 */
public class Event extends DomainObject {

    /**
     * The list of RSVPs for the event.
     */
    private List<Rsvp> rsvps;

    /**
     * The title of the event.
     */
    private String title;

    /**
     * Description of the event.
     */
    private String description;

    /**
     * Number of attendees for the event.
     */
    private Integer attendees;

    /**
     * Venue where the event is held.
     */
    private Venue venue;

    /**
     * Date and time of the event in milliseconds since epoch.
     */
    private Long dateTime;

    /**
     * Indicates if the event has been cancelled.
     */
    private Boolean cancelled;

    /**
     * Student club organizing the event.
     */
    private StudentClub club;

    /**
     * The version number to use for concurrency checking.
     */
    private Integer version;

    /**
     * Constructs a new Event with no initial values.
     */
    public Event() {
    }

    /**
     * Constructs a new Event with the specified identifier.
     *
     * @param id the unique identifier for this event
     */
    public Event(Long id) {
        super(id);
    }

    public Event(Long id, Integer version) {
        super(id);
        this.version = version;
    }

    /**
     * Constructs a new Event with the specified values.
     *
     * @param id          the unique identifier for this event
     * @param title       the title of the event
     * @param description the description of the event
     * @param attenders   the number of people attending the event
     * @param dateTime    the date and time of the event
     * @param cancelled   whether the event is cancelled
     */
    public Event(Long id, String title, String description, Integer attenders,
            Long dateTime, Boolean cancelled) {
        super(id);
        this.title = title;
        this.description = description;
        this.attendees = attenders;
        this.dateTime = dateTime;
        this.cancelled = cancelled;
    }

    /**
     * Constructs a new Event with the specified values.
     *
     * @param id          the unique identifier for this event
     * @param title       the title of the event
     * @param description the description of the event
     * @param attenders   the number of people attending the event
     * @param venue       the venue where the event is held
     * @param dateTime    the date and time of the event
     * @param club        the student club organizing the event
     * @param cancelled   whether the event is cancelled
     */
    public Event(Long id, String title, String description, int attenders, Venue venue, long dateTime, StudentClub club,
            boolean cancelled) {
        super(id);
        this.title = title;
        this.description = description;
        this.attendees = attenders;
        this.venue = venue;
        this.dateTime = dateTime;
        this.club = club;
        this.cancelled = cancelled;
    }

    /**
     * Returns the title of the event. Loads data if necessary.
     *
     * @return the title of the event
     */
    public String getTitle() {
        if (title == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findTitle(getId())
                    .ifPresent(title -> this.title = title);
        }
        return title;
    }

    /**
     * Sets the title of the event.
     *
     * @param title the new title of the event
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the event. Loads data if necessary.
     *
     * @return the description of the event
     */
    public String getDescription() {
        if (description == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findDescription(getId())
                    .ifPresent(description -> this.description = description);
        }
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the new description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the number of people attending the event. Loads data if necessary.
     *
     * @return the number of attenders
     */
    public Integer getAttendees() {
        if (attendees == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findAttenders(getId())
                    .ifPresent(attendees -> this.attendees = attendees);
        }
        return attendees;
    }

    /**
     * Sets the number of people attending the event.
     *
     * @param attendees the new number of attenders
     */
    public void setAttendees(Integer attendees) {
        this.attendees = attendees;
    }

    /**
     * Returns the venue of the event. Loads data if necessary.
     *
     * @return the venue of the event
     */
    public Venue getVenue() {
        if (venue == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findVenue(getId())
                    .ifPresent(venue -> this.venue = venue);
        }
        return venue;
    }

    /**
     * Sets the venue of the event.
     *
     * @param venue the new venue of the event
     */
    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    /**
     * Returns the date and time of the event. Loads data if necessary.
     *
     * @return the date and time of the event
     */
    public Long getDateTime() {
        if (dateTime == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findDateTime(getId())
                    .ifPresent(dateTime -> this.dateTime = dateTime);
        }
        return dateTime;
    }

    /**
     * Sets the date and time of the event.
     *
     * @param dateTime the new date and time of the event
     */
    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the student club organizing the event. Loads data if necessary.
     *
     * @return the student club
     */
    public StudentClub getClub() {
        if (club == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findClub(getId())
                    .ifPresent(club -> this.club = club);
        }
        return club;
    }

    /**
     * Sets the student club organizing the event.
     *
     * @param club the new student club
     */
    public void setClub(StudentClub club) {
        this.club = club;
    }

    /**
     * Returns the ID of the club organizing the event.
     *
     * @return the ID of the club
     */
    public Long getClubID() {
        return club != null ? club.getId() : null;
    }

    /**
     * Returns the ID of the venue where the event takes place.
     *
     * @return the ID of the venue
     */
    public Long getVenueID() {
        return venue != null ? venue.getId() : null;
    }

    /**
     * Returns whether the event is cancelled. Loads data if necessary.
     *
     * @return {@code true} if the event is cancelled; {@code false} otherwise
     */
    public Boolean getCancelled() {
        if (cancelled == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findCancelled(getId())
                    .ifPresent(cancelled -> this.cancelled = cancelled);
        }
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled.
     *
     * @param cancelled {@code true} if the event is cancelled; {@code false}
     *                  otherwise
     */
    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Returns the list of RSVPs for the event. Loads data if necessary.
     *
     * @return the list of RSVPs
     */
    public List<Rsvp> getRsvps() {
        if (rsvps == null) {
            rsvps = ((RsvpMapper) DataMapper.getMapper(Rsvp.class))
                    .findByEvent(getId());
        }
        return rsvps;
    }

    /**
     * Returns the version number identifier for this event.
     * @return the version number identifier.
     */
    public Integer getVersion() {
        if (version == null) {
            ((EventMapper) DataMapper.getMapper(Event.class))
                    .findVersion(getId())
                    .ifPresent(this::setVersion);
        }
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Adds an RSVP to the event.
     *
     * @param rsvp the RSVP to add
     */
    public void addRSVP(Rsvp rsvp) {
        getRsvps().add(rsvp);
    }

}