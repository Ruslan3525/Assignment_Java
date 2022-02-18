package project.friendmeapp.springboot.sockets.console.posts;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/friendmedb";
    private static final String USERNAME = "elnur";
    private static final String PASSWORD = "mamekov_a1";

    @Getter @Setter
    private static Connection connection;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // connection to the database
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
