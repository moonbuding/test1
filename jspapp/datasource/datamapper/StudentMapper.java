package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The StudentMapper class maps Student objects to and from the database.
 */
public class StudentMapper extends DataMapper {

    /**
     * Inserts a Student record into the database.
     *
     * @param obj the Student object to insert.
     */
    @Override
    public void insert(DomainObject obj) {
        DBConnection conn = null;
        Student student = (Student) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            INSERT INTO Students (name, email, password)
                            VALUES (?, ?, ?)
                            """,
                    student.getName(), student.getEmail(), student.getPassword());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates an existing Student record in the database.
     *
     * @param obj the Student object with updated information.
     */
    @Override
    public void update(DomainObject obj) {
        DBConnection conn = null;
        Student student = (Student) obj;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            UPDATE Students
                            SET name = ?, email = ?, password = ?
                            WHERE studentID = ?
                            """,
                    student.getName(),
                    student.getEmail(),
                    student.getPassword(),
                    student.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Deletes a Student record from the database.
     *
     * @param obj the Student object to delete.
     */
    @Override
    public void delete(DomainObject obj) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    """
                            DELETE FROM Students
                            WHERE studentID = ?
                            """,
                    obj.getId());
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Finds a Student by email.
     *
     * @param email the email of the Student to find.
     * @return an Optional containing the found Student, or Optional.empty()
     * if not found.
     */
    public Optional<Student> findByEmail(String email) {
        DBConnection conn = null;
        Student student = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT * FROM Students
                            WHERE email = ?
                            """,
                    email);

            if (rs.next()) {
                student = new Student(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getString(4));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to find student by email", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(student);
    }

    /**
     * Finds the students administrating the given StudentClub.
     *
     * @param clubID the unique identifier of the student club.
     * @return the list of members if found, or an empty list if not.
     */
    public List<Student> findByClub(Long clubID) {
        DBConnection conn = null;
        List<Student> members = new ArrayList<>();

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT studentID FROM Memberships WHERE clubID = ?
                            """,
                    clubID);

            while (rs.next()) {
                Long studentID = rs.getLong("studentID");
                members.add(new Student(studentID));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to get database field", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return members;
    }

    /**
     * Finds and retrieves the name of a student.
     *
     * @param id the ID of the student.
     * @return an Optional containing the name of the student, or
     * Optional.empty() if not found.
     */
    public Optional<String> findName(Long id) {
        DBConnection conn = null;
        String name = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT name FROM Students
                            WHERE studentID = ?
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
     * Finds and retrieves the email of a student.
     *
     * @param id the ID of the student.
     * @return an Optional containing the email of the student, or
     * Optional.empty() if not found.
     */
    public Optional<String> findEmail(Long id) {
        DBConnection conn = null;
        String email = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT email FROM Students
                            WHERE studentID = ?
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
     * Finds and retrieves the password of a student.
     *
     * @param id the ID of the student.
     * @return an Optional containing the password of the student, or
     * Optional.empty() if not found.
     */
    public Optional<String> findPassword(Long id) {
        DBConnection conn = null;
        String password = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT password FROM Students
                            WHERE studentID = ?
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

    public Optional<Student> findById(Long id) {
        DBConnection conn = null;
        Student student = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    """
                            SELECT * FROM Students
                            WHERE studentID = ?
                            """,
                    id);
            if (rs.next()) {
                student = new Student(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getString(4));
            }

            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            LogManager.getLogger().error("Unable to find student by ID", e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(student);
    }
}
