package pairBlock;

import dataView.*;


public class FactorAllocationVariable extends RandomisedCatVariable {
	private NumVariable covariate;
	private boolean matchedByCovariate = false;
	
	public FactorAllocationVariable(String theName, NumVariable covariate, String factorLevels) {
		super(theName);
		this.covariate = covariate;
		readLabels(factorLevels);
		int nVals = covariate.noOfValues();
		int nCats = noOfCategories();
		int nMatchedGroups = nVals / nCats;
		String valueString = "";
		for (int i=0 ; i<nCats ; i++)
			valueString += nMatchedGroups + "@" + i + " ";
		readValues(valueString);
	}
	
	public void setMatchedByCovariate(boolean matchedByCovariate) {
		this.matchedByCovariate = matchedByCovariate;
	}
	
	protected void randomiseMap() {
		if (matchedByCovariate) {
			int nCats = noOfCategories();
			int nVals = noOfValues();
			if (map == null || map.length != nVals)
				map = new int[nVals];
			
			int nMatchedGroups = nVals / nCats;
			
			for (int i=0 ; i<nMatchedGroups ; i++) {
				int index[] = new int[nCats];
				for (int j=0 ; j<nCats ; j++)
					index[j] = covariate.rankToIndex(i * nCats + j);
				permute(index);
				
				for (int j=0 ; j<nCats ; j++)
					map[index[j]] = nMatchedGroups  * j + i;
			}
		}
		else
			super.randomiseMap();
	}
	
	private void permute(int[] index) {
		for (int i=index.length-1 ; i>0 ; i--) {
			int target = (int)Math.round(Math.floor(generator.nextDouble() * (i + 1)));
			int temp = index[i];
			index[i] = index[target];
			index[target] = temp;
		}		
	}
}
