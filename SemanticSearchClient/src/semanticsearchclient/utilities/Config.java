/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Simple static class meant to hold the configuration properties gotten from
 * the properties file.
 * @author Jason
 */
public class Config {
    
    public static boolean splitKeywords;
    public static boolean splitQuery;
    public static boolean debug;
    public static boolean useSemantics;
    public static boolean calcMetrics;
    public static int maxSearchResults;
    public static String serverIP;
    public static int uploadPort;
    public static int searchPort;
    
    //Read in the properties
    public static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
            
            //Handle the different properties and assign them.
            //Handle for splitting keywords or not
            splitKeywords = ("true".equals(properties.getProperty("splitKeywords")));
            
            //Handle for splitting the query or not
            splitQuery = ("true".equals(properties.getProperty("splitQuery")));
            
            //Handle for debugging or not
            debug = ("true".equals(properties.getProperty("debug")));
            
            useSemantics = ("true".equals(properties.getProperty("useSemantics")));
            
            calcMetrics = ("true".equals(properties.getProperty("calcMetrics")));
            
            maxSearchResults = Integer.parseInt(properties.getProperty("maxSearchResults"));
            uploadPort = Integer.parseInt(properties.getProperty("uploadPort"));
            searchPort = Integer.parseInt(properties.getProperty("searchPort"));
            
            serverIP = properties.getProperty("serverIP");
        } catch (IOException e) {
            System.err.println("Configuration file not found");
        }
    }
}