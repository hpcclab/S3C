package semanticsearchclient.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import semanticsearchclient.utilities.Config;

public class Remover {

	String path = null;
	String result;
	public Remover(String location) {
		path = location;
	}

	public boolean remove() throws Exception {
		boolean success = true;

		Socket sock;
		
		try{
			sock = new Socket(Config.serverIP, 7070);
			try{
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject((Object) path);
				
				System.out.println("Removing...");
				
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				Object obj = ois.readObject();
				result = obj.toString();
				System.out.println("Result is "+result);
			} catch (IOException e) {
				// TODO: handle exception
				System.out.println("Error sending remove query");
			} catch (ClassNotFoundException e) {
				// TODO: handle exception
				System.out.println("Something happened ??");
			}
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error connecting to server");
		}
		
		if(result.equals("1"))
			success = true;
		else 
			success = false;
		
		return success;
	}
}
