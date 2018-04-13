package boxPlotProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import boxPlot.*;
import utils.*;
import random.RandomNormal;


public class RandomBoxDotApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private XButton takeSampleButton;
	private XChoice sampleSizeChoice;
	private RandomNormal generator;
	
	private DataSet data;
	
	int sampleSize[] = new int[3];
	
	public void setupApplet() {
		StringTokenizer theValues = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		for (int i=0 ; i<3 ; i++)
			sampleSize[i] = Integer.parseInt(theValues.nextToken());
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		generator.setSampleSize(sampleSize[0]);
		double vals[] = generateData();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		BoxAndDotView theView = new BoxAndDotView(data, this, theHorizAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		takeSampleButton = new XButton(translate("Another sample"), this);
		thePanel.add(takeSampleButton);
		
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<3 ; i++)
			sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
		sampleSizeChoice.select(0);
		thePanel.add(sampleSizeChoice);
		
		return thePanel;
	}
	
	private double[] generateData() {
		return generator.generate();
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			double vals[] = generateData();
			data.getNumVariable().setValues(vals);
			data.variableChanged("y");
			return true;
		}
		else if (target == sampleSizeChoice) {
			int sampSizeIndex = sampleSizeChoice.getSelectedIndex();
			generator.setSampleSize(sampleSize[sampSizeIndex]);
			double vals[] = generateData();
			data.getNumVariable().setValues(vals);
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}