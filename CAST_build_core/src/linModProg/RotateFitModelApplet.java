package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import linMod.*;


public class RotateFitModelApplet extends RotateHistoApplet {
	static final private String FIT_NAME_PARAM = "fitName";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String MAX_PARAM = "maxParams";
	
	private DataSet data;
	
	private XValueSlider xSlider;
	private PredictionEqnView predictedMean;
	
	private NumValue minX, maxX, xStep, startX;
	private NumValue intMax, slopeMax, sdMax, maxPrediction;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		minX = new NumValue(theParams.nextToken());
		maxX = new NumValue(theParams.nextToken());
		xStep = new NumValue(theParams.nextToken());
		startX = new NumValue(theParams.nextToken());
		
		StringTokenizer paramLimits = new StringTokenizer(getParameter(MAX_PARAM));
		intMax = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		sdMax = new NumValue(paramLimits.nextToken());
		maxPrediction = new NumValue(paramLimits.nextToken());
		
		xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = super.readData();
		
		StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		int intDecs = Integer.parseInt(st.nextToken());
		int slopeDecs = Integer.parseInt(st.nextToken());
		int sdDecs = Integer.parseInt(st.nextToken());
		
		LinearModel yDistn = new LinearModel(getParameter(FIT_NAME_PARAM), data, "x");
		yDistn.setLSParams("y", intDecs, slopeDecs, sdDecs);
		data.addVariable("model", yDistn);
		
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateDragXView theView =  new RotateDragXView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y", startX);
		theView.setModelDrawType(RotateDragXView.DRAW_SIMPLE_BAND_PDF);
		theView.setShowData(true);
		return theView;
	}
	
	protected int rotationPanelOrientation() {
		return RotateButton.VERTICAL;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(10, 0, 0, 0);
		thePanel.setLayout(new BorderLayout());
		
			thePanel.add("North", rotationPanel());
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																					VerticalLayout.VERT_SPACED, 3));
			
					xSlider.setForeground(Color.red);
				bottomPanel.add(xSlider);
				
				XPanel predictionPanel = new XPanel();
				predictionPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																					VerticalLayout.VERT_CENTER, 7));
						predictedMean = new PredictionEqnView(data, this, null, null, "model", intMax,
																														slopeMax, maxX, maxPrediction, xSlider);
						predictedMean.setEstimated(true);
						predictedMean.setForeground(Color.blue);
						predictedMean.setFont(getBigFont());
					predictionPanel.add(predictedMean);
					
						YSDView sdView = new YSDView(data, this, "model", sdMax);
						sdView.setEstimated(true);
						sdView.setFont(getBigFont());
					predictionPanel.add(sdView);
				bottomPanel.add(predictionPanel);
			
			thePanel.add("Center", bottomPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			NumValue newX = xSlider.getNumValue();
			((RotateDragXView)theView).setPDFDrawX(newX);
			theView.repaint();
			predictedMean.repaint();
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