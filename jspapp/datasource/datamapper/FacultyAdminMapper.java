package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.FacultyAdmin;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles database operations for FacultyAdmin objects.
 */
public class FacultyAdminMapper extends DataMapper {

    /**
     * Inserts a new faculty administrator into the database.
     *
     * @param obj the faculty administrator to insert
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        FacultyAdmin admin = ((FacultyAdmin) obj);

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            INSERT INTO FacultyAdministrators
                                (name, email, password)
                            VALUES (?, ?, ?)
                            """,
                    admin.getName(), admin.getEmail(), admin.getPassword());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to insert database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing faculty administrator in the database.
     *
     * @param obj the faculty administrator to update
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        FacultyAdmin admin = ((FacultyAdmin) obj);

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE FacultyAdministrators
                            SET name = ?, email = ?, password = ?
                            WHERE facultyID = ?
                            """,
                    admin.getName(), admin.getEmail(), admin.getPassword(),
                    admin.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to update database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a faculty administrator from the database.
     *
     * @param obj the faculty administrator to delete
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM FacultyAdministrators
                            WHERE facultyID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to delete database entry", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Finds the name of a faculty administrator by their unique identifier.
     *
     * @param id the unique identifier of the faculty administrator
     * @return an Optional containing the name if found, or empty if not
     */
    public Optional<String> findName(Long id) {
        DBConnection conn = null;
        String name = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT name FROM FacultyAdministrators
                            WHERE facultyID = ?
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
     * Finds the email of a faculty administrator by their unique identifier.
     *
     * @param id the unique identifier of the faculty administrator
     * @return an Optional containing the email if found, or empty if not
     */
    public Optional<String> findEmail(Long id) {
        DBConnection conn = null;
        String email = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT email FROM FacultyAdministrators
                            WHERE facultyID = ?
                            """,
                    id);
            if (rs.next()) {
                email = rs.getString("email");
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(email);
    }

    /**
     * Finds the password of a faculty administrator by their unique identifier.
     *
     * @param id the unique identifier of the faculty administrator
     * @return an Optional containing the password if found, or empty if not
     */
    public Optional<String> findPassword(Long id) {
        DBConnection conn = null;
        String password = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT password FROM FacultyAdministrators
                            WHERE facultyID = ?
                            """,
                    id);
            if (rs.next()) {
                password = rs.getString("password");
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(password);
    }

    /**
     * Finds a faculty administrator by their email address.
     *
     * @param email the email address of the faculty administrator
     * @return an Optional containing the faculty administrator if found, or empty if not
     */
    public static Optional<FacultyAdmin> findByEmail(String email) {
        DBConnection conn = null;
        FacultyAdmin admin = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT facultyID, name, password
                            FROM FacultyAdministrators
                            WHERE email = ?
                            """,
                    email);
            if (rs.next()) {
                admin = new FacultyAdmin(
                        rs.getLong("facultyID"),
                        rs.getString("name"),
                        email,
                        rs.getString("password"));
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(admin);
    }
}
