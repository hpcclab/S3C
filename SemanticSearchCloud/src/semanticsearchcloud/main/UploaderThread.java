/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.main;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jason
 */
public class UploaderThread implements Runnable{
    public IndexFile index;
    
    public UploaderThread(IndexFile i) {
        index = i;
    }
    
    @Override
    public void run() {
        //Retrieve files from the watched folder
            RetrieveUploadedFiles retriever = new RetrieveUploadedFiles(index);
            retriever.retrieve();
            
            //Write everything to the index file.
            try {
                index.writePostingListToIndexFile();
                index.writeDocSizesToFile();
            } catch (Exception ex) {
            Logger.getLogger(UploaderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
