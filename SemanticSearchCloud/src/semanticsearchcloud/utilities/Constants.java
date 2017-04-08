/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.utilities;

import java.io.File;

/**
 *
 * @author Jason
 */
public class Constants {
    
    //Location of the directory being watched.
    public static String watchLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "watch";
    
    //Location of the directory to move files to.
    public static String storageLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "storage";
    
    public static String utilitiesLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "utilities";
    
    public static String userDownloadPath = ".." + File.separator + "SemanticSearchClient"
            + File.separator + "data" + File.separator + "output";
    
    //Name of the index file
    public static String indexFileName = "Index.txt";
    
    //DocSizes file name.
    public static String docSizesFileName = "DocSizes.txt";
    
    //A bool to check when doing something that might be considered debugging
    public static boolean debug = true;
    
    //A bool to dictate using importance terms or not
    public static boolean useImportance = true;
    
    //For metrics info
    public static String modelName = "Model 3";
    public static String metricsFileName = ".." + File.separator + ".." + File.separator + "expanding_network.txt";
}
