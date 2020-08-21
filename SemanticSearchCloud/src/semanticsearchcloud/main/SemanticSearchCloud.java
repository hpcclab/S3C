/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import semanticsearchcloud.utilities.Config;
 
/**
 *
 * @author Jason
 */
public class SemanticSearchCloud {
 
    /**Main Application Code.
     * Server side code meant to run continually.
     * Current Version:
     *  Upon boot, creates an index file object based off what is currently in
     *  the index file.
     *    This creates a postingList that is a hashmap mapping hashed keyword strings
     *    to a list of file names.
     *  Checks the watched directory (where incoming files are) for new files.
     *  Loads those new files and adds them to the the index list.
     * 
     * TODO: Make the directory watched with a thread.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("You are using the cloud encrypted semantic searcher");
         
        ServerSocket listener;
        Socket sock = null;
        String request;
        Object obj;
        
        //First, read in the index file so we have an internal representation of it
        try {
            //Load the properties
            Config.loadProperties();
            //Create new index file.  Loads the inverted index inot a hashmap
            /*NOTE: The index file is built entirely on the cloud side.  The
              first time this is run, it will have to create a new one.
            */
            IndexFile index = new IndexFile();
             
            //Retrieve files from the watched folder
            RetrieveUploadedFiles retriever = new RetrieveUploadedFiles(index);
            retriever.retrieve();
             
            //Write everything to the index file.
            index.writePostingListToIndexFile();
            index.writeDocSizesToFile();
             
             
            //Now start accepting search requests
            Searcher search = new Searcher();
            
            Remover remover = new Remover(index);
            
            Thread uThread = new Thread(){
            	public void run(){
            		while(true)
            			retriever.retrieve();
            	}
            };
            
            Thread sThread = new Thread(){
            	public void run(){
            		while(true){
            			search.retrieveSearchQuery(); //ok
            			search.rankRelatedFiles(index);
                        search.sendRankedFilesToClient();
            		}
            	}
            };
            
            Thread rThread = new Thread(){
            	public void run(){
            		while(true){
            			remover.waitToRemove();
            		}
            	}
            };
            
            System.out.println("Waiting to serve!");
            
            uThread.start();
            sThread.start();
            rThread.start();
//            while (true){
//            	System.out.println("Waiting to serve!");
//	            try{
//	                listener = new ServerSocket(9090);
//	                sock = listener.accept();
//	            }catch (IOException e) {
//					// TODO: handle exception
//	            	System.err.println("Error opening server socket");
//				}
//	            
//	            try{
//	            	ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
//	            	
//	            	try{
//	            		obj = ois.readObject();
//	            		request = obj.toString();
//	            		if(request.contains("#$%-r")){
//	            			System.out.println("Request: " + request.toString());
//	            			request = request.replace("#$%-r", "");
//	            			String requestFile = request.toString();
//	            			remover.waitToRemove(requestFile, sock);
//	            		}
//	            		else{
//	            			search.retrieveSearchQuery(sock, obj);
//	                        search.rankRelatedFiles(index);
//	                        search.sendRankedFilesToClient();   			
//	            		}
//	            	}catch (ClassNotFoundException e) {
//						// TODO: handle exception
//	            		
//					}
//	            }catch (IOException e) {
//					// TODO: handle exception
//	            	
//				}
//            }
             
            //Now that the search is complete, get a choice of file from the user
            //SendRequestedFile sender = new SendRequestedFile(search.searchResults);
            //sender.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
         
         
        //Now we hav a single search query.  This code will run only after it's received
         
         
    }
     
}