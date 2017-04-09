/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import semanticsearchclient.utilities.ClientMetrics;
import semanticsearchclient.utilities.Config;
import semanticsearchclient.utilities.StopwordsRemover;

/**
 *
 * @author jason
 */
public class Searcher {
    HashMap<String, Float> weights; //Maps a term to its weight
    ArrayList<String> queryVector; //All query terms, with their weights
    ArrayList<String> searchResults; //List of file names returned from the server
    Thesaurus thesaurus;
    ExtractWikipedia wikiExtractor;
    String orignalQuery;
    StopwordsRemover stop;
    StopwordsRemover wikiStop;
    
    /**
     * Constructor.
     * Gathers all semantic terms, gives them weights, and prepares them 
     * to be sent to the server.
     * 
     * The final query vector will consist of the following strings:
     * -The entire original query
     * -The query's adjacent subsets
     * -The query's individual words, minus stopwords
     * -Synonyms for the whole query
     * -Wiki extracts for the whole query
     * -Synonyms for each word and subset
     * -Wiki extracts for each word and subset
     * 
     * Weights for each term will be calculated and put into a weights mapping.
     * @param query The original user query
     */
    public Searcher(String query) {
        weights = new HashMap<>();
        orignalQuery = query;
        thesaurus = new Thesaurus();
        wikiExtractor = new ExtractWikipedia();
        queryVector = new ArrayList<>();
        
        //Stopword removers
        stop = new StopwordsRemover("stopwords_en.txt");
        wikiStop = new StopwordsRemover("wiki_stopwords_en.txt");
        
        long begin = System.currentTimeMillis();
        
        //Add the original query and its synonyms to the set, download its wiki page
        addTerm(query, 1.0f);
        
        //Split the query into parts and remove stopwords
        String[] subQueries = splitQuery(query);
        subQueries = stop.remove(subQueries);
        
        //If the query was multi-phrase, get all semantic data for all of the terms.
        addTerms(subQueries, 1.0f);
        
        //Now we have weights for queries and synonyms.  We need to get the wikis.
        //NOTE: This is only done this way because Maui only likes to run once per run.
        //NOTE: Passing weights to the extractor is bad, but is easiest cause of Maui getting all folders at once.
        //  This should be refactored later.
        if (Config.useSemantics) {
        	if(subQueries.length>0)
        		wikiExtractor.getWikiTopics(weights, weights.get(subQueries[0]));
        }
        
        System.out.println(weights);
        
        //Now we should hash it all and put it in the queryVector
        hashQuery();
        
        //End the timing
        long end = System.currentTimeMillis();
        if (Config.calcMetrics) 
            ClientMetrics.writeQueryTime(end-begin, query);
    }

    
    
    //---------------------QUERY PROCESSING-------------------------
    
    
    /**
     * Add Term to Weights.
     * Adds the given term and its weight to the weights map.
     * Grabs all synonyms for the given term, and puts them in.
     * Downloads the wiki page for the given term.
     * @param 
     */
    private void addTerm(String term, float weight) {
        //Put the original query lower case in, with weight 1
        weights.put(term.toLowerCase(), weight);
        
        if (Config.useSemantics) {
            //Add synonyms for the whole query
            ArrayList<String> querySyns = thesaurus.getSynonyms(term);
            
            for (int i = 1; i < querySyns.size(); i++) {
                //Strip the synonym of all kinds of useless text
                String syn = querySyns.get(i).toLowerCase();
                
                //Only do this if the term isn't already there
                if (!weights.containsKey(syn)) {
                    syn.replace(" (similar term)", "");
                    syn.replace(" (related term)", "");
                    weights.put(syn, weight / querySyns.size());
                }
            }

            //download the wiki page
            wikiExtractor.downloadWikiContent(term);
        }
    }
    
    /**
     * Add Multiple Terms in an Array to Weights.
     * Must take the parent weight of all of the terms in the array.
     * The terms' weights will be parentWeight / terms.length
     * 
     * E.G. those terms that result from query splitting should get the parent 
     * weight of 1 from the original query.
     * @param terms Array of terms to be added.
     * @param parentWeight Weight of the parent to the terms.
     */
    private void addTerms(String[] terms, float parentWeight) {
        for (String term : terms) {
            addTerm(term, parentWeight / terms.length);
        }
    }

    /**
     * Split a Given Query.
     * Splitting can either be just by spaces (into individual words)
     * or into adjacent subsets of queries.  Determined in Config.
     * @param query Query to be split.
     * @return A list of sub queries.
     */
    private String[] splitQuery(String query) {
        String[] subQueries;
        //Config should tell us how to split the query
        if (Config.splitQuery) {
            subQueries = subdivideQuery(query);
        } else {
            //Just split it by spaces
            subQueries = query.split(" ");
        }
        
        return subQueries;
    }
    
    /**
     * Subdivide query.
     * This method takes in a string and subdivides it to get all possible subsets,
     * (though not permutations of them). 
     * @param query The string to be subdivided
     * @return All subsets of the query, but not the query itself
     */
    private String[] subdivideQuery(String query) {
        ArrayList<String> subdivision = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String[] split = query.split(" ");

        //Code on how to split into subqueries
        for (int i = split.length - 1; i > 0; i--) {
            for (int j = 0; j <= split.length - i; j++) {
                for (int k = j; k < i + j; k++) {
                    //Put spaces in between words, but only if there's already another word.
                    if (sb.length() > 0) 
                        sb.append(" ").append(split[k]);
                    else 
                        sb.append(split[k]);
                }
                subdivision.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        
        String[] a = new String[0];
        return subdivision.toArray(a);
    }

    private void hashQuery() {
        for (String term : weights.keySet()) {
            queryVector.add(term.hashCode() + "");
            queryVector.add(weights.get(term) + "");
        }
    }
    
    
    
    //-------------  SEARCHING AND RESULTS ------------------------
    
    /**
     * Search.
     * 
     * Gather the required data to add to the query vector and send it to the
     * cloud.
     * @return 
     */
    public ArrayList<String> Search() {
        // ------ METRICS -------
        //If we want to measure the search time on the cloud, we need to know
        //What query we're searching.  Thus we need to send it over.
        //The cloud's config file should also have metrics turned on to avoid confusion
        if (Config.calcMetrics)
            queryVector.add(0, orignalQuery);

        Socket sock;
        
        try {
            sock = new Socket(Config.serverIP, Config.searchPort);
            try {
                //Send the Query over to the server
                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                oos.writeObject(queryVector);
                
                System.out.println("Waiting for file list from server");
                
                
                //Recieve the list of files from the server
                ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                Object obj = ois.readObject();
                searchResults = (ArrayList<String>)obj;
            } catch (IOException e) {
                System.out.println("Error sending search query");
            } catch (ClassNotFoundException e) {
                System.out.println("ArrayList Class not found.  What?");
            }
        } catch(IOException e) {
            System.err.println("Error connecting to server");
        }
        
        ArrayList<String> resultNames = ProcessResults();
        
        return resultNames;
    }
    
    /**
     * Process Results.
     * Do whatever is needed to split the results into usable data
     * Prints the max number that's signified in the config file
     * @param results 
     */
    private ArrayList<String> ProcessResults() {
        ArrayList<String> resultNames = new ArrayList<>();
        
        int i = 1;
        for (String result : searchResults) {
            if (i > Config.maxSearchResults)
                break;
            
            int index = result.indexOf(".txt");
            String fileName = result.substring(0, index + 4);
            String score = result.substring(index + 5);
            
            //Store file name
            resultNames.add(fileName);
            
            System.out.println(i + " - Document " + fileName + " has score: " + score);
            i++;
        }
        
        return resultNames;
    }
}
