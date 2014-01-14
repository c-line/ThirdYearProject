import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class keywordReduction	{
	
	public static void main(String[] args)	{

		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String result = "heaven";
        HashMap grammarHM = new HashMap();
       

          try {
              // The newInstance() call is a work around for some
              // broken Java implementations

              Class.forName("com.mysql.jdbc.Driver").newInstance();
          }
          catch (Exception ex) {
              // handle the error
          }

          try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=");
          }
          catch (SQLException ex) {
          // handle any errors
	          System.out.println("SQLException: " + ex.getMessage());
	          System.out.println("SQLState: " + ex.getSQLState());
	          System.out.println("VendorError: " + ex.getErrorCode());
          }

      try {
              stmt = conn.createStatement();
              rs = stmt.executeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WEHRE TABLE_NAME='keywords');");
               rs = stmt.executeQuery("CREATE TABLE keywords(definition varchar(), keyword varchar(160), confidence(int));");

             

              //don't forget to close any statements
              //and close the connection
              stmt.close();
              conn.close();
      }
      catch (SQLException ex) {
              System.out.println("SQLException: " + ex.getMessage());
              System.out.println("SQLState: " + ex.getSQLState());
              System.out.println("VendorError: " + ex.getErrorCode());
      }
      finally {
          // it is a good idea to release
          // resources in a finally{} block
          // in reverse-order of their creation
          // if they are no-longer needed
          if (rs != null) {
                    try {
                        rs.close();
                    }catch (SQLException sqlEx) {
                      // ignore
                    } 
                    rs = null;
          }
          if (stmt != null) {
                    try {
                        stmt.close();
                    }catch (SQLException sqlEx) { 
                      //ignore
                    } 
                    stmt = null;
          }
      }

	}
}