/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import semanticsearchcloud.utilities.Config;
import semanticsearchcloud.utilities.Constants;

/**
 * Retrieve Uploaded Files.
 * 
 * Extracts the files from the watched directory and puts encrypted files
 * in the designated storage section or translates key files into data into
 * the index's posting list.
 * @author Jason
 */
public class RetrieveUploadedFiles {
    IndexFile index;
    
    /**
     * Constructor.
     * 
     * Just sets the object's index file to the passed in one.  Done so that
     * this object can add on to the posting list.
     * @param index Overall index file object
     */
    RetrieveUploadedFiles(IndexFile index) {
        this.index = index;
    }
    
    /**
     * Retrieve.
     * Goes into the watched directory and 
     * 1. Moves the encrypted data files out into the proper storage location
     * 2. Gets the size of all encrypted data files and puts them in the hash map
     * 2. Reads all .key files in the folder and puts their hashed key phrases
     *      into the file name's slot in the posting list.
     * 3. Deletes all .key files.  They are no longer necessary.
     */
    public void retrieve() {
        retrieveNetworked();
        retrieveWatched();
    }
    
    private void retrieveNetworked() {
        ServerSocket servsock = null;
        Socket sock = null;
        DataInputStream dis = null;
        int numFiles = 0;
        
        
        try {
            servsock = new ServerSocket(Config.uploadPort);
            System.out.println("Now listening on port " + Config.uploadPort);
            sock = servsock.accept();
            System.out.println("Accepted connection to: " + sock);
            
            dis = new DataInputStream(sock.getInputStream());
            numFiles = dis.readInt();
            
            System.out.println("Retrieving " + numFiles + " files.");
            System.out.println();
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error reading number of files.  Quitting.");
            System.exit(0);
        }
        
//        try {
//            sock.setKeepAlive(true);
//            sock.setSoTimeout(10000);
//        } catch (SocketException ex) {
//            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        //Read as many times as we had a file
        for (int i = 0; i < numFiles; i++) {
            retrieveAndStoreFile(dis);
        }
        
        try {
            dis.close();
            sock.close();
            servsock.close();
        } catch (SocketException ex) {
            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void retrieveAndStoreFile(DataInputStream dis) {
        int fileSize;
        String fileName;
        int bytesRead;
        int current = 0;
        BufferedOutputStream bos;
        byte[] fileBytes;
        
        try {
            //Get the file info from client
            fileName = dis.readUTF();
            fileSize = dis.readInt();
            fileBytes = new byte[fileSize];
            
            System.out.print("Attempting to read " + fileName + " from client...");
            
            //Set up the new file
            File file = new File(Constants.watchLocation + File.separator + fileName);
            if (file.exists())
                file.delete();
            file.createNewFile();
            
            
            
            //Set up output stream
            bos = new BufferedOutputStream(new FileOutputStream(file));
            
            //Read the file into the bytes array
            bytesRead = dis.read(fileBytes, 0, fileSize);
            current = bytesRead;
            
            //If the whole thing wasn't read, try reading more
            if (bytesRead != fileSize) {
                do {
                    bytesRead = dis.read(fileBytes, current, (fileBytes.length - current));
                    if (bytesRead >= 0) current += bytesRead;
                } while (bytesRead > -1);
            }
            
            //Write to the new file
            bos.write(fileBytes);
            bos.flush();
            
            bos.close();
            
            System.out.println("done!");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": 123 " + ex.getMessage());
        }
    }
    
    private void retrieveWatched() {
        //Get the names of all files in the watced directory
        ArrayList<String> files = getFiles();
        
        //Iterate through all the files 
        for (String file : files) {
            //If it's a text file...
            if (file.endsWith(".txt")) {
                processTextFile(file);
                storeFile(file);
            } else if (file.endsWith(".key")) {
                //We know this is a key file and must be added to the posting list
                processKeyFile(file);
            }
        }
    }
    
    /**
     * Get Files.
     * Gets the file names of all files in the watch folder.
     * @return A list of all files in the watched directory
     */
    private ArrayList<String> getFiles() {
        File dir = new File(Constants.watchLocation);
        ArrayList<String> files = new ArrayList<String>();
        
        //This should definitely be a folder, since it's pre setup to be.
        //But check anyway just in case.
        if (dir.isDirectory()) {
            //Get an array of relative file names in the folder
            String[] contents = dir.list();
            for (String file : contents) {
                files.add(file); //Add just the file name
            }
        } 
        
        return files;
    }

    
    /**
     * Store File.
     * 
     * Moves the given file name from the watch location to the appropriate
     * storage folder.
     * @param fileName Name of file to be moved.
     */
    private void storeFile(String fileName) {
        Path sourcePath = Paths.get(Constants.watchLocation + File.separator + fileName);
        Path storagePath = Paths.get(Constants.storageLocation + File.separator + fileName);
        
        try {
            Files.move(sourcePath, storagePath, REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error moving file " + fileName + " from " + sourcePath + " to "+ storagePath);
        }
        
        File file = new File(fileName);
        file.delete();
    }
    
    
    /**
     * Process Text File.
     * Currently just gets its size puts it in the appropriate hashmap
     * @param fileName 
     */
    private void processTextFile(String fileName) {
        File file = new File(Constants.watchLocation + File.separator + fileName);
        long size = file.length();
        
        index.addToDocSizes(fileName, size);
        
//        file.delete();
    }

    
    /**
     * Process Key File.
     * 
     * Adds the given file name into the hashed keywords' slots in the posting
     * list.  
     * The .key file represented by the passed file name will have a number
     * of keywords (hashed into numbers).  This function reads in each, then calls
     * the function to add to the posting list with the line's keyword and the
     * file name.
     * 
     * Preconditions: The fileName represents an existing file that exists in the
     *    watch location.
     * Postconditions: The file is removed and its contents are added to the indexFile.
     * @param fileName Name of .key file
     */
    private void processKeyFile(String fileName) {
        try {
            File file = new File(Constants.watchLocation + File.separator + fileName);
            FileInputStream is = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            //Go through the contents of the file line by line
            String keyPhrase;
            while ((keyPhrase = br.readLine()) != null) {
            	System.out.println(keyPhrase);
                //NOTE: This should be modified so that it's not only for .txt files
                index.addToPostingList(keyPhrase, fileName.replace(".key", ".txt"));
            }
            
            //Delete the .key file
            br.close();
            file.delete();
        } catch (Exception e) {
            System.out.println("Couldn't find file " + fileName);
            e.printStackTrace();
        }
        
        
    }
    
}
