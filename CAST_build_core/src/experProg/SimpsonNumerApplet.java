package experProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;
import exper.*;


public class SimpsonNumerApplet extends XApplet {
	static final private String SLICE_MEAN_PARAM = "sliceMean";
	static final private String OVERALL_PARAM = "overallMean";
	
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String Z_CAT_NAME_PARAM = "zCatVarName";
	static final private String Z_CAT_VALUES_PARAM = "zCatValues";
	static final private String Z_CAT_LABELS_PARAM = "zCatLabels";
	
	static final private String INITIAL_DISPLAY_PARAM = "initialDisplay";
	
	private SimpsonNumerView theDotPlot;
	
	private XChoice pooledChoice;
	private int currentChoice;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		CatVariable x = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		x.readLabels(getParameter(CAT_LABELS_PARAM));
		x.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("x", x);
		
		CatVariable z = new CatVariable(getParameter(Z_CAT_NAME_PARAM), Variable.USES_REPEATS);
		z.readLabels(getParameter(Z_CAT_LABELS_PARAM));
		z.readValues(getParameter(Z_CAT_VALUES_PARAM));
		data.addVariable("z", z);
			
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis groupAxis = new VertAxis(this);
			CatVariable groupingVariable = (CatVariable)data.getVariable("x");
			groupAxis.setCatLabels(groupingVariable);
		thePanel.add("Left", groupAxis);
			
			theDotPlot = new SimpsonNumerView(data, this, theHorizAxis, groupAxis, getParameter(SLICE_MEAN_PARAM),
									new LabelValue(getParameter(OVERALL_PARAM)), "x", "z", SimpsonNumerView.GROUP_MODE);
			theDotPlot.lockBackground(Color.white);
									
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				pooledChoice = new XChoice(this);
				pooledChoice.addItem("Overall mean");
				pooledChoice.addItem("Mean in each " + data.getVariable("z").name);
				
				String initDisplayString = getParameter(INITIAL_DISPLAY_PARAM);
				currentChoice = 0;
				if (initDisplayString != null)
					currentChoice = Integer.parseInt(initDisplayString);
				if (currentChoice != 0) {
					pooledChoice.select(currentChoice);
					theDotPlot.setShowSubMeans(true);
				}
			choicePanel.add(pooledChoice);
		
		thePanel.add(ProportionLayout.LEFT, choicePanel);
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				CatKey3View theKey = new CatKey3View(data, this, "z");
				theKey.setFillNotCross(false);
				theKey.setKeyFillColor(Color.white);
			keyPanel.add(theKey);
		
		thePanel.add(ProportionLayout.RIGHT, keyPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pooledChoice) {
			int newChoice = pooledChoice.getSelectedIndex();
			if (currentChoice != newChoice) {
				currentChoice = newChoice;
				theDotPlot.setShowSubMeans(newChoice != 0);
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