
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.*;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.*;


public class Analysis	{

	LexParserObj lp;
	IDictionary dict;

	public Analysis()	{
		lp = new LexParserObj();
		String wnhome = System.getenv("WNHOME");
		String path = wnhome + File.separator + "dict";
		

		try{
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
			dict.open();
		}catch (Exception e) {  
	             e.printStackTrace();  
	    }  
		
	}

	 public HashMap toSense(HashMap gibberish) {

	 	HashMap senseMap = new HashMap();
	    Set set = gibberish.entrySet();
	    String grammar = null;
	    String word = null;
	    char code;

	    Iterator iter = set.iterator();
	    while (iter.hasNext())  {
		    Map.Entry g = (Map.Entry)iter.next();
		   // word = g.getKey().toString();
		    code = g.getValue().toString().charAt(0);
		    //System.out.println("Key: " + g.getKey() + " Value: " + g.getValue());

		    if (code == 'J')	{
		      	grammar = "ADJECTIVE";
		    }
		    else if (code == 'N')	{
		      	grammar = "NOUN";
		    }
		    else if (code == 'V')	{
		      	grammar = "VERB";
		    }
		    else if (code == 'R')	{
		      	grammar = "ADVERB";	
		    }
		    
		    senseMap.put(g.getKey(), grammar);
		    grammar = null;
	   // System.out.println(g.getValue() + " " + grammar);


	 }
	 System.out.println("Sense map: " + senseMap);
	 return senseMap;
	}

	public IIndexWord wordStems(String original, String gram, Analysis analyser)	{
		//need to use IStemmer get the stem of words such as plurals
		//also use this method to get verb, adj, adv, or noun

		IStemmer stemmer = new SimpleStemmer();
		List<String> stems = new ArrayList();
		IIndexWord idxWord;
		POS pos = null;

		if (gram.equals("NOUN"))	{
			pos = POS.NOUN;
		}
		else if (gram.equals("ADJECTIVE"))	{
			pos = POS.ADJECTIVE;
		}
		else if (gram.equals("ADVERB"))	{
			pos = POS.ADVERB;
		}
		else if (gram.equals("VERB"))	{
			pos = POS.VERB;
		}
		//System.out.println("In words stems with: " + original + " gram: " + gram);

		
		stems = stemmer.findStems(original, pos);
		if (stems.size() > 0)	{
			for (Iterator<String> toSearch = stems.listIterator(); toSearch.hasNext();)	{
				idxWord = analyser.dict.getIndexWord(toSearch.next(), pos);
				if (idxWord!=null)	{
					return idxWord;
				}
			}
		}
	
		//this gets executed if no stems were found, or stems found had no definition in dictionary
		return analyser.dict.getIndexWord(original, pos);
				
	}
	
	public static void main(String[] args) throws Exception	{



		Analysis analyser = new Analysis();

		
		//String[] testArray = new String[]{"football", "rugby", "hockey", "chocolate"};
		HashMap grammarHM = new HashMap();

		try	{
			grammarHM = analyser.lp.getTweetsFromDB();
			//System.out.println("In Analysis, the HM is:\n-------------------\n" + grammarHM);
		}catch (Exception e) {  
             e.printStackTrace();  
        }  

        grammarHM = analyser.toSense(grammarHM);
        Set set = grammarHM.entrySet();




		LinkedList<hypernymTreeNode> queue = new LinkedList<hypernymTreeNode>();
		 
		hypernymTreeNode current;
		hypernymTreeNode hypernymNode;
		//int counter = 40;
		//String foundCategory = null;
		hypernymTree tree = new hypernymTree();
		/*
			Add to the tree
			add all the initial nodes we are looking for
			Then do loop to add all hypernyms with breadth first search
		*/
	
		IIndexWord idxWord = null;
		IWordID wordID = null;
		IWord word = null;
		String gram = null;
		String stringWord;
		
		//add the initial leaves trying to find the common ground for
		//works while guaranteed to find a common one like sport. Won't work for mass tweets
		Iterator iter = set.iterator();
		Map.Entry g;
		 while (iter.hasNext())  {
		    g = (Map.Entry)iter.next();
		    while (g.getValue() == null)	{
		      	g = (Map.Entry)iter.next();
		     }
		    gram = g.getValue().toString();
				//might need to change the get(0), that just gets the first one off the list
				//which isn't necessarily the one needed	
		    idxWord = analyser.wordStems(g.getKey().toString(), gram, analyser);
				
			if (idxWord != null)	{
				wordID = idxWord.getWordIDs().get(0);
				word = analyser.dict.getWord(wordID);
				List<hypernymTreeNode> initialRoot = new ArrayList<hypernymTreeNode>(); 
				hypernymTreeNode node = tree.addNode(word, wordID, initialRoot, gram);
				if (node != null)	{
					initialRoot.add(node);
					node.addRoots(initialRoot, tree.getRoots());
					//This adds the initial leaf nodes to queue
					queue.add(node); 
				}
			}
		}

		tree.addRoots(tree.getNodes());
		
		 System.out.println("*tree roots: ");
		 tree.printTree();

		//iterate through unexpanded nodes of tree, findinig hypernyms, adding to tree
		//keep going till one is in common
		 Integer counter = 10;

		while (queue.peek() != null) {
			counter--;
			if (counter == 0)	{
				break;
			}
			
			
			idxWord = null;
			current = (hypernymTreeNode)queue.poll();
			System.out.println("\nNext in queue: " + current.getWordString() + " from: " + current.listToString());
			gram = current.getGrammar();
			idxWord = analyser.wordStems(current.getWordString(), gram, analyser);
			//System.out.println(idxWord.toString());
			
			//might need to change the get(0), that just gets the first one off the list
			//which isn't necessarily the one needed
			wordID = idxWord.getWordIDs().get(0);
			word = analyser.dict.getWord(wordID);

			//get the synonyms of the word investigating in the queue (need this for hypernyms)
			ISynset synset = word.getSynset();

			List<IWord> synList = synset.getWords();
			System.out.println("It's synonyms are: ");
			for(Iterator<IWord> i = synList.iterator(); i.hasNext();) {
      			  System.out.println(i.next());
  			 }

  			 //get the hypernyms of the synset
			List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
			
			System.out.println("Their hypernyms are: ");
			

			List<IWord> words;	//will become set of words that are synonyms of the hypernyms

			//for each sid in the list of hypernyms
			for(ISynsetID sid : hypernyms)	{

				words = analyser.dict.getSynset(sid).getWords();
				//for all the hypernyms
				for(Iterator<IWord> i = words.iterator(); i.hasNext();)	{
					word = i.next();
					System.out.println(word.getLemma());
					idxWord = analyser.wordStems(word.getLemma(), gram, analyser);
					
					
					//idxWord = analyser.dict.getIndexWord(word.getLemma(), POS.NOUN);
					wordID = idxWord.getWordIDs().get(0);
					word = analyser.dict.getWord(wordID);
					//System.out.println("Investigating: " + wordID);

					hypernymNode = tree.addHypernym(word, wordID, current, current.getRoots(), gram);

					if (tree.getCategory() != null)	{
						break;
					}
					queue.add(hypernymNode);


					//the for loop will run for as many hypernyms there are. 
					//Hypernyms are appended to the list and the loop keeps running
					//will keep track of ones not done because done ones are behind
					
				}

				if (tree.getCategory() != null)	{
						break;
				}
				
			}
			

			if (tree.getCategory() != null)	{
				System.out.println("\nCommon Category: \n============ \n" + tree.getCategory().getWordString() + "\n");
				System.out.println("From leaves: ");
				tree.getCategory().printRoots();
				break;
			}
		} 

		// tree.printTree();
		// hypernymTreeNode toFind = tree.findNode("food");
		// if (toFind != null)	{
		// 	toFind.printRoots();
		// }
		// else {
		// 	System.out.println("Couldn't find word");
		// }
	

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



pop node from queue	(it has roots but no hypernyms)
get synonyms
iterate through synonyms, add all hypernyms to queue without expanding










*/

		