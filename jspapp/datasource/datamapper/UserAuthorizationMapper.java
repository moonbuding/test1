package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.datasource.ConnectionPool;
import com.unimelb.swen90007.jspapp.datasource.DBConnection;
import com.unimelb.swen90007.jspapp.util.ConnectionUnavailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserAuthorizationMapper {
    private static final Logger logger = LogManager.getLogger(UserAuthorizationMapper.class);

    public void insertToken(long id, String token, boolean isAdmin) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            if (isAdmin) {
                conn.update(
                        "INSERT INTO UserAuthorization (facultyID, token) VALUES (?, ?)",
                        id, token);
            } else {
                conn.update(
                        "INSERT INTO UserAuthorization (studentID, token) VALUES (?, ?)",
                        id, token);
            }
        } catch (SQLException | ConnectionUnavailableException e) {
            logger.error("Error inserting token for user " + id, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public Optional<Long> findUserIDByToken(String token) {
        Long userID = null;
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    "SELECT studentID, facultyID FROM UserAuthorization WHERE token = ?",
                    token);
            if (rs.next()) {
                userID = rs.getLong("studentID");
                if (rs.wasNull()) {
                    userID = rs.getLong("facultyID");
                }
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            logger.error("Error finding userID for token " + token, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(userID);
    }

    public Optional<String> findUserTypeByToken(String token) {
        String userType = null;
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            ResultSet rs = conn.execute(
                    "SELECT studentID, facultyID FROM UserAuthorization WHERE token = ?",
                    token);
            if (rs.next()) {
                if (rs.getObject("studentID") != null) {
                    userType = "student";
                } else if (rs.getObject("facultyID") != null) {
                    userType = "facultyAdmin";
                }
            }
            rs.getStatement().close();
        } catch (SQLException | ConnectionUnavailableException e) {
            logger.error("Error finding userType for token " + token, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }

        return Optional.ofNullable(userType);
    }

    public void deleteToken(String token) {
        DBConnection conn = null;

        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.update(
                    "DELETE FROM UserAuthorization WHERE token = ?",
                    token);
        } catch (SQLException | ConnectionUnavailableException e) {
            logger.error("Error deleting token " + token, e);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}