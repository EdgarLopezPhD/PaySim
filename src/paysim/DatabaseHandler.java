package paysim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Connection;


public class DatabaseHandler {

    private Connection con = null;
    private String url = "";
    private String user = "";
    private String password = "";

    public DatabaseHandler(String urli, String useri, String passwordi) {
        this.url = urli;
        this.user = useri;
        this.password = passwordi;
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(Transaction trans) {
        try {
            String sql = "INSERT INTO paysim.paysimLog (logName, pType, pAmount, cliFrom,pOldBalanceFrom,pNewBalanceFrom,"
                    + "cliTo,pOldBalanceTo,pNewBalanceTo,isFraud,isFlaggedFraud,step) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, ParameterizedPaySim.simulatorName);
            st.setString(2, trans.getType() + "");
            st.setDouble(3, trans.getAmount());
            st.setString(4, trans.getClientOrigBefore().getName());
            st.setDouble(5, trans.getClientOrigBefore().getBalance());
            st.setDouble(6, trans.getClientOrigAfter().getBalance());
            st.setString(7, trans.getClientDestBefore().getName());
            st.setDouble(8, trans.getClientDestBefore().getBalance());
            st.setDouble(9, trans.getClientDestAfter().getBalance());
            st.setBoolean(10, trans.isFraud());
            st.setBoolean(11, trans.isFlaggedFraud());
            st.setLong(12, trans.getStep());
            st.executeUpdate();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }


    public void close() {
        try {
            this.con.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

class DBase {
    public DBase() {
    }

    public Connection connect(String db_connect_str, String db_userid,
                              String db_password) {
        Connection conn;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(db_connect_str, db_userid,
                    db_password);

        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }

        return conn;
    }

}
