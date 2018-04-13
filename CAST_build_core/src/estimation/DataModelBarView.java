package estimation;

import java.awt.*;

import dataView.*;
import axis.*;

import distribution.*;


public class DataModelBarView extends DiscreteProbView {
	static final private Color kCrossColor = new Color(0x990000);
	static final private Color kDimCrossColor = new Color(0xFF9999);
	
	private int n;
	private int freq[];
	private boolean dimCrosses = false;
	
	public DataModelBarView(DataSet theData, XApplet applet, String distnKey, String dataKey,
																								HorizAxis countAxis) {
		super(theData, applet, distnKey, null, countAxis, DiscreteProbView.NO_DRAG);
		
		NumVariable y = (NumVariable)getData().getVariable("y");
		double maxYDouble = 0.0;
		n = y.noOfValues();
		for (int i=0 ; i<n ; i++)
			maxYDouble = Math.max(maxYDouble, y.doubleValueAt(i));
		int maxY = (int)Math.round(maxYDouble);
		freq = new int[maxY + 1];
		for (int i=0 ; i<n ; i++) {
			int yVal = (int)Math.round(y.doubleValueAt(i));
			freq[yVal] ++;
		}
		int maxFreq = 0;
		for (int i=0 ; i<freq.length ; i++)
			maxFreq = Math.max(maxFreq, freq[i]);
		
		setCrossSize(maxFreq > 10 ? LARGE_CROSS : HUGE_CROSS);
		
		setForceZeroOneAxis(true);
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		g.setColor(dimCrosses ? kDimCrossColor : kCrossColor);
		Point crossCenter = null;
		for (int i=0 ; i<freq.length ; i++)
			try {
				int horizPos = countAxis.numValToPosition(i);
				for (int j=0 ; j<freq[i] ; j++) {
					int crossHeight = (getCrossPix() + 2) * (1 + 2 * j);
					crossCenter = translateToScreen(horizPos, crossHeight, crossCenter);
					drawBoldCross(g, crossCenter);
				}
			}catch (AxisException e) {
			}
	}
	
	public void setDimCrosses(boolean doDim) {
		dimCrosses = doDim;
		setDensityColor(doDim ? Color.black : kDistnColor);
		setWiderBars(doDim ? 3 : 0);
	}
}