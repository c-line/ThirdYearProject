import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.*;
import java.lang.NullPointerException;

public class KeywordReduction	{

	public static void main(String[] args)	{

		    Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
       
        //KeywordReduction kr = new KeywordReduction();

          try {
              // The newInstance() call is a work around for some
              // broken Java implementations
              Class.forName("com.mysql.jdbc.Driver").newInstance();
          }
          catch (Exception ex) {
              // handle the error
          }

          try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/thirdyearproject?user=root&password=");
          }
          catch (SQLException ex) {
          // handle any errors
	          System.out.println("SQLException: " + ex.getMessage());
	          System.out.println("SQLState: " + ex.getSQLState());
	          System.out.println("VendorError: " + ex.getErrorCode());
          }

          int result;
      try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='keywords' AND TABLE_SCHEMA='thirdyearproject';");
           // rs = stmt.executeQuery("CREATE TABLE keywords(definition varchar(), keyword varchar(160), confidence(int));");
            if(rs.next()) {
                result = rs.getInt("COUNT(*)");
                if (result == 0)  {
                  System.out.println("in count == 0");
                  //there is no keywords table
                  //need to build one and run the keyword extraction on all tweets
                  buildKeywords(conn);
                }
                else {
                  System.out.println("in count != 0");
                  //keywords table exists
                  //search for new tweets that maybe don't have any. 
                  updateKeywords(conn);
                }               
            }
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

  public static void updateKeywords(Connection conn)  {

    System.out.println("updateKeywords");
  }

  public static void buildKeywords(Connection conn) {

    System.out.println("buildKeywords");
    try {
      Statement stmt = conn.createStatement();

      int res = stmt.executeUpdate("CREATE TABLE keywords ( definition varchar(50), synsetID varchar(20), keyword varchar(50), grammar varchar(10), confidence int(3), primary key(definition) );");
      //System.out.println(Integer.toString(res));
       //and close the connection

      stmt.close();

      /*
      building a table of keywords in the database:

      iterate through all tweets
      run the lexparserobj on it, 
      put the words in the database
          search if word already there
          if not then add. 
          for optimising, should add in alphabetical order to database, seraching then quicker
      also add the key word to a list that gets added as a separate column to original_tweets
      (saves doing the parsing again)
      */

      
      //set up the lexparser
      LexParserObj lpo = new LexParserObj();

      //set up the wordnet environment
      String wnhome = System.getenv("WNHOME");
     String path = wnhome + File.separator + "dict";
     IDictionary dict = null;

     try{
        URL url = new URL("file", null, path);
        dict = new Dictionary(url);
        dict.open();
      }
      catch(MalformedURLException e)  {
        System.out.println(e.toString());
        System.out.println("Caught malformed url exception");
    }
    catch(IOException ex) {
      System.out.println(ex.toString());
      System.out.println("Could not find dictionary");
    }
      /*
      get lpo to send objects
      put them in database apporpriately
      do tweet at
      */

      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select text from thirdyearproject.original_tweets;");
      
      HashMap keywordsHM = new HashMap();
      String result;
      String currentWord;
      String gram;
      Statement stmt2 = conn.createStatement();
      ResultSet rs2;

      int counter = 50;

       //*This while loop will get all the results
        while (rs.next())  {
          // counter--;
          // if (counter == 0) {
          //   break;
          // }

         result = rs.getString("text");
         result = result.replaceAll("(#)[^ ]*|(http:)[^ ]*", "");
         System.out.println("tweet" + result);
         //idiot proofing for empty tweets. (Trim just incase some white space in empty tweet)
         while (result.matches("^[ ]*$")) {
            rs.next();
            result = rs.getString("text");
            result = result.replaceAll("(#)[^ ]*|(http:)[^ ]*", "");
         }
         //if (result.contains("frothsof 4e: Trimming the Fat Part 4: Killing Off Revenants Once and for All http://t.co/gu2yAKEALr #dnd")) {
            keywordsHM = lpo.demoAPI(result);
            Set set = keywordsHM.entrySet();
            
           Iterator iter = set.iterator();
           while (iter.hasNext())  {
             // word = iter.next();
              Map.Entry g = (Map.Entry)iter.next();
              currentWord = g.getKey().toString();
              gram = g.getValue().toString();
              getDefinitions(dict, conn, currentWord, gram);
         // }
              /*
                for all the possible keyword defintions, assess which is likely the 
                definition in this tweets context


              */
           } 
        }
  

        // for ONE tweet      
        // if (rs.next())  {
        //     result = rs.getString("text");
        //     keywordsHM = lpo.demoAPI(result);
        //     Set set = keywordsHM.entrySet();
            
        //    Iterator iter = set.iterator();
        //    while (iter.hasNext())  {
        //      // word = iter.next();
        //       Map.Entry g = (Map.Entry)iter.next();
        //       currentWord = g.getKey().toString();
        //       gram = g.getValue().toString();
        //       getDefinitions(dict, conn, currentWord, gram);
              
        //    }
        //  }

        //getDefinitions(dict, conn, "sweet", "NN");

          System.out.println(keywordsHM);
       

        // System.out.println("\n\n" + keywordsHM);
        stmt.close();


    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }


  public static void getDefinitions(IDictionary dict, Connection conn, String word, String gram)  {

    POS pos = null;
    try {
      if (gram.charAt(0)=='N')  {
        pos = POS.NOUN;
      }
      else if (gram.charAt(0)=='J')  {
        pos = POS.ADJECTIVE;
      }
      else if (gram.charAt(0)=='R') {
        pos = POS.ADVERB;
      }
      else if (gram.charAt(0)=='V') {
        pos = POS.VERB;
      }
      else  {
        return;
      }
    }catch(Exception e)  {
      e.printStackTrace();
    }
    
    try{
       
        IIndexWord idxWord = dict.getIndexWord(word, pos);
        if (idxWord == null)  {
          return;
        }

        System.out.println("Getting definitions for: " + word);
        List<IWordID> definitions = idxWord.getWordIDs();
        IWordID wordID = null;
        String query = "";

        ISynsetID synsetID = null;
        Statement stmt;
        int rs;

        //adds all possible definitions of word to database. 
       for (Iterator<IWordID> iter = definitions.iterator(); iter.hasNext();)  {
            stmt = conn.createStatement();
            wordID = iter.next();
            synsetID = wordID.getSynsetID();
      
            query = "insert into keywords values(\"" + wordID.toString() + "\", \"" + synsetID.toString() + "\", \"" + word + "\", \"" + gram + "\", " + "0);";
            rs = stmt.executeUpdate(query);
            stmt.close();
       } 
       
    }catch(Exception e) {
     // e.printStackTrace();
    }

    /*
    after all keywords are in database need to assess context for each

    */
  


  }
}