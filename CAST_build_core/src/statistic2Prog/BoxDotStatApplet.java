package statistic2Prog;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import statistic2.*;
import random.RandomSkewNormal;
import valueList.ProportionView;


public class BoxDotStatApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	HorizAxis theHorizAxis;
	BoxDotStatView theView;
	DataSet data;
	
	private RandomSkewNormal generator;
	private XButton takeSampleButton;
	private XChoice statChoice = null;
	protected XChoice distnChoice = null;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomSkewNormal(randomInfo);
		double vals[] = generateData();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		return data;
	}
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(10, 0));
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add("Center", displayPanel(data));
		dataPanel.add("South", proportionPanel(data));
		
		
		add("Center", dataPanel);
		add("East", controlPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		
		theView = new BoxDotStatView(data, this, theHorizAxis, decimals);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel distnChoicePanel(DataSet data) {
		XPanel distPanel = new XPanel();
		distPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, 0));
			XLabel distnLabel = new XLabel(translate("Distribution"), XLabel.LEFT, this);
			distnLabel.setFont(getStandardBoldFont());
		distPanel.add(distnLabel);
	
			distnChoice = new XChoice(this);
			distnChoice.addItem(translate("Symmetric"));
			distnChoice.addItem(translate("Fairly Skew"));
			distnChoice.addItem(translate("Very Skew"));
			distnChoice.select(0);
		distPanel.add(distnChoice);
		
		return distPanel;
	}
	
	protected XPanel subControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, 10));
		
			XPanel statPanel = new XPanel();
			statPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, 0));
				XLabel statLabel = new XLabel(translate("Statistic"), XLabel.LEFT, this);
				statLabel.setFont(getStandardBoldFont());
			statPanel.add(statLabel);
			
				statChoice = new XChoice(this);
				statChoice.addItem(SpreadCalculator.getName(0));
				statChoice.addItem(SpreadCalculator.getName(1));
				statChoice.select(0);
			statPanel.add(statChoice);
		thePanel.add(statPanel);
		
		thePanel.add(distnChoicePanel(data));
		return thePanel;
	}
	
	protected XPanel sampleButtonPanel(DataSet data) {
		XPanel sampPanel = new XPanel();
		sampPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			takeSampleButton = new XButton(translate("Another data set"), this);
		sampPanel.add(takeSampleButton);
		return sampPanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
		controlPanel.add(subControlPanel(data));
		controlPanel.add(sampleButtonPanel(data));
		
		return controlPanel;
	}
	
	protected XPanel proportionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
		
		ProportionView propView = new ProportionView(data, "y", this);
		propView.setFont(getStandardBoldFont());
		thePanel.add(propView);
		return thePanel;
	}
	
	private double[] generateData() {
		return generator.generate();
	}
	
	private void takeSample() {
		double vals[] = generateData();
		data.getNumVariable().setValues(vals);
		data.variableChanged("y");
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			takeSample();
			return true;
		}
		else if (target == statChoice) {
			int statIndex = statChoice.getSelectedIndex();
			theView.setSpreadStat(statIndex);
			return true;
		}
		else if (target == distnChoice) {
			int skewness = distnChoice.getSelectedIndex();
			double fraction = (skewness == 0) ? 1.0 : (skewness == 1) ? 0.5 : 0.25;
			generator.setPower(theHorizAxis.minOnAxis, theHorizAxis.maxOnAxis, fraction);
			takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}