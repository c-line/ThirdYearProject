
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

public class recursiveTreeNode	{
	
	public List<recursiveTreeNode> hypernymArray;
	public List<recursiveTreeNode> hyponymArray;
	public String grammar;
	public ISynsetID synsetID;
	public int heuristic;

	public recursiveTreeNode(String gram, ISynsetID sID, recursiveTreeNode hyponym)	{

		grammar = gram;
		hypernymArray = new ArrayList<recursiveTreeNode>();
		hyponymArray = new ArrayList<recursiveTreeNode>();
		if (hyponym != null)	{
			hyponymArray.add(hyponym);
		}
		synsetID = sID;
		heuristic = 1;

	}

	public void addHypernym(recursiveTreeNode hypernym)	{
		//check if that word id is already there?
		//this just puts a reference to the node in this array. Node is also in tree
		hypernymArray.add(hypernym);

	}

	public List<recursiveTreeNode> getHypernyms()	{
		return hypernymArray;
	}

	public ISynsetID getSynsetID()	{
		return synsetID;
	}

	public String getGrammar()	{
		return grammar;
	}

	public void setHeuristic(int h)	{
		//potentially need to give it the value to set
		this.heuristic = h;
	}

	public int getHeuristic()	{
		return this.heuristic;
	}

	public void addHyponym(recursiveTreeNode toAdd)	{
		hyponymArray.add(toAdd);
	}
	
	public List<recursiveTreeNode> getHyponyms()	{
		return hyponymArray;
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
		
	}

}
/*




Like the blank space





*/