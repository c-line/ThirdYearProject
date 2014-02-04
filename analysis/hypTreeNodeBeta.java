
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

public class hypTreeNodeBeta	{
	
	public List<hypTreeNodeBeta> hypernymArray;
	public String grammar;
	public ISynsetID synsetID;
	public List<hypTreeNodeBeta> originalRoots;

	public hypTreeNodeBeta(List<hypTreeNodeBeta> fromNode, String gram, ISynsetID sID)	{

		grammar = gram;
		hypernymArray = new ArrayList<hypTreeNodeBeta>();
		synsetID = sID;
		originalRoots = new ArrayList<hypTreeNodeBeta>();
		originalRoots.addAll(fromNode);

	}

	public void addHypernym(hypTreeNodeBeta hypernym)	{

		//check if that word id is already there?
		//this just puts a reference to the node in this array. Node is also in tree
		hypernymArray.add(hypernym);

	}

	public List<hypTreeNodeBeta> getHypernyms()	{
		return hypernymArray;
	}

	public ISynsetID getSynsetID()	{
		return synsetID;
	}

	public String getGrammar()	{
		return grammar;
	}
	public List<hypTreeNodeBeta> getRoots()	{
		return originalRoots;
	}

	public boolean addRoots(List<hypTreeNodeBeta> rootsToAdd, List<hypTreeNodeBeta> treeRoots)	{
		hypTreeNodeBeta rootToAdd;
		// System.out.println("Adding roots to node: " + this.wordString + " Before: ");
		// printRoots();
		for (Iterator<hypTreeNodeBeta> toAdd = rootsToAdd.listIterator(); toAdd.hasNext();)	{
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

	public boolean isTheOne(List<hypTreeNodeBeta> treeRoots)	{

		
		hypTreeNodeBeta aTreeRoot;
		hypTreeNodeBeta anOrigRoot;

		//System.out.println("Searching for The One with " + this.wordString);

		for (Iterator<hypTreeNodeBeta> iterTree = treeRoots.listIterator(); iterTree.hasNext();)	{
			aTreeRoot = iterTree.next();
			if (originalRoots.contains(aTreeRoot)==false)	{
				return false;
			}
		}
		for (Iterator<hypTreeNodeBeta> iter = originalRoots.listIterator(); iter.hasNext();)	{
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
		// for (Iterator<hypTreeNodeBeta> iter = treeRoots.listIterator(); iter.hasNext();)	{
		// 	System.out.println(iter.next().getWordString());
		// }
		// System.out.println("--------------------------");

		return true;
	}

	public void printRoots(IDictionary dict)	{
		

		List<IWord> words = new ArrayList();
		hypTreeNodeBeta print;
		ISynset synset;
		IWord word;

		for (Iterator<hypTreeNodeBeta> toPrint = originalRoots.listIterator(); toPrint.hasNext();)	{
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

	public void printNode(IDictionary dict)	{

		List<IWord> words = new ArrayList();
		//hypTreeNodeBeta print;
		ISynset synset;
		IWord word;

		System.out.println("*Printing node: ");
		synset = dict.getSynset(this.synsetID);
		words = synset.getWords();
			
			
		for (Iterator<IWord> iter = words.listIterator(); iter.hasNext();)	{
			word = iter.next();
			System.out.println(word.getLemma());
		}

		System.out.println("     *From roots: ");
		printRoots(dict);
		
	}

}
/*




Like the blank space





*/