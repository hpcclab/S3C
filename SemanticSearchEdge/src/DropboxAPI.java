
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.SearchResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * DropboxAPI
 *
 * Class that implements the CloudAPI interface to connect to and use Dropbox as
 * a repository. WARNING: Cloud APIs are volatile. Things deprecate quickly.
 *
 * @author Conor Fontenot
 */
public class DropboxAPI implements CloudAPIs {

    // Connection to the Dropbox Client. All methods must flow through this variable.
    private DbxClientV2 service;

    /**
     * Constructor
     *
     * Gives client the authorization into HPCC's Dropbox repository.
     */
    public DropboxAPI() {
        // Set up the HTTP config and connection to the Dropbox web client.
        DbxRequestConfig requestConfig = new DbxRequestConfig("S3C Repository");
        
        try{
            // Use the above config with our App key to access our personal dropbox. This key can be found in the Dropbox developers hub online.
            // Login: s3cdropbox@gmail.com      Pass: hpccull16
            service = new DbxClientV2(requestConfig, "fvDsz7uOH-AAAAAAAAAAC-AJAXHB-K0PkjEw--n1GshytLu6sHLT_jvAkIP0Bvo9");
        } catch (Exception e){
            System.out.println("Authentication");
        }
    }

    /**
     * Upload
     *
     * Sends all files passed into Dropbox.
     *
     * @param encryptedFiles List of Strings with all file paths to be uploaded
     * @param filenames List of Strings with all filenames to be uploaded
     * Corresponds with the encryptedFiles list
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UploadErrorException
     * @throws DbxException
     */
    @Override
    public void upload(List<String> encryptedFiles, List<String> filenames) throws Exception{
        for (int i = 0; i < encryptedFiles.size(); i++) {
            if (encryptedFiles.get(i).endsWith(".txt")) {
                
                // This if check takes into account updating a file already in our dropbox.
                // If it is there, remove it and add this new one.
                if (alreadyExists(filenames.get(i))){
                    autoRemove(filenames.get(i));
                }
                try (InputStream in = new FileInputStream(encryptedFiles.get(i))) {
                    // APIs upload method.
                    // The first parameter is the filename to be saved on the Dropbox.
                    // Must have / at the beginning to dictate the directory it will go into (can have multiple dropbox folders)
                    service.files().uploadBuilder("/" + filenames.get(i)).uploadAndFinish(in);
                }
            }
        }
    }

    /**
     * Fetch
     *
     * Finds a specific file in Dropbox and puts it in inputEncrypted.
     *
     * @param filename String to know which file to grab from Dropbox.
     * @throws Exception
     */
    @Override
    public void fetch(String filename) throws Exception {
        if(alreadyExists(filename)){
            // Create a file in inputEncrypted that we will copy the dropbox file to.
            java.io.File saveLocation = new java.io.File(Config.inputEncrypted + java.io.File.separator + filename);
            OutputStream downloadFile = new FileOutputStream(saveLocation);
            service.files().downloadBuilder("/" + filename).download(downloadFile);
            downloadFile.close();
        } else {
            System.out.println("That file is not in this repository.");
        }
        
    }

    /**
     * Remove
     *
     * Deletes the specified file from Dropbox
     *
     * @param filename Name of the file to delete
     * @throws Exception
     */
    @Override
    public void remove(String filename) throws Exception {
        if(alreadyExists(filename))
            service.files().deleteV2("/" + filename);
        else
            System.out.println("That file is not in this repository.");
    }
    
    /**
     * Auto-Remove
     * 
     * Quicker way to delete the file from the repository if we have already checked that it exists in the repository.
     * Saves time from redundant alreadyExists calls.
     * 
     * @param filename
     * @throws Exception 
     */
    private void autoRemove(String filename) throws Exception{
        service.files().deleteV2("/" + filename);
    }
    
    /**
     * Already Exists
     * 
     * Checks if a file of that name already exists in the Dropbox repository. This keeps errors from killing everything, and
     * allows us to "update" files in the repository when changes are made to them.
     * @param filename
     * @return
     * @throws SearchErrorException
     * @throws DbxException 
     */
    private boolean alreadyExists(String filename) throws SearchErrorException, DbxException{
        SearchResult searcher = service.files().searchBuilder("", filename).start();
        List<SearchMatch> results  = searcher.getMatches();
        return !results.isEmpty();
    }
}
