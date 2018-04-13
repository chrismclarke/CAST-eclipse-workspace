package transform;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class Log10HorizAxis extends HorizAxis {
	static final public int SHOW_RAW = 0;
	static final public int SHOW_LOGS = 1;
	
	private int valueType;
	private Value rawLabel[];
	private Value logLabel[];
	
	public Log10HorizAxis(XApplet applet, int valueType) {
		super(applet);
		this.valueType = valueType;
	}
	
	public void readLogLabels(String labelInfo) {
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
				
			int logMinNumer = Integer.parseInt(theLabels.nextToken());
			int logMinDenom = Integer.parseInt(theLabels.nextToken());
			
			int oddSigDigits = 0;
			if (theLabels.hasMoreTokens())
				oddSigDigits = Integer.parseInt(theLabels.nextToken());
			
			labels.removeAllElements();
			setPowerIndex(kOnePowerIndex);
			
			int logNumer = logMinNumer;
			double logVal = logNumer / (double)logMinDenom;
			double labelVal = Math.pow(10.0, logVal);
			while (labelVal >= minOnAxis && labelVal <= maxOnAxis) {
				double position = (labelVal - minOnAxis) / (maxOnAxis - minOnAxis);
				int decimals;
				if (logNumer % logMinDenom == 0)
					decimals = Math.max(0, -logNumer / logMinDenom);
				else
					decimals = Math.max(0, oddSigDigits -logNumer / logMinDenom - 1);
				NumValue val = new NumValue(labelVal, decimals);
				addAxisLabel(val, position);
				
				logNumer ++;
				logVal = logNumer / (double)logMinDenom;
				labelVal = Math.pow(10.0, logVal);
			}
			
			if (valueType == SHOW_LOGS) {
				rawLabel = new Value[labels.size()];
				for (int i=0 ; i<rawLabel.length ; i++)
					rawLabel[i] = ((AxisLabel)labels.elementAt(i)).label;
				logLabel = new Value[labels.size()];
				int logDecimals = (logMinDenom == 1) ? 0 : 1;
				for (int i=0 ; i<rawLabel.length ; i++)
					logLabel[i] = new NumValue((logMinNumer + i) / (double)logMinDenom, logDecimals);
			}
		} catch (Exception e) {
			throw new RuntimeException("bad axis specification");
		}
	}
	
	public void corePaint(Graphics g) {
		if (valueType == SHOW_LOGS)
			for (int i=0 ; i<rawLabel.length ; i++)
				((AxisLabel)labels.elementAt(i)).label = logLabel[i];
		
		super.corePaint(g);
		
		if (valueType == SHOW_LOGS)
			for (int i=0 ; i<rawLabel.length ; i++)
				((AxisLabel)labels.elementAt(i)).label = rawLabel[i];
	}
	
}