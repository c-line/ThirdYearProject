
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
	public List<hypernymTreeNode> hyponymArray;
	public String grammar;
	public ISynsetID synsetID;
	public boolean attached;

	public hypernymTreeNode(String gram, ISynsetID sID, hypernymTreeNode hyponym)	{

		grammar = gram;
		hypernymArray = new ArrayList<hypernymTreeNode>();
		hyponymArray = new ArrayList<hypernymTreeNode>();
		if (hyponym != null)	{
			hyponymArray.add(hyponym);
		}
		synsetID = sID;
		attached = false;

	}

	public void addHypernym(hypernymTreeNode hypernym)	{
		//check if that word id is already there?
		//this just puts a reference to the node in this array. Node is also in tree
		hypernymArray.add(hypernym);

	}

	public List<hypernymTreeNode> getHypernyms()	{
		return hypernymArray;
	}

	public ISynsetID getSynsetID()	{
		return synsetID;
	}

	public String getGrammar()	{
		return grammar;
	}

	public void setAttached()	{
		this.attached = true;
	}

	public boolean getAttached()	{
		return this.attached;
	}

	public void addHyponym(hypernymTreeNode toAdd)	{
		hyponymArray.add(toAdd);
	}
	
	public List<hypernymTreeNode> getHyponyms()	{
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