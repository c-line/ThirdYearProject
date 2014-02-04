import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.SynsetID;
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

      stmt = conn.createStatement();
      res = stmt.executeUpdate("ALTER TABLE original_tweets ADD column keywords varchar(200)");
      //ALTER TABLE original_tweets DROP keywords
      
      
     

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

     //set up the lexparser
      LexParserObj lpo = new LexParserObj();


      /*
      get lpo to send objects
      put them in database apporpriately
      do tweet at
      */

      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select text, tweet_ID from thirdyearproject.original_tweets;");
      
      HashMap keywordsHM = new HashMap();
      String result;
      String currentWord;
      String gram;
      String analysis;
      Statement stmt2 = conn.createStatement();
      Statement stmt3 = conn.createStatement();
      int rs2;
      int rs3;
      //SingleTweetAnalysis analyser = new SingleTweetAnalysis();
      ArrayList<HashMap> allHMDefinitions;
      HashMap definitionsHM = new HashMap();

      int counter = 0;

    
       //*This while loop will get all the results
        while (rs.next())  {
          allHMDefinitions = new ArrayList();
          if (counter < 150) {
          // counter--;
          // if (counter == 0) {
          //   break;
          // }
          counter++;
          System.out.println(counter);

         result = rs.getString("text");
         analysis = result.replaceAll("(#)[^ ]*|(http:)[^ ]*", "");
         //idiot proofing for empty tweets. (Trim just incase some white space in empty tweet)
         while (analysis.matches("^[ ]*$")) {
            rs.next();
            result = rs.getString("text");
            analysis = result.replaceAll("(#)[^ ]*|(http:)[^ ]*", "");
         }
          //  System.out.println(analysis);

            keywordsHM = lpo.demoAPI(analysis);
            Set set = keywordsHM.entrySet();
            
           Iterator iter = set.iterator();
           String keywordsString = "";
           while (iter.hasNext())  {

             // word = iter.next();
              Map.Entry g = (Map.Entry)iter.next();
              currentWord = g.getKey().toString();
              gram = g.getValue().toString();

              //System.out.println(currentWord);
              definitionsHM = getDefinitions(dict, conn, currentWord, gram);
             // System.out.println("It's HM is: " + definitionsHM);
              if (definitionsHM!=null)  {
                //only add words that were found in wordnet, so not null
                allHMDefinitions.add(definitionsHM); 
              }
              
              keywordsString = keywordsString + currentWord + ";";//when extracting from original tweets can then split on the ;
              /*
                for all the possible keyword defintions, assess which is likely the 
                definition in this tweets context
             */
           }


            //System.out.println("Sending to contextChooser " + allHMDefinitions);
            //keywordsHM is null if coudln't find a category
          if (allHMDefinitions.size() > 0)  {
             keywordsHM = contextChooser(allHMDefinitions);
          }
         
          // System.out.println(keywordsHM);

           if (keywordsHM != null)  {
              set = keywordsHM.entrySet();
              
              Iterator itr = set.iterator();
              while (itr.hasNext())  {
                Map.Entry gg = (Map.Entry)itr.next();
                String synID = gg.getKey().toString();
                rs3 = stmt3.executeUpdate("UPDATE keywords SET confidence = confidence + 1 WHERE synsetID = \"" + synID + "\";");
              }
          
           }
           
          long tweetID = rs.getLong("tweet_ID");

          rs2 = stmt2.executeUpdate("UPDATE original_tweets SET keywords = \"" + keywordsString + "\" WHERE tweet_ID=" + Long.toString(tweetID) + ";");
         }//if counter <100

        }

        
        stmt.close();
        stmt2.close();
        stmt3.close();
        rs.close();
        //rs2 and 3 were ints not ResultSet so don't need closing 

        /*
        From here, need to cut off certain definitions
        For now, just cutting definitions which never got used (confidence = 0)
        */

        stmt = conn.createStatement();
        rs2 = stmt.executeUpdate("DELETE FROM keywords WHERE confidence = 0;");


    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }


  public static HashMap getDefinitions(IDictionary dict, Connection conn, String word, String gram)  {

    HashMap definitionsHM = new HashMap();

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
        return null;
      }
    }catch(Exception e)  {
      e.printStackTrace();
    }
    
    try{
       
       //stem words here!!!!
        IIndexWord idxWord = dict.getIndexWord(word, pos);
        if (idxWord == null)  {
          return null;
        }

       // System.out.println("Getting definitions for: " + word);
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
            
            definitionsHM.put(synsetID.toString(), gram);
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
    return definitionsHM;


  }


  public static HashMap contextChooser(ArrayList<HashMap> allHMDefinitions)  {

    //System.out.println("There are: " + Integer.toString(allHMDefinitions.size()) + " keyword Hashmaps");



    HashMap solution = new HashMap();
    solution = recursion(solution, allHMDefinitions, 0);  //initially passing solution is fine because empty array list adn that won't change, avoids declaring new HashMap 
    if (solution.isEmpty()==true) {
      //System.out.println("Fudge");
    }
    else  {
    //  System.out.println("HELL YES!!!!\n\n\n\n");
      return solution;
      }
    System.gc(); 
    return null;  

  }//ends contextChooser

  public static HashMap recursion(HashMap args, ArrayList<HashMap> list, int i) {


    /*
    this may not need to be done for all tweets
    need to consider if words like "this, it, all" etc make it trhough. Won't be part of classification

    */

    // System.out.println(list);
    // System.out.println(Integer.toString(i));
    HashMap currentHM = list.get(i);
    //System.out.println(currentHM);
    Set set = currentHM.entrySet();
    Iterator iter = set.iterator();

    String synsetIDString = null;
    ISynsetID synsetID = null;
    String gram = null;

    HashMap solution = new HashMap();

    Boolean found = false;

     //iterate through words and grams in currentHM
     while (iter.hasNext()) {


        Map.Entry mp = (Map.Entry)iter.next();
        synsetIDString = mp.getKey().toString(); //convert to datastructure SynsetID
        gram = mp.getValue().toString();

        args.put(synsetIDString, gram); //add something to args before sending it off     

        if (i == list.size()-1) {
            // System.out.println("\n\n\n");
            // System.out.println("args: " + args);
            //this step only important once have all words
            // synsetIDString = parseSynsetID(synsetIDString);
            SingleTweetAnalysis sta = new SingleTweetAnalysis();
            try {
              found = sta.start(args);
            }catch(Exception e) {
              e.printStackTrace();
            }
            
            if (found == true)  {
              return args;
            }

             
        }

        else  {
        
          i = i+1;  //indexing the next HashMap
          solution = recursion(args, list, i);

          i = i-1;//return i to the value for this level

          if (solution.isEmpty() == false)  { 
            return solution;  //pass correct hashmap back up
            //allows return through all loops
          }
         
        }
      args.remove(mp.getKey());//remove that word from args ready to put the new def in
     }//ends iterator for current HashMap
    return solution;
      
  }

}//ends class

/*






love the space









*/