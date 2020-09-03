/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import semanticsearchcloud.utilities.Constants;
import semanticsearchcloud.utilities.Config;
import semanticsearchcloud.utilities.ScoredDocument;


/**Ranking.
 * This class gives an implementation of the BM25 method of scoring a document
 * based on a query term, as well as additional methods to rank all of the documents
 * in the system's set against a whole query.
 * @author Jason
 */
public class Ranking {
    private double avgDocLength;
    private int numDocuments;
    private float k1 = .2f; //Constant parameters
    private float b = 0.75f;
    IndexFile index;
    
    
    
    Ranking(IndexFile in) {
        index = in;
        
        //Gather desired info from index file's data
        numDocuments = index.documentSizes.size();
        double totalSize = 0;
        for (long size : index.documentSizes.values())
            totalSize += size;
        avgDocLength = totalSize / numDocuments;
    }
    
    
    /**
     * Score a single document against a single query term using the BM25 method.
     * Requires a proper docSizes hashmap and inverted index hashmap.
     * @param docName File name of the document desired
     * @param queryTerm 
     * @param termImportance 
     * @return 
     */
    public double ScoreSingleDocument(String docName, String queryTerm, float termImportance) {
        //Compute Inverse Document Frequency term
        
        HashSet<String> fs = index.postingList.get(queryTerm); //Make sure that there are files that have this term in them
        double IDF;
        if (fs != null) {
            int IDFofTerm = index.postingList.get(queryTerm).size();
            double IDFEquation = (numDocuments - IDFofTerm + 0.5) / (IDFofTerm + 0.5);
            IDF = Math.log10(IDFEquation);
        } else {
            return 0;
        }
        //Get term frequency
        //TODO: Compute actual term frequency
        float termFrequency = (index.postingList.get(queryTerm).contains(docName)) ? termImportance : 0;
        
        //Get document length term.  Normalizes it by checking against the average
        //and using some predetermined constant
        try{
        	double docLengthNormalization = k1 * (1 - b + (b * (index.documentSizes.get(docName) / avgDocLength)));
        
        	double score = IDF * ( (termFrequency * (k1 + 1)) / (termFrequency + docLengthNormalization) );
        	
        	if (Config.debug)
                System.out.println(queryTerm +  " gave " + docName + " a score of " + score);
            
            return score;
            
        } catch (NullPointerException e) {
//        	System.out.println("Error normalization!");	
//        	e.printStackTrace();
        }
        
        return 0;
    }
    
    
    /**
     * Scores a single document against an entire multi phrase query.
     * Simply gets the score of the document against all items in the query vector.
     * @param docName Document name
     * @param queryVector A list of all queries wished to be tested
     * @return 
     */
    public double ScoreSingleDocument(String docName, ArrayList<String> queryVector) {
        double score = 0;
        
        if (Constants.useImportance) {
            //Very Important, must be kept with 1:1 term to importance factor ratio
            for (int i = 0; i < queryVector.size(); i += 2) {
                if (Config.debug) 
                    System.out.println("Score " + docName + " with term " + queryVector.get(i));
                score += ScoreSingleDocument(docName, queryVector.get(i), Float.parseFloat(queryVector.get(i+1)));
            }
        } else {
            for (String term : queryVector) {
                score += ScoreSingleDocument(docName, term, 1.0f);
            }
        }
        
        if (Config.debug) {
            System.out.println("Score for document " + docName + " is: " + score);
        }
        return score;
    }
    
    
    /**
     * Score all documents in the given index using the BM25.
     * NOTE: The queryVector must be set up in the style:
     *   hashedword value hashedword value ...
     * @param relevantDocs The list of documents that do contain something in the query
     * @param queryVector The list of queries interleaved with their importance values
     * @return An ordered array of file names
     */
    public ArrayList<String> ScoreAllDocuments(ArrayList<String> relevantDocs, ArrayList<String> queryVector) {
        ArrayList<ScoredDocument> scoredDocs = new ArrayList<>();
        ArrayList<String> documentsInOrder = new ArrayList<>();
        System.out.println("This is query vector in ScoreAllDocument function: "+queryVector.toString());
        //Get scores for all relevant docs
        for (String docName : relevantDocs) {
            scoredDocs.add(new ScoredDocument(docName, ScoreSingleDocument(docName, queryVector)));
        }
        
        Collections.sort(scoredDocs, ScoredDocument.DocComparator);
        System.out.println(scoredDocs);

        //Turn the list of scored documents into a straight array of strings.
        for (ScoredDocument doc : scoredDocs) {
            documentsInOrder.add(doc.docName + " " + doc.score);
        }
        System.out.println(documentsInOrder);

        return documentsInOrder;
    }
}
