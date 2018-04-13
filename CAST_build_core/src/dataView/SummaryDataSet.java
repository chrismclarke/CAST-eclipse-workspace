package dataView;

import java.util.*;

import utils.StatusInterface;

public class SummaryDataSet extends DataSet {
	static final private int kSeedLengthIncrement = 10;
	
	
	protected DataSet sourceData;
	private String sourceVarKey;
	private long[] sourceSeed = new long[kSeedLengthIncrement];
	private int seedsUsed = 0;
	
	private boolean accumulateSummaries = false;
	
	public SummaryDataSet(DataSet sourceData, String sourceVarKey) {
		this.sourceData = sourceData;
		this.sourceVarKey = sourceVarKey;
	}
	
	public void takeSample() {
		CoreVariable v = sourceData.getVariable(sourceVarKey);
		if (! (v instanceof SampleInterface))
			return;
		
		SampleInterface sv = (SampleInterface)v;
		long nextSeed = sv.generateNextSample();
		sourceData.variableChanged(sourceVarKey);
		
		if (accumulateSummaries) {
			if (sourceSeed.length <= seedsUsed) {
				long[] oldSeed = sourceSeed;
				sourceSeed = new long[seedsUsed + kSeedLengthIncrement];
				System.arraycopy(oldSeed, 0, sourceSeed, 0, oldSeed.length);
			}
			sourceSeed[seedsUsed] = nextSeed;
			seedsUsed ++;
		}
		else {
			sourceSeed[0] = nextSeed;
			clearData();
			seedsUsed = 1;
		}
			
		addSummariesToVariables();
	}
	
	public void setSingleSummaryFromData() {
		if (seedsUsed > 0)
			sourceSeed[0] = sourceSeed[seedsUsed - 1];
		clearData();
		seedsUsed = 1;
		addSummariesToVariables();
	}
	
	public void redoLastSummary() {
		Enumeration e = getVariableEnumeration();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof SummaryVariable) {
				SummaryVariable sumV = (SummaryVariable)v;
				sumV.redoLastSummary(sourceData);
			}
		}
		valueChanged(seedsUsed);			//		don't change selection
	}
	
	public void changeSampleSize(int n) {
		CoreVariable v = sourceData.getVariable(sourceVarKey);
		if (! (v instanceof SampleInterface))
			return;
		SampleInterface sv = (SampleInterface)v;
		sv.setSampleSize(n);
		long nextSeed = sv.generateNextSample();
		sourceData.variableChanged(sourceVarKey);
		
		sourceSeed[0] = nextSeed;
		clearData();
		seedsUsed = 1;
			
		addSummariesToVariables();
	}
	
	protected void addSummariesToVariables() {
		Enumeration e = getVariableEnumeration();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof SummaryVariable) {
				SummaryVariable sumV = (SummaryVariable)v;
				sumV.addSummary(sourceData);
			}
		}
		valuesAdded(seedsUsed);
		setSelection(seedsUsed - 1);
	}
	
	public void setAccumulate(boolean accumulateSummaries) {
		if (this.accumulateSummaries != accumulateSummaries) {
			this.accumulateSummaries = accumulateSummaries;
			int noOfVals = getSelection().getNoOfFlags();
			if (!accumulateSummaries && noOfVals > 1) {
				int currentSelection = getSelection().findSingleSetFlag();
				if (currentSelection < 0)
					currentSelection = noOfVals - 1;
				long currentSeed = sourceSeed[currentSelection];
				
				sourceSeed[0] = currentSeed;
				clearData();
				seedsUsed = 1;
				
				addSummariesToVariables();
			}
		}
	}
	
//------------------------------------------------------------

	public StatusInterface getStatusRecord() {
		return new StatusInterface() {
			public String getStatus() {
				String s = "";
				for (int i=0 ; i<seedsUsed ; i++)
					s += sourceSeed[i];
				
				return s;
			}
			
			public void setStatus(String status) {
				clearData();
				SampleInterface sourceVar = (SampleInterface)sourceData.getVariable(sourceVarKey);
				StringTokenizer st = new StringTokenizer(status);
				while (st.hasMoreTokens()) {
					long newSeed = Long.parseLong(st.nextToken());
					sourceSeed[seedsUsed++] = newSeed;
					sourceVar.setSampleFromSeed(newSeed);
					addSummariesToVariables();
				}
				sourceData.variableChanged(sourceVarKey);
			}
		};
	}
	
//------------------------------------------------------------
	
//	private boolean pendingClear = false;
//	private int pendingInvert = -1;
	
	public synchronized boolean clearSelection() {
//		pendingClear = true;
		return false;
	}

	public synchronized boolean setSelection(boolean newSelection[]) {
		boolean selectionChanged = super.setSelection(newSelection);
		if (selectionChanged)
			checkSourceData();
		
		return selectionChanged;
	}
	
	public synchronized boolean setSelection(int index) {
		boolean selectionChanged = super.setSelection(index);
		if (selectionChanged)
			checkSourceData();
		
		return selectionChanged;
	}
	
	public synchronized boolean setSelection(String key, double min, double max) {
		boolean selectionChanged = super.setSelection(key, min, max);
		if (selectionChanged)
			checkSourceData();
		
		return selectionChanged;
	}
	
	public synchronized boolean setSelection(Flags newSelection) {
		boolean selectionChanged = super.setSelection(newSelection);
		if (selectionChanged)
			checkSourceData();
		
		return selectionChanged;
	}
	
	protected void checkSourceData() {
		int flagSet = getSelection().findSingleSetFlag();
		if (flagSet >= 0) {
			CoreVariable sourceVar = sourceData.getVariable(sourceVarKey);
			if (sourceVar instanceof SampleInterface) {
				SampleInterface sv = (SampleInterface)sourceVar;
				boolean changedSource = sv.setSampleFromSeed(sourceSeed[flagSet]);
				if (changedSource)
					sourceData.variableChanged(sourceVarKey);
			}
		}
	}
	
	public void clearData() {
		Enumeration e = getVariableEnumeration();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof SummaryVariable) {
				SummaryVariable sumV = (SummaryVariable)v;
				sumV.clearSummaries();
//				variableChanged(getKey(sumV));
			}
		}
		getSelection().checkSize(0);
		seedsUsed = 0;
	}
}
