import java.util.List;

/**
 * Cloud API Interface
 * 
 * Interface to outline the basics of what a cloud repository should do with 
 * S3C.
 * @author Conor Fontenot
 */
public interface CloudAPIs {
  
    /**
     * Upload
     * 
     * Sends a list of file paths and a list of filenames to be uploaded to the 
     * cloud repository.
     * @param fileList List of Strings with current paths to the needed files.
     * @param filenames List of Strings with current names of the files 
     * corresponding to the fileList.
     * @throws Exception 
     */
    void upload(List<String> fileList, List<String> filenames) throws Exception;
    
    /**
     * Fetch
     * 
     * Looks for the file in the cloud repository.
     * @param filename String to designate which file is needed.
     * @throws Exception 
     */
    void fetch(String filename) throws Exception;
    
    /**
     * Remove
     * 
     * Deletes the specified file from the drive.
     * @param filename Name of the file to delete
     * @throws Exception
     */
    void remove(String filename) throws Exception;
}
