package transform;

import java.util.*;

import dataView.*;
import axis.*;


public class LogAxis extends HorizAxis {
	public LogAxis(XApplet applet) {
		super(applet);
	}
	
	public void readExtremes(String labelInfo) {
		StringTokenizer theLabels = new StringTokenizer(labelInfo);
		
		try {
			if (!theLabels.hasMoreTokens())
				throw new AxisException(AxisException.FORMAT_ERROR);
			String minString = theLabels.nextToken();
			minOnAxis = Double.parseDouble(minString);
			
			if (!theLabels.hasMoreTokens())
				throw new AxisException(AxisException.FORMAT_ERROR);
			String maxString = theLabels.nextToken();
			maxOnAxis = Double.parseDouble(maxString);
			if (maxOnAxis <= minOnAxis)
				throw new AxisException(AxisException.FORMAT_ERROR);
			
			labels.removeAllElements();
			setPowerIndex(kZeroPowerIndex);
			setTransValueDisplay(TRANS_VALUES);
		} catch (Exception e) {
			throw new RuntimeException("bad axis specification");
		}
	}
	
	public void setTransValueDisplay(boolean rawNotTransValues) {
		this.rawNotTransValues = rawNotTransValues;
		labels.removeAllElements();
		
		int minWholeLog = (int)Math.round(minPower + 0.5);
		int maxWholeLog = (int)Math.round(maxPower - 0.5);
		
		for (int log=minWholeLog ; log<=maxWholeLog ; log++) {
			double position = (log - minPower) / powerRange;
			NumValue val = null;
			if (rawNotTransValues) {
				double labelVal = Math.pow(10.0, log);
				int decimals = Math.max(0, -log);
				val = new NumValue(labelVal, decimals);
			}
			else
				val = new NumValue(log, 0);
			addAxisLabel(val, position);
		}
		repaint();
	}
}