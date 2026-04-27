package com.uob.portal.torque3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import com.uob.portal.torque3.dto.GtpUserRow;

/**
 * CRUD for {@code gtp_user} using Torque 3.x {@link BasePeer} for reads (Village {@link Record}) and
 * parameterized JDBC writes on a Torque-managed connection.
 */
public final class GtpUserLegacyCrudService {

    private static final String DATABASE_NAME = "uob_portal";

    private GtpUserLegacyCrudService() {}

    public static GtpUserRow insert(
            String loginName, String passwordValue, String firstName, String lastName, String email)
            throws TorqueException, SQLException, DataSetException {
        return insertWithTurbineUserId(null, loginName, passwordValue, firstName, lastName, email);
    }

    public static GtpUserRow insertWithTurbineUserId(
            Integer turbineUserId,
            String loginName,
            String passwordValue,
            String firstName,
            String lastName,
            String email)
            throws TorqueException, SQLException, DataSetException {
        String sql =
                "INSERT INTO gtp_user (turbine_user_id, login_name, password_value, first_name, last_name, email) VALUES (?,?,?,?,?,?)";
        Connection con = Torque.getConnection(DATABASE_NAME);
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (turbineUserId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, turbineUserId.intValue());
            }
            ps.setString(2, loginName);
            ps.setString(3, passwordValue);
            ps.setString(4, firstName);
            ps.setString(5, lastName);
            ps.setString(6, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Insert succeeded but no generated key returned");
                }
                int id = keys.getInt(1);
                return findByUserId(id);
            }
        } finally {
            Torque.closeConnection(con);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<GtpUserRow> findAll() throws TorqueException, DataSetException {
        String sql =
                "SELECT user_id, turbine_user_id, login_name, password_value, first_name, last_name, email FROM gtp_user";
        List<Record> rows = BasePeer.executeQuery(sql, DATABASE_NAME);
        List<GtpUserRow> out = new ArrayList<>(rows.size());
        for (Record row : rows) {
            out.add(mapRecord(row));
        }
        return out;
    }

    public static GtpUserRow findByUserId(int userId) throws TorqueException, DataSetException {
        String sql =
                "SELECT user_id, turbine_user_id, login_name, password_value, first_name, last_name, email FROM gtp_user WHERE user_id = "
                        + userId;
        @SuppressWarnings("unchecked")
        List<Record> rows = BasePeer.executeQuery(sql, DATABASE_NAME);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRecord(rows.get(0));
    }

    public static GtpUserRow findByLoginName(String loginName) throws TorqueException, SQLException, DataSetException {
        String sql =
                "SELECT user_id, turbine_user_id, login_name, password_value, first_name, last_name, email FROM gtp_user WHERE login_name = ?";
        Connection con = Torque.getConnection(DATABASE_NAME);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loginName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new GtpUserRow(
                        rs.getObject("user_id", Integer.class),
                        rs.getObject("turbine_user_id", Integer.class),
                        rs.getString("login_name"),
                        rs.getString("password_value"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"));
            }
        } finally {
            Torque.closeConnection(con);
        }
    }

    public static boolean updateEmail(int userId, String email) throws TorqueException, SQLException {
        String sql = "UPDATE gtp_user SET email = ? WHERE user_id = ?";
        Connection con = Torque.getConnection(DATABASE_NAME);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } finally {
            Torque.closeConnection(con);
        }
    }

    public static GtpUserRow findByTurbineUserId(int turbineUserId)
            throws TorqueException, SQLException, DataSetException {
        String sql =
                "SELECT user_id, turbine_user_id, login_name, password_value, first_name, last_name, email FROM gtp_user WHERE turbine_user_id = ?";
        Connection con = Torque.getConnection(DATABASE_NAME);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turbineUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new GtpUserRow(
                        rs.getObject("user_id", Integer.class),
                        rs.getObject("turbine_user_id", Integer.class),
                        rs.getString("login_name"),
                        rs.getString("password_value"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"));
            }
        } finally {
            Torque.closeConnection(con);
        }
    }

    public static boolean deleteByUserId(int userId) throws TorqueException, SQLException {
        String sql = "DELETE FROM gtp_user WHERE user_id = ?";
        Connection con = Torque.getConnection(DATABASE_NAME);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } finally {
            Torque.closeConnection(con);
        }
    }

    private static GtpUserRow mapRecord(Record row) throws DataSetException {
        return new GtpUserRow(
                row.getValue("user_id").asIntegerObj(),
                row.getValue("turbine_user_id").asIntegerObj(),
                row.getValue("login_name").asString(),
                row.getValue("password_value").asString(),
                row.getValue("first_name").asString(),
                row.getValue("last_name").asString(),
                row.getValue("email").asString());
    }
}
