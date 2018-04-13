package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import linMod.*;
import loess.*;


public class LogLogPIApplet extends LogLogLSApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String MAX_PREDICT_PARAM = "maxPrediction";
	static final private String PREDICT_NAME_PARAM = "predictName";
	
	private NumValue maxPrediction, maxX;
	
	private XValueSlider xSlider;
	private TransPIView theView, theTransView;
	private PIValueView piView;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		NumValue minX = new NumValue(theParams.nextToken());
		maxX = new NumValue(theParams.nextToken());
		NumValue xStep = new NumValue(theParams.nextToken());
		NumValue startX = new NumValue(theParams.nextToken());
		xSlider = new XValueSlider(minX, maxX, xStep, startX, XValueSlider.NO_SHOW_VALUES, this);
		
		maxPrediction = new NumValue(getParameter(MAX_PREDICT_PARAM));
		
		super.setupApplet();
	}
	
	protected double leftProportion() {
		return 0.6;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			theTransView = new TransPIView(data, this, theHorizAxis, theVertAxis, "x", "y", xSlider);
			return theTransView;
		}
		else {
			theView = new TransPIView(data, this, theHorizAxis, theVertAxis, "x", "y", xSlider);
			theView.setCalcAxes(xTransformAxis, yTransformAxis);
			return theView;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			XPanel centerPanel = new XPanel();
			centerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, 0));
			
				piView = new PIValueView(data, this, "x", maxX, maxPrediction, xSlider, theTransView,
																												getParameter(PREDICT_NAME_PARAM));
				piView.setForeground(TransPIView.kIntervalColor);
				piView.setFont(getStandardBoldFont());
			centerPanel.add(piView);
			
			centerPanel.add(xSlider);
			
		thePanel.add(centerPanel);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			theView.repaint();
			theTransView.repaint();
			piView.redrawAll();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}