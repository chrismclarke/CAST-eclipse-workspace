package histo;

import java.awt.*;

import dataView.*;
import axis.*;


public class HistoSampleView extends HistoView {
//	static public final String HISTO_SAMPLE = "histoSample";
	
	protected String freqKey;
	protected boolean popNotSamp;
	
	public HistoSampleView(DataSet theData, XApplet applet,
							NumCatAxis valAxis, HistoDensityInfo densityAxis, double class0Start, 
							double classWidth, String freqKey, boolean popNotSamp) {
		super(theData, applet, valAxis, densityAxis, class0Start,  classWidth);
																//		no border under histo
		this.freqKey = freqKey;
		this.popNotSamp = popNotSamp;
	}

//-------------------------------------------------------------------
	
	protected int[] countClasses(double localClassStart[]) {
		int noOfClasses = localClassStart.length - 1;
		int localClassCount[] = new int[noOfClasses];
		
		boolean useFreq = !popNotSamp && (freqKey != null);
		FreqVariable f = useFreq ? (FreqVariable)getVariable(freqKey) : null;
		
		NumVariable theVariable = getNumVariable();
		NumValue sortedVals[] = theVariable.getSortedData();
		int sortedIndex[] = theVariable.getSortedIndex();
		int noOfPopVals = sortedVals.length;
		
		if (useFreq) {
			noOfVals = 0;
			for (int i=0 ; i<noOfPopVals ; i++)
				if (((FreqValue)f.valueAt(i)).intValue > 0)
					noOfVals ++;
		}
		else
			noOfVals = sortedVals.length;
		
		int index = 0;
		while (index < noOfVals && sortedVals[index].toDouble() < localClassStart[0])
			index++;
		tooLowCount = 0;		//		not used since we do not highlight selection
		
		int classIndex = 0;
		while (index < noOfPopVals) {
			if (sortedVals[index].toDouble() <= localClassStart[classIndex + 1]) {
				int freq = useFreq ? ((FreqValue)f.valueAt(sortedIndex[index])).intValue : 1;
				if (freq > 0)
					localClassCount[classIndex]++;
				index++;
			}
			else {
				classIndex++;
				if (classIndex >= noOfClasses)
					break;
			}
		}
		
		return localClassCount;
	}
	
	protected void paintOneClass(Graphics g, int classIndex, BarHeight lastBarHt,
						int lastClassEnd, int previousCount, BarHeight thisBarHt, int thisClassEnd,
						int theCount, int maxHt, Flags selection, int[] sortedIndex, int screen0Pos) {
		if (theCount > 0) {
			g.setColor(getHistoColor(classIndex));
			fillRect(lastClassEnd, thisClassEnd, thisBarHt, findBarHt(classIndex, 0, maxHt), g);
			
			g.setColor(Color.black);
			
			if (!thisBarHt.tooHigh) {
				Point lineStart = translateToScreen(lastClassEnd, thisBarHt.ht, null);
				Point lineEnd = translateToScreen(thisClassEnd, thisBarHt.ht, null);
				g.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
			}
		}
		
		int maxBarHt = Math.max(thisBarHt.ht, lastBarHt.ht);
		if (maxBarHt != 0) {
			Point barBottom = translateToScreen(lastClassEnd, 0, null);
			Point barTop = translateToScreen(lastClassEnd, maxBarHt, null);
			g.drawLine(barTop.x, barTop.y, barBottom.x, barBottom.y);
		}
		
		else if (lastBarHt.ht != thisBarHt.ht && (!thisBarHt.tooHigh || !lastBarHt.tooHigh)) {
			Point start = translateToScreen(lastClassEnd, lastBarHt.ht, null);
			Point end = translateToScreen(lastClassEnd, thisBarHt.ht, null);
			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}
	
	protected void drawHighlight(Graphics g) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
