package corr;

import java.awt.*;

import dataView.*;
import axis.*;


public class CorrelTransView extends CorrelationView {
//	static public final String CORREL_TRANS = "correlTrans";
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	public CorrelTransView(DataSet theData, String xKey, String yKey,
							boolean drawFormula, HorizAxis xAxis, VertAxis yAxis, XApplet applet) {
		super(theData, xKey, yKey, drawFormula, kDefaultDecimals, applet);
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	protected String getValueString() {
		NumVariable x = (NumVariable)getVariable(xKey);
		NumVariable y = (NumVariable)getVariable(yKey);
		ValueEnumeration xe = x.values();
		ValueEnumeration ye = y.values();
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;
		int nVals = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double xVal = xAxis.transform(xe.nextDouble());
			double yVal = yAxis.transform(ye.nextDouble());
			sx += xVal;
			sy += yVal;
			sxx += xVal * xVal;
			syy += yVal * yVal;
			sxy += xVal * yVal;
			nVals++;
		}
		double corr = (sxy - sx * sy / nVals)
										/ Math.sqrt((sxx - sx * sx / nVals) * (syy - sy * sy / nVals));
		return (new NumValue(corr, kDefaultDecimals)).toString();
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (theAxis == xAxis || theAxis == yAxis)
			repaint();
	}
}
