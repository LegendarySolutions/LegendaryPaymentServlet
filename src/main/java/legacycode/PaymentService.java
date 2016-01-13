package legacycode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import legacycode.infra.DB;
import legacycode.payment.Payment;
import legacycode.transaction.Transaction;

public class PaymentService {

    public PaymentService() {

        Connection connection = null;
        
        try {
            
            connection = DB.getConnection();
            connection.getAutoCommit();
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }
    
    public List<Transaction> findTransactionsByPaymentId(String paymentId) {

        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("select * from TRANSACTION where PAYMENT_ID = ?");
            statement.setString(1, paymentId);
            
            System.out.println(statement);
            
            ResultSet rs = statement.executeQuery();

            List<Transaction> result = new ArrayList<>();

            while (rs.next()) {

                result.add(getTransactionFromResultSet(rs));
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }

    }

    private void close(Connection conn) {

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            // ignore
        }

    }

    private Transaction getTransactionFromResultSet(ResultSet rs) throws SQLException {

        Transaction transaction = new Transaction();
        transaction.setId(rs.getString("id"));
        transaction.setActive(rs.getBoolean("active"));
        transaction.setContactEmail(rs.getString("contact_email"));
        transaction.setContactPerson(rs.getString("contact_person"));
        transaction.setPaymentId(rs.getString("payment_id"));
        return transaction;
    }

    public Transaction findTransactionById(String orderId) {

        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("select * from TRANSACTION where ID = ?");
            statement.setString(1, orderId);
            
            System.out.println(statement);
            
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return getTransactionFromResultSet(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }

    }

    public void setInactiveTransaction(Transaction transaction) {

        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("update TRANSACTION set ACTIVE=? where ID = ?");
            statement.setBoolean(1, false);
            statement.setString(2, transaction.getId());
            
            System.out.println(statement);

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    public Payment findPaymentById(String paymentId) {
        
        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("select * from PAYMENT where ID = ?");
            statement.setString(1, paymentId);
            
            System.out.println(statement);
            
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return getPaymentFromResultSet(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    private Payment getPaymentFromResultSet(ResultSet rs) throws SQLException {
        
        Payment payment = new Payment();
        payment.setId(rs.getString("id"));
        payment.setState(rs.getString("status"));
        payment.setAmount(rs.getInt("amount"));
        
        return payment;
    }

    public void updatePayment(Payment payment) {
       
        Connection conn = null;

        try {

            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("update PAYMENT set STATUS=?, AMOUNT=? where ID = ?");
            statement.setString(1, payment.getState());
            statement.setInt(2, payment.getAmount());
            statement.setString(3, payment.getId());
            
            System.out.println(statement);
            
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    private Connection getConnection() throws SQLException {
        return DB.getConnection();
    }
}
