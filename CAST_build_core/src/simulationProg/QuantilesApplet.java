package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;
import simulation.*;

public class QuantilesApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String NORMAL_INFO_PARAM = "normalInfo";
	static final private String DATA_DECIMALS_PARAM = "decimals";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private XChoice sampleSizeChoice;
	
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	private DataSet data;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		data = createData();
		
		setLayout(new BorderLayout(15, 0));
			
		add("Center", displayPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 20));
			rightPanel.add("North", controlPanel());
				
				ScrollValueList quantileList = new ScrollValueList(data, this, ScrollValueList.HEADING);
				quantileList.addVariableToList("quantiles", ScrollValueList.RAW_VALUE);
			rightPanel.add("Center", quantileList);
		add("East", rightPanel);
	}
	
	protected int readSampleSizes() {
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
	
	private DataSet createData() {
		DataSet data = new DataSet();
		
		String normalParamString = getParameter(NORMAL_INFO_PARAM);
		NormalDistnVariable popnVar = new NormalDistnVariable(translate("Population"));
		popnVar.setParams(normalParamString);
		data.addVariable("distn", popnVar);
		
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		int initialSampleSize = readSampleSizes();
		QuantileFunctionVariable quantiles = new QuantileFunctionVariable(translate("Quantiles"), popnVar,
																			initialSampleSize, decimals);
		
		data.addVariable("quantiles", quantiles);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		thePanel.add("Bottom", horizAxis);
		
		StripedDensityView theView = new StripedDensityView(data, this, horizAxis, "distn", "quantiles");
		
		thePanel.add("Center", theView);
		
//		DotPlotView dataPlot = new DotPlotView(data, this, horizAxis, 1.0);
//		dataPlot.setActiveNumVariable("y");
//		thePanel.add("BottomMargin", dataPlot);
		
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
		thePanel.add(new XLabel(translate("Sample size") + ":", XLabel.CENTER, this));
			
			sampleSizeChoice = new XChoice(this);
			for (int i=0 ; i<sampleSize.length; i++)
				sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
			sampleSizeChoice.select(currentSizeIndex);
		
		thePanel.add(sampleSizeChoice);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newChoice) {
		currentSizeIndex = newChoice;
		int noOfValues = sampleSize[currentSizeIndex];
		
		QuantileFunctionVariable quantiles = (QuantileFunctionVariable)data.getVariable("quantiles");
		quantiles.setNoOfQuantiles(noOfValues);
		data.variableChanged("quantiles");
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else if (evt.target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex)
				changeSampleSize(newChoice);
			return true;
		}
		return false;
	}
}