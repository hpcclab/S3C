import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 * WordSemantics
 * 
 * Class used to calculate the semantic similarity between two words or phrases.
 * @author Conor
 */
public class WordSemantics {
    
    private ILexicalDatabase db;
    private WuPalmer wp;
    
    /**
     * WordSemantics
     * 
     * Basic constructor just to make the objects used by the comparison package.
     */
    public WordSemantics() {
        db = new NictWordNet();
        wp = new WuPalmer(db);
    }
    
    /**
     * Compare
     * 
     * Uses the package's function to compare how semantically close two words are.
     * Each parameter is split to test if it is a phrase vs just a word. If it is a phrase,
     * each word in the phrase is compared to the query (word1) individually and added together
     * to form the final score. The scores of a phrase are done individually and added because the
     * package cannot compare a word to a phrase.
     * 
     * @param word1 : String - the original query provided by the user
     * @param word2 : String - wikipedia or thesaurus word or phrase
     * @return s : Double - the weighting of word2 by its similarity to word1
     */
    
    public double compare(String word1, String word2){
        WS4JConfiguration.getInstance().setMFS(true);
        String[] querySplit = word1.split(" ");
        String[] similarSplit = word2.split(" ");
        double s = 0;
        
        if(querySplit.length > 1){
            if(similarSplit.length > 1){
                for(int i = 0; i<querySplit.length; i++){
                    for(int j = 0; j<similarSplit.length; j++){
                        if(querySplit[i].compareTo(similarSplit[j]) != 0){
                            s += wp.calcRelatednessOfWords(querySplit[i], similarSplit[j]);
                        }
                    }
                }
            } else {
                for(int i = 0; i<querySplit.length; i++){
                    if(querySplit[i].compareTo(word2) != 0){
                        s += wp.calcRelatednessOfWords(querySplit[i], word2);
                    }
                }
            }
        } else {  
            if(similarSplit.length > 1){
                for(int i = 0; i<similarSplit.length; i++){
                    if(similarSplit[i].compareTo(word1) != 0){
                        s += wp.calcRelatednessOfWords(word1, similarSplit[i]);
                    }
                }
            } else {
                if(word1.compareTo(word2) != 0)
                    s += wp.calcRelatednessOfWords(word1, word2);
            }
        }
        
        return s;
    }
}
