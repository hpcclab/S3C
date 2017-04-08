/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import semanticsearchcloud.utilities.Constants;

/**
 * Class to retrieve a user request from the client on which file to send back,
 * then send back that requested file.
 * @author Jason
 */
public class SendRequestedFile {
    private ArrayList<String> searchResults;
    private ServerSocket server;
    private Socket sock;
    /**
     * Constructor for sending requested file.
     * Basically does all the work in this one call, since there's
     * no point in breaking up the steps if we'll always want to do the whole
     * thing.
     * Takes in a sorted list of search results, gets a user request, then sends
     * that file back to the user
     * @param searchResults 
     */
    public SendRequestedFile(ArrayList<String> searchResults) {
        this.searchResults = searchResults;
        
    }
    
    public void send() {
        int choice = getUserChoice();
        
        //We can get the file name, not the absolute path, from the search results
        String fileNameWithScore = searchResults.get(choice - 1);
        int index = fileNameWithScore.indexOf(".txt");
        String fileName = fileNameWithScore.substring(0, index+4);
        
        //Once we have the file name, move that file to the user's output location
        boolean success = download(fileName);
        
        confirmToUser(success, fileName);
    }
    
    /**
     * Get the choice of file from the user.
     * Also opens up a server connection to the client.
     * @return The user's requested choice as an index to the search results
     */
    private int getUserChoice() {
        int choice = 1; //If we can't read it, just assume they want the first
        try {
            server = new ServerSocket(9090);
            sock = server.accept();
            
            //Retrieve data from client
            DataInputStream dis = new DataInputStream(sock.getInputStream());
            choice = dis.readInt();
            
            
        } catch(IOException ex) {
            System.err.println("Error connecting to client");
        }
        
        return choice;
    }
    
    
    /**
     * Send the files to the client.
     * Right now this is just a Files.move operation, but in the future
     * this should involve actual internet stuff.
     * @param fileName Name of the file to be sent
     */
    private boolean download(String fileName) {
        String filePathString = Constants.storageLocation + File.separator + fileName;
        Path filePath = Paths.get(filePathString);
        String downloadPathString = Constants.userDownloadPath + File.separator + fileName;
        Path downloadPath = Paths.get(downloadPathString);
        
        //Try moving the files
        try {
            Files.copy(filePath, downloadPath, REPLACE_EXISTING);
            
        } catch (IOException ex) {
            System.err.println("Error copying " + filePathString + " to " + downloadPathString);
            return false;
        }   
        return true;
    }
    
    
    
    private void confirmToUser(boolean success, String fileName) {
        //Attempt to send a confirmation message to the user
        try {
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            if (success)
                dos.writeUTF(fileName);
            else
                dos.writeUTF("Failure");
            dos.close();
            sock.close();
            server.close();
        } catch (IOException ex) {
            System.err.println("Error sending boolean to client");
        }
    }
}
