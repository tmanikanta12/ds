import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class Client {
    public static void main(String[] args) {
        Socket clisock;
        DataInputStream input;
        PrintStream ps;
        String url, ip, s, u, p, str;
        int pno = 5123;
        Connection con;
        Statement smt;
        ResultSet rs;
        boolean status = true;

        try {
            ip = s = p = u = "";
            System.out.println("Enter name to resolve:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            url = br.readLine();

            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Database Connection
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DNS", "root", "");

            smt = con.createStatement();

            while (status) {
                s = "";
                System.out.println("IN CLIENT, URL IS: " + url);
                
                StringTokenizer st = new StringTokenizer(url, ".");

                if (st.countTokens() == 1) {
                    status = false;
                }

                while (st.countTokens() > 1) {
                    s = s + st.nextToken() + ".";
                }

                // Remove trailing dot
                if (!s.isEmpty()) {
                    s = s.substring(0, s.length() - 1).trim();
                }

                u = st.nextToken();
                System.out.println("u = " + u);

                // Query database for IP and port
                rs = smt.executeQuery("SELECT port, ipadd FROM client WHERE name='" + u + "'");

                if (rs.next()) {
                    p = rs.getString(1);
                    
                    // ✅ Validate if the retrieved port is a number before parsing
                    if (p.matches("\\d+")) {
                        pno = Integer.parseInt(p);
                    } else {
                        System.err.println("Error: Invalid port number retrieved from database: " + p);
                        return;
                    }

                    str = rs.getString(2);
                    url = s;
                    ip = str + "." + ip;
                } else {
                    System.out.println("pno = " + pno);

                    // Connect to DNS server
                    clisock = new Socket("127.0.0.1", pno);
                    input = new DataInputStream(clisock.getInputStream());
                    ps = new PrintStream(clisock.getOutputStream());

                    ps.println(url);

                    // ✅ Read port response and validate before parsing
                    p = input.readLine();
                    if (p != null && p.matches("\\d+")) {
                        pno = Integer.parseInt(p);
                    } else {
                        System.err.println("Error: Received invalid port response from server: " + p);
                        clisock.close();
                        return;
                    }

                    // Read other data from server
                    str = input.readLine();
                    url = input.readLine();
                    ip = str + "." + ip;

                    // Store resolved name in database
                    smt.executeUpdate("INSERT INTO client VALUES('" + u + "','" + str + "','" + p + "')");

                    // Close connection
                    clisock.close();
                }

                System.out.println("ip = " + ip);
            }

            // Remove trailing dot from final IP
            if (!ip.isEmpty()) {
                ip = ip.substring(0, ip.length() - 1).trim();
            }

            System.out.println("Resolved IP Address: " + ip);

            // Close DB connection
            con.close();
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Network Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

