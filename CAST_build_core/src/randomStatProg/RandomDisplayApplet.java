package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;
import coreGraphics.*;

import histo.*;
import boxPlot.*;


public class RandomDisplayApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String SAMPLING_SEED_PARAM = "samplingSeed";
	static final private String CLASS_INFO_PARAM = "classInfo";
	
	private DataSet data;
	private XButton takeSampleButton;
	private XChoice sampleSizeChoice;
	private int currentSizeIndex;
	
	private StackedDotPlotView theDotPlot;
	
	private int[] sampleSize;		//		last entry is for all values
	private String[] sampleSizeString;
	
	public void setupApplet() {
		data = readLabelledData();
		
		readSampleSizes(data);
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", dataViewPanel(data));
		add("East", controlPanel(data));
		add("North", valuePanel(data));
		
		takeSample();
	}
	
	private void readSampleSizes(DataSet data) {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		sampleSizeString = new String[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			try {
				sampleSize[i] = Integer.parseInt(nextSize);
				sampleSizeString[i] = String.valueOf(sampleSize[i]);
			} catch (NumberFormatException e) {
				NumVariable y = data.getNumVariable();
				sampleSize[i] = y.noOfValues();
				sampleSizeString[i] = "All " + String.valueOf(sampleSize[i]);
			}
		}
	}
	
	private DataSet readLabelledData() {
		DataSet data = new DataSet();
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		y.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", y);
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		
		int noOfValues = y.noOfValues();
		FreqVariable f = new FreqVariable("frequency", noOfValues,
											Long.parseLong(getParameter(SAMPLING_SEED_PARAM)));
		int intVal[] = new int[noOfValues];
		f.setValues(intVal);
		data.addVariable("freq", f);
		
		return data;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel labelPanel = new XPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		labelPanel.add(new OneValueView(data, "label", this));
		labelPanel.add(new OneValueView(data, "y", this));
		return labelPanel;
	}
	
	private XPanel dataViewPanel(DataSet data) {
		XPanel panel1 = new XPanel();
		panel1.setLayout(new ProportionLayout(0.3, 0, ProportionLayout.VERTICAL));
		
		panel1.add(ProportionLayout.TOP, dotPlotPanel(data));
		
		XPanel panel2 = new XPanel();
		panel2.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL));
		
		panel2.add(ProportionLayout.TOP, histoPanel(data));
		panel2.add(ProportionLayout.BOTTOM, boxPlotPanel(data));
		
		panel1.add(ProportionLayout.BOTTOM, panel2);
		
		return panel1;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theDotPlot = new StackedDotPlotView(data, this, theHorizAxis, "freq", false);
		thePanel.add("Center", theDotPlot);
		theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel histoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		String classInfo = getParameter(CLASS_INFO_PARAM);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		double maxDensity = Double.parseDouble(theParams.nextToken());
		
		DensityAxis theDensityAxis = new DensityAxis(DensityAxis.NO_LABELS, maxDensity, classWidth,
																		data.getNumVariable().noOfValues(), this);
		theDensityAxis.show(false);
		thePanel.add("Left", theDensityAxis);
		
		HistoSampleView theHisto = new HistoSampleView(data, this, theHorizAxis, theDensityAxis,
																													class0Start, classWidth, "freq", false);
		
		thePanel.add("Center", theHisto);
		theHisto.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel boxPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		BoxSampleView theBoxPlot = new BoxSampleView(data, this, theHorizAxis, "freq", false);
		thePanel.add("Center", theBoxPlot);
		theBoxPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
		
		XPanel sizePanel = new XPanel();
		sizePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		sizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.LEFT, this));
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++)
			sampleSizeChoice.addItem(sampleSizeString[i]);
		currentSizeIndex = 0;
		sizePanel.add(sampleSizeChoice);
		
		controlPanel.add(sizePanel);
		
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
		takeSampleButton = new XButton(translate("Take sample"), this);
		samplePanel.add(takeSampleButton);
		
		controlPanel.add(samplePanel);
		
		return controlPanel;
	}
	
	private void takeSample() {
		int theSampleSize = sampleSize[currentSizeIndex];
		((FreqVariable)data.getVariable("freq")).sample(theSampleSize,
																			FreqVariable.WITHOUT_REPLACEMENT);
		theDotPlot.setCrossSize(		//		(theSampleSize > 200) ? DataView.DOT_CROSS :
										(theSampleSize > 50) ? DataView.SMALL_CROSS : DataView.MEDIUM_CROSS);
		data.variableChanged("freq");
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSizeIndex != currentSizeIndex) {
				currentSizeIndex = newSizeIndex;
				takeSample();
				if (newSizeIndex == sampleSize.length - 1)
					takeSampleButton.disable();
				else
					takeSampleButton.enable();
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}