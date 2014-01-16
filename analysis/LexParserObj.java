
import java.io.StringReader;
import java.util.*;

import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

class LexParserObj {

  /**
   * The main method demonstrates the easiest way to load a parser.
   * Simply call loadModel and specify the path, which can either be a
   * file or any resource in the classpath.  For example, this
   * demonstrates loading from the models jar file, which you need to
   * include in the classpath for ParserDemo to work.
   */

  public LexicalizedParser lp;

  public LexParserObj() {
     lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

  }

  public HashMap getTweetsFromDB() {
   // LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    

        //GetTweets from database
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String result = "heaven";
        HashMap grammarHM = new HashMap();
       

          try {
              // The newInstance() call is a work around for some
              // broken Java implementations

              Class.forName("com.mysql.jdbc.Driver").newInstance();
          }catch (Exception ex) {
              // handle the error
          }

          try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/thirdyearproject?user=root&password=");
          }catch (SQLException ex) {
          // handle any errors
          System.out.println("SQLException: " + ex.getMessage());
          System.out.println("SQLState: " + ex.getSQLState());
          System.out.println("VendorError: " + ex.getErrorCode());
          }

      try {
              stmt = conn.createStatement();
              rs = stmt.executeQuery("SELECT text FROM thirdyearproject.original_tweets where user_ID=72109014");

              //*This while loop will get all the results
              // while (rs.next())  {
              //   result = rs.getString("text");
              //   String[] resultArray = result.split(" ");
              //   System.out.println(result);
              //   demoAPI(lp, resultArray);
              // }

              //*This one get the top row
              if(rs.next()) {
                rs.next();
                result = rs.getString("text");
               // result = "I love receiving sweets. They are as cool as chocolate";
                result = "There are so many chocolates and sweets";
                System.out.println(result);
                grammarHM = demoAPI(result);
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

    return grammarHM;

  }


  /**
   * demoAPI demonstrates other ways of calling the parser with
   * already tokenized text, or in some cases, raw text that needs to
   * be tokenized as a single sentence.  Output is handled with a
   * TreePrint object.  Note that the options used when creating the
   * TreePrint can determine what results to print out.  Once again,
   * one can capture the output by passing a PrintWriter to
   * TreePrint.printTree.
   */
  public HashMap demoAPI(String sentence) {
    // This option shows parsing a list of correctly tokenized words
    //String[] sent = { "This", "is", "an", "easy", "sentence", "." };

    
    String[] words = sentence.split(" ");
     System.out.println("sentence: " + sentence);

    List<String> wordsAndTags = new ArrayList<String>(); 



    List<CoreLabel> rawWords = Sentence.toCoreLabelList(words);
    Tree parse = lp.apply(rawWords);
   // parse.pennPrint();
    //System.out.println(parse.toString());
    HashMap grammarHM = new HashMap();


    Tree parent;
    TreeGraphNode node;
    TreeGraph tweet = new TreeGraph(parse);
    String p;
    String n;
    Collection<TreeGraphNode> nodes = tweet.getNodes();
    

   
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
    //System.out.println(tdl);

    // Iterator ii = tdl.iterator();
    // while (ii.hasNext())  {
    //   System.out.println(ii.next().toString());
    // }

     for(Iterator<TreeGraphNode> i = nodes.iterator(); i.hasNext();)  {
      node = i.next();
      if (node.isLeaf()) {
        parent = node.parent();
        p = parent.toString();
        n = node.toString();
    
        grammarHM.put(n, p);
        
      }
     //where node is a leaf, it's parent is it's label
    }

    System.out.println(grammarHM);
    grammarHM = toObjects(tdl, grammarHM);

    Set set = grammarHM.entrySet();
    
    // Iterator iter = set.iterator();
    // while (iter.hasNext())  {
    //   Map.Entry g = (Map.Entry)iter.next();
    //   System.out.println("Objects:\nKey: " + g.getKey() + " Value: " + g.getValue());
    // }

    // System.out.println();
    // for(Iterator<TypedDependency> i = tdl.iterator(); i.hasNext();) {
    //    // System.out.println(i.next());
    // }

    

   return grammarHM;
  }

  

  public HashMap toObjects(List<TypedDependency> allDeps, HashMap grammar)  {


    /*
      removing apostrophes from I've etc....sort this out?
    */


    HashMap objects = new HashMap();
    TypedDependency dependency;
    String relation;
    String word;
    String gram = null;


    for(Iterator<TypedDependency> i = allDeps.iterator(); i.hasNext();) {

      dependency = i.next();
      //System.out.println(dependency);
      relation = dependency.reln().toString();
     
      gram = null;

      //can use or's on these two for further relations, first if statement is for ones needing .gov() second is for .dep()
      if (relation.contains("prep")) {
       
        word = dependency.dep().toString();
        word = word.replaceAll("'$", "");
        gram = (String)grammar.get(word);

        word = word.replaceAll("-[0-9]*$", "");
        word = word.replaceAll("[^a-zA-Z0-9]", ""); 
        gram = gram.replaceAll("-[0-9]*$", "");
        gram = gram.replaceAll("[^a-zA-Z0-9]", "");
          
          objects.put(word, gram);
      }
     

      if (relation.contains("obj")) {
     
        word = dependency.dep().toString();
        word = word.replaceAll("'$", "");
        gram = (String)grammar.get(word);

        gram = gram.replaceAll("-[0-9]*$", "");
        gram = gram.replaceAll("[^a-zA-Z0-9]", "");
        word = word.replaceAll("-[0-9]*$", "");
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        objects.put(word, gram);
        if (word.equals("me")) {
          //don't want I during dynamic categorising, completely dependent on context
        }
        else  {
          objects.put(word, gram);
        }
      }

      //not sure if subject is so important
      if (relation.contains("subj")) {
      
        word = dependency.dep().toString();

        word = word.replaceAll("'$", "");
        gram = (String)grammar.get(word);

        gram = gram.replaceAll("-[0-9]*$", "");
        gram = gram.replaceAll("[^a-zA-Z0-9]", "");
        word = word.replaceAll("-[0-9]*$", "");
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        if (word.equals("I")) {
          //don't want I during dynamic categorising, completely dependent on context
        }
        else  {
          objects.put(word, gram);
        }
        
      }

      if (relation.contains("aux")) {

        word = dependency.gov().toString();
        word = word.replaceAll("'$", "");
        gram = (String)grammar.get(word);
        gram = gram.replaceAll("-[0-9]*$", "");
        gram = gram.replaceAll("[^a-zA-Z0-9]", "");
        word = word.replaceAll("-[0-9]*$", "");
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        objects.put(word, gram);
      }

       if (relation.equals("conj_and")) {
        //intersted in this if the other side of the and was deemed an object to send, because then the other 
        //word is. So need ot find out if it's already in objects (subject to ordering!!! :s)
        String word1 = dependency.dep().toString();
        word = dependency.gov().toString();
        word = word.replaceAll("'$", "");
        word1 = word1.replaceAll("'$", "");
        gram = (String)grammar.get(word);
        
        gram = gram.replaceAll("-[0-9]*$", "");
        gram = gram.replaceAll("[^a-zA-Z0-9]", "");

        word = word.replaceAll("-[0-9]*$", "");
        word = word.replaceAll("[^a-zA-Z0-9]", "");

        word1 = word1.replaceAll("-[0-9]*$", "");
        word1 = word1.replaceAll("[^a-zA-Z0-9]", "");
        //we are only adding the new conj_and word to objects if it's and partner is in there
        if (objects.containsKey(word) == true) {
          //if word was in there, add word1
           objects.put(word1, gram);
        }
        else if (objects.containsKey(word1) == true) {
          //if word1 was in there, add word
           objects.put(word, gram);
        }
       
      }

    }//ends iterator


    return objects;
  }

  public String cleanText(String cleaning) {

      cleaning = cleaning.replaceAll("-[0-9]*$", "");
      //cleaning the word of punctuation and numbers that will cause faults in the dictionary
      cleaning = cleaning.replaceAll("[^a-zA-Z0-9]", "");
      return cleaning;
  }

 


}


// CC Coordinating conjunction
// CD Cardinal number
// DT Determiner
// EX Existential there
// FW Foreign word
// IN Preposition or subordinating conjunction
// JJ Adjective
// JJR Adjective, comparative
// JJS Adjective, superlative
// LS List item marker
// MD Modal
// NN Noun, singular or mass
// NNS Noun, plural
// NNP Proper noun, singular
// NNPS Proper noun, plural
// PDT Predeterminer
// POS Possessive ending
// PRP Personal pronoun
// PRP$ Possessive pronoun
// RB Adverb
// RBR Adverb, comparative
// RBS Adverb, superlative
// RP Particle
// SYM Symbol
// TO to
// UH Interjection
// VB Verb, base form
// VBD Verb, past tense
// VBG Verb, gerund or present participle
// VBN Verb, past participle
// VBP Verb, non­3rd person singular present
// VBZ Verb, 3rd person singular present
// WDT Wh­determiner
// WP Wh­pronoun
// WP$ Possessive wh­pronoun
// WRB Wh­adverb

/*
Ideas:

If the subject of the tweet is "I", only interested in object

Sentiment analyse subtrees and combine. Compare to sentiment anlaysis on whole sentence

If can't determing the noun e.g viallis, use the adjective to get an idea. e.g half-eaten implies food noun




*/
