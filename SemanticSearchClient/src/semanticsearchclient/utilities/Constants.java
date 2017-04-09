/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.utilities;

import java.io.File;

/**
 *
 * @author Jason
 */
public class Constants {
    
    //Options that tell Maui what to do
    public static String [] mauiKeyOptions = {
		"-l", "data/tmp/", "-m", "keyphrextr", "-t", "PorterStemmer", "-v", "none"
    };
    
    //Folder path for upload
    public static String uploadLocation = ".." + File.separator + "cloud" + File.separator + 
            "cloudserver" + File.separator + "watch";
    
    //Folder path for output from retrieving the requested file
    public static String outputLocation = "data" + File.separator + "output";
    public static String outputLocation_decrypted = "data" + File.separator + "output_decrypted";
    public static String inputLocation_encrypted = "data" + File.separator + "input_encrypted";

    public static String stopwordsLocation = "data" + File.separator + "stopwords";
    
    //Index file name.  This may not be necessary.
    public static String indexFileName = "Index.txt";
    
    //Encryption Key.
    //TODO this should be randomized at first start, then stored.
    public static String cipherKey = "SemanticSearch";
    
    //TODO should this be kept within the same project folder?
//    public static String tempLocation = "data" + File.separator + "tmp";
    public static String tempLocation = "uploads";
    
    //Name of the system.  Referenced in writing to the evaluation file.
    public static String systemName = "Model 3";
    
    //Name of the evaluation file.
    public static String evaluationFileName = ".." + File.separator + ".." + File.separator + "evaluation.txt";
    
    public static String modelName = "Model 3";
    
    public static String metricsFileName = ".." + File.separator + ".." + File.separator + "expanding_network.txt";
    
    /**
     * Get Maui Extraction Options.
     * Returns the options array that represents the appropriate Maui options
     * for extracting keywords from the file(s) in the provided path.
     * @param path Location of the desired file(s)
     * @return The array of Maui options.
     */
    public static String[] getMauiExtractionOptions(String path) {
        String [] options = mauiKeyOptions;
        options[1] = path;
        return options;
    }
}
