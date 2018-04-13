package pairBlock;

import dataView.*;
import distn.*;

public class BlockedPValueVariable extends NumSummaryVariable {
	private NumVariable yVar;
	private CatVariable treatVar, blockVar;
	private int decimals;
	
	public BlockedPValueVariable(String theName, DataSet data, String yKey, String treatKey,
																									String blockKey, int decimals) {
		super(theName);
		yVar = (NumVariable)data.getVariable(yKey);
		treatVar = (CatVariable)data.getVariable(treatKey);
		blockVar = (CatVariable)data.getVariable(blockKey);
		this.decimals = decimals;
	}
	
	protected NumValue evaluateSummary(DataSet sourceData) {
		int cellCount[][] = treatVar.getCounts(blockVar);
		int nTreats = treatVar.noOfCategories();
		int nBlocks = blockVar.noOfCategories();
		int nPerCell = cellCount[0][0];
		boolean balanced = true;
		for (int i=0 ; i<nTreats ; i++)
			for (int j=0 ; j<nBlocks ; j++)
				if (cellCount[i][j] != nPerCell) {
					balanced = false;
					break;
				}
		
		if (!balanced)
			throw new RuntimeException("Blocks and treatmens not balanced.");
		
		double syTreat[] = new double[nTreats];
		double syBlock[] = new double[nBlocks];
		double sy = 0.0;
		double syy = 0.0;
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration be = blockVar.values();
		ValueEnumeration te = treatVar.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int blockIndex = blockVar.labelIndex(be.nextValue());
			int treatIndex = treatVar.labelIndex(te.nextValue());
			syBlock[blockIndex] += y;
			syTreat[treatIndex] += y;
			sy += y;
			syy += y * y;
		}
		
		int n = yVar.noOfValues();
		double yMean = sy / n;
		double totalSsq = syy - sy * yMean;
		
		double blockSsq = 0.0;
		for (int i=0 ; i<nBlocks ; i++) {
			double blockEffect = syBlock[i] / (nTreats * nPerCell) - yMean;
			blockSsq += blockEffect * blockEffect;
		}
		blockSsq *= (nTreats * nPerCell);
		
		double treatSsq = 0.0;
		for (int i=0 ; i<nTreats ; i++) {
			double treatEffect = syTreat[i] / (nBlocks * nPerCell) - yMean;
			treatSsq += treatEffect * treatEffect;
		}
		treatSsq *= (nBlocks * nPerCell);
		
		double residSsq = totalSsq - blockSsq - treatSsq;
		int residDf = n - nTreats - nBlocks + 1;
		double meanResidSsq = residSsq / residDf;
		
		int treatDf = nTreats - 1;
		double meanTreatSsq = treatSsq / treatDf;
		
		double fRatio = meanTreatSsq / meanResidSsq;
		double pValue = 1.0 - FTable.cumulative(fRatio, treatDf, residDf);
		
		return new NumValue(pValue, decimals);
	}
}
