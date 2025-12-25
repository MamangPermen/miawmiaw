package model;
import java.sql.*;

public class Database
{
    private Connection connection;

    // constructor
    public Database() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/db_tmd", // sesuaikan nama db
                "root",
                "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk select
    public ResultSet executeQuery(String sql) {
        try {
            // Bikin Statement BARU setiap kali query dipanggil
            Statement stmt = connection.createStatement(); 
            return stmt.executeQuery(sql);
            // Nanti Statement ini bakal ditutup sama Model/Presenter lewat rs.getStatement().close()
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk insert, update, delete
    public int insertUpdateDeleteQuery(String sql) {
        try {
            // Bikin Statement lokal, pake, terus langsung tutup
            Statement stmt = connection.createStatement();
            int result = stmt.executeUpdate(sql);
            stmt.close(); // Langsung tutup karena gak ngehasilin ResultSet
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //  buat tutup koneksi kalau game ditutup
    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}