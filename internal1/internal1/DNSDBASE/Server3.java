import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class Server3 {
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
            // Initialize strings properly
            s = u = "";

            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Database Connection
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DNS", "root", "");

            // Create SQL Statement
            smt = con.createStatement();

            // Start ServerSocket on port 5125
            sock = new ServerSocket(5125);
            System.out.println("DNS Server3 is running on port 5125...");

            while (true) {
                client = sock.accept();
                input = new DataInputStream(client.getInputStream());
                ps = new PrintStream(client.getOutputStream());

                // Read URL from client
                url = input.readLine();
                System.out.println("IN SERVER3, URL IS: " + url);

                // Tokenize the URL
                StringTokenizer st = new StringTokenizer(url, ".");
                s = ""; // Reset for each request

                while (st.countTokens() > 1) {
                    s = s + st.nextToken() + ".";
                }

                // Remove trailing dot
                if (!s.isEmpty()) {
                    s = s.substring(0, s.length() - 1).trim();
                }

                u = st.nextToken();

                // Query the database for IP resolution
                rs = smt.executeQuery("SELECT port, ipadd FROM google WHERE name='" + u + "'");

                if (rs.next()) {
                    // Send port and IP to client
                    ps.println(rs.getString(1));
                    ps.println(rs.getString(2));
                    ps.println(s);
                } else {
                    ps.println("Illegal address. Please check the spelling again.");
                }

                // Close the client connection after responding
                client.close();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

