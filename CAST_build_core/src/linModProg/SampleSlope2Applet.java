package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;

import linMod.*;


class ShapeSlider extends XNoValueSlider {
	public ShapeSlider(XApplet applet) {
		super(applet.translate("Small"), applet.translate("Large"), applet.translate("X variability"), 0, 100, 50, applet);
	}
	
	protected double getProportion() {
		return getProportion(getValue());
	}
	
	protected double getProportion(int val) {
		return (50 - val) * 0.02;
	}
}


class ErrorSDSlider extends XNoValueSlider {
	static private int startValue(String limitString) {
		StringTokenizer st = new StringTokenizer(limitString);
		NumValue minSD = new NumValue(st.nextToken());
		NumValue maxSD = new NumValue(st.nextToken());
		NumValue startSD = new NumValue(st.nextToken());
		return (int)Math.round(50 * (startSD.toDouble() - minSD.toDouble())
																		/ (maxSD.toDouble() - minSD.toDouble()));
	}
	
	private NumValue minSD, maxSD;
	
	public ErrorSDSlider(XApplet applet, String limitString) {
		super(applet.translate("Small"), applet.translate("Large"), applet.translate("Response st devn"),
																														0, 50, startValue(limitString), applet);
		StringTokenizer st = new StringTokenizer(limitString);
		minSD = new NumValue(st.nextToken());
		maxSD = new NumValue(st.nextToken());
	}
	
	protected double getSD() {
		return getSD(getValue());
	}
	
	protected double getSD(int val) {
		return minSD.toDouble() + val * 0.02 * (maxSD.toDouble() - minSD.toDouble());
	}
}


public class SampleSlope2Applet extends SampleSlopeApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String SD_LIMITS_PARAM = "sdLimits";
	
	static final private Color kSamplingBackground = new Color(0xEEEEDD);
	
	private XChoice sampleSizeChoice;
	private ErrorSDSlider errorSlider;
	private ShapeSlider xShapeSlider;
	
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	protected int getValueCount(DataSet data) {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		return sampleSize[currentSizeIndex];
	}
	
	protected int initialTheoryDisplay() {
		return DataPlusDistnInterface.CONTIN_DISTN;
	}
	
	protected DataPlusDistnInterface getDotDistnView(DataSet data, String paramKey, String theoryKey,
																											HorizAxis horizAxis) {
		return new JitterPlusNormalView(data, this, horizAxis, theoryKey, 1.0);
	}
	
	protected XPanel samplingControlPanel(DataSet summaryData, int topInset, int bottomInset) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XPanel samplingPanel = new InsetPanel(0, 10, 0, 0);
			samplingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				XPanel innerPanel = new InsetPanel(20, 0);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					XPanel actualPanel = super.samplingControlPanel(summaryData, 10, 10);
				innerPanel.add(actualPanel);
			
				innerPanel.lockBackground(kSamplingBackground);
			samplingPanel.add(innerPanel);
			
		thePanel.add("Center", samplingPanel);
		thePanel.add("South", adjustmentPanel());
		
		return thePanel;
	}
	
	private XPanel adjustmentPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 5));
		
		XPanel sampleSizePanel = new XPanel();
		sampleSizePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			sampleSizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.LEFT, this));
			sampleSizeChoice = new XChoice(this);
			for (int i=0 ; i<sampleSize.length; i++)
				sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
			sampleSizeChoice.select(currentSizeIndex);
			
			sampleSizePanel.add(sampleSizeChoice);
		
		thePanel.add("West", sampleSizePanel);
		
		XPanel sliderPanel = new XPanel();
		sliderPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
			errorSlider = new ErrorSDSlider(this, getParameter(SD_LIMITS_PARAM));
			sliderPanel.add(ProportionLayout.LEFT, errorSlider);
			
			xShapeSlider = new ShapeSlider(this);
			sliderPanel.add(ProportionLayout.RIGHT, xShapeSlider);
			
		thePanel.add("Center", sliderPanel);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newChoice) {
		currentSizeIndex = newChoice;
		int noOfValues = sampleSize[currentSizeIndex];
		summaryData.changeSampleSize(noOfValues);
		setTheoryParams(data, summaryData);
		repaintSummaries();
	}
	
	private boolean localAction(Object target) {
		if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex)
				changeSampleSize(newChoice);
			return true;
		}
		else if (target == xShapeSlider) {
			NumClusterVariable x = (NumClusterVariable)data.getVariable("x");
			x.setClustering(xShapeSlider.getProportion());
			data.variableChanged("x");
			setTheoryParams(data, summaryData);
			summaryData.setSingleSummaryFromData();
			return true;
		}
		else if (target == errorSlider) {
			LinearModel model = (LinearModel)data.getVariable("model");
			model.setSD(errorSlider.getSD());
			data.variableChanged("error");
			setTheoryParams(data, summaryData);
			summaryData.setSingleSummaryFromData();
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