import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class Server1 {
    public static void main(String[] args) {
        ServerSocket sock;
        Socket client;
        DataInputStream input;
        PrintStream ps;
        String url, u, s;
        Connection con;
        Statement smt;
        ResultSet rs;

        try {
            // Initialize variables
            s = u = "";

            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Database Connection
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DNS", "root", "");

            // Create Statement
            smt = con.createStatement();

            // Create ServerSocket on port 5123
            sock = new ServerSocket(5123);
            System.out.println("DNS Server is running on port 5123...");

            while (true) {
                client = sock.accept();
                input = new DataInputStream(client.getInputStream());
                ps = new PrintStream(client.getOutputStream());

                // Read URL from client
                url = input.readLine();
                System.out.println("IN SERVER2, URL IS: " + url);

                // Tokenize the URL
                StringTokenizer st = new StringTokenizer(url, ".");
                s = "";  // Reset s for each new request

                while (st.countTokens() > 1) {
                    s = s + st.nextToken() + ".";
                }

                // Remove trailing dot
                if (!s.isEmpty()) {
                    s = s.substring(0, s.length() - 1).trim();
                }

                u = st.nextToken();

                // Query the database for IP resolution
                rs = smt.executeQuery("SELECT port, ipadd FROM client WHERE name='" + u + "'");

                if (rs.next()) {
                    // Send port and IP to client
                    ps.println(rs.getString(1));
                    ps.println(rs.getString(2));
                    ps.println(s);
                } else {
                    // Send error message
                    ps.println("Illegal address. Please check the spelling again.");
                }

                // Close client connection
                client.close();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

