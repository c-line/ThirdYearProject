
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

public class hypTreeBeta	{

	public List<hypTreeNodeBeta> nodes;
	public List<hypTreeNodeBeta> roots;
	public hypTreeNodeBeta category;
	public IDictionary dict;
	/*
	To find common ground, when branches merge into tree. At what node merges them all
	*/

	public hypTreeBeta(IDictionary dictionary)	{
		nodes = new ArrayList<hypTreeNodeBeta>();
		roots = new ArrayList<hypTreeNodeBeta>();
		category = null;
		dict = dictionary;
	}


	public hypTreeNodeBeta addNode(List<hypTreeNodeBeta> rootsInit, String gram, ISynsetID hypSynID)	{
		
		//need to check if node already exists
		if(isNode(hypSynID)==null)	{
			//System.out.println("Adding node to tree: " + word.getLemma());
			
			hypTreeNodeBeta newNode = new hypTreeNodeBeta(rootsInit, gram, hypSynID);
			nodes.add(newNode);
			//newNode.printNode(dict);
			return newNode;
		}
		return null;
	}

	public hypTreeNodeBeta addHypernym(hypTreeNodeBeta node, List<hypTreeNodeBeta> roots, String gram, ISynsetID hypSynID)	{
		//check if node already exists
		hypTreeNodeBeta hypernymNode = isNode(hypSynID);

		//if not, 
		if (hypernymNode == null)	{
			//System.out.println("New node " + hypSynID);
			//add it to the tree and as a hypernym
			hypernymNode = addNode(roots, gram, hypSynID);
			//add it to the list of parents for word finding hypernyms for
			node.addHypernym(hypernymNode);
		}
		//else, we have seen this node before!
		//link as hypernym to current node
		//add a new root to it! (method checks to see if already have this root added from a different path)
		else {
			// System.out.println("seen this node before:");
			// hypernymNode.printNode(dict);
			node.addHypernym(hypernymNode);
			//will already have roots from one child
			//if adding roots this will perform the test of if it's the one
			boolean theOne = hypernymNode.addRoots(roots, this.roots);
			if (theOne == true)	{

				setCategory(hypernymNode);
			}
			//can also check if it reaches all original leaf roots

			/*
				Note for future development:
					One topic won't meet all leaf nodes, but can consider it significant if
					it meets 2/3rds for example. 
					This is where we will deal with that!!!!!
			*/
			
		}

		return hypernymNode;
	}

	public hypTreeNodeBeta isNode(ISynsetID id)	{
		/*
		Check if the node already exists in this branch
		*/
		hypTreeNodeBeta checking;
		for (Iterator<hypTreeNodeBeta> toCheck = nodes.listIterator(); toCheck.hasNext();)	{
			checking = toCheck.next();
			if (checking.getSynsetID().equals(id))	{
				return checking;
			}
		}
		return null;
	}

	public boolean isTreeRoot(ISynsetID id)	{
		/*
		Check if the node already exists in this branch
		*/
		hypTreeNodeBeta root;
		for (Iterator<hypTreeNodeBeta> iter = roots.listIterator(); iter.hasNext();)	{
			root = iter.next();
			if (root.getSynsetID().equals(id))	{
				return true;
			}
		}
		return false;
	}

	public List<hypTreeNodeBeta> getNodes()	{
		return nodes;
	}

	public List<hypTreeNodeBeta> getRoots()	{
		//original roots of the whole tree
		return roots;
	}

	public void addRoots(List<hypTreeNodeBeta> rootsToAdd)	{
		
		/*
			This method sets the tree roots at the very beginning
		*/
		hypTreeNodeBeta rootToAdd;
		
		if (rootsToAdd.size() == 1)	{
			//if there's only one root, then that is the category. So setCategory
			setCategory(rootsToAdd.get(0));
		}
		else	{
			for (Iterator<hypTreeNodeBeta> toAdd = rootsToAdd.listIterator(); toAdd.hasNext();)	{
				rootToAdd = toAdd.next();
				if (isTreeRoot(rootToAdd.getSynsetID()))	{
					//don't add to roots because already there
				}
				else 	{
					roots.add(rootToAdd);
				}
			}
		}

		
	}

	public hypTreeNodeBeta getCategory()	{
		return category;
	}

	public void setCategory(hypTreeNodeBeta category)	{
		
		// System.out.println("******Setting Category");
		// category.printNode(dict);
		this.category = category;
	}

	public void printTree()	{
		
		List<IWord> words = new ArrayList();
		hypTreeNodeBeta print;
		ISynset synset;
		IWord word;

		for (Iterator<hypTreeNodeBeta> toPrint = nodes.listIterator(); toPrint.hasNext();)	{
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

	public void printRoots()	{
		for (Iterator<hypTreeNodeBeta> iter = roots.listIterator(); iter.hasNext();)	{

			System.out.println(iter.next().getSynsetID());
		}
	}

	  public void clean() {
      	
      	nodes.clear();
		roots.clear();
		category = null;
		dict.close();
  }



}