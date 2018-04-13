package sampDesignProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import sampDesign.*;
import cat.*;


public class LawLargeNosApplet extends XApplet {
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String RANDOM_SEED_PARAM = "random";
	static final private String PROB_PARAM = "probability";
	
	static final private int kMaxSampleSize = 5000;
	static final private int kPropnDecimals = 4;
	
	private DataSet data;
	
	private ExpandingTimeAxis timeAxis;
	
	private RepeatingButton sampleButton, sample10Button;
	private XButton resetButton;
	
	private Random generator;
	private double successProb;
	
	private Value successVal, failureVal;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(20, 0));
		
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM));
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		failureVal = v.getLabel(0);
		successVal = v.getLabel(1);
		data.addVariable("x", v);
		
		successProb = Double.parseDouble(getParameter(PROB_PARAM));
		generator = new Random(Long.parseLong(getParameter(RANDOM_SEED_PARAM)));
		
		return data;
	}

//---------------------------------------------------------------------

	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable x = (CatVariable)data.getVariable("x");
		thePanel.add("North", new XLabel(translate("Proportion of") + " " + x.getLabel(1).toString(), XLabel.LEFT, this));
		
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				timeAxis = new ExpandingTimeAxis(this, 10, kMaxSampleSize);
				timeAxis.setAxisName(translate("Sample size"));
				
			graphPanel.add("Bottom", timeAxis);
			
			
				VertAxis theProbAxis = new VertAxis(this);
				String labelInfo = getParameter(PROB_AXIS_PARAM);
				theProbAxis.readNumLabels(labelInfo);
			graphPanel.add("Left", theProbAxis);
			
				ExpandingTimeView theView = new ExpandingTimeView(data, this, timeAxis, theProbAxis, "x");
			
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
		
		thePanel.add("Center", graphPanel);
		
		return thePanel;
	}

	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			FreqTableView theTable = new FreqTableView(data, this, "x", FreqTableView.NO_DRAG, kPropnDecimals);
			theTable.setRelFreqDisplay(FreqTableView.PROPN);
			theTable.setFont(getBigFont());
		
		thePanel.add("West", theTable);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				XPanel samplePanel = new XPanel();
				samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
				
						sampleButton = new RepeatingButton(translate("Find new value"), this);
				samplePanel.add(sampleButton);
						sample10Button = new RepeatingButton(translate("Find 10 values"), this);
				samplePanel.add(sample10Button);
				
			rightPanel.add(samplePanel);
			
				XPanel resetPanel = new XPanel();
				resetPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
					resetButton = new XButton(translate("Reset"), this);
				resetPanel.add(resetButton);
				
			rightPanel.add(resetPanel);
		
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	private void clearSample() {
		CatVariable x = (CatVariable)data.getVariable("x");
		x.clearData();
		
		timeAxis.setNoOfValues(0);
		data.variableChanged("x");
		
		sampleButton.enable();
		sample10Button.enable();
	}
	
	private void takeSample(int sampleSize) {
		CatVariable x = (CatVariable)data.getVariable("x");
		
		int oldCount = x.noOfValues();
		
		for (int i=0 ; i<sampleSize ; i++) {
			Value nextCat = (generator.nextDouble() < successProb) ? successVal : failureVal;
			x.addValue(nextCat);
			if (oldCount + i + 1 >= kMaxSampleSize) {
				sampleButton.disable();
				sample10Button.disable();
				break;
			}
		}
		
		timeAxis.setNoOfValues(x.noOfValues());
		data.variableChanged("x");
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			if (sampleButton.isEnabled())
				takeSample(1);
			return true;
		}
		else if (target == sample10Button) {
			if (sample10Button.isEnabled())
				takeSample(10);
			return true;
		}
		else if (target == resetButton) {
			clearSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}