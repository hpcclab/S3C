import java.util.ArrayList;
/*
 * Child class of Searcher / override createTrapdoor method
 * Use combination/permutation from the search query and their 
 * Wikipedia data extracted using Maui and synonym from WordNik as trapdoor extension 
 */
public class SearcherWKSN extends Searcher_temp {
	
	public SearcherWKSN() {
		// TODO Auto-generated constructor stub
		super();
		System.out.println("Searching with option of both Synonym and Wikipedia!");
	}

	@Override
	public void createTrapdoor(String[] queries) {
		// TODO Auto-generated method stub
		for (String term : queries) {
			if (term != "") {
				
				if (term.charAt(term.length()-1) == ' ')
					term = term.substring(0, term.length()-1);
				
				float termweight = (super.CORE_WEIGHT / queries.length) * term.split(" ").length;
				
				if(!weights.containsKey(term))
					weights.put(term, (float)computeSemantics(term, termweight, 1));

				ArrayList<String> synonyms = new ArrayList<>();
				
				/*
				 * Get synonym from WorkNik
				 */
				synonyms = thesaurus.getSynonyms(term);
				for (String syn : synonyms) {
					syn = syn.toLowerCase();

					if (!weights.containsKey(syn)) {
                                            float score = (float)computeSemantics(syn, termweight, synonyms.size());
						weights.put(syn, score);
						synonymsKey.put(syn, score);
					}
				}
				
				/*
				 * Download Wikipedia data
				 */
				wikipedia.downloadWikiContent(term);
			}
		}
		
		/*
		 * Extract Wikipedia data to keyword and add to trapdoor
		 */
		if (queries.length > 0) {
			wikipedia.getWikiTopics(weights, weights.get(query));
			System.out.println("Wikipedia expansion: " + wikipedia.getWikiKey());
		}
		
		System.out.println("Synonyms expansion: " + synonymsKey);
               
                ArrayList<String> combo = new ArrayList();
                combo.add(queries[0].trim());
                combo.addAll(wikipedia.getWikiKey().keySet());
                combo.addAll(synonymsKey.keySet());

		System.out.print("Query extension: ["); 
		for (String i : combo) 
			System.out.print(i + ", ");
		System.out.println("]");
	}

}
