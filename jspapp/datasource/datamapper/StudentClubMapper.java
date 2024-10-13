package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.FundingApplication;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data mapper for StudentClub objects, responsible for mapping database
 * records to StudentClub instances.
 */
public class StudentClubMapper extends DataMapper {

    /**
     * Finds and retrieves the clubs for a student.
     *
     * @param studentID the ID of the student
     * @return a list of StudentClub objects, or an empty list if not found
     */
    public List<StudentClub> findByStudent(Long studentID) {
        DBConnection conn = null;
        List<StudentClub> clubs = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT clubID FROM Memberships
                            WHERE studentID = ?
                            """,
                    studentID);

            while (rs.next()) {
                clubs.add(new StudentClub(rs.getLong("clubID")));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return clubs;
    }

    /**
     * Finds the name of the StudentClub.
     *
     * @param id the unique identifier of the student club
     * @return an Optional containing the name if found, or Optional.empty()
     * if not
     */
    public Optional<String> findName(Long id) {
        DBConnection conn = null;
        String name = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT name FROM StudentClubs WHERE clubID = ?
                            """,
                    id);

            if (rs.next()) {
                name = rs.getString("name");
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(name);
    }

    /**
     * Finds the description of the StudentClub.
     *
     * @param id the unique identifier of the student club
     * @return an Optional containing the description if found, or
     * Optional.empty() if not
     */
    public Optional<String> findDescription(Long id) {
        DBConnection conn = null;
        String description = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT description FROM StudentClubs WHERE clubID = ?
                            """,
                    id);

            if (rs.next()) {
                description = rs.getString("description");
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
     * Finds the list of funding applications submitted by the StudentClub.
     *
     * @param id the unique identifier of the student club
     * @return the list of funding applications if found, or an empty list if
     * not
     */
    public List<FundingApplication> findFundingApplications(Long id) {
        DBConnection conn = null;
        List<FundingApplication> fundingApplications = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT fundingApplicationID
                            FROM FundingApplications
                            WHERE clubID = ?
                            """,
                    id);

            while (rs.next()) {
                Long fundingApplicationID = rs.getLong("fundingApplicationID");
                fundingApplications.add(
                        new FundingApplication(fundingApplicationID));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return fundingApplications;
    }

    /**
     * Inserts a new StudentClub object into the database.
     *
     * @param obj the StudentClub object to insert
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        StudentClub studentClub = (StudentClub) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            INSERT INTO StudentClubs (name, description)
                            VALUES (?, ?)
                            """,
                    studentClub.getName(), studentClub.getDescription());

            for (Student member : studentClub.getAdmins()) {
                conn.update(
                        """
                                INSERT INTO Memberships
                                    (studentID, clubID)
                                VALUES (?, ?)
                                """,
                        member.getId(), studentClub.getId());
            }
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to insert database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing StudentClub object in the database.
     *
     * @param obj the StudentClub object to update
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        StudentClub studentClub = (StudentClub) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE StudentClubs
                            SET name = ?, description = ?
                            WHERE clubID = ?
                            """,
                    studentClub.getName(), studentClub.getDescription(),
                    studentClub.getId());

            // Remove all existing memberships for this club
            conn.update(
                    """
                            DELETE FROM Memberships
                            WHERE clubID = ?
                            """,
                    studentClub.getId());

            // Add membership entries for current admins
            for (Student admin : studentClub.getAdmins()) {
                conn.update(
                        """
                                INSERT INTO Memberships (studentID, clubID)
                                VALUES (?, ?)
                                """,
                        admin.getId(), studentClub.getId());
            }
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to update database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a StudentClub object from the database.
     *
     * @param obj the StudentClub object to delete
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM StudentClubs
                            WHERE clubID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public Optional<StudentClub> find(Long clubId) {
        DBConnection conn = null;
        StudentClub studentClub = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();

            ResultSet rs = conn.execute(
                    """
                            SELECT * FROM StudentClubs WHERE clubID = ?
                            """,
                    clubId);
            if (rs.next()) {
                studentClub = new StudentClub(clubId,
                        rs.getString("name"),
                        rs.getString("description"));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(studentClub);
    }
}
