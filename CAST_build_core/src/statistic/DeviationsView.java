package statistic;

import java.awt.*;

import dataView.*;
import valueList.*;


public class DeviationsView extends ValueView {
	static final public int MEAN_ABS_DEVN = 0;
	static final public int MEAN_SQR_DEVN = 1;
	static final public int ROOT_MEAN_SQR_DEVN = 2;
	
	static final private String kStartString = "=  ";
//	static final private int kMaxWait = 30000;		//		30 seconds
	
	protected String xKey;
	private NumValue maxValue;
	private DragCrossView devnView;
	private int summaryType;
	
	public DeviationsView(DataSet theData, XApplet applet,
													String xKey, NumValue maxValue, DragCrossView devnView) {
		super(theData, applet);
		this.xKey = xKey;
		this.maxValue = maxValue;
		this.devnView = devnView;
		summaryType = MEAN_ABS_DEVN;
	}
	
	public void setSummaryType(int summaryType) {
		this.summaryType = summaryType;
		repaint();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kStartString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable x = (NumVariable)getVariable(xKey);
		ValueEnumeration xe = x.values();
		
		double target = devnView.getTarget();
		
		double sum = 0.0;
		int nVals = 0;
		while (xe.hasMoreValues()) {
			double devn = xe.nextDouble() - target;
			if (devn < 0.0)
				devn = -devn;
			sum += (summaryType == MEAN_ABS_DEVN) ? devn : devn * devn;
			nVals++;
		}
		sum /= nVals;
		if (summaryType == ROOT_MEAN_SQR_DEVN)
			sum = Math.sqrt(sum);
		return (new NumValue(sum, maxValue.decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(kStartString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
