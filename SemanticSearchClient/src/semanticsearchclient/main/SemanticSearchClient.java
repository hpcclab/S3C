/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.Cipher;

import semanticsearchclient.utilities.CipherText;
import semanticsearchclient.utilities.ClientMetrics;
import semanticsearchclient.utilities.Config;
import semanticsearchclient.utilities.Constants;

/**
 *
 * @author Jason
 */
public class SemanticSearchClient {
    
    /**
     * Main Application Code.
     * 
     * General outline of steps in main:
     *   Ask the user if they want to upload or search.
     *   Ask the user for the input/output folder or file 
     *   Create an uploader.
     *   Attempt to upload using the files from the input.
     * 
     * If args are supplied to the system, the search 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean hasArgs = false;
        String queryString = "";
        String choice = "";
        String inputFolder = "";
        String removeFile = "";
        String encryptedFile = "";
        boolean isRemoved = false;
        
        System.out.println("You are using the encrypted searcher client");
        
        //Put args in place if there are any
        if (args.length > 1) {
            hasArgs = true;
            choice = args[0];
            queryString = args[1];
            inputFolder = args[1];
            removeFile = args[1];
            encryptedFile = args[1];
        }
        try {
            //First load our configuration properties
            Config.loadProperties();
            
            
            //Get the user's choice of upload or search
            Scanner scan;
            scan = new Scanner(System.in);
            
            if (!hasArgs) {
                System.out.println("Upload -u  or Search -s or Remove -r or Decrypt -d:");
                choice = scan.nextLine(); //key is user input
            }
            
            switch (choice) {
                case "-u": //Upload
                    //We only need to get the destination if we're uploading
                    //Get the destination folder
                    
                    if (!hasArgs) {
                        inputFolder = "";
                        System.out.println("Enter the input/output folder");
                        inputFolder = scan.nextLine();
                    }
                    
                    File folder = new File(inputFolder);
                    
                    //The folder must exist in order to get stuff.
                    if (!folder.exists()) {
                        System.out.println("Could not find requested folder");
                        System.exit(0);
                    }
                    
                    //Create new uploader object.
                    Uploader up = new Uploader(inputFolder);
                    //Upload using that object
                    up.upload();
                    //TODO: Clear the input folder of files
                    break;
                    
                case "-r": //Delete file
                	if (!hasArgs){
	            		removeFile = "";
	            		System.out.println("Enter the file to remove");
	            		removeFile = scan.nextLine();
	            	}
	            	removeFile =  removeFile + "#$%-r";
	            	System.out.println("Send "+removeFile);
	            	Remover r = new Remover(removeFile);
	            	isRemoved = r.remove();
	            	if(isRemoved) 
	            		System.out.println("Successfully removed the file");
	            	else
	            		System.out.println("The file "+removeFile+" is not existed in the system");
	            	break;
	            	
                case "-d":
                	if (!hasArgs){
	            		encryptedFile = "";
	            		System.out.println("Enter the file to remove");
	            		encryptedFile = scan.nextLine();
	            	}
                	
//                	encryptedFile = encryptedFile+"";
                	System.out.println("File to decrypt: "+encryptedFile);
                	CipherText thecipher = new CipherText();
                	File eFile = new File(Constants.inputLocation_encrypted+File.separator+encryptedFile);
                	if(eFile.exists()){
                		System.out.println("Decrypting the file ...");
                		try{
                			thecipher.decrypt(Constants.cipherKey, eFile.getAbsolutePath(), Constants.outputLocation_decrypted+File.separator+encryptedFile);
                			System.out.println("Decrypted!");
                		}catch (Exception e) {
							e.printStackTrace();
						}catch (Throwable e){
							e.printStackTrace();
						}
                	}
                	else{
                		System.out.println("The file does not exist!");
                	}
                	break;
                
                case "-s": //Search
                    //Get the search query from the user
                    if (!hasArgs) {
                        System.out.println("Enter search query: ");
                        queryString = scan.nextLine();
                    }
                    
                    //Searcher searcher = new Searcher(queryString);
                    //System.exit(0);
                    
                    //Begin search timing
                    long begin = System.currentTimeMillis();
                    
                    //Use the query to prepare a more advanced, hashed query to send to the server.
                    Searcher search = new Searcher(queryString);
                    
                    //Do the search.  This will provide the user with a string of options
                    ArrayList<String> results;
                    results = search.Search();
                    
                    //Finished search timing.  Write to metrics file
                    long end = System.currentTimeMillis();
                    if (Config.calcMetrics)
                        ClientMetrics.writeSearchTime(end-begin, queryString);
                    
                    
                    //-----EVALUATION-----
                    //Only calculate if we aren't using main args
                    if (!hasArgs) {
                        System.out.println();
                        System.out.println("Would you like to run an evaluation on this query? (y/n");

                        String evaluate = scan.nextLine();

                        if (evaluate.toLowerCase().charAt(0) == 'y') {
                            Evaluator eval = new Evaluator();
                            float systemValue = eval.evaluate(queryString.toLowerCase(), results);

                            System.out.println("The value of this system is " + systemValue);
                            //Only write that evaluation to a file if all files in the returned list were evaluated.
                            if (systemValue > 0)
                                eval.writeToFile();
                        }
                    }
                    
                    
                    
                    
                    /* TODO: LEt uuser pick a file
                    //Now ask the user to pick a file
                    System.out.println("Pick a file, any file: ");
                    int fileChoice = Integer.parseInt(scan.nextLine());
                    
                    RetrieveRequestedFile retriever = new RetrieveRequestedFile();
                    retriever.retrieve(fileChoice);
                    */
                    break;
                case "-t": //Thesaurus testing
                    Thesaurus t = new Thesaurus();
                    String inputWord = "";
                    while (inputWord != "-q") {
                        System.out.println("Enter a word: ");
                        inputWord = scan.nextLine();
                        ArrayList<String> synonyms = t.getSynonyms(inputWord);

                        System.out.println(synonyms);
                    }
                    
                    break;
            }
            
            
        } catch (FileNotFoundException e) {
            System.out.println("Stopwords file not found");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
}
