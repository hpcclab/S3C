/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import semanticsearchclient.utilities.CipherText;
import semanticsearchclient.utilities.Constants;

/**
 * Class for retrieving a file from the server.
 * This class is just meant to take in an integer and ask the server to transfer
 * that file to the user.
 * @author Jason
 */
public class RetrieveRequestedFile {
    CipherText cipher;
    
    public RetrieveRequestedFile() {
        cipher = new CipherText();
    }
    /**
     * Request to download the file
     * @param choice The int choice based off the list that was given to the user.
     */
    public void retrieve(int choice) {
        Socket sock;
        try {
            sock = new Socket("localhost", 9090);
            try {
                //Send int over to the server
                DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                dos.writeInt(choice);
                
                //Now wait for ther server to say something back
                System.out.println("Waiting for the server to move files...");
                
                //Get the file name as confirmation from the server
                DataInputStream dis = new DataInputStream(sock.getInputStream());
                String fileName = dis.readUTF();
                
                //The server will send an empty string if it didn't work
                if ("".equals(fileName)) {
                    System.err.println("Error on server side sending file");
                } else {
                    String fileLocation = Constants.outputLocation + File.separator + fileName;
                    System.out.println("Success!  " + fileName + " is now in " + fileLocation);
//                    try {
//                        cipher.decrypt(Constants.cipherKey, fileLocation , fileLocation);
//                    } catch (Throwable ex) {
//                        System.err.println("Error with decrypting file");
//                    }
                }
                
            } catch (IOException ex) {
                System.err.println("Error sending user choice to server");
            }
            
        } catch (IOException ex) {
            System.err.println("Error connecting to server");
        } 
    }
}
