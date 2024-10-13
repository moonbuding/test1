package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.*;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TicketMapper class is responsible for mapping Ticket objects to and from the
 * database.
 */
public class TicketMapper extends DataMapper {

    /**
     * Finds and retrieves the tickets associated with the RSVP.
     *
     * @param rsvpID the unique identifier of the RSVP
     * @return a list of Tickets, or an empty list if not found
     */
    public List<Ticket> findByRsvp(Long rsvpID) {
        DBConnection conn = null;
        List<Ticket> tickets = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT ticketID FROM Tickets
                            WHERE rsvpID = ?
                            """,
                    rsvpID);

            while (rs.next()) {
                Long ticketID = rs.getLong("ticketID");
                tickets.add(new Ticket(ticketID));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return tickets;
    }

    /**
     * Finds the RSVP associated with the given Ticket ID.
     *
     * @param ticketId the ID of the Ticket.
     * @return an Optional containing the RSVP, or Optional.empty() if not
     * found.
     */
    public Optional<Rsvp> findRsvp(Long ticketId) {
        DBConnection conn = null;
        Rsvp rsvp = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT rsvpID FROM Tickets
                            WHERE id = ?
                            """,
                    ticketId);

            if (rs.next()) {
                rsvp = new Rsvp(rs.getLong("rsvpID"));
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
     * Finds the Student associated with the given Ticket ID.
     *
     * @param ticketId the ID of the Ticket.
     * @return an Optional containing the Student, or Optional.empty() if not
     * found.
     */
    public Optional<Student> findStudent(Long ticketId) {
        DBConnection conn = null;
        Student student = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT studentID FROM Tickets
                            WHERE ticketID = ?
                            """,
                    ticketId);

            if (rs.next()) {
                student = new Student(rs.getLong("studentID"));
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
     * Finds the Event associated with the given Ticket ID.
     *
     * @param ticketId the ID of the Ticket.
     * @return an Optional containing the Event, or Optional.empty() if not
     * found.
     */
    public Optional<Event> findEvent(Long ticketId) {
        DBConnection conn = null;
        Event event = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT eventID FROM Tickets
                            WHERE id = ?
                            """,
                    ticketId);

            if (rs.next()) {
                event = new Event(rs.getLong("eventID"));
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
     * Inserts a new Ticket record into the database.
     *
     * @param obj the DomainObject to insert, which must be a Ticket.
     */
    @Override
    public void insert(DomainObject obj) throws SQLException {
        DBConnection conn = null;
        Ticket ticket = (Ticket) obj;

        try {
            // Check if the Ticket already exists
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rsCheck = conn.execute(
                    """
                            SELECT ticketID FROM Tickets
                            WHERE eventID = ? AND studentID = ?
                            """,
                    ticket.getEvent().getId(), ticket.getStudent().getId());

            if (rsCheck.next()) {
                // Ticket already exists
                rsCheck.getStatement().close();
                throw new SQLException("Duplicate Ticket");
            }
            rsCheck.getStatement().close();

            // Insert Ticket into database
            ResultSet rs = conn.execute(
                    """
                            INSERT INTO Tickets (rsvpID, studentID, eventID)
                            VALUES (?, ?, ?)
                            RETURNING ticketID
                            """,
                    ticket.getRsvp().getId(), ticket.getStudent().getId(),
                    ticket.getEvent().getId());

            if (rs.next()) {
                Long ticketId = rs.getLong(1);
                ticket.setId(ticketId);
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to insert database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing Ticket record in the database.
     *
     * @param obj the DomainObject to update, which must be a Ticket.
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        Ticket ticket = (Ticket) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE Tickets
                            SET rsvpID = ?, studentID = ?, eventID = ?
                            WHERE id = ?
                            """,
                    ticket.getRsvp().getId(), ticket.getStudent().getId(),
                    ticket.getEvent().getId(), ticket.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to update database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a Ticket record from the database.
     *
     * @param obj the DomainObject to delete, which must be a Ticket.
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM Tickets
                            WHERE ticketID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Finds a Ticket by its RSVP and Student.
     *
     * @param rsvp    the RSVP associated with the Ticket.
     * @param student the Student associated with the Ticket.
     * @return an Optional containing the found Ticket, or Optional.empty()
     * if not found.
     */
    public Optional<Ticket> findByRsvpAndStudent(Rsvp rsvp, Student student)
            throws SQLException {
        DBConnection conn = null;
        Ticket ticket = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();

            ResultSet rs = conn.execute(
                    """
                            SELECT ticketID FROM Tickets
                            WHERE rsvpID = ? AND studentID = ?
                            """,
                    rsvp.getId(), student.getId());

            if (rs.next()) {
                ticket = new Ticket(rs.getLong("ticketID"));
            }

            rs.getStatement().close();
        } catch (ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(ticket);
    }

    /**
     * Finds all Tickets associated with an RSVP.
     *
     * @param rsvp the RSVP associated with the Tickets.
     * @return a List of Tickets associated with the RSVP.
     */
    public List<Ticket> findByRsvp(Rsvp rsvp) throws SQLException {
        DBConnection conn = null;
        List<Ticket> tickets = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT ticketID FROM Tickets WHERE rsvpID = ?
                            """,
                    rsvp.getId());
            while (rs.next()) {
                tickets.add(new Ticket(rs.getLong("ticketID")));
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return tickets;
    }

    /**
     * Counts the number of Tickets associated with an Event.
     *
     * @param event the Event associated with the Tickets.
     * @return the number of Tickets associated with the Event.
     */
    public int countTicketsByEvent(Event event) throws SQLException {
        DBConnection conn = null;
        int count = 0;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT COUNT(*) FROM Tickets WHERE eventID = ?
                            """,
                    event.getId());

            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.getStatement().close();
        } catch (ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return count;
    }
}
