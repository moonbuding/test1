package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.*;
import com.unimelb.swen90007.jspapp.util.ConcurrencyException;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles database operations for FundingApplication objects with LazyLoad
 * pattern.
 */
public class FundingApplicationMapper extends DataMapper {

    /**
     * Find a funding application by ID. Load the version number initially
     * as this cannot be lazily loaded.
     *
     * @param id The id of the FundingApplication to retrieve.
     * @return An optional containing the FundingApplication if found, or
     *         Optional.empty() otherwise.
     */
    public Optional<FundingApplication> find(Long id) {
        DBConnection conn = null;
        FundingApplication fundingApplication = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT version FROM FundingApplications WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                fundingApplication = new FundingApplication(id,
                        rs.getInt("version"));
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(fundingApplication);
    }

    /**
     * Inserts a new funding application into the database.
     *
     * @param obj the funding application to insert
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        FundingApplication application = (FundingApplication) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            INSERT INTO FundingApplications
                                (description, amount, status, clubID, semester)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                    application.getDescription(), application.getAmount(), application.getStatus().toString(),
                    application.getClub().getId(), application.getSemester());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates a funding application in the database.
     *
     * @param obj the funding application to update
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        FundingApplication application = (FundingApplication) obj;

        try {
            // Retrieve event version
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);
            ResultSet rs = conn.execute(
                    """
                            SELECT version FROM FundingApplications
                            WHERE applicationID = ?
                            FOR SHARE
                            """, application.getId());

            // Event has been deleted, throw error
            if (!rs.next()) {
                conn.commit();
                rs.getStatement().close();
                throw new ConcurrencyException("FundingApplication " +
                        application.getId() + " was modified after deletion");
            }

            // Event has been modified, throw error
            int currVersion = rs.getInt("version");
            if (application.getVersion() != currVersion) {
                conn.commit();
                rs.getStatement().close();
                throw new ConcurrencyException("FundingApplication " +
                        application.getId() + " had concurrent modifications");
            }

            // Otherwise, update the event
            conn.update(
                    """
                            UPDATE FundingApplications
                            SET description = ?, amount = ?,
                                status = ?, clubID = ?, version = ?
                            WHERE applicationID = ?
                            """,
                    application.getDescription(), application.getAmount(),
                    application.getStatus().toString(), application.getClub().getId(),
                    currVersion + 1, application.getId());
            conn.commit();
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException | ConcurrencyException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a funding application from the database.
     *
     * @param obj the funding application to delete
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM FundingApplications
                            WHERE applicationID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Finds the reviewed applications for a faculty administrator by their
     * unique identifier.
     *
     * @param adminID the unique identifier of the faculty administrator
     * @return the list of reviewed applications if found, or an empty list if
     *         not
     */
    public List<FundingApplication> findByReviewer(Long adminID) {
        DBConnection conn = null;
        List<FundingApplication> applications = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT applicationID FROM FundingReviews
                            WHERE facultyID = ?
                            """,
                    adminID);
            while (rs.next()) {
                Long id = rs.getLong("applicationID");
                find(id).ifPresent(applications::add);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return applications;
    }

    /**
     * Finds the name of a funding application.
     *
     * @param clubId the unique identifier of the funding application
     * @return the name of the funding application
     */
    public List<FundingApplication> findByClub(Long clubId) {
        DBConnection conn = null;
        List<FundingApplication> applications = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT applicationID
                            FROM FundingApplications
                            WHERE clubID = ?
                            """,
                    clubId);
            while (rs.next()) {
                Long id = rs.getLong("applicationID");
                find(id).ifPresent(applications::add);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get funding applications for club", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return applications;
    }

    /**
     * Finds the description of a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the description of the funding application
     */
    public Optional<String> findDescription(Long id) {
        DBConnection conn = null;
        String description = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT description
                            FROM FundingApplications
                            WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                description = rs.getString(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(description);
    }

    /**
     * Finds the amount of a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the amount of the funding application
     */
    public Optional<Double> findAmount(Long id) {
        DBConnection conn = null;
        Double amount = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT amount FROM FundingApplications WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                amount = rs.getDouble(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(amount);
    }

    /**
     * Finds the status of a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the status of the funding application
     */
    public Optional<FundingStatus> findStatus(Long id) {
        DBConnection conn = null;
        FundingStatus status = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT status FROM FundingApplications WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                status = FundingStatus.valueOf(rs.getString(1));
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(status);
    }

    /**
     * Finds the associated student club for a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the student club associated with the funding application
     */
    public Optional<StudentClub> findStudentClub(Long id) {
        DBConnection conn = null;
        StudentClub studentClub = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT clubID FROM FundingApplications WHERE ApplicationID = ?
                            """,
                    id);
            if (rs.next()) {
                Long clubId = rs.getLong(1);
                studentClub = new StudentClub(clubId);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(studentClub);
    }

    /**
     * Finds the faculty administrator reviewer for a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the faculty administrator reviewing the funding application
     */
    public Optional<FacultyAdmin> findReviewer(Long id) {
        DBConnection conn = null;
        FacultyAdmin reviewer = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT reviewerID FROM FundingApplications WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                Long reviewerId = rs.getLong("reviewerID");
                reviewer = new FacultyAdmin(reviewerId);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(reviewer);
    }

    /**
     * Finds the semester of a funding application.
     *
     * @param id the unique identifier of the funding application
     * @return the semester of the funding application
     */
    public Optional<Integer> findSemester(Long id) {
        DBConnection conn = null;
        Integer semester = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT semester FROM FundingApplications WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                semester = rs.getInt(1);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(semester);
    }

    /**
     * Finds the version identifier of this funding application.
     *
     * @param id the unique identifier of the funding application
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

    public Optional<FundingApplication> findById(Long id) {
        DBConnection conn = null;
        FundingApplication fundingApplication = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT applicationID, description, amount, status, clubID, semester, version
                            FROM FundingApplications
                            WHERE applicationID = ?
                            """,
                    id);
            if (rs.next()) {
                Long applicationId = rs.getLong("applicationID");
                String description = rs.getString("description");
                Double amount = rs.getDouble("amount");
                FundingStatus status = FundingStatus.valueOf(rs.getString("status"));
                Long clubId = rs.getLong("clubID");
                Integer semester = rs.getInt("semester");
                Integer version = rs.getInt("version");

                fundingApplication = new FundingApplication(applicationId, description, amount, status, semester,
                        new StudentClub(clubId));
                fundingApplication.setVersion(version);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get funding application", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(fundingApplication);
    }

    public List<FundingApplication> findAll() {
        List<FundingApplication> applications = new ArrayList<>();
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT applicationID, description, amount, status, clubID, semester, version
                            FROM FundingApplications
                            """);
            while (rs.next()) {
                Long id = rs.getLong("applicationID");
                String description = rs.getString("description");
                Double amount = rs.getDouble("amount");
                FundingStatus status = FundingStatus.valueOf(rs.getString("status"));
                Long clubId = rs.getLong("clubID");
                Integer semester = rs.getInt("semester");
                Integer version = rs.getInt("version");

                FundingApplication application = new FundingApplication(id, description, amount, status, semester,
                        new StudentClub(clubId));
                application.setVersion(version);
                applications.add(application);
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get funding applications", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return applications;
    }
}