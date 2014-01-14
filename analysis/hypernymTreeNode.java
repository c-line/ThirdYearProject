
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

public class hypernymTreeNode	{
	
	public List<hypernymTreeNode> hypernymArray;
	public IWord word;
	public String wordString;
	public IWordID wordID;
	public String grammar;
	public List<hypernymTreeNode> originalRoots;

	public hypernymTreeNode(IWord nodeWord, IWordID id, List<hypernymTreeNode> fromNode, String gram)	{

		word = nodeWord;
		wordString = nodeWord.getLemma();
		wordID = id;
		grammar = gram;
		hypernymArray = new ArrayList<hypernymTreeNode>();
		originalRoots = new ArrayList<hypernymTreeNode>();
		originalRoots.addAll(fromNode);

	}

	public void addHypernym(hypernymTreeNode hypernym)	{

		//check if that word id is already there?
		//this just puts a reference to the node in this array. Node is also in tree
		hypernymArray.add(hypernym);

	}

	public List<hypernymTreeNode> getHypernyms()	{
		return hypernymArray;
	}

	public IWord getWord()	{
		return word;
	}

	public String getWordString()	{
		return wordString;
	}

	public IWordID getWordId()	{
		return wordID;
	}

	public String getGrammar()	{
		return grammar;
	}
	public List<hypernymTreeNode> getRoots()	{
		return originalRoots;
	}

	public boolean addRoots(List<hypernymTreeNode> rootsToAdd, List<hypernymTreeNode> treeRoots)	{
		hypernymTreeNode rootToAdd;
		// System.out.println("Adding roots to node: " + this.wordString + " Before: ");
		// printRoots();
		for (Iterator<hypernymTreeNode> toAdd = rootsToAdd.listIterator(); toAdd.hasNext();)	{
			rootToAdd = toAdd.next();
			//System.out.println("Being passed to add: " + rootToAdd.getWordString());
			if (originalRoots.contains(rootToAdd))	{
				//do nothing
			//	System.out.println("Doing nothin, as rootToAdd is: " + rootToAdd.getWordString());
			}
			else	{
				
				//add it to original roots
				originalRoots.add(rootToAdd);
				//System.out.println("Adding root: " + rootToAdd.getWordString());
				//should do the check here!
				if (isTheOne(treeRoots) == true)	{
					return true;
				}
			}

		}

		// System.out.println("After adding, roots to " + this.wordString + ": ");
		// printRoots();
		// System.out.println("\n");
		return false;

	}

	public boolean isTheOne(List<hypernymTreeNode> treeRoots)	{

		
		hypernymTreeNode aTreeRoot;
		hypernymTreeNode anOrigRoot;

		//System.out.println("Searching for The One with " + this.wordString);

		for (Iterator<hypernymTreeNode> iterTree = treeRoots.listIterator(); iterTree.hasNext();)	{
			aTreeRoot = iterTree.next();
			if (originalRoots.contains(aTreeRoot)==false)	{
				return false;
			}
		}
		for (Iterator<hypernymTreeNode> iter = originalRoots.listIterator(); iter.hasNext();)	{
			anOrigRoot = iter.next();
			if (treeRoots.contains(anOrigRoot)==false)	{
				return false;
			}
		}

		// System.out.println("--------------------------");
		// System.out.println("Found the one: " + this.wordString);
		// System.out.println("Roots for The One are: ");
		// printRoots();
		// System.out.println("Tree roots are: ");
		// for (Iterator<hypernymTreeNode> iter = treeRoots.listIterator(); iter.hasNext();)	{
		// 	System.out.println(iter.next().getWordString());
		// }
		// System.out.println("--------------------------");

		return true;
	}

	public void printRoots()	{
		for (Iterator<hypernymTreeNode> iter = originalRoots.listIterator(); iter.hasNext();)	{
			System.out.println(iter.next().getWordString());
		}
	}

	public String listToString()	{
		String listString = "$";
		for (Iterator<hypernymTreeNode> iter = originalRoots.listIterator(); iter.hasNext();)	{
			listString = listString + " " + iter.next().wordString;
		}
		return listString;
	}
}
/*




Like the blank space





*/