package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import formula.*;

import distribution.*;


public class RectMaxDistnApplet extends CoreContinDistnApplet {
	static final private String N_LIMITS_PARAM = "nLimits";
	
	static final private double kLowDrawProb = 0.0001;
	
	private ParameterSlider nSlider;
	private XCheckbox normalCheck;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		NormalDistnVariable theDistn = new NormalDistnVariable("y");
		data.addVariable("normal", theDistn);
		data.setSelection("normal", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		return data;
	}
	
	protected ContinDistnVariable getDistn() {
		RectMaxDistnVariable theDistn = new RectMaxDistnVariable("x");
		return theDistn;
	}
	
	protected void setParamsFromSliders() {
		RectMaxDistnVariable maxVar = (RectMaxDistnVariable)data.getVariable("distn");
		int n = (int)Math.round(nSlider.getParameter().toDouble());
		maxVar.setN(n);
		
		NormalDistnVariable doubleMean = (NormalDistnVariable)data.getVariable("normal");
		doubleMean.setMean(1.0);
		double sd = 1.0 / Math.sqrt(3 * n);
		doubleMean.setSD(sd);
		
		pdfView.setSupport(maxVar.getQuantile(kLowDrawProb), 1.0);	//	to make max distn easier to draw
		
		pdfView.set2ndSupport(1.0 - 4 * sd, 1.0 + 4 * sd);					//	to make normal distn easier to draw
	}
	
	protected void setDistnSupport(ContinuousProbView pdfView) {
	}
	
	protected XPanel pdfPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		Vector axisLabels = xAxis.getLabels();
		axisLabels.removeAllElements();
		double zeroPos = (0.0 - xAxis.minOnAxis) / (xAxis.maxOnAxis - xAxis.minOnAxis);
		axisLabels.addElement(new AxisLabel(new NumValue(0, 0), zeroPos));
		double onePos = (1.0 - xAxis.minOnAxis) / (xAxis.maxOnAxis - xAxis.minOnAxis);
		axisLabels.addElement(new AxisLabel(new LabelValue(MText.expandText("#beta#")), onePos));
		thePanel.add("Bottom", xAxis);
		
		densityAxis = new VertAxis(applet);
		densityAxis.readNumLabels(getParameter(DENSITY_AXIS_PARAM));
		thePanel.add("Left", densityAxis);
		
		pdfView = new ContinuousProbView(data, applet, "distn", xAxis, densityAxis);
//		pdfView.addSecondDistn("normal");
		pdfView.lockBackground(Color.white);
		thePanel.add("Center", pdfView);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(20, 0));
			
			String nString = translate("Sample size") + ", n";
			
			StringTokenizer st = new StringTokenizer(getParameter(N_LIMITS_PARAM));
			NumValue nMin = new NumValue(st.nextToken());
			NumValue nMax = new NumValue(st.nextToken());
			NumValue nStart = new NumValue(st.nextToken());
			
			nSlider = new ParameterSlider(nMin, nMax, nStart, nString, this);
		thePanel.add("Center", nSlider);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				normalCheck = new XCheckbox(translate("Show moments estimator"), this);
			buttonPanel.add(normalCheck);
		
		thePanel.add("East", buttonPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == nSlider) {
			setParamsFromSliders();
			data.variableChanged("distn");
			return true;
		}
		else if (target == normalCheck) {
			pdfView.setSecondDistn(normalCheck.getState() ? "normal" : null);
			pdfView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}