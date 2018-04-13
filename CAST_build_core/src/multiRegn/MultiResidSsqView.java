package multiRegn;

import dataView.*;
import models.*;
import regn.*;


public class MultiResidSsqView extends ResidSsqView {
//	static public final String MULTI_RESID_SSQ = "multiResidSsq";
	
	private String[] xKey;
	private String modelKey;
	
	public MultiResidSsqView(DataSet theData, String yKey,
												String modelKey, NumValue biggestRss, XApplet applet) {
		super(theData, null, yKey, null, biggestRss, applet);
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		xKey = model.getXKey();
		this.modelKey = modelKey;
	}

//--------------------------------------------------------------------------------
	
	protected String getValueString() {
		NumVariable y = (NumVariable)getVariable(yKey);
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		ValueEnumeration xe[] = new ValueEnumeration[xKey.length];
		for (int i=0 ; i<xe.length ; i++)
			xe[i] = ((NumVariable)getVariable(xKey[i])).values();
		ValueEnumeration ye = y.values();
		double rss = 0.0;
		double xi[] = new double[xe.length];
		while (ye.hasMoreValues()) {
			for (int i=0 ; i<xe.length ; i++)
				xi[i] = xe[i].nextDouble();
			double yVal = ye.nextDouble();
			double resid = yVal - model.evaluateMean(xi);
			rss += resid * resid;
		}
		return (new NumValue(rss, biggestRss.decimals)).toString();
	}
}
