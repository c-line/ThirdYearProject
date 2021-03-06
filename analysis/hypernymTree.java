
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

public class hypernymTree	{

	public List<hypernymTreeNode> nodes;
	public hypernymTreeNode category;
	public IDictionary dict;
	/*
	To find common ground, when branches merge into tree. At what node merges them all
	*/

	public hypernymTree(IDictionary dictionary)	{
		nodes = new ArrayList<hypernymTreeNode>();
		category = null;
		dict = dictionary;
	}


	public hypernymTreeNode addNode(String gram, ISynsetID hypSynID, hypernymTreeNode current)	{
		
		//need to check if node already exists
		if(isNode(hypSynID)==null)	{
			//System.out.println("Adding node to tree: " + word.getLemma());
			
			hypernymTreeNode newNode = new hypernymTreeNode(gram, hypSynID, current);
			nodes.add(newNode);

			return newNode;
		}
		return null;
	}

	public hypernymTreeNode addHypernym(hypernymTreeNode node, String gram, ISynsetID hypSynID)	{
		//check if node already exists
		hypernymTreeNode hypernymNode = isNode(hypSynID);

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
			node.setAttached();
			return hypernymNode;
			
		}

		return hypernymNode;
	}

	public void addHyponym(hypernymTreeNode hypernym, hypernymTreeNode current)	{
		//add the current node as hyponym to the hypernym
		hypernym.addHyponym(current);
	}

	public hypernymTreeNode isNode(ISynsetID id)	{
		/*
		Check if the node already exists in this branch
		*/
		hypernymTreeNode checking;
		for (Iterator<hypernymTreeNode> toCheck = nodes.listIterator(); toCheck.hasNext();)	{
			checking = toCheck.next();
			if (checking.getSynsetID().equals(id))	{
				return checking;
			}
		}
		return null;
	}
	

	public List<hypernymTreeNode> getNodes()	{
		return nodes;
	}

	public void printTree()	{
		
		List<IWord> words = new ArrayList();
		hypernymTreeNode print;
		ISynset synset;
		IWord word;

		for (Iterator<hypernymTreeNode> toPrint = nodes.listIterator(); toPrint.hasNext();)	{
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