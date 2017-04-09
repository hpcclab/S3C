/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import semanticsearchclient.utilities.CipherText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import semanticsearchclient.utilities.Config;
import semanticsearchclient.utilities.Constants;

/**
 * Uploader.
 * 
 * The uploader is meant to pull files from the provided folder path,
 * extract the keywords from them, and then upload them to the cloud.

 * @author Jason
 */
public class Uploader {
    
    //Location of the files
    String path = null;
    //The extractor object for the key phrases
    ExtractKeyPhrases extractPhrases = null;
    //A cipher for encrypting and decrypting text
    CipherText cipher = null;
    
    /**
     * Uploader Constructor.
     * Creates a new Keyphrase extractor and cipher.
     * May malfunction if data/stopwords/stopwords_en.txt is not there cause of
     * exception thrown by ExtractKeyPhrases()
     * @param location Path of the files to be uploaded.
     * @throws FileNotFoundException Thrown if data/stopwords/stopwords_en.txt does not exist.
     */
    public Uploader(String location) throws FileNotFoundException {
        path = location;
        //Sets up maui to extract phrases from the files in given location
        extractPhrases = new ExtractKeyPhrases();
        //Set up the cipher, though it doesn't actually have a constructor.
        cipher = new CipherText();
        
        if (Config.debug)
            System.out.println("Made the uploader");
    }
    
    
    /**
     * Upload.
     * 
     * Attempts to upload documents in the supplied, verified location.
     * * Steps:
     * * Extracts key phrases from files in the given path and puts them in .key files
     *   - These files contain 10 phrases on one line each
     * * Get the names of each file.
     * 
     * @return If the upload was successful
     * @throws java.lang.Exception
     */
    public boolean upload() throws Exception {
        boolean success = true;
        long start = System.currentTimeMillis(); //Measure how long it takes.
        
        //Get the options desired for the Maui extractor
        //Replaces the "data/tmp" directory with the given path
        String[] options = Constants.getMauiExtractionOptions(path);
        //Attempt to extract the keywords
        try {
            //Uses Maui to make the .key files
            extractPhrases.extract(options);
        } catch(Exception e) {
            System.err.println("Problem extracting from Maui");
            e.printStackTrace();
        }
        
        //Now we have a .key file w/ 10 keywords for each file in the inputted folder.
        //If keyword splitting is enabled, we'll have the split up words from all
        //of those keyphrases as well.
        long end = System.currentTimeMillis();
        System.out.println("Keyword Extraction took " + (end - start) + " ms");
        
        //Get a list of all files in the given folder
        List<String> files = getFiles(path);
        List<String> keyFiles = new ArrayList<>();
        List<String> textFiles = new ArrayList<>();
        
        //Act on each file based on its ending
        /*NOTE: Right now each file gets uploaded individually through here
        * This will need to be changed when it's a web based deal.
        * Probably will need some sort of batch upload.
        */
        files.stream().forEach((String file) -> {
            if (file.endsWith(".key")) {
                //What do we want to do with each key file?
                //If we want to split the keywords in the file, do so
                if (Config.splitKeywords) {
                    splitKeywords(file);
                }
                keyFiles.add(file);
                //Just hash the contents and then upload it.
                hash(file);
            } else if (file.endsWith(".txt")) {
                //What do we want to do with each text file?
                textFiles.add(file);
                //These should just be ready for encryption and upload.
                encrypt(file);
            }
        });
        
        //TODO Upload/Move files in bulk
        uploadFilesOnNetwork();
        
        return success;
    }

    
    /**
     * Get Files.
     * Creates a list of all filenames in the supplied directory in this class.
     * @return A list of file names.
     */
    private List<String> getFiles(String absPath) {
        File dir = new File(absPath);
        List<String> files = new ArrayList<String>();
        
        //Make sure this is a folder, not just a file
        if (dir.isDirectory()) {
            //Get an array of relative file names in the folder
            String[] contents = dir.list();
            for (String file : contents) {
                files.add(path + File.separator + file); //Give each file its full pathname.
            }
        } else { //if it's just one file, return its path name
            files.add(path);
        }
        
        return files;
    }
    
    
    /**
     * Get File Name.
     * Gets the simple file name of a file at a given absolute path
     * @param path Absolute path location of the file
     * @return The file name
     */
    private String getFileName(String path) {
        Path p = Paths.get(path);
        return p.getFileName().toString();
    }
    
    
    /**
     * Encrypt a File.
     * Encrypts the file at the given absolute path.
     * @param file Absolute file path.
     */
    private void encrypt(String file) {
        try {

            // Construct the .key file
            String fileName = getFileName(file);
            System.out.println("Encrypting file : " + fileName);  
            // Encrypt the input file
            //TODO Fix upload location
            cipher.encrypt(Constants.cipherKey, file, Constants.tempLocation + "/"
                            + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    /**
     * Hash a File.
     * Hashes the contents of a file line by line.
     * Places a new file with the hashed contents in the same data/output/
     * directory.  Same as what happens with encryption.
     * Should be called on all key files
     * @param file Absolute file path 
     */
    private void hash(String filePath) {
        try {
            String fileName = getFileName(filePath);
            System.out.println("Hashing file: " + fileName);
            cipher.HashFileContents(filePath, fileName);
            
        } catch(IOException e) {
            System.err.println("Error writing or reading file.");
            e.printStackTrace();
        }
    }
    
    
    /**
     * Split keywords in the .key file provided.
     * Reads in the file provided line by line.
     * If a line contains a phrase with multiple words, it will split it into
     * its individual words and add these to the file.
     * @param filePath Absolute path of the file being considered
     */
    private void splitKeywords(String filePath) {
        try {
            String fileName = getFileName(filePath);
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            LinkedHashSet<String> lines = new LinkedHashSet<>();
            
            //Go through the file, appending and splitting as it goes
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                //Being a hash set should take care of the possible repetition
                lines.add(currentLine);
                
                String[] split = currentLine.split(" ");
                for (String word : split) {
                    lines.add(word);
                }
            }
            br.close();
            
            //Write everything just added to the hash set to the file
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException e) {
            System.err.println("Error splitting keywords in file " + filePath);
        }
    }

    
    /**
     * Upload File.
     * Moves a single file to the "upload" location.
     * Assumes that every file is correctly in the temp location.
     * TODO This could be moved into the encrypt and hash functions
     * @param fileName Name, not path, of the file 
     */
    private void uploadFile(String fileName) {
        //"Output" here just means that this is where the file was output to when
        //it was created
        Path outputPath = Paths.get(Constants.tempLocation + File.separator + fileName);
        String uploadLocation = Constants.uploadLocation + File.separator + fileName;
        Path uploadPath = Paths.get(uploadLocation);
        try {
            Files.move(outputPath, uploadPath, REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Error Moving Files");
        }
    }
    
    private void uploadFilesOnNetwork() {
        //We want to try until we successfully connect to the server
        boolean successfulConnect = false;
        Socket sock = null;
        DataOutputStream dos = null;
        Scanner scan = new Scanner(System.in);
        
        while (!successfulConnect) {
            try {
                //Try connecting to the server
                System.out.println("Attempting to connect to " + Config.serverIP + "...");
                sock = new Socket(Config.serverIP, Config.uploadPort);
                System.out.println("Connecting to " + Config.serverIP);
                
                successfulConnect = true;
            } catch (UnknownHostException ex) {
                System.err.println(this.getClass().getName() + ": Error " + ex.getMessage() + ".  Ready to try again?");
                scan.nextLine();
            } catch (IOException ex) {
                System.err.println(this.getClass().getName() + ": Error " + ex.getMessage() + ".  Ready to try again?");
                scan.nextLine();
            }
        }
        
        //By the time we get here, we must have a successful connection
        System.out.println("Server accepted connection!");
       
        //Now we want to go through all of those files and upload them
        List<String> files = getFiles(Constants.tempLocation);
        
        try {
            //First send over how many files we're sending
            dos = new DataOutputStream (sock.getOutputStream());
            dos.writeInt(files.size());
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error sending num of files.  Quitting to prevent further harm");
            System.exit(0);
        }
        
        try {
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (String file : files) {
            System.out.print("Attempting to upload " + getRelativeFileName(file) + "... ");
            uploadFileOnNetwork(sock, dos, file);
            System.out.println("done!");
        }
        
        try {
            dos.close();
            sock.close();
        } catch (SocketException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean uploadFileOnNetwork(Socket sock, DataOutputStream dos, String absFilePath) {
        FileInputStream fis;
        BufferedInputStream bis;
        String fileName = getRelativeFileName(absFilePath);
        
        try {
            //Send over the file name
            
//            //Read the file in as bytes
            File file = new File(absFilePath);
            byte[] fileBytes = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(fileBytes, 0, fileBytes.length);
            
            //Send the file name and the length of the file to server.
            dos.writeUTF(fileName);
            dos.writeInt(fileBytes.length);
            dos.flush();
//            
//            //Now try to write the file out
            dos.write(fileBytes, 0, fileBytes.length);
            dos.flush();
            
            fis.close();
            bis.close();
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ":  Error uploading " + fileName + "!  " + ex.getMessage());
            return false;
        } 
        
        return true;
    }
    
    private String getRelativeFileName(String absPath) {
        Path p = Paths.get(absPath);
        return p.getFileName().toString();
    }
}
