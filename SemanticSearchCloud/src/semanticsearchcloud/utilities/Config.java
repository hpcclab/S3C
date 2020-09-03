/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Simple static class meant to hold the configuration properties gotten from
 * the properties file.
 * @author Jason
 */
public class Config {
    
    public static enum indexStorage { NAMEKEYWORD, INVERTEDINDEX };
    
    public static indexStorage indexStorageMethod;
    public static boolean debug;
    public static boolean calcMetrics;
    public static int uploadPort;
    public static int searchPort;
    
    //Read in the properties
    public static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
            
            //Handle the different properties and assign them.
            //Handle for the index storage method
            switch(properties.getProperty("indexStorageStyle", "inverted-index")) {
                case "name-keyword":
                    indexStorageMethod = indexStorage.NAMEKEYWORD;
                    break;
                case "inverted-index":
                    indexStorageMethod = indexStorage.INVERTEDINDEX;
                    break;
            }
            
            debug = ("true".equals(properties.getProperty("debug")));
            
            calcMetrics = ("true".equals(properties.getProperty("calcMetrics")));
            
            
            System.out.println(properties.getProperty("uploadPort"));
            uploadPort = Integer.parseInt(properties.getProperty("uploadPort"));
            searchPort = Integer.parseInt(properties.getProperty("searchPort"));
        } catch (IOException e) {
            System.err.println("Configuration file not found");
        }
    }
}
