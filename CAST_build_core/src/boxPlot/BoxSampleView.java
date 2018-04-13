package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class BoxSampleView extends BoxView {
//	static public final String BOX_SAMPLE_PLOT = "boxSamplePlot";
	
	protected String freqKey;
	protected boolean popNotSamp;
	
	public BoxSampleView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																											String freqKey, boolean popNotSamp) {
		super(theData, applet, theAxis);
		this.freqKey = freqKey;
		this.popNotSamp = popNotSamp;
	}
	
	protected NumValue[] getSortedValues(NumVariable variable) {
		NumValue popVal[] = variable.getSortedData();
		
		boolean useFreq = !popNotSamp && (freqKey != null);
		if (!useFreq)
			return popVal;
		else { 
			FreqVariable f = useFreq ? (FreqVariable)getVariable(freqKey) : null;
			int sortedIndex[] = getNumVariable().getSortedIndex();
			int noOfPopVals = popVal.length;
			int noOfSampleVals = 0;
			for (int i=0 ; i<noOfPopVals ; i++)
				if (((FreqValue)f.valueAt(i)).intValue > 0)
					noOfSampleVals ++;
			
			NumValue sampleVal[] = new NumValue[noOfSampleVals];
			int sampIndex = 0;
			for (int popIndex=0 ; popIndex<noOfPopVals ; popIndex++)
				if (((FreqValue)f.valueAt(sortedIndex[popIndex])).intValue > 0)
					sampleVal[sampIndex ++] = popVal[popIndex];
			
			return sampleVal;
		}
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(freqKey)) {
			initialised = false;
			repaint();
		}
		else
			super.doChangeVariable(g, key);
	}
}
	
