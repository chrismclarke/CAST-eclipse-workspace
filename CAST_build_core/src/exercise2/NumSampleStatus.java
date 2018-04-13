package exercise2;

import java.util.*;

import dataView.*;
import utils.*;
import random.*;


public class NumSampleStatus implements StatusInterface {
														//		Saves and restores seed for generator and number of values generated
	private Random generator;
	private NumVariable yVar;
	private int decimals;
	
	private long initialSeed;
	
	public NumSampleStatus(Random generator, long initialSeed, NumVariable yVar, int decimals) {
		this.generator = generator;
		this.initialSeed = initialSeed;
		this.yVar = yVar;
		this.decimals = decimals;
	}
	
	public void noteResetSample(long initialSeed) {
		this.initialSeed = initialSeed;
	}
	
	public String getStatus() {
		return initialSeed + " " + yVar.noOfValues();
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		initialSeed = Long.parseLong(st.nextToken());
		generator.setSeed(initialSeed);
		
		int n = Integer.parseInt(st.nextToken());
		yVar.clearData();
		for (int i=0 ; i<n ; i++) {
			double y;
			if (generator instanceof RandomDiscrete)
				y = ((RandomDiscrete)generator).generateOne();
			else
				y = ((RandomContinuous)generator).generateOne();
			yVar.addValue(new NumValue(y, decimals));
		}
	}
}