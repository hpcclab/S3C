/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import semanticsearchcloud.utilities.CloudMetrics;
import semanticsearchcloud.utilities.Config;
import semanticsearchcloud.utilities.Constants;

/**
 * Index File.
 * Represents a loaded inverted index file that stores keywords (hashed) and what files contain them.
 * The class provides functions for loading in an index file from some directory specified in Constants,
 * as well as working on the index file, stored in postingList.
 * @author Jason
 */
class IndexFile {
    
    public static File indexFile = null;
    public static File docSizesFile = null;
    //Map a topic (string) to set of document names (strings)
    HashMap<String, HashSet<String>> postingList = null;
    //Map a document name (string) to a size
    HashMap<String, Long> documentSizes = null;
    String indexFileLocation = Constants.utilitiesLocation + File.separator + Constants.indexFileName;
    String docSizesFileLocation = Constants.utilitiesLocation + File.separator + Constants.docSizesFileName;
    
    /**
     * Index File Constructor.
     * Loads the index file object from the desired path.  If it's not there,
     * it creates one.
     * Prepares the posting list, the internal representation of the inverted index file.
     * @throws IOException 
     */
    public IndexFile() throws IOException{
        indexFile = new File(indexFileLocation);
        docSizesFile = new File(docSizesFileLocation);
        
        if (!indexFile.exists()) {
            System.out.println("Index File Not Found, Creating New Index.txt at " +
                    Constants.utilitiesLocation);
            indexFile.createNewFile(); //Create a new index file
        }
        
        if (!docSizesFile.exists()) {
            System.out.println("Doc Sizes file not found, creating new DocSizes.txt at " + 
                    Constants.utilitiesLocation);
            docSizesFile.createNewFile();
        }
        
        //Begin timing for the construction of the utility classes
        long begin = System.currentTimeMillis();
        
        System.out.println("Reading inverted index file...");
        preparePostingList();
        
        System.out.println("Reading document sizes file...");
        prepareDocSizes();
        long end = System.currentTimeMillis();
        
        if (Config.calcMetrics) {
            //Write the metrics for the time it took to construct the index file
            //And the number of entries in the inverted index
            CloudMetrics.writeIndexTime(end-begin);
            CloudMetrics.writeNumberEntries(postingList.keySet().size());
        }
    }

    
    /**
     * Prepare Posting List.
     * Reads the contents of the encrypted index file and adds it to the internal
     * representation of the inverted index (the posting list).
     * Reads lines one by one that are organized in format:
     *   FileName Topic(hashed)
     * 
     * Then puts in the posting list where each topic is a key and the list of 
     * files associated with them is the value.
     *   topic1 - file1, file4, file5
     *   topic2 - file3, file4
     *   topic3 - file2, file3
     */
    private void preparePostingList() {
        postingList = new HashMap<>();
       
        switch (Config.indexStorageMethod) {
        case NAMEKEYWORD:
        	System.out.println("preparePostingList");
            readNameKeywordStyle();
            break;
        case INVERTEDINDEX:
        	System.out.println("preparePostingListcalled");
            readInvertedIndexStyle();
            break;
        default:
            readNameKeywordStyle();
            break;
        }
    }
    
    /**
     * Read the index file in if it's using the name keyword style.
     * This is a sister function of readInvertedIndexStyle.  The call is
     * determined by the config file.
     */
    private void readNameKeywordStyle() {
        try {
            BufferedReader br = null;
            String currentLine;
            //Open the hashed index file for reading
            br = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile.getAbsolutePath())));
            //Each line should give us a filename follwowed by a topic (hashed)
            /*NOTE: This currently relies on file names not being encrypted and
            * being text files ending in .txt.  Something else couldmaybe work if we
            * want to encrypt file names as well.
            */
            while ((currentLine = br.readLine()) != null) {
                //Make sure the line has a proper filename in it
                if (currentLine.lastIndexOf(".txt") == -1) {
                    System.err.println("Unexpected file format in index file");
                    continue;
                }

                //Get the file name and topic
                int idx = currentLine.lastIndexOf(".txt");
                String fileName = currentLine.substring(0, idx+4);
                String topic = currentLine.substring(idx+5, currentLine.length());

                //Add this to the posting list
                addToPostingList(topic, fileName);
            }
        } catch (IOException e) { 
            System.err.println("Error reading index file");
        }
    }
    
    private void readInvertedIndexStyle() {
        try {
            BufferedReader br = null;
            String currentLine;
            //Open the hashed index file for reading
            br = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile.getAbsolutePath())));
            //Each line should give us a topic name followed by a list of file
            //names associated with it.
            //Topic - file1 file2 file3
            /*NOTE: This currently relies on file names not being encrypted and
            * being text files ending in .txt.  Something else couldmaybe work if we
            * want to encrypt file names as well.
            * NOTE: This currently will only support file names with no space
            */
            while ((currentLine = br.readLine()) != null) {
                String[] lineTokens = currentLine.split(" ");
                String topic = lineTokens[0];
                //Every part in the line after that should be a file name
                for (int i = 1; i < lineTokens.length; i++) {
                    String fileName = lineTokens[i];
                    //Add this to the posting list
                    addToPostingList(topic, fileName);
                    
                }
            }
            br.close();
        } catch (IOException e) { 
            System.err.println("Error reading index file");
            e.printStackTrace();
        }
    }
    
    
    /**
     * Prepare the document sizes hashmap.
     * This goes through the DocSizes.txt file and puts the file names and their
     * appropriate sizes into a hash map.
     */
    public void prepareDocSizes() {
        documentSizes = new HashMap<>();
        BufferedReader br = null;
        String currentLine;
        
        try {
            //Open the file for reading
            br = new BufferedReader(new InputStreamReader(new FileInputStream(docSizesFile.getAbsolutePath())));
            //Each line iin the file should be a document name followed by an integer
            while((currentLine = br.readLine()) != null) {
                if (currentLine.lastIndexOf(".txt") == -1) {
                    System.err.println("Unexpected file format in doc sizes file");
                    continue;
                }
                
                //Get the file name and length
                int idx = currentLine.lastIndexOf(".txt");
                String fileName = currentLine.substring(0, idx+4);
                String lenString = currentLine.substring(idx+5, currentLine.length());
                long len = Long.parseLong(lenString);
                
                addToDocSizes(fileName, len);
            }
        } catch (IOException e) {
            System.out.println("Error reading doc sizes file");
            e.printStackTrace();
        }
    }
    
    
    
    public void addToDocSizes(String fileName, long length) {
        documentSizes.put(fileName, length);
    }

    
    /**
     * Add To Posting List.
     * Add the file name to the topic's spot in the posting list.
     * @param topic
     * @param fileName 
     */
    public void addToPostingList(String topic, String fileName) {
        HashSet<String> files = postingList.get(topic);
        //Check if we need to make a new Hashset
        if (files == null || files.isEmpty()) {
            files = new HashSet<>();
        }
        
        files.add(fileName);
        postingList.put(topic, files);
    }
    
    /**
    * Write Posting List To Index File.
    * This should be called after the posting list has been updated with
    * everything that's been uploaded.
    * TODO: Change index.txt to have topic - file1, file2, file3
    * @throws Exception 
    */
    public void writePostingListToIndexFile() throws Exception {
        System.out.println("Writing new index file");
        
        switch (Config.indexStorageMethod) {
        case NAMEKEYWORD:
        	
            writeNameKeywordStyle();
            break;
        case INVERTEDINDEX:
        	
            writeInvertedIndexStyle();
            break;
        }
    }
    
    /**
     * Write the index file in the "filename topic" style.
     * Goes through the posting list and writes for each topic/file combination:
     * "filename topic" into the cloudserver/utilities/Index.txt file
     */
    private void writeNameKeywordStyle() throws IOException {
        BufferedWriter bw = null;
        try {
            //This is the file that's in the data/tmp/Index.txt
            FileOutputStream fos = new FileOutputStream(indexFile);

            bw = new BufferedWriter(new OutputStreamWriter(fos));

            //Iterate through the keys in the posting list.
            //The keys are all the topics
            for (Iterator<String> it = postingList.keySet().iterator(); it.hasNext();) {
                String topic = it.next();

                //Get all of the file names that the topic is a keyword for
                HashSet<String> files = postingList.get(topic);
                for(String file : files)
                {
                    //Write "filename topic" to the Index.txt file 
                    bw.write(file + " " + topic.toLowerCase());
                    bw.newLine();
                }
                
            }
            
            
        } catch (Exception e) {
            System.err.println("Error in writing posting list");
            e.printStackTrace();
            throw e;
        } finally {
            bw.close();
        }
    }
    
    private void writeInvertedIndexStyle() throws IOException {
        BufferedWriter bw = null;
        try {
            //This is the file that's in the data/tmp/Index.txt
            FileOutputStream fos = new FileOutputStream(indexFile);

            bw = new BufferedWriter(new OutputStreamWriter(fos));
            
            //Iterate through the keys in the posting list.
            //The keys are all the topics
            for (Iterator<String> it = postingList.keySet().iterator(); it.hasNext();) {
                //Start building a string to write to the file
                StringBuilder line = new StringBuilder();
                
                String topic = it.next();
                line.append(topic);
                
                //Get all of the file names that the topic is a keyword for
                HashSet<String> files = postingList.get(topic);
                for(String file : files)
                {
                    //Append the file name to the string
                    line.append(" ").append(file);
                }
                bw.write(line.toString());
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error in writing posting list");
            e.printStackTrace();
            throw e;
        } finally {
            bw.close();
        }
    }
    
    
    public void writeDocSizesToFile() throws Exception {
        BufferedWriter bw = null;
        System.out.println("Writing new doc sizes file");
        try {
            //This is the file that's in the data/tmp/Index.txt
            FileOutputStream fos = new FileOutputStream(docSizesFile);

            bw = new BufferedWriter(new OutputStreamWriter(fos));

            //Iterate through all the documents
            for (String doc : documentSizes.keySet()) {
                String line = doc + " " + documentSizes.get(doc);
                bw.write(line);
                bw.newLine();
            }
            
        } catch (Exception e) {
            System.err.println("Error in writing posting list");
            e.printStackTrace();
            throw e;
        } finally {
            bw.close();
        }
    }

    @Override
    public String toString() {
        return "IndexFile{" + "postingList=" + postingList + '}' + "\nDoc Sizes={" + documentSizes + ')';
    }
    
    
}
