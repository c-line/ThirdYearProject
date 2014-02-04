import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.Synset;
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

import java.util.*;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.ObjectInputStream.GetField;
import java.lang.NullPointerException;

public class Categorising	{

	public static recursiveTree tree;
	public static LinkedList<recursiveTreeNode> queue;
	public static IDictionary dict;

	public static void main(String[] args)	{


		String wnhome = System.getenv("WNHOME");
		String path = wnhome + File.separator + "dict";
		dict = null;

		try{
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
			dict.open();
		}catch (Exception e) {  
	        e.printStackTrace();  
	    }  


		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

		
        //initialise global variables
		queue = new LinkedList<recursiveTreeNode>();
		tree = new recursiveTree(dict);


		try {
          // The newInstance() call is a work around for some
          // broken Java implementations
          Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (Exception ex) {
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

		/*
			Now have a certain amount of keywords left. 
			Need to properly categorise these
			Don't want to think about individual tweets. 
			Want to look just at keywords (as context for keywords has already been determined)

			Add keyword to graph
			Attempt to connect it (for first/one node there si nothing to connect to so done)
			add a node, keep expanding till reaches something

			Heuristic:
			Need to establish a heuristic which estimates/knows distance from "entitiy"
			
		*/
	// 	recursiveTree tree = new recursiveTree(dict);
	// 	String word;
	// 	SynsetID synsetID = null;
	// 	String synsetIDString = null;
	// 	String gram = null;
	// 	ISynset synset;
	// 	List<ISynsetID> hypernyms;
	// 	ISynsetID hypSynID;
	// 	//class might not be the same!
	// 	recursiveTreeNode hypernymNode = null;

	// 	try {
	// 		stmt = conn.createStatement();
	// 		rs = stmt.executeQuery("select synsetID, grammar, keyword from keywords;");
	// 	}catch(Exception e){
	// 		e.printStackTrace();
	// 	}
	// 	try{
	// 	while(rs.next())	{

	// 		synsetIDString = rs.getString("synsetID");
	// 		gram = rs.getString("grammar");
	// 		System.out.println("Adding " + rs.getString("keyword") + " as root");

	// 		synsetID = synsetID.parseSynsetID(synsetIDString);

	// 		//all keywrods are roots, so have no hyponyms hence null
	// 		recursiveTreeNode node = tree.addNode(gram, synsetID, null);
	// 		if (node == null)	{
				
	// 		}
	// 		else	{
	// 			//expand it till entity or attached
	// 			while (node.getAttached() == false)	{
	// 				synset = dict.getSynset(synsetID);
	// 				//get the hypernyms of the synset
	// 				hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
					
	// 				//for each sid in the list of hypernyms
	// 				for (Iterator<ISynsetID> i = hypernyms.listIterator(); i.hasNext();)	{
	// 					hypSynID = i.next();
	// 					hypernymNode = tree.addHypernym(node, gram, hypSynID);
	// 					if (hypernymNode.getAttached() == true)	{
	// 						System.out.println("Breaking because atttached on: ");
	// 						hypernymNode.printNode(dict);
	// 						System.out.println("\n\n");
	// 						//this is executed if when the node was added it was found somewhere
	// 						//else in the graph. So we can stop expanding
	// 						//once entity has been added once, this should also be executed there
	// 						break;
	// 					}	
	// 				}

	// 				node = hypernymNode;
	// 			}
	// 		}

		
	// 	}//ends while rs.next

	// } catch(SQLException sqle)	{

	// }




		

		String word;
		SynsetID synsetID = null;
		String synsetIDString = null;
		String gram = null;
		ISynset synset;
		List<ISynsetID> hypernyms;
		ISynsetID hypSynID;
		//class might not be the same!
		recursiveTreeNode hypernymNode = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select synsetID, grammar, keyword from keywords;");
		}catch(Exception e){
			e.printStackTrace();
		}
		try{

		// if(rs.next())	{
		// 	//for the first result, need to set attacehd
		// 	synsetIDString = rs.getString("synsetID");
		// 	gram = rs.getString("grammar");
		// 	System.out.println("Adding " + rs.getString("keyword") + " as root");
		// 	synsetID = synsetID.parseSynsetID(synsetIDString);
			
		// 	//all keywrods are roots, so have no hyponyms hence null
		// 	recursiveTreeNode node = tree.addNode(gram, synsetID, null);
		// 	if (node == null)	{
		// 		//for whatever reason, couldn't add node so returned null
		// 	}
		// 	else{
		// 		node = addNodeRecursion(node);
		// 	}

		// 	System.out.println("\n");


		// }
		while(rs.next())	{

			/*
				get the next keyword, 
				add it as node, 
				recurively expand till hits something or entity
				on return from recursion, calculate heuristics! 
			*/
			synsetIDString = rs.getString("synsetID");
			gram = rs.getString("grammar");
			System.out.println("Adding " + rs.getString("keyword") + " as root");
			synsetID = synsetID.parseSynsetID(synsetIDString);
			
			//all keywrods are roots, so have no hyponyms hence null
			recursiveTreeNode node = tree.addNode(gram, synsetID, null);
			if (node == null)	{
				//for whatever reason, couldn't add node so returned null
			}
			else{
				node = addNodeRecursion(node);
			}
		
		}//ends while rs.next

	} catch(SQLException sqle)	{

	}

	/*
	
	Now that the graph is built. Find x many categories 
	
	*/

	/*
	Start at entity, find how many children it has
	Use WordNet Hyponym relation to get SynsetIDs of the children
	Go to the ones I have in graph
	repeat till as deep as want to be
	*/

	//entity
	LinkedList<recursiveTreeNode> queueHypo = new LinkedList<recursiveTreeNode>();


	
	//initialising all necessary things, starting wthi entity
	IIndexWord idxWord = dict.getIndexWord("entity", POS.NOUN);
	ISynsetID synsetId = null;
	List<IWordID> wordIDs = idxWord.getWordIDs();
	IWordID  wordID = wordIDs.get(0);
	synsetId = wordID.getSynsetID();

	//entity may not be there after filtering!!!!

	//add entity to queue
	recursiveTreeNode currentNode = tree.isNode(synsetId);
	if (currentNode==null)	{
		//need to start searching for next highest node 
	}
	//entity not found! 
	//because first node added to graph expanded down 2 different branches and bumped into itself! 

	
	queueHypo.add(currentNode);
	System.out.println("Current ");
	System.out.println(currentNode);
	try {
		currentNode.printNode(dict);	
	}catch(NullPointerException e)	{
		e.printStackTrace();
	}
	

	//initialise other variables
	ISynsetID hypoSynID = null;

	List<ISynsetID> hyponyms;
	
	do {
		currentNode = queueHypo.poll();
		synsetId = currentNode.getSynsetID();
		synset = dict.getSynset(synsetId);

		hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM);
		for (Iterator<ISynsetID> i = hyponyms.listIterator(); i.hasNext();)	{
			hypoSynID = i.next();	
			recursiveTreeNode hypo = tree.isNode(hypoSynID);

			if (hypo != null)	{
				queueHypo.add(hypo);
			}
		}
		//System.out.println("queue size: " + Integer.toString(queueHypo.size()));

		if (queueHypo.size() == 30)	{
			System.out.println("\n*Categories! :");
			for (ListIterator<recursiveTreeNode> ii = queueHypo.listIterator(); ii.hasNext();)	{
				recursiveTreeNode h = ii.next();
				h.printNode(dict);
			}
			break;
		}

	} while(queueHypo.peek() != null);


	}//ends main

	public static recursiveTreeNode addNodeRecursion(recursiveTreeNode currentNode)	{
		/*
		This should get called when adding a new node
		Should return when all done connecting it to the tree
		Heuristic value should be added here
		*/

		/*
		initial root is added
		get it's hypernyms
		add all the hypernyms to a queue
		queue is global to allow for BFS
		iterate through the queue, adding them to tree adn it's hypernyms to queue
		*/

		//int heuristic = null;
		recursiveTreeNode returned = null;
		recursiveTreeNode hypernymNode = null;
		ISynsetID hypSynID = null;
		ISynset synset;
		String gram;
		List<ISynsetID> hypernyms;

		while (currentNode != null & currentNode.getHeuristic() == 1)	{
			//System.out.println("starting recursion with " + currentNode);
			synset = dict.getSynset(currentNode.getSynsetID());
			hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
			//for each sid in the list of hypernyms
			if (hypernyms.isEmpty() == false) {
				for (Iterator<ISynsetID> i = hypernyms.listIterator(); i.hasNext();)	{
					//for each hypernym make a node for it. Add node to queue
					hypSynID = i.next();
					gram = hypSynID.getPOS().toString();
					hypernymNode = tree.addHypernym(currentNode, gram, hypSynID);
					queue.add(hypernymNode);
				}
			}
			else {
				//if hypernyms == null
				//no hypernyms for this word, check if it's null
				IIndexWord idxWord = dict.getIndexWord("entity", POS.NOUN);
				List<IWordID> wordIDs = idxWord.getWordIDs();
				IWordID  wordID = wordIDs.get(0);
				ISynsetID synsetId = wordID.getSynsetID();

				//System.out.println("No hyps");
				//System.out.println(currentNode.getSynsetID() + " " + synsetId);
				if (currentNode.getSynsetID().equals(synsetId))	{
					currentNode.setHeuristic(0);
					System.out.println("Breaking on finding entity");
					return currentNode;
				}
			}

			
			//the returned node will be the next in the path that leads to either the 
			//attacehd tree or entity
			if (queue.peek() != null)	{
				//System.out.println("Queue.poll = " + queue.peek());

				returned = addNodeRecursion(queue.poll());
			//	System.out.println("Returned = " + returned);

				if (returned.getHeuristic() != 1)	{
					System.out.println("returning because attached. Level" + Integer.toString(returned.getHeuristic()));

					int heuristic = returned.getHeuristic() - 1;
					currentNode.setHeuristic(heuristic);
					return currentNode;
				}
			
			}
				

			return currentNode;
		}

		return currentNode;
		
	}//ends recursion




}//ends class


/*



love the space



*/