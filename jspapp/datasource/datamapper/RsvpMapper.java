package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Event;
import com.unimelb.swen90007.jspapp.domain.Rsvp;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data mapper for RSVP objects, responsible for mapping database records to
 * RSVP instances.
 */
public class RsvpMapper extends DataMapper {

    /**
     * Inserts a new RSVP object into the database.
     *
     * @param obj the RSVP to insert
     */
    @Override
    public void insert(DomainObject obj) throws SQLException {
        DBConnection conn = null;
        Rsvp rsvp = (Rsvp) obj;

        try {
            // Check if the RSVP already exists
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rsCheck = conn.execute(
                    """
                            SELECT rsvpID FROM RSVPs
                            WHERE studentID = ? AND eventID = ?
                            """,
                    rsvp.getStudent().getId(), rsvp.getEvent().getId());

            if (rsCheck.next()) {
                // RSVP already exists
                rsCheck.getStatement().close();
                throw new SQLException("Duplicate RSVP");
            }
            rsCheck.getStatement().close();

            // Insert the new RSVP
            ResultSet rs = conn.execute(
                    """
                            INSERT INTO RSVPs (studentID, eventID, cancelled)
                            VALUES (?, ?, ?)
                            RETURNING rsvpID
                            """,
                    rsvp.getStudent().getId(), rsvp.getEvent().getId(),
                    rsvp.getCancelled());

            if (rs.next()) {
                Long rsvpId = rs.getLong("rsvpID");
                rsvp.setId(rsvpId);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing RSVP in the database.
     *
     * @param obj the RSVP to update
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        Rsvp rsvp = (Rsvp) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE RSVPs
                            SET studentID = ?, eventID = ?, cancelled = ?
                            WHERE rsvpID = ?
                            """,
                    rsvp.getStudent().getId(), rsvp.getEvent().getId(),
                    rsvp.getCancelled(), rsvp.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes an existing RSVP from the database.
     *
     * @param obj the RSVP to delete
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM RSVPs
                            WHERE rsvpID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Retrieves a list of RSVPs associated with a specific event.
     *
     * @param eventID the unique identifier of the event
     * @return a list of RSVPs associated with the event or an empty list if no
     *         RSVPs are found
     */
    public List<Rsvp> findByEvent(Long eventID) {
        DBConnection conn = null;
        List<Rsvp> rsvps = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT rsvpID
                            FROM RSVPs
                            WHERE eventID = ?
                            """,
                    eventID);

            while (rs.next()) {
                long rsvpId = rs.getLong("rsvpID");
                Rsvp rsvp = new Rsvp(rsvpId, eventID);
                rsvps.add(rsvp);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Failed to find event rsvps "
                    + eventID, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return rsvps;
    }

    /**
     * Finds and retrieves the RSVPs for a student.
     *
     * @param studentID the ID of the student
     * @return a list of RSVP objects, or an empty list if not found
     */
    public List<Rsvp> findByStudent(Long studentID) {
        DBConnection conn = null;
        List<Rsvp> rsvps = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT rsvpID FROM RSVPs
                            WHERE studentID = ?
                            """,
                    studentID);
            while (rs.next()) {
                rsvps.add(new Rsvp(rs.getLong("rsvpID")));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return rsvps;
    }

    public Optional<Rsvp> findById(Long id) {
        DBConnection conn = null;
        Rsvp rsvp = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT studentID, eventID, issueDate, cancelled
                            FROM RSVPs
                            WHERE rsvpID = ?
                            """,
                    id);

            if (rs.next()) {
                Long studentID = rs.getLong("studentID");
                Long eventID = rs.getLong("eventID");
                Timestamp issueDate = rs.getTimestamp("issueDate");
                Boolean cancelled = rs.getBoolean("cancelled");
                rsvp = new Rsvp(id, studentID, eventID, issueDate, cancelled);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(rsvp);
    }

    /**
     * Finds and retrieves the Student associated with the RSVP.
     *
     * @param id the unique identifier of the RSVP
     * @return an Optional containing the Student, or Optional.empty() if not
     *         found
     */
    public Optional<Student> findStudent(Long id) {
        DBConnection conn = null;
        Student student = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT studentID FROM RSVPs
                            WHERE rsvpID = ?
                            """,
                    id);

            if (rs.next()) {
                Long studentID = rs.getLong("studentID");
                student = new Student(studentID);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(student);
    }

    /**
     * Finds and retrieves the Event associated with the RSVP.
     *
     * @param id the unique identifier of the RSVP
     * @return an Optional containing the Event, or Optional.empty() if not
     *         found
     */
    public Optional<Event> findEvent(Long id) {
        DBConnection conn = null;
        Event event = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT eventID FROM RSVPs
                            WHERE rsvpID = ?
                            """,
                    id);

            if (rs.next()) {
                Long eventID = rs.getLong("eventID");
                event = new Event(eventID);
            }

            rs.getStatement().close(); // Close the statement to release resources
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(event);
    }

    /**
     * Finds and retrieves the cancellation status of the RSVP.
     *
     * @param id the unique identifier of the RSVP
     * @return an Optional containing the cancellation status, or
     *         Optional.empty() if not found
     */
    public Optional<Boolean> findCancelled(Long id) {
        DBConnection conn = null;
        Boolean cancelled = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT cancelled FROM RSVPs
                            WHERE rsvpID = ?
                            """,
                    id);

            if (rs.next()) {
                cancelled = rs.getBoolean("cancelled");
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(cancelled);
    }

    /**
     * Finds and retrieves the RSVP for a specific event and student.
     *
     * @param event   the event
     * @param student the student
     * @return an Optional containing the RSVP, or Optional.empty() if not found
     */
    public Optional<Rsvp> findByEventAndStudent(Event event, Student student) {
        DBConnection conn = null;
        Rsvp rsvp = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT rsvpID FROM RSVPs
                            WHERE eventID = ? AND studentID = ?
                            """,
                    event.getId(), student.getId());

            if (rs.next()) {
                Long rsvpId = rs.getLong("rsvpID");
                rsvp = new Rsvp(rsvpId);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(rsvp);
    }
}