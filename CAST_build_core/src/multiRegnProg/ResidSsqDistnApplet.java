package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import distn.*;
import graphics3D.*;
import imageUtils.*;

import variance.*;
import multivarProg.*;
import multiRegn.*;


public class ResidSsqDistnApplet extends RotateApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String SSQ_AXIS_PARAM = "ssqAxis";
	static final private String MEAN_SSQ_AXIS_PARAM = "meanSsqAxis";
	
	static final private int kSsqPanelHeight = 170;
	static final private Color kDarkGreen = new Color(0x009900);
	
	protected MultiRegnDataSet data;
	protected AnovaSummaryData summaryData;
	
	private Model3ResidView theView;
	
	private XButton spinButton;
	private RepeatingButton sampleButton;
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	
	private NumValue maxSsq, maxMsq, maxRSqr, maxF;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		data.addBasicComponents();
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		return data;
	}
	
	private AnovaSummaryData getSummaryData(MultiRegnDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxRSqr = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
									BasicComponentVariable.kComponentKey, maxSsq.decimals, maxRSqr.decimals,
									maxMsq.decimals, maxF.decimals);
		
			
			GammaDistnVariable chiSquaredSsq = new GammaDistnVariable("residSsqDistn");
		summaryData.addVariable("residSsqDistn", chiSquaredSsq);
			GammaDistnVariable chiSquaredMsq = new GammaDistnVariable("residMsqDistn");
		summaryData.addVariable("residMsqDistn", chiSquaredMsq);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable("model");
		double errorSD = model.evaluateSD().toDouble();
		double errorVar = errorSD * errorSD;
		
		BasicComponentVariable residComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[2]);
		int residDF = residComponent.getDF();
		
		GammaDistnVariable chiSquaredSsq = (GammaDistnVariable)summaryData.getVariable("residSsqDistn");
		chiSquaredSsq.setScale(2.0 * errorVar);
		chiSquaredSsq.setShape(residDF * 0.5);
		
		GammaDistnVariable chiSquaredMsq = (GammaDistnVariable)summaryData.getVariable("residMsqDistn");
		chiSquaredMsq.setScale(2.0 * errorVar / residDF);
		chiSquaredMsq.setShape(residDF * 0.5);
		
		summaryData.variableChanged("residSsqDistn");
		summaryData.variableChanged("residMsqDistn");
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls", MultiRegnDataSet.xKeys, "y");
			theView.setShowSelectedArrows(false);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this));
			
				spinButton = new XButton(translate("Spin"), this);
			rotatePanel.add(spinButton);
		
		thePanel.add(rotatePanel);
		
			XPanel samplingPanel = new XPanel();
			samplingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
		
				XPanel sampleSizePanel = new XPanel();
				sampleSizePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
				
					XLabel sampleSizeLabel = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
					sampleSizeLabel.setFont(getStandardBoldFont());
				sampleSizePanel.add(sampleSizeLabel);
				
					sampleSizeChoice = ((MultiRegnDataSet)data).dataSetChoice(this);
				sampleSizePanel.add(sampleSizeChoice);
			
			samplingPanel.add(sampleSizePanel);
			
				XPanel samplePanel = new XPanel();
				samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
					sampleButton = new RepeatingButton(translate("Take sample"), this);
				samplePanel.add(sampleButton);
			
			samplingPanel.add(samplePanel);
			
		thePanel.add(samplingPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(999, kSsqPanelHeight));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.HORIZONTAL,
																											ProportionLayout.TOTAL));
			innerPanel.add(ProportionLayout.LEFT, ssqPanel(summaryData, "resid", "residSsqDistn",
																		maxSsq, Color.blue, getParameter(SSQ_AXIS_PARAM), 
																		"xEquals/residualSsqWords.png", 27, false));
			innerPanel.add(ProportionLayout.RIGHT, ssqPanel(summaryData, "m-resid", "residMsqDistn",
																		maxMsq, kDarkGreen, getParameter(MEAN_SSQ_AXIS_PARAM), 
																		"xEquals/meanResidSsq.png", 27, true));
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel ssqPanel(SummaryDataSet summaryData, String ssqKey, String ssqDistnKey,
										NumValue maxSsqValue, Color ssqColor, String axisInfo, 
										String ssqImageGif, int imageAscent, boolean isMeanSsq) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				OneValueImageView ssqValue = new OneValueImageView(summaryData, ssqKey, this,
																								ssqImageGif, imageAscent, maxSsqValue);
				ssqValue.setForeground(ssqColor);
			valuePanel.add(ssqValue);
		thePanel.add("North", valuePanel);
		
			XPanel distnPanel = new XPanel();
			distnPanel.setLayout(new AxisLayout());
			
				HorizAxis axis = new HorizAxis(this);
				axis.readNumLabels(axisInfo);
				axis.setForeground(ssqColor);
				
			distnPanel.add("Bottom", axis);
			
				ScaledChi2View ssqView = new ScaledChi2View(summaryData, this, axis, ssqDistnKey, maxSsq.decimals);
				ssqView.setActiveNumVariable(ssqKey);
				if (!isMeanSsq)
					ssqView.setIsMeanSumOfSquares(false);
				ssqView.setForeground(ssqColor);
				ssqView.lockBackground(Color.white);
				
			distnPanel.add("Center", ssqView);
		
		thePanel.add("Center", distnPanel);
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		data.changeDataSet(newSizeIndex);
		
		adjustDF(summaryData);
		data.variableChanged("y");
	}

	
	private boolean localAction(Object target) {
		if (target == spinButton) {
			theView.startAutoRotation();
			return true;
		}
		else if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoice;
				data.changeDataSet(newChoice);
				summaryData.clearData();
				adjustDF(summaryData);
				summaryData.takeSample();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}