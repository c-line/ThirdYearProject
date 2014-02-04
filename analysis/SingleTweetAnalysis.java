
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
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.*;
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException;
import edu.mit.jwi.data.IClosable;


public class SingleTweetAnalysis	{

	IDictionary dict;


	public SingleTweetAnalysis()	{
		//lp = new LexParserObj();
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
	// System.out.println("Sense map: " + senseMap);
	 return senseMap;
	}

	public boolean start(HashMap keywords) throws Exception	{

		//System.out.println("Starting new tree\n====================\n");


		//SingleTweetAnalysis analyser = new SingleTweetAnalysis();
		HashMap grammarHM = new HashMap();
        keywords = toSense(keywords);

		LinkedList<hypTreeNodeBeta> queue = new LinkedList<hypTreeNodeBeta>();
		 
		hypTreeNodeBeta current;
		hypTreeNodeBeta hypernymNode;
		//int counter = 40;
		//String foundCategory = null;
		hypTreeBeta tree = new hypTreeBeta(dict);
		/*
			Add to the tree
			add all the initial nodes we are looking for
			Then do loop to add all hypernyms with breadth first search
		*/
	
		
		String gram = null;
		SynsetID synID = null;
				
		//add the initial leaves trying to find the common ground for
		//works while guaranteed to find a common one like sport. Won't work for mass tweets
		Set set = keywords.entrySet();
		Iterator iter = set.iterator();
		Map.Entry mp;

		 while (iter.hasNext())  {
		    mp = (Map.Entry)iter.next();
		    while (mp.getValue() == null)	{
		      	mp = (Map.Entry)iter.next();
		     }
		    gram = mp.getValue().toString();
				//might need to change the get(0), that just gets the first one off the list
				//which isn't necessarily the one needed	

		    synID = synID.parseSynsetID(mp.getKey().toString());
					
			// wordID = idxWord.getWordIDs().get(0);
			// word = analyser.dict.getWord(wordID);
			List<hypTreeNodeBeta> initialRoot = new ArrayList<hypTreeNodeBeta>(); 
			hypTreeNodeBeta node = tree.addNode(initialRoot, gram, synID);
			if (node != null)	{
				initialRoot.add(node);
				node.addRoots(initialRoot, tree.getRoots());
				//This adds the initial leaf nodes to queue
				queue.add(node); 
			}
		}

		tree.addRoots(tree.getNodes());
		
		// System.out.println("\n\n*tree roots: ");
		// tree.printTree();
		// System.out.println("\n");

		//initialise hopping values
		Integer queueLength = queue.size();
		Integer totalHops = 0;
		Integer reset = 0;

	

		//iterate through unexpanded nodes of tree, findinig hypernyms, adding to tree
		//keep going till one is in common

		while (queue.peek() != null) {
			
			if(dict.isOpen()==true) {
				current = (hypTreeNodeBeta)queue.poll();

				//hops made
				totalHops++;

				gram = current.getGrammar();
				
				//get the SynsetID stored as variable of node
				ISynsetID currentSynsetID = current.getSynsetID();


				ISynset synset = dict.getSynset(currentSynsetID);
	  			 //get the hypernyms of the synset
				List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
				
				//for each sid in the list of hypernyms
				for (Iterator<ISynsetID> i = hypernyms.listIterator(); i.hasNext();)	{

					//next hypernym Synset ID in the list
					ISynsetID hypSynID = i.next();
					
					
					//addHypernym(current node, the roots of it, the grammar, ID of synset for new node)
					//System.out.println("add hypernym");
					hypernymNode = tree.addHypernym(current, current.getRoots(), gram, hypSynID);


					if (tree.getCategory() != null)	{
						dict.close();
						tree.clean();
						tree = null;
						return true;
					}
					
					if (totalHops == queueLength)	{
						//we've done the first round of hops
						queueLength = queue.size();
						totalHops = 0;
						//for every time this reset happens, add an extra one
						
						if (reset==3)	{
							//System.out.println("Quitting for hops sake");
							dict.close();
							tree.clean();
							tree = null;
							return false;
						}
						reset++;

					}
					queue.add(hypernymNode);
					
				}
			}
			else	{
				System.out.println("Dictionary not opening");
				dict.close();
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

		}
	
		dict.close();
		tree.clean();
		tree = null;
		return false;
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

		