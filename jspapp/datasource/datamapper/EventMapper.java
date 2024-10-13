package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Event;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import com.unimelb.swen90007.jspapp.domain.Venue;
import com.unimelb.swen90007.jspapp.util.ConcurrencyException;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles database operations for Event objects.
 */
public class EventMapper extends DataMapper {

    /**
     * Find an event by ID. Load the version number initially as this cannot be
     * lazily loaded.
     *
     * @param id The id of the Event to retrieve.
     * @return An optional containing the Event if found, or Optional.empty()
     * otherwise.
     */
    public Optional<Event> find(Long id) {
        DBConnection conn = null;
        Event event = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT version FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                Integer version = rs.getInt("version");
                event = new Event(id, version);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(event);
    }

    /**
     * Inserts a new event into the database.
     *
     * @param obj the event to insert
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        Event event = (Event) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            INSERT INTO Events (title, description, attenders,
                                                venueID, dateTime, clubID,
                                                cancelled)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    event.getTitle(),
                    event.getDescription(),
                    event.getAttendees(),
                    event.getVenue().getId(),
                    new java.sql.Timestamp(event.getDateTime()),
                    event.getClub().getId(),
                    event.getCancelled());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to insert event", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing event in the database.
     *
     * @param obj the event to update
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        Event event = (Event) obj;

        try {
            // Retrieve event version
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);
            ResultSet rs = conn.execute(
                    """
                            SELECT version FROM Events
                            WHERE eventID = ?
                            FOR SHARE
                            """, event.getId()
            );

            // Event has been deleted, throw error
            if (!rs.next()) {
                conn.commit();
                rs.getStatement().close();
                throw new ConcurrencyException("Event " + event.getId()
                        + " was modified after deletion");
            }

            // Event has been modified, throw error
            int currVersion = rs.getInt("version");
            if (event.getVersion() != currVersion) {
                conn.commit();
                rs.getStatement().close();
                throw new ConcurrencyException("Event " + event.getId()
                        + " had concurrent modifications");
            }

            // Otherwise, update the event
            conn.update(
                    """
                            UPDATE Events
                            SET title = ?, description = ?,
                                attenders = ?, venueID = ?,
                                dateTime = ?, clubID = ?, cancelled = ?,
                                version = ?
                            WHERE eventID = ?
                            """,
                    event.getTitle(),
                    event.getDescription(),
                    event.getAttendees(),
                    event.getVenue().getId(),
                    new java.sql.Timestamp(event.getDateTime()),
                    event.getClub().getId(),
                    event.getCancelled(),
                    currVersion + 1,
                    event.getId());
            conn.commit();
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException |
                 ConcurrencyException e) {
            LogManager.getLogger().error("Failed to update event", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes an event from the database.
     *
     * @param obj the event to delete
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM Events
                            WHERE eventID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to delete event", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Finds the list of events organized by the StudentClub.
     *
     * @param clubID the unique identifier of the student club
     * @return the list of events if found, or an empty list if not
     */
    public List<Event> findByClub(Long clubID) {
        DBConnection conn = null;
        List<Event> events = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT eventID, version FROM Events WHERE clubID = ?
                            """,
                    clubID);
            while (rs.next()) {
                Long eventID = rs.getLong("eventID");
                Integer version = rs.getInt("version");
                events.add(new Event(eventID, version));
            }

            LogManager.getLogger().info(
                    "Successfully retrieved events for clubID: " + clubID
                            + ", number of events: " + events.size());
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return events;
    }

    /**
     * Finds events by a search query.
     *
     * @param search the search query
     * @return a list of events matching the search query
     */
    public List<Event> findBySearch(String search) {
        DBConnection conn = null;
        List<Event> events = new ArrayList<>();

        try {
            // Execute the search query
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT eventID, version
                            FROM Events
                            WHERE to_tsvector('english', title) @@ to_tsquery(?);
                            """,
                    search);
            if (!rs.isBeforeFirst()) {
                System.out.println("No data returned.");
            }
            while (rs.next()) {
                Long eventID = rs.getLong("eventID");
                Integer version = rs.getInt("version");
                events.add(new Event(eventID, version));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get events by search", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return events;
    }

    /**
     * Finds all events in the database.
     *
     * @return a list of all events
     */
    public List<Event> findAll() {
        DBConnection conn = null;
        List<Event> events = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    "SELECT eventID, version FROM Events");
            while (rs.next()) {
                Long eventID = rs.getLong("eventID");
                Integer version = rs.getInt("version");
                Event event = new Event(eventID, version);
                events.add(event);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get all events", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return events;
    }

    /**
     * Finds the title of an event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the title if found, or empty if not
     */
    public Optional<String> findTitle(Long id) {
        DBConnection conn = null;
        String title = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT title FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                title = rs.getString(1);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event title " + id,
                    e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(title);
    }

    /**
     * Finds the description of an event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the description if found, or empty if not
     */
    public Optional<String> findDescription(Long id) {
        DBConnection conn = null;
        String description = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT description FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                description = rs.getString(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event description "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(description);
    }

    /**
     * Finds the number of attenders of an event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the number of attenders if found, or empty if
     * not
     */
    public Optional<Integer> findAttenders(Long id) {
        DBConnection conn = null;
        Integer attenders = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT attenders FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                attenders = rs.getInt(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event attenders "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(attenders);
    }

    /**
     * Finds the date and time of an event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the date and time if found, or empty if not
     */
    public Optional<Long> findDateTime(Long id) {
        DBConnection conn = null;
        Long dateTime = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT dateTime FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                dateTime = rs.getTimestamp(1).getTime();
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event dateTime "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * Finds the venue of an event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the Venue if found, or empty if not
     */
    public Optional<Venue> findVenue(Long id) {
        DBConnection conn = null;
        Venue venue = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT venueID FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                long venueId = rs.getLong(1);
                venue = new Venue(venueId);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event venue "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(venue);
    }

    /**
     * Finds whether an event is cancelled by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing whether the event is cancelled if found, or
     * empty if not
     */
    public Optional<Boolean> findCancelled(Long id) {
        DBConnection conn = null;
        Boolean cancelled = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT cancelled FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                cancelled = rs.getBoolean(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event cancelled "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(cancelled);
    }

    /**
     * Finds the student club organizing the event by its unique identifier.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the StudentClub if found, or empty if not
     */
    public Optional<StudentClub> findClub(Long id) {
        DBConnection conn = null;
        StudentClub club = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT clubID FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                long clubId = rs.getLong(1);
                club = new StudentClub(clubId);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event club "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(club);
    }

    /**
     * Finds the version identifier of this event.
     *
     * @param id the unique identifier of the event
     * @return an Optional containing the version if found, or empty if not
     */
    public Optional<Integer> findVersion(Long id) {
        DBConnection conn = null;
        Integer version = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT version FROM Events WHERE eventID = ?
                            """,
                    id);
            if (rs.next()) {
                version = rs.getInt("version");
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event version "
                    + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(version);
    }
}