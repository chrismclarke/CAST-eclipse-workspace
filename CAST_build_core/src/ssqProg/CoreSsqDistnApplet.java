package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;
import models.*;
import imageUtils.*;

import ssq.*;


abstract public class CoreSsqDistnApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MAX_MSSQ_PARAM = "maxMeanSsq";
	static final private String MAX_F_PARAM = "maxF";
	static final private String SSQ_AXIS_PARAM = "ssqAxis";
	
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String SLOPE_LIMITS_PARAM = "slopeLimits";
	static final private String MAX_EFFECTS_PARAM = "maxEffects";
	static final private String DATA_INSET_PARAM = "dataInset";
	
	static final protected Color kRegnSsqColor = Color.red;
	static final protected Color kRegnMeanSsqColor = new Color(0xFFB0B9);
	static final protected Color kResidSsqColor = Color.blue;
	static final protected Color kResidMeanSsqColor = new Color(0x68A1FF);
	static final protected Color kTotalSsqColor = new Color(0x006300);;
	static final protected Color kTotalMeanSsqColor = new Color(0x01EF05);
	
	static final protected NumValue kMaxRSquared = new NumValue(1.0, 3);
	
//	static final private int kDotPlotHeight = 100;
	static final private int kEffectSteps = 100;
	
	protected CoreModelDataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMeanSsq, maxF;
	
	protected boolean xNumNotCat;
	
	private XCheckbox theoryCheck;
	private RepeatingButton sampleButton;
	
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	
	private ParameterSlider slopeSlider;
	protected double xPivot, yPivot;
	private XNoValueSlider effectSlider;
	private double maxEffect[];
	private double meanEffect;
	
	private DataWithComponentsPanel dataView;
	protected DataView ssqView;
	
	public void setupApplet() {
		readMaxes();
		
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(20, 0));
		
			dataView = dataPanel(data);
			
			String dataInsetString = getParameter(DATA_INSET_PARAM);
		if (dataInsetString == null)
			add("Center", dataView);
		else {
			int dataInset = Integer.parseInt(dataInsetString);
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new BorderLayout(dataInset,0));
			dataPanel.add("Center", dataView);
			dataPanel.add("West", new XPanel());
			dataPanel.add("East", new XPanel());
			
			add("Center", dataPanel);
		}
		
		add("East", controlPanel(data, summaryData));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			
				XPanel anovaTable = anovaTablePanel(summaryData);
			if (anovaTable != null)
				bottomPanel.add("North", anovaTable);
			bottomPanel.add("Center", ssqAndDistnPanel(summaryData));
		
		add("South", bottomPanel);
	}
	
	abstract protected int componentType();
	abstract protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData);
	
	abstract protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis);
	abstract protected Color getSsqColor();
	
	protected XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		return null;
	}
	
	
	private void readMaxes() {
		String s = getParameter(MAX_SSQ_PARAM);
		if (s !=  null)
			maxSsq = new NumValue(s);
		s = getParameter(MAX_MSSQ_PARAM);
		if (s !=  null)
			maxMeanSsq = new NumValue(s);
		s = getParameter(MAX_F_PARAM);
		if (s !=  null)
			maxF = new NumValue(s);
	}
	
	protected CoreModelDataSet readData() {
		CoreModelDataSet data;
		xNumNotCat = (getParameter(X_LABELS_PARAM) == null);
		if (xNumNotCat)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		if (maxMeanSsq != null && maxF != null)
			return new AnovaSummaryData(sourceData, "error", BasicComponentVariable.kComponentKey,
									maxSsq.decimals, kMaxRSquared.decimals, maxMeanSsq.decimals, maxF.decimals);
		else
			return new AnovaSummaryData(sourceData, "error", BasicComponentVariable.kComponentKey,
									maxSsq.decimals, kMaxRSquared.decimals);
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
							//		FILL so that slider will be laid out correctly
		return thePanel;
	}
	
//-------------------------------------------------------------------
	
	protected DataWithComponentsPanel dataPanel(DataSet data) {
		DataWithComponentsPanel dataView = new DataWithComponentsPanel(this);
		dataView.setupPanel(data, "x", "y", "ls", "model", componentType(), this);
		return dataView;
	}
	
	protected XPanel getSampleSizePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
		thePanel.add(new XLabel("n =", XLabel.LEFT, this));
			
			sampleSizeChoice = data.dataSetChoice(this);
		thePanel.add(sampleSizeChoice);
		
		return thePanel;
	}
	
	protected XPanel getSampleButton(String buttonText) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
			sampleButton = new RepeatingButton(buttonText, this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected XSlider getEffectSizeSlider(CoreModelDataSet data) {
		if (xNumNotCat) {
			StringTokenizer st = new StringTokenizer(getParameter(SLOPE_LIMITS_PARAM));
			NumValue slopeMin = new NumValue(st.nextToken());
			NumValue slopeMax = new NumValue(st.nextToken());
			NumValue slopeStart = new NumValue(st.nextToken());
			slopeSlider = new ParameterSlider(slopeMin, slopeMax, slopeStart, translate("Slope"), this);
			NumVariable xVar = (NumVariable)data.getVariable("x");
			ValueEnumeration xe = xVar.values();
			double sx = 0.0;
			while (xe.hasMoreValues())
				sx += xe.nextDouble();
			xPivot = sx / xVar.noOfValues();
			LinearModel yDistn = (LinearModel)data.getVariable("model");
			yPivot = yDistn.evaluateMean(xPivot);
			
			yDistn.setSlope(slopeStart);
			yDistn.setIntercept(yPivot - xPivot * slopeStart.toDouble());
			return slopeSlider;
		}
		else {
			StringTokenizer st = new StringTokenizer(getParameter(MAX_EFFECTS_PARAM));
			CatVariable xVar = (CatVariable)data.getVariable("x");
			int nx = xVar.noOfCategories();
			maxEffect = new double[nx];
			meanEffect = 0.0;
			for (int i=0 ; i<nx ; i++) {
				double effectI = Double.parseDouble(st.nextToken());
				maxEffect[i] = effectI;
				meanEffect += effectI;
			}
			meanEffect /= nx;
			if (nx == 2)
				effectSlider = new XNoValueSlider(translate("Same"), translate("Different"), translate("Population means"), 0, kEffectSteps, 0, this);
			else
				effectSlider = new XNoValueSlider(translate("None"), translate("Strong"), translate("Effect size"), 0, kEffectSteps, 0, this);
			GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
			for (int i=0 ; i<nx ; i++)
				yDistn.setMean(meanEffect, i);
			return effectSlider;
		}
	}
	
	
	protected OneValueImageView getSsqValueView(AnovaSummaryData summaryData, String ssqKey,
																													String ssqGif, Color ssqColor) {
		OneValueImageView ssqValueView = new OneValueImageView(summaryData, ssqKey, this, ssqGif, 12, maxSsq);
		ssqValueView.setHighlightSelection(false);
		ssqValueView.setForeground(ssqColor);
		return ssqValueView;
	}
	
	protected XCheckbox getTheoryCheck() {
		theoryCheck = new XCheckbox("Show theory", this);
		return theoryCheck;
	}
	
	protected XPanel ssqPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
			thePanel.setLayout(new FixedSizeLayout(150, 150));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis axis = new HorizAxis(this);
				axis.readNumLabels(getParameter(SSQ_AXIS_PARAM));
				axis.setForeground(getSsqColor());
				
			mainPanel.add("Bottom", axis);
			
				ssqView = getSsqDotView(summaryData, axis);
				ssqView.setForeground(getSsqColor());
				ssqView.lockBackground(Color.white);
				
			mainPanel.add("Center", ssqView);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		data.changeDataSet(newSizeIndex);
		data.variableChanged("x");
		summaryData.clearData();
		summaryData.takeSample();			//	can't use summaryData.setSingleSummaryFromData()
																	//	because random seed isn't set right in source data
	}
	
	protected void changeComponentDataDisplay(int newComponent) {
		dataView.getView().changeComponentDisplay(newComponent);
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == theoryCheck) {
			DataPlusDistnInterface ssqDistnView = (DataPlusDistnInterface)ssqView;
			ssqDistnView.setShowDensity(theoryCheck.getState() ? DataPlusDistnInterface.CONTIN_DISTN
																												: DataPlusDistnInterface.NO_DISTN);
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoiceIndex = sampleSizeChoice.getSelectedIndex();
			if (newChoiceIndex != currentSampleSizeIndex) {
				currentSampleSizeIndex = newChoiceIndex;
				changeSampleSize(newChoiceIndex);
			}
			return true;
		}
		else if (target == slopeSlider) {
			double newSlope = slopeSlider.getParameter().toDouble();
			LinearModel yDistn = (LinearModel)data.getVariable("model");
			yDistn.setSlope(newSlope);
			yDistn.setIntercept(yPivot - xPivot * newSlope);
			
			data.variableChanged("model");
			summaryData.setSingleSummaryFromData();
			return true;
		}
		else if (target == effectSlider) {
			double propn = effectSlider.getValue() / (double)kEffectSteps;
			
			CatVariable xVar = (CatVariable)data.getVariable("x");
			int nx = xVar.noOfCategories();
			GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
			for (int i=0 ; i<nx ; i++)
				yDistn.setMean(meanEffect + propn * (maxEffect[i] - meanEffect), i);
			
			data.variableChanged("model");
			summaryData.setSingleSummaryFromData();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}