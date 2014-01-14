//package edu.mit.jwi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import java.util.*;

public class WordTest	{
	
	public static void main (String[] args)	{

		String wnhome = System.getenv("WNHOME");
		System.out.println(wnhome);
		String path = wnhome + File.separator + "dict";
		

		try{
			URL url = new URL("file", null, path);
			IDictionary dict = new Dictionary(url);
			dict.open();
			IIndexWord idxWord = dict.getIndexWord("eating", POS.NOUN);
			IWordID wordID = idxWord.getWordIDs().get(0);
			IWord word = dict.getWord(wordID);
			//System.out.println("ID = " + wordID);
			System.out.println("Lemma = " + word.getLemma());
			//System.out.println("Gloss = " + word.getSynset().toString());

			ISynset synset = word.getSynset();
			List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
			// print out each hypernym’s id and synonyms
		    List<IWord> words;
			for(ISynsetID sid : hypernyms){
				words = dict.getSynset(sid).getWords();
				System.out.print(sid + " {");
				for(Iterator<IWord> i = words.iterator(); i.hasNext();){
					System.out.print(i.next().getLemma());
					if(i.hasNext()) System.out.print(", ");
				}
					 System.out.println("}");
			}



		}
		catch(MalformedURLException e)	{
			System.out.println(e.toString());
			System.out.println("Caught malformed url exception");
		}
		catch(IOException ex)	{
			System.out.println(ex.toString());
			System.out.println("Could not find dictionary");
		}


	}

	

}