
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.security.*;
import javax.servlet.http.Part;

@ManagedBean
@RequestScoped

public class MyBean {

    private static String fileName = "";
    private static Boolean sortByNameASC = true;
    private static Boolean sortBySurameASC = true;
    private static Boolean sortByLoginASC = true;

    private static ArrayList<user> listForXml = new ArrayList<user>();
    private static ArrayList<user> listForDisplay = new ArrayList<user>();
    private Part file;

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String upload() throws IOException, ClassNotFoundException, SQLException {
        try {
            listForXml = new ArrayList<user>();
            listForDisplay = new ArrayList<user>();

            file.write("C:\\TextToXML\\" + getFilename(file));
            readFromDiskFileXml();
            downloadingDataFromDatabase();
        } catch (Exception e) {
            return "index";
        }
        return "printDatabase";
    }

    public String readFromDiskFileXml() {

        FileReader fr = null;
        String linia = "";

        try {
            fr = new FileReader("C:\\TextToXML\\" + fileName);
        } catch (FileNotFoundException e) {
            System.out.println("BŁĄD PRZY OTWIERANIU PLIKU!");
            System.exit(1);
        }
        fileName = "";

        BufferedReader bfr = new BufferedReader(fr);
        user userr = new user();
        try {
            while ((linia = bfr.readLine()) != null) {
                if (linia.indexOf("<name>") != -1) {
                    userr.setName(linia.substring(linia.indexOf("<name>") + 6, linia.indexOf("</name>")));
                } else if (linia.indexOf("<surname>") != -1) {
                    userr.setSurname(linia.substring(linia.indexOf("<surname>") + 9, linia.indexOf("</surname>")));
                } else if (linia.indexOf("<login>") != -1) {
                    userr.setLogin(linia.substring(linia.indexOf("<login>") + 7, linia.indexOf("</login>")));
                    listForXml.add(userr);
                    userr = new user();
                }
            }
            insertDataToDatabase();
            fr = null;
        } catch (IOException e) {
            System.out.println("BŁĄD ODCZYTU Z PLIKU!");
            System.exit(2);
        }
        return "";
    }

    public static void insertDataToDatabase() {
        Connection myConn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test082016", "root", "");

            Statement stm = myConn.createStatement();
            for (user a : listForXml) {
                stm.executeUpdate("insert into users values ('" + a.getName() + "','" + a.getSurname() + "','" + a.getLogin() + "')");
            }
            myConn.close();
        } catch (Exception exc) {
        }
    }

    private static String getFilename(Part part) {

        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                fileName = filename;
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }

    public void downloadingDataFromDatabase() throws ClassNotFoundException, SQLException {
        listForDisplay = new ArrayList<user>();
        if (listForDisplay.isEmpty()) {
            Connection myConn = null;
            Statement myStmt = null;
            ResultSet myRs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test082016", "root", "");
                myStmt = myConn.createStatement();
                myRs = myStmt.executeQuery("select * from users");
                while (myRs.next()) {
                    listForDisplay.add(new user(myRs.getString("name"), myRs.getString("surname") + "_" + getMD5(myRs.getString("name")), myRs.getString("login")));
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            } finally {
                if (myRs != null) {
                    myRs.close();
                }
                if (myStmt != null) {
                    myStmt.close();
                }
                if (myConn != null) {
                    myConn.close();
                }
            }
        }
    }

    public void sorting(String sortingValue, String sortDirection) throws ClassNotFoundException, SQLException {
            listForDisplay = new ArrayList<user>();
            if (listForDisplay.isEmpty()) {
                Connection myConn = null;
                Statement myStmt = null;
                ResultSet myRs = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test082016", "root", "");
                    myStmt = myConn.createStatement();
                    myRs = myStmt.executeQuery("select * from users ORDER BY " + sortingValue + " " + sortDirection);
                    while (myRs.next()) {
                        listForDisplay.add(new user(myRs.getString("name"), myRs.getString("surname") + "_" + getMD5(myRs.getString("name")), myRs.getString("login")));
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                } finally {
                    if (myRs != null) {
                        myRs.close();
                    }
                    if (myStmt != null) {
                        myStmt.close();
                    }
                    if (myConn != null) {
                        myConn.close();
                    }
                }
            }
    }

    public void sortingName(String sortingValue) throws ClassNotFoundException, SQLException {
        if (sortByNameASC) {
            sorting(sortingValue,"DESC");
            sortByNameASC = false;
        } else {
            sorting(sortingValue,"ASC");
            sortByNameASC = true;
        }
    }
        public void sortingSurname(String sortingValue) throws ClassNotFoundException, SQLException {
        if (sortBySurameASC) {
            sorting(sortingValue,"DESC");
            sortBySurameASC = false;
        } else {
            sorting(sortingValue,"ASC");
            sortBySurameASC = true;
        }
    }
            public void sortingLogin(String sortingValue) throws ClassNotFoundException, SQLException {
        if (sortByLoginASC) {
            sorting(sortingValue,"DESC");
            sortByLoginASC = false;
        } else {
            sorting(sortingValue,"ASC");
            sortByLoginASC = true;
        }
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String kupa) {
        this.fileName = kupa;
    }

    public Part getFile() {
        return file;
    }

    public static ArrayList<user> getListForDisplay() {
        return listForDisplay;
    }

    public static void setListForDisplay(ArrayList<user> lista2) {
        MyBean.listForDisplay = lista2;
    }

    public static ArrayList<user> getListForXml() {
        return listForXml;
    }

    public static void setListForXml(ArrayList<user> lista1) {
        MyBean.listForXml = lista1;
    }
}
