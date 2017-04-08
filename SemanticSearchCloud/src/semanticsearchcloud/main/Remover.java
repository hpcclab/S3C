package semanticsearchcloud.main;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import semanticsearchcloud.utilities.Config;
import semanticsearchcloud.utilities.Constants;

public class Remover {
	private ServerSocket serverSocket;
	private Socket sock;
	private String fileToRemove;
	private IndexFile indexFile;
	private String isRemoved;
	
	public Remover(IndexFile idf){
		isRemoved = "";
		indexFile = idf;
	}

        /*
        To remove something these things need to be removed:
            The .txt in storage
            The .key in Watch
            The instance in index (postingList)
            The instance in docSizes
        */
	public void waitToRemove() {
		// TODO Auto-generated method stub	
		serverSocket = null;
		sock = null;
		try{
			fileToRemove = "";
			serverSocket = new ServerSocket(7070);
			System.out.println("Now listening on port " + 7070);
			sock = serverSocket.accept();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error opening server socket");
		}
		
		try{
			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			
			try{
				Object obj = ois.readObject();
				fileToRemove = obj.toString();
				if(fileToRemove.contains("#$%-r")){
        			System.out.println("Request: " + fileToRemove.toString());
        			fileToRemove = fileToRemove.replace("#$%-r", "");
        			System.out.println(fileToRemove);
				}
			} catch(ClassNotFoundException e) {
				System.err.println("Undefined type of query");
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error getting access to input stream to remove file");
			e.printStackTrace();
		}
		
		
                // Remove from doc sizes
		System.out.println("File with size " + indexFile.documentSizes.remove(fileToRemove) + " is removed");
		indexFile.documentSizes.remove(fileToRemove);
		
                
                // Removing files from the index (posting List)
		for(String obj: indexFile.postingList.keySet()){
			if(indexFile.postingList.get(obj).contains(fileToRemove)){
				indexFile.postingList.get(obj).remove(fileToRemove);
				System.out.println("Removed " + fileToRemove + " from the index file");
				System.out.println("Removed this file associated with " + obj);
				isRemoved= "1";

			}
		}
                
                // Write the updated index back to index.txt
		try {
			indexFile.writePostingListToIndexFile();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
                
                // Write the updated docSizes to docSizes.txt
		try {
			indexFile.writeDocSizesToFile();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
                
                // Delete .txt from storage
                File storedFile = new File(Constants.storageLocation + File.separator + fileToRemove);
                if (storedFile.exists()) {
                    System.out.println("Removing " + fileToRemove + " from storage.");
                    storedFile.delete();
                }
                
                // Delete .key from Watch
                String keyFileToRemove = fileToRemove.replace(".txt", ".key");
                File keyFile = new File(Constants.watchLocation + File.separator + keyFileToRemove);
                if (keyFile.exists()) {
                    System.out.println("Removing " + keyFileToRemove + " from watch.");
                    keyFile.delete();
                }
                
//		System.out.println(indexFile.documentSizes);
//		System.out.println(postList);
		System.out.println("Sending result back to client");
		
		try{
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(isRemoved);
			oos.close();
			sock.close();
			serverSocket.close();
		}
		catch(IOException e){
			System.err.println("Problem connecting to client");
		}
		
		
	}
	
}
