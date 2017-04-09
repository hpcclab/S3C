/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import semanticsearchclient.utilities.ClientMetrics;
import semanticsearchclient.utilities.Config;
import semanticsearchclient.utilities.StopwordsRemover;

/**
 * A Semantic Searcher.
 * This object is made to perform a search operation on a given query from 
 * the appropriate server in the cloud.
 * @author Jason
 */
class SemanticSearcher {
    HashMap<String, Float> weights;
    ArrayList<String> queryVector;
    ArrayList<String> searchResults;
    Thesaurus thesaurus;
    ExtractWikipedia wikiExtractor;
    String originalQuery;
    
    
    /**
     * Constructor.
     * Makes a vector out of the given query and prepares the object 
     * to send the search query to the server.
     * 
     * The query vector will consist of the following strings:
     * -The entire given query
     * -The query's individual words minus stopwords
     * -Synonyms for the whole query 
     * -Wiki extracts for the whole query
     * -Synonyms for each word
     * -Wiki extracts for each word
     * 
     * The queryvector should get some "importance values"
     *   this means after every string is added, some number will be added to it
     *   based on the following idea:
     *   1 - original query
     *   1/n - n words in query
     *   1/m - m synonyms/wikis for original query
     *   1/n*m - m synonyms of n words in query
     * 
     * @param query 
     */
    public SemanticSearcher(String query) {
        originalQuery = query;
        thesaurus = new Thesaurus();
        wikiExtractor = new ExtractWikipedia();
        queryVector = new ArrayList<>();
        StopwordsRemover stop = new StopwordsRemover("stopwords_en.txt");
        StopwordsRemover wikiStop = new StopwordsRemover("wiki_stopwords_en.txt");
        
        //Handle everything for the whole query:
        //Add the given query
        queryVector.add(query.toLowerCase());
        queryVector.add("1");
        
        
        //Start query processing timing
        long begin = System.currentTimeMillis();
        
        //Add synonyms for the whole query
        ArrayList<String> querySyns = thesaurus.getSynonyms(query);
        for (int i = 0; i < querySyns.size(); i++) {
            queryVector.add(querySyns.get(i).toLowerCase());
            queryVector.add((1.0 / querySyns.size()) + "");
        }
        
        //Download a wiki file for the whole query
        wikiExtractor.downloadWikiContent(query);
        
        
        
        //Split the query into subsets
        /*
        The query is broken into subsets of decreasing size.
        A sample query "big apple pie filling" will be broken into:
        big apple pie, apple pie filling,
        big apple, applie pie, pie filling,
        big, apple, pie, filling
        */
        String[] queryWords;
        if (Config.splitQuery) {
            queryWords = subdivideQuery(query);
        } else {
            queryWords = query.split(" ");
        }
        //Remove stopwords
        queryWords = stop.remove(queryWords);
        
        if (Config.debug)
            System.out.println(Arrays.toString(queryWords));
        
        
        //Get synonyms of each word in the query
        //First check if the query had more than one word
        if (query.contains(" ")) {
            addQueryTermsToQueryVector(queryWords);
        }
        
        ArrayList<String> wikiKeys = new ArrayList<>();
        //Only extract wiki keys if we want to do semantic work
        if (Config.useSemantics) {
            //Use the wiki extractor to get the wiki topics downloaded into the QV
            wikiKeys = wikiExtractor.getWikiTopics(query.length());
            wikiStop.remove(wikiKeys);
            queryVector.addAll(wikiKeys);
        }
        
        //if (Config.debug) {
            System.out.println("WIki Keys:" +  wikiKeys);
            System.out.println("Query: " + queryVector);
        //}
        
        
        //Get rid of any unwanted data and hash
        processQuery();
        
        //End query processing timing
        long end = System.currentTimeMillis();
        if (Config.calcMetrics)
            ClientMetrics.writeQueryTime(end-begin, query);
    }
    
    
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
            queryVector.add(0, originalQuery);

        Socket sock;
        
        try {
            sock = new Socket(Config.serverIP, 9090);
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
    
    
    /**
     * Remove all unwanted data from the query.
     * Preconditions: the queryVector has been set to some array of queries
     * Postconditions: the queryVector will
     *  - be free of duplicates NOTE: This is now handled while building the query
     *  - be free of wikipedia-specific stopwords
     *  - be free of general stopwords
     *  - be hashed
     * NOTE: The importance values will be interleaved with the query words in
     *   order to save space.
     */
    private void processQuery() {
        //Now that everything is in the vector, hash every other word (the actual query words)
        for (int i = 0; i < queryVector.size(); i += 2) {
            Integer hashCode = queryVector.get(i).hashCode();
            queryVector.set(i, hashCode.toString());
        }
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
    
    
    /**
     * Takes in a Query subset and processes it to add it to the query vector.
     * 
     * @param queryWords 
     */
    private void addQueryTermsToQueryVector(String[] queryWords) {
        for (int i = 0; i < queryWords.length; i++) { //TODO: Improve regex
            String word = queryWords[i]; 

            queryVector.add(word.toLowerCase());
            //Add importance of 1 / num words in query
            queryVector.add((1.0 / queryWords.length) + "");

            //Only get syns if we want to do semantic work
            if (Config.useSemantics) {
                //Go through all the synonyms of all of the words in the query
                ArrayList<String> wordSyns = thesaurus.getSynonyms(word);
                for (int j = 0; j < wordSyns.size(); j++) {
                    //Make sure the synonym is not already in the query
                    String syn = wordSyns.get(j);
                    syn.replace(" (similar term)", "");
                    if (!queryVector.contains(syn)) {
                        queryVector.add(wordSyns.get(j).toLowerCase());
                        //Importance of 1 / num of words in query * num of synonyms for word
                        //FIXME: Fix this, as the importance is no longer just determined by length of query words
                        queryVector.add((1.0 / (queryWords.length * wordSyns.size())) + "");
                    }
                }
            

                //Download the wiki page for this query term
                wikiExtractor.downloadWikiContent(word);
            }
        }
    }
}
