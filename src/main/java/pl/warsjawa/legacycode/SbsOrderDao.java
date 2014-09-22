package pl.warsjawa.legacycode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pl.warsjawa.legacycode.infra.DB;

public class SbsOrderDao {

    private static final SbsOrderDao instance = new SbsOrderDao();

    private SbsOrderDao() {

        try {

            Class.forName("org.h2.Driver");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static SbsOrderDao getInstance() {
        return instance;
    }

    public Order findOrderById(String orderId) {

        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from ORDERS where ID = ?");
            stmt.setString(1, orderId);
            
            System.out.println(stmt);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String id = rs.getString("id");
                String status = rs.getString("status");
                int totalPrice = rs.getInt("total");
                String email = rs.getString("email");
                String fullName = rs.getString("full_name");

                Order order = new Order();
                order.setId(id);
                order.setStatus(status);
                order.setTotalPrice(totalPrice);
                order.setCustomerData(new CustomerData(email, fullName));

                return order;

            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }

        }
    }

    private Connection getConnection() throws SQLException {
        return DB.getConnection();
    }

    public void save(Order order) {

        Connection conn = null;
        try {

            if (findOrderById(order.getId()) == null) {

                String insertSql = "insert into ORDERS(id, status, total, email, full_name) values (?, ?, ?, ?, ?)";
                conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertSql);
                stmt.setString(1, order.getId());
                stmt.setString(2, order.getStatus());
                stmt.setInt(3, order.getTotalPrice());
                stmt.setString(4, order.getCustomerData().getEmail());
                stmt.setString(5, order.getCustomerData().getFullName());
                
                System.out.println(insertSql);

                stmt.execute();
                
            } else {

                String updateSql = "update ORDERS set status = ?, total = ?, email = ?, full_name = ? where id = ?";
                conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(updateSql);
                stmt.setString(5, order.getId());
                stmt.setString(1, order.getStatus());
                stmt.setInt(2, order.getTotalPrice());
                stmt.setString(3, order.getCustomerData().getEmail());
                stmt.setString(4, order.getCustomerData().getFullName());

                System.out.println(updateSql);
                
                stmt.execute();

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }

        }

    }

}
