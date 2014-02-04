
import java.io.StringReader;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.IStemmer;

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
  public IDictionary dict;

  public LexParserObj() {
     lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    
    
  }



  public String wordStems(String original, String gram)  {
   //need to use IStemmer get the stem of words such as plurals
   //also use this method to get verb, adj, adv, or noun

   IStemmer stemmer = new SimpleStemmer();
   List<String> stems = new ArrayList();
   IIndexWord idxWord;
   POS pos = null;
   String possibility;

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
   else {

   }

   //System.out.println("In words stems with: " + original + " gram: " + gram);

  if (original.matches("^[ ]*$")) {
    return null;
  }
  else  {
    stems = stemmer.findStems(original, pos);
   if (stems.size() > 0) {
     for (Iterator<String> toSearch = stems.listIterator(); toSearch.hasNext();) {
        possibility = toSearch.next();
        idxWord = dict.getIndexWord(possibility, pos);
        if (idxWord != null)  {
          return possibility;
        }
     }
   }
  }
   
  
   //this gets executed if no stems were found, or stems found had no definition in dictionary
   return original;
        
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

    String wnhome = System.getenv("WNHOME");
    String path = wnhome + File.separator + "dict";
    

    try{
      URL url = new URL("file", null, path);
      dict = new Dictionary(url);
      dict.open();
    }catch (Exception e) {  
               e.printStackTrace();  
    }  


    String[] words = sentence.split(" ");


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

    //System.out.println(grammarHM);
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

    dict.close();
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
    String[] cleaned = new String[2];

    for(Iterator<TypedDependency> i = allDeps.iterator(); i.hasNext();) {

      dependency = i.next();
      //System.out.println(dependency);
      relation = dependency.reln().toString();
     
      gram = null;

      //can use or's on these two for further relations, first if statement is for ones needing .gov() second is for .dep()
      if (relation.contains("prep")) {
       
        word = dependency.dep().toString();
        gram = (String)grammar.get(word);

        word = cleanText(word, gram);
        if (word == null)  {

        }
        else  {
          cleaned = word.split(";");
          word = cleaned[0];
          gram = cleaned[1];
          //word = wordStems(word, gram);
          objects.put(word, gram);
        }
      
      }
     

      if (relation.contains("obj")) {
     
        word = dependency.dep().toString();
        gram = (String)grammar.get(word);
       
        word = cleanText(word, gram);
        if (word == null)  {

        }
        else  {
           cleaned = word.split(";");
          word = cleaned[0];
          gram = cleaned[1];
          if (word.equals("me")) {
            //don't want I during dynamic categorising, completely dependent on context
          }
          else  {
            objects.put(word, gram);
          }
        }
      }

      //not sure if subject is so important
      if (relation.contains("subj")) {
      
        word = dependency.dep().toString();
        gram = (String)grammar.get(word);

        word = cleanText(word, gram);
        if (word == null)  {

        }
        else  {
          cleaned = word.split(";");
          word = cleaned[0];
          gram = cleaned[1];
          if (word.equals("I")) {
            //don't want I during dynamic categorising, completely dependent on context
          }
          else  {
            objects.put(word, gram);
          }
        }
      }
      

      if (relation.contains("aux")) {
        word = dependency.gov().toString();
        gram = (String)grammar.get(word);
     
        word = cleanText(word, gram);
        if (word == null)  {

        }
        else  {
          cleaned = word.split(";");
          word = cleaned[0];
          gram = cleaned[1];
          //word = wordStems(word, gram);
          objects.put(word, gram);
        }
        
        
      }

       if (relation.equals("conj_and")) {
        String[] cleaned2 = new String[2];
        //intersted in this if the other side of the and was deemed an object to send, because then the other 
        //word is. So need ot find out if it's already in objects (subject to ordering!!! :s)
        String word1 = dependency.dep().toString();
        word = dependency.gov().toString();
        gram = (String)grammar.get(word);
          

          
      //we are only adding the new conj_and word to objects if it's and partner is in there

       word = cleanText(word, gram);
       word1 = cleanText(word1, gram);
        
        if (word == null)  {

        }
        else if (word1 == null)  {

        }
        else  {
          cleaned = word.split(";");
          cleaned2 = word1.split(";");
          word = cleaned[0];
          gram = cleaned[1];
          word1 = cleaned2[0];
          //word = wordStems(word, gram);
          //objects.put(word, gram);
           if (objects.containsKey(word) == true) {
              //if word was in there, add word1
               objects.put(word1, gram);
            }
            else if (objects.containsKey(word1) == true) {
              //if word1 was in there, add word
               objects.put(word, gram);
            }
        }
         
      }
    }//ends iterator
    return objects;
  }

  public String cleanText(String cleaning, String gram) {


    if (gram.charAt(0) == 'N' || gram.charAt(0) == 'J' || gram.charAt(0) == 'V' || gram.charAt(0) == 'R') {
      //System.out.println("Given: " + cleaning + " " + gram);

      cleaning = cleaning.replaceAll("'$", "");
      cleaning = cleaning.replaceAll("-[0-9]*$", "");
      //cleaning the word of punctuation and numbers that will cause faults in the dictionary
      cleaning = cleaning.replaceAll("[^a-zA-Z0-9]", "");
      gram = gram.replaceAll("-[0-9]*$", "");
      gram = gram.replaceAll("[^a-zA-Z0-9]", "");
      
     // System.out.println("Sending to stemmer: " + cleaning + " " + gram);
      cleaning = wordStems(cleaning, gram);

      if (cleaning == null) {
        return null;
      }
   // System.out.println("Returning: " + cleaning + " " + gram);
      return cleaning + ";" + gram;

    }
    else {
      return null;
    }

      
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
