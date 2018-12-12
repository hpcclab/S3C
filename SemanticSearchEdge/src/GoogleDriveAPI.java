import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Google Drive API
 * 
 * Class that implements the CloudAPI interface to allow for connection into
 * Google Drive.
 * WARNING: Cloud APIs are volatile. Things deprecate quickly.
 * @author Conor Fontenot
 */
public class GoogleDriveAPI implements CloudAPIs{
    
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // Gives the service instance limitations on what it can do.
    // This specific scope is ALL access. (Read, Write, Delete, etc.)
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    // Instance of the connection between the user and Google Drive.
    private static Drive service;
    
    /**
     * Constructor
     * 
     * Gives client the default authorization into HPCC's Google Drive repository.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public GoogleDriveAPI() throws IOException, GeneralSecurityException{
        try{
            // Fetch the json file from our resources folder.
            // For accessing the credentials or changing settings on the service account go here: https://console.cloud.google.com
            // Login: s3cdropbox@gmail.com      Pass: hpccull16
            // A ton of settings and fun toys to play with, but mostly useless for this.
            // If you create a new private key, the current one will be invalid.
            GoogleCredential credential = GoogleCredential.fromStream(GoogleDriveAPI.class.getResourceAsStream("/gdrivepkey.json")).createScoped(SCOPES);
            // The JSON has the HTTP information and connection details so Google handles all the hard stuff.
            service = new Drive.Builder(credential.getTransport(), JSON_FACTORY, credential).setApplicationName("HPCC S3C Google Drive Access").build();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }  
    }
  
    /**
     * Get Files
     * 
     * Gets all the text files from the Google Drive repository connected to.
     * This is necessary to search over the files in Drive because the files
     * can only be "found" via their unique file ID which Google creates upon
     * uploading. Since we don't want to save those IDs, we have to get a list
     * of all the files in the repository.
     * Called by fetch()
     * @return Hashmap of Google Files Key: filename Value: id
     * @throws IOException 
     */
    private HashMap<String,String> getFiles() throws IOException {
        try{
            // Basic commands to search for certain types of files
            // Here its grabbing all text files
            FileList result = service.files().list()
                .setQ("mimeType = 'text/plain'")
                .setFields("nextPageToken, files(id, name)")
                .execute();
            
            // The FileList is extended as a set of Tuples (basically), so 
            // we will decode that set and add it into this HashMap for easy searching.
            HashMap<String,String> decodedFiles = new HashMap();
            
            for (File file : result.getFiles()){
                decodedFiles.put(file.getName(), file.getId());
            }
            return decodedFiles;
            
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        
        
        
    }
    
    /**
     * Upload
     * 
     * Sends all the files given to the connected Google Drive.
     * @param files List of Strings of file paths to be sent.
     * @param filenames List of Strings of corresponding filenames.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @Override
    public void upload(List<String> files, List<String> filenames) throws FileNotFoundException, IOException{
        
        HashMap<String,String> currentFiles = getFiles();
        
        try{
            for(int i = 0; i < files.size(); i++){
                if (files.get(i).endsWith(".txt")){
                    
                    // Give google drive a direct reference to the file on our computer so it can copy it.
                    java.io.File localFile = new java.io.File(files.get(i));
                    FileContent mediaContent = new FileContent("text/plain", localFile);
                    File newFile = new File();
                    newFile.setName(filenames.get(i));
                    
                    if (currentFiles.get(filenames.get(i)) != null){
                        service.files().update(currentFiles.get(filenames.get(i)), newFile, mediaContent).execute();
                    } else {
                        service.files().create(newFile, mediaContent).setFields("id").execute();
                    }
                }
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        
    }
    
    /**
     * Fetch
     * 
     * Gets a file from Google Drive and puts it in the output directory.
     * @param filename String of the name of a needed file
     * @throws IOException 
     */
    @Override
    public void fetch(String filename) throws IOException{
        
        HashMap<String,String> files = getFiles();
        
        if(files.containsKey(filename)){
            System.out.println("File found in Google Drive!");
            System.out.println("Attempting to move to output...");
            // Move file to inputEncrypted
            java.io.File downloadedFile = new java.io.File(Config.inputEncrypted + java.io.File.separator + filename);
            OutputStream outputStream = new FileOutputStream(downloadedFile);
            service.files().get(files.get(filename)).executeMediaAndDownloadTo(outputStream);
            System.out.println("done!");
        }
    }
    
    /**
     * Remove
     * 
     * Deletes the specified file from Google Drive.
     * @param filename Name of the file to delete
     * @throws Exception
     */
    @Override
    public void remove (String filename) throws Exception{
        HashMap<String,String> files = getFiles();
        
        try{
            if(files.containsKey(filename)){
                System.out.println("File found in Google Drive!");
                System.out.println("Attempting to delete...");
                service.files().delete(files.get(filename)).execute();
                System.out.println("done!");
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        
    }
    
    
    /**
     * Reset
     * 
     * Developer testing method
     * Used to wipe all the text files in the Google Drive when resetting the index file.
     * **USE WITH CAUTION**
     * @throws Exception 
     */
    public void reset() throws Exception{
        HashMap<String,String> files = getFiles();
        Set<String> allFiles = files.keySet();
        
        for(String name : allFiles){
            service.files().delete(files.get(name)).execute();
        }
        System.out.println("Gdrive empty.");
    }
}
