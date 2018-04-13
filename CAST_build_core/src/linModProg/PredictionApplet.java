package linModProg;

import java.awt.*;
import java.util.*;

import axis.*;
import utils.*;
import dataView.*;
import coreGraphics.*;

import linMod.*;


public class PredictionApplet extends ScatterApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String MAX_PREDICT_PARAM = "maxPrediction";
	
	private XValueSlider xSlider;
	private NumValue maxPrediction, maxXLabel;
	
	private PredictionIntervalView theView;
	private PredictionCIView predictionView, ciView;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		NumValue minX = new NumValue(theParams.nextToken());
		NumValue maxX = new NumValue(theParams.nextToken());
		NumValue xStep = new NumValue(theParams.nextToken());
		maxXLabel = new NumValue(maxX.toDouble(), xStep.decimals);
		NumValue startX = new NumValue(theParams.nextToken());
		xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
		
		maxPrediction = new NumValue(getParameter(MAX_PREDICT_PARAM));
		super.setupApplet();
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
			xSlider.setForeground(Color.red);
			
		thePanel.add("Center", xSlider);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
				predictionView = new PredictionCIView(data, this, "y", "x", maxXLabel,
								maxPrediction, PredictionCIView.CONFIDENCE_INTERVAL, xSlider);
				predictionView.setFont(getBigFont());
			bottomPanel.add(predictionView);
				
				ciView = new PredictionCIView(data, this, "y", "x", maxXLabel,
								maxPrediction, PredictionCIView.PREDICTION_INTERVAL, xSlider);
				ciView.setFont(getBigFont());
			bottomPanel.add(ciView);
				
		thePanel.add("South", bottomPanel);
		return thePanel;
		
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new PredictionIntervalView(data, this, theHorizAxis, theVertAxis, "x", "y", xSlider);
		return theView;
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			theView.repaint();
			predictionView.redrawAll();
			ciView.redrawAll();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}