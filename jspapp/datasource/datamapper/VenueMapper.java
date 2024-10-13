package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Venue;
import com.unimelb.swen90007.jspapp.domain.VenueType;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * The VenueMapper class is responsible for mapping Venue objects to and from
 * the database.
 */
public class VenueMapper extends DataMapper {

    /**
     * Finds the type of Venue by its ID.
     *
     * @param id the ID of the Venue.
     * @return an Optional containing the VenueType if found, or an empty
     * Optional if not.
     */
    public Optional<VenueType> findType(Long id) {
        DBConnection conn = null;
        VenueType type = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT location FROM Venues
                            WHERE venueID = ?
                            """,
                    id
            );

            if (rs.next()) {
                type = VenueType.fromPretty(rs.getString("location"));
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(type);
    }

    /**
     * Finds the capacity of a Venue by its ID.
     *
     * @param id the ID of the Venue.
     * @return an Optional containing the capacity if found, or an empty
     * Optional if not.
     */
    public Optional<Integer> findCapacity(Long id) {
        DBConnection conn = null;
        Integer capacity = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT capacity FROM Venues
                            WHERE venueID = ?
                            """,
                    id
            );

            if (rs.next()) {
                capacity = rs.getInt("capacity");
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
        return Optional.ofNullable(capacity);
    }

    /**
     * Finds the address of a Venue by its ID.
     *
     * @param id the ID of the Venue.
     * @return an Optional containing the address if found, or an empty
     * Optional if not.
     */
    public Optional<String> findAddress(Long id) {
        DBConnection conn = null;
        String address = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT address FROM Venues
                            WHERE venueID = ?
                            """,
                    id
            );

            if (rs.next()) {
                address = rs.getString("address");
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(address);
    }

    /**
     * Inserts a new Venue into the database.
     *
     * @param obj the Venue object to insert into the database.
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        Venue venue = (Venue) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            INSERT INTO Venues (location, address, capacity)
                            VALUES (?, ?, ?)
                            RETURNING venueid
                            """,
                    venue.getType().toString(), venue.getAddress(),
                    venue.getCapacity());
            if (rs.next()) {
                Long venueID = rs.getLong("venueid");
                venue.setId(venueID);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing Venue in the database.
     *
     * @param obj the Venue object containing updated information.
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        Venue venue = (Venue) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE Venues
                            SET location = ?, address = ?, capacity = ?
                            WHERE venueID = ?
                            """,
                    venue.getType().toString(), venue.getAddress(),
                    venue.getCapacity(), venue.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a Venue from the database.
     *
     * @param obj the Venue object to delete from the database.
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM Venues
                            WHERE venueID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
