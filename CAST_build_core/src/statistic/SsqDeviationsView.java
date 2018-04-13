package statistic;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class SsqDeviationsView extends ValueImageView {
	
	static final public int ROOT_MEAN_SSQ = 0;
	static final public int SUM_SSQ = 1;
	
	protected String xKey;
	private NumValue maxValue;
	private DataView dataCrossView;
	private int ssqType;
	
	private boolean popNotSamp = true;
	
	public SsqDeviationsView(DataSet theData, XApplet applet,
									String imageName, int imageAscent, String xKey, NumValue maxValue,
									DataView dataCrossView, int ssqType) {
		super(theData, applet, imageName, imageAscent);
		this.xKey = xKey;
		this.maxValue = maxValue;
		this.dataCrossView = dataCrossView;
		this.ssqType = ssqType;
	}
	
	public SsqDeviationsView(DataSet theData, XApplet applet, String imageName,
										int imageAscent, String xKey, NumValue maxValue, DataView dataCrossView) {
		this(theData, applet, imageName, imageAscent, xKey, maxValue, dataCrossView, ROOT_MEAN_SSQ);
	}
	
	public void setPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
		redrawValue();
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable x = (NumVariable)getVariable(xKey);
		ValueEnumeration xe = x.values();
		
		double target;
		if (dataCrossView instanceof DragCrossView)
			target = ((DragCrossView)dataCrossView).getTarget();
		else
			target = ((DragTargetView)dataCrossView).getTarget();
		
		double sum = 0.0;
		int nVals = 0;
		while (xe.hasMoreValues()) {
			double devn = xe.nextDouble() - target;
			sum += devn * devn;
			nVals++;
		}
		double result = (ssqType == ROOT_MEAN_SSQ) ? Math.sqrt(sum / (popNotSamp ? nVals : (nVals - 1))) : sum;
		return (new NumValue(result, maxValue.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
