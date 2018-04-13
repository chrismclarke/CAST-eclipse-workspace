package corrProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreVariables.*;
import coreGraphics.*;

//import scatter.*;
import corr.*;
//import scatterProg.*;


class CurvatureSlider extends XSlider {
	public CurvatureSlider(XApplet applet) {
		super(applet.translate("linear"), applet.translate("curved"), applet.translate("Curvature"), 0, 100, 0, applet);
	}
	
	protected Value translateValue(int val) {
		return null;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return 0;
	}
	
	protected double getProportion() {
		return getProportion(getValue());
	}
	
	protected double getProportion(int val) {
		return val * 0.01;
	}
}

public class NonlinearApplet extends ScatterApplet {
	static final private String LIMIT_PARAM = "limits";
	
	private double p = -1.0;				//	to flag that it has not been initialised
	private int displayDecimals = 0;
	private double a0, b0, c0, a1, b1, c1;
	
	private QuadraticVariable yVariable;
	private CurvatureSlider curvatureSlider;
	
	public void setupApplet() {
		readLimits(getParameter(LIMIT_PARAM));
		super.setupApplet();
	}
	
	public void readLimits(String transformParam) {
		double x0 = 0.0;
		double y0 = 0.0;
		double x1 = 1.0;
		double y1 = 1.0;
		if (transformParam != null) 
			try{
				StringTokenizer limits = new StringTokenizer(transformParam);
				x0 = Double.parseDouble(limits.nextToken());
				y0 = Double.parseDouble(limits.nextToken());
				x1 = Double.parseDouble(limits.nextToken());
				y1 = Double.parseDouble(limits.nextToken());
				
				displayDecimals = Integer.parseInt(limits.nextToken());
			} catch (NumberFormatException e) {
			}
		double xMean = (x0 + x1) * 0.5;
		double k = 4.0 * (y1 - y0) / ((x0 - x1) * (x0 - x1));
		
		a0 = (y0*x1 - y1*x0) / (x1 - x0);
		b0 = (y1 - y0) / (x1 - x0);
		c0 = 0.0;
		a1 = y1 - k * xMean * xMean;
		b1 = 2.0 * k * xMean;
		c1 = -k;
	}
	
	private void setProportion(DataSet data, double p) {
		if (this.p != p) {
			this.p = p;
			double a = a0 * (1.0 - p) + a1 * p;
			double b = b0 * (1.0 - p) + b1 * p;
			double c = c0 * (1.0 - p) + c1 * p;
			yVariable.changeParameters(a, b, c);
			data.variableChanged("y");
		}
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		NumVariable xVariable = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xVariable.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVariable);
		
		yVariable = new QuadraticVariable(getParameter(Y_VAR_NAME_PARAM), xVariable,
																							0.0, 0.0, 0.0, displayDecimals);
		data.addVariable("y", yVariable);
		setProportion(data, 0.0);
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		CorrelationView theCorr = new CorrelationView(data, "x", "y", CorrelationView.NO_FORMULA, this);
			theCorr.setFont(getBigFont());
		
		thePanel.add(theCorr);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		curvatureSlider = new CurvatureSlider(this);
		thePanel.add("North", curvatureSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == curvatureSlider) {
			setProportion(data, curvatureSlider.getProportion());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}