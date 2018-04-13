package dataView;

import models.*;
import random.RandomContinuous;

public class NumSampleLsVariable extends NumSampleVariable {
	private DataSet data;
	private String[] lsKey;
	private String yKey;
	
	public NumSampleLsVariable(String theName, RandomContinuous generator, int decimals,
																										DataSet data, String[] lsKey, String yKey) {
		super(theName, generator, decimals);
		this.data = data;
		this.lsKey = lsKey;
		this.yKey = yKey;
	}
	
	public long generateNextSample() {
		long currentSeed = super.generateNextSample();
		
		for (int i=0 ; i<lsKey.length ; i++) {
			CoreModelVariable lsModel = (CoreModelVariable)data.getVariable(lsKey[i]);
			lsModel.updateLSParams(yKey);
		}
		
		return currentSeed;
	}
	
	public boolean setSampleFromSeed(long newSeed) {
		boolean changed = super.setSampleFromSeed(newSeed);
		
		if (changed)
			for (int i=0 ; i<lsKey.length ; i++) {
				CoreModelVariable lsModel = (CoreModelVariable)data.getVariable(lsKey[i]);
				lsModel.updateLSParams(yKey);
			}
		
		return changed;
	}
}
