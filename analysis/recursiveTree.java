
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.*;

public class recursiveTree	{

	public List<recursiveTreeNode> nodes;
	public recursiveTreeNode category;
	public IDictionary dict;
	/*
	To find common ground, when branches merge into tree. At what node merges them all
	*/

	public recursiveTree(IDictionary dictionary)	{
		nodes = new ArrayList<recursiveTreeNode>();
		dict = dictionary;
	}


	public recursiveTreeNode addNode(String gram, ISynsetID hypSynID, recursiveTreeNode current)	{
		
		recursiveTreeNode newNode = null;
		//if node does NOT exist
		if(isNode(hypSynID) == null)	{
			//System.out.println("Adding node to tree: " + word.getLemma());
			
			newNode = new recursiveTreeNode(gram, hypSynID, current);
			nodes.add(newNode);
			
		}
		else	{
			//node exists, 
			//still need to update heuristics
			//
		}


		return newNode;
	}

	public recursiveTreeNode addHypernym(recursiveTreeNode node, String gram, ISynsetID hypSynID)	{
		//check if node already exists
		recursiveTreeNode hypernymNode = isNode(hypSynID);

		//if not, 
		if (hypernymNode == null)	{
			//System.out.println("New node " + hypSynID);
			//add it to the tree and as a hypernym
			hypernymNode = addNode(gram, hypSynID, node);
			//add it to the list of parents for word finding hypernyms for
			node.addHypernym(hypernymNode);
		}
		//else, we have seen this node before!
		//link as hypernym to current node
		//add a new root to it! (method checks to see if already have this root added from a different path)
		else {
			
			node.addHypernym(hypernymNode);
			hypernymNode.addHyponym(node);
			//might need to be sure it's not joing up with it's own branch of the tree
			//attached can only be set if it's joining the main tree. 
			//only set attached when joining a node whose getAttached == true

			if (hypernymNode.heuristic != 1)	{
				//node.setHeuristic(hypernymNode.getHeuristic() - 1);
			}
			
			
		}

		return hypernymNode;
	}

	public void addHyponym(recursiveTreeNode hypernym, recursiveTreeNode current)	{
		//add the current node as hyponym to the hypernym
		hypernym.addHyponym(current);
	}

	public recursiveTreeNode isNode(ISynsetID id)	{
		/*
		Check if the node already exists in this branch
		*/
		recursiveTreeNode checking;
		for (Iterator<recursiveTreeNode> toCheck = nodes.listIterator(); toCheck.hasNext();)	{
			checking = toCheck.next();
			if (checking.getSynsetID().equals(id))	{
				return checking;
			}
		}
		return null;
	}
	

	public List<recursiveTreeNode> getNodes()	{
		return nodes;
	}

	
	

	public void printTree()	{
		
		List<IWord> words = new ArrayList();
		recursiveTreeNode print;
		ISynset synset;
		IWord word;

		for (Iterator<recursiveTreeNode> toPrint = nodes.listIterator(); toPrint.hasNext();)	{
			print = toPrint.next();
			synset = dict.getSynset(print.getSynsetID());
			words = synset.getWords();
			for (Iterator<IWord> iter = words.listIterator(); iter.hasNext();)	{
				word = iter.next();
				System.out.println(word.getLemma());
			}

			//System.out.println(print.getSynsetID());
		}
	}


}
/*





love the space




*/