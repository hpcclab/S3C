/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import semanticsearchcloud.utilities.CloudMetrics;
import semanticsearchcloud.utilities.Config;
import semanticsearchcloud.utilities.Constants;
 
/**
 * This class is meant to allow the user to perform all search related activities.
 * The class can retrieveSearchQuery the query from the user through opening a socket, then
 find the files relating to a given search query.
 * @author Jason
 */
public class Searcher {
    private IndexFile index; //So we can have access to the posting list.
    private Ranking rank;
    private ArrayList<String> queryVector;
    ArrayList<String> searchResults;
    private ServerSocket serverSocket;
    private Socket sock;
     
     
    //DO something here?
    public Searcher() {
         
    }
     
     
    /**
     * Retrieve.
     * Retrieves the vector of strings representing the query from the user.
     * @return The array of query terms
     */
    public ArrayList<String> retrieveSearchQuery() {
        queryVector = new ArrayList<>();
        //Try opening up the sockets
        try {
            serverSocket = new ServerSocket(Config.searchPort);
            System.out.println("Now listening on port " + Config.searchPort);
            sock = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Error opening server socket");
        }
         
        //Attempt to connect and get the query from the user
        try {
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
 
            try {
                //Try to read an incoming object as an arraylist
                Object obj = ois.readObject();
                queryVector = (ArrayList<String>)obj;
                System.out.println("Received: "+queryVector.toString());
            } catch(ClassNotFoundException e) {
                System.err.println("Couldn't find ArrayList Class");
                e.printStackTrace();
            }
 
        } catch (IOException e) {
            System.err.println("Error getting access to input stream");
            e.printStackTrace();
        }
         
        if (Config.debug)
            System.out.println("Received Query: " + queryVector);
         
        return queryVector;
    }
     
     
    public void debugSearchQuery() {
        queryVector = new ArrayList<>();
        queryVector.add("list beans".hashCode() + "");
        queryVector.add("list".hashCode() + "");
        queryVector.add("beans".hashCode() + "");
    }
     
     
    /**
     * Find files related to the search query in the given index.
     * Goes over the search query and finds any documents that have one of
     * the terms in the query as a keyword.
     * @param ind The index to use for this
     * @return A lsit of file names that contain the query word in them
     */
    private ArrayList<String> findRelatedFiles() {
         
        ArrayList<String> relatedFiles = new ArrayList<>();
         
        //Iterate through the topics in the inv index
        for (String topic : index.postingList.keySet()) {
            //If the query contains one of these topics, add it to the related files
        	
        	
            if (queryVector.contains(topic)) {
                relatedFiles.addAll(index.postingList.get(topic));
            }
        }
         
        //Remove duplicates.  This might be a dumb way of doing it.
        relatedFiles = new ArrayList(new LinkedHashSet(relatedFiles));
         
        if (Config.debug)
            System.out.println("Related files: " + relatedFiles);
         
        return relatedFiles;
    }
     
     
    /**
     * Rank Related Files.
     * Uses the BM25 ranking method to rank the already found related files in
     * order of perceived relevance.
     * Note, this can be used instead of findRelatedFiles, since it calls it.
     * @param index The index holding the appropriate inverted index
     * @return 
     */
    public ArrayList<String> rankRelatedFiles(IndexFile index) {
        //-----METRICS-------
        //If we're taking the search time metrics, the unaffected query will be
        //The first thing in the query vector, so extract that.
        String unHashedQuery = "";
        long begin = System.currentTimeMillis();
        if (Config.calcMetrics) {
            unHashedQuery = queryVector.remove(0);
            System.out.println(unHashedQuery);
        }
         
        this.index = index;
        ArrayList<String> files =  findRelatedFiles();
        System.out.println(files); 
        rank = new Ranking(index);
        searchResults = rank.ScoreAllDocuments(files, queryVector);
         
         
        //End the metrics time and write it
        if (Config.calcMetrics) {
            long end = System.currentTimeMillis();
            CloudMetrics.writeCloudSearchTime(end-begin, unHashedQuery);
        }
         
        return searchResults; 
    }
     
    /**
     * Send Ranked Files to Client.
     * Preconditions: Must have some list of files that have been ranked by
     *   their BM25 score
     * Postconditions: The connection to the client is closed and the list has
     *   been sent.
     */
    public void sendRankedFilesToClient() {
         
        System.out.println("Sending file list to client");
        //Use the connection to the client to send list back
        try {
        	System.out.println(searchResults.toString());
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            oos.writeObject(searchResults);
            oos.close();
            sock.close();
            serverSocket.close();
        } catch(IOException e) {
            System.err.println("Problem connecting to client");
        } 
    }
}