
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
	public List<hypernymTreeNode> roots;
	public hypernymTreeNode category;
	/*
	To find common ground, when branches merge into tree. At what node merges them all
	*/

	public hypernymTree()	{
		nodes = new ArrayList<hypernymTreeNode>();
		roots = new ArrayList<hypernymTreeNode>();
		category = null;

	}


	public hypernymTreeNode addNode(IWord word, IWordID id, List<hypernymTreeNode> rootsInit, String gram)	{
		//need to check if node already exists
		if(isNode(id)==null)	{
			//System.out.println("Adding node to tree: " + word.getLemma());
			
			hypernymTreeNode newNode = new hypernymTreeNode(word, id, rootsInit, gram);
			nodes.add(newNode);
			return newNode;
		}
		return null;
	}

	public hypernymTreeNode addHypernym(IWord hypernym, IWordID id, hypernymTreeNode node, List<hypernymTreeNode> roots, String gram)	{
		//check if node already exists
		hypernymTreeNode hypernymNode = isNode(id);

		//if not, 
		if (hypernymNode == null)	{
			//add it to the tree and as a hypernym
			hypernymNode = addNode(hypernym, id, roots, gram);
			//add it to the list of parents for word finding hypernyms for
			node.addHypernym(hypernymNode);
		}
		//else, we have seen this node before!
		//link as hypernym to current node
		//add a new root to it! (method checks to see if already have this root added from a different path)
		else {
			//System.out.println("Node: " + hypernym.getLemma() + " exists.");
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

	public hypernymTreeNode isNode(IWordID id)	{
		/*
		Check if the node already exists in this branch
		*/
		hypernymTreeNode checking;
		for (Iterator<hypernymTreeNode> toCheck = nodes.listIterator(); toCheck.hasNext();)	{
			checking = toCheck.next();
			if (checking.getWordId() == id)	{
				return checking;
			}
		}
		return null;
	}

	public boolean isTreeRoot(IWordID id)	{
		/*
		Check if the node already exists in this branch
		*/
		hypernymTreeNode root;
		for (Iterator<hypernymTreeNode> iter = roots.listIterator(); iter.hasNext();)	{
			root = iter.next();
			if (root.getWordId() == id)	{
				return true;
			}
		}
		return false;
	}

	public List<hypernymTreeNode> getNodes()	{
		return nodes;
	}

	public List<hypernymTreeNode> getRoots()	{
		//original roots of the whole tree
		return roots;
	}

	public void addRoots(List<hypernymTreeNode> rootsToAdd)	{
		//roots.addAll(toSet);
		hypernymTreeNode rootToAdd;
		
		if (rootsToAdd.size() == 1)	{
			setCategory(rootsToAdd.get(0));
		}
		else	{
			for (Iterator<hypernymTreeNode> toAdd = rootsToAdd.listIterator(); toAdd.hasNext();)	{
				rootToAdd = toAdd.next();
				if (isTreeRoot(rootToAdd.getWordId()))	{
					//don't add to roots because already there
				}
				else 	{
					roots.add(rootToAdd);
				}
			}
		}

		
	}

	public hypernymTreeNode getCategory()	{
		return category;
	}

	public void setCategory(hypernymTreeNode category)	{
		System.out.println("******Setting Category");
		this.category = category;
	}

	public void printTree()	{
		// Object[] array = nodes.toArray();
		// for (int i = 0; i<array.length; i++)	{
		// 	System.out.println(array[i].getWord() + "\n");

		// }
		hypernymTreeNode print;

		for (Iterator<hypernymTreeNode> toPrint = nodes.listIterator(); toPrint.hasNext();)	{
			print = toPrint.next();
			System.out.println(print.getWordString() + " " + print.getWordId());
		}
	}

	public void printRoots()	{
		for (Iterator<hypernymTreeNode> iter = roots.listIterator(); iter.hasNext();)	{

			System.out.println(iter.next().getWordString());
		}
	}

	public hypernymTreeNode findNode(String word)	{
		hypernymTreeNode test = null;
		hypernymTreeNode toReturn = null;
		for (Iterator<hypernymTreeNode> toSearch = nodes.listIterator(); toSearch.hasNext();)	{
			test = toSearch.next();
			//System.out.println("Looking at node: " + test.getWordString() + "against " + word);
			if (test.getWordString().equals(word))	{
				if (toReturn != null)	{
					System.out.println("FOUND ANOTHER NODE CALLED " + word);
				}
				System.out.println("*Found the word");
				toReturn = test;
			}
		}
		return toReturn;
	}
}