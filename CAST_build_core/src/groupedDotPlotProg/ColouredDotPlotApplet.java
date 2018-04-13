package groupedDotPlotProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import dotPlot.*;
import utils.*;
import valueList.OneValueView;
import random.RandomNormal;


public class ColouredDotPlotApplet extends XApplet implements GroupingAppletInterface {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final private String GROUP_EFFECT_PARAM = "groupEffect";
	
	protected DataView theDotPlot;
	
	private CatKey theKey;
	private XChoice jitterOrStack;
	
	private XCheckbox colourCheck;
	
	public void setupApplet() {
		DataSet data = createData();
		
		setLayout(new BorderLayout());
		add("Center", dotPlotPanel(data));
		
			XPanel keyPanel = infoPanel(data);
		if (keyPanel != null)
			add("East", keyPanel);
		
		LabelVariable lv = (LabelVariable)data.getVariable("label");
		if (lv != null)
			add("North", labelPanel(data));
		
		add("South", controlPanel());
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		synchronized (data) {
			data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
			
			String valuesParam = getParameter(VALUES_PARAM);
			if (valuesParam == null) {
				CatVariable group = data.getCatVariable();
				
				Vector groupEffect = new Vector(8);
				StringTokenizer theEffects = new StringTokenizer(getParameter(GROUP_EFFECT_PARAM));
				while (theEffects.hasMoreTokens()) {
					String nextItem = theEffects.nextToken();
					groupEffect.addElement(Double.valueOf(nextItem));
				}
				
				String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
				RandomNormal generator = new RandomNormal(randomInfo);
				double vals[] = generator.generate();
				
				for (int i=0 ; i<vals.length ; i++) {
					int groupIndex = group.getItemCategory(i);
					vals[i] += ((Double)groupEffect.elementAt(groupIndex)).doubleValue();
				}
				
				data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
			}
			else {
				data.addNumVariable("y", getParameter(VAR_NAME_PARAM), valuesParam);
				String labelVarName = getParameter(LABEL_NAME_PARAM);
				if (labelVarName != null)
					data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
			}
		}
		return data;
	}
	
	protected XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", theHorizAxis);
		
			theDotPlot = new ColouredDotPlotView(data, this, theHorizAxis);
			theDotPlot.lockBackground(Color.white);
			theDotPlot.setRetainLastSelection(true);
		thePanel.add("Center", theDotPlot);
		return thePanel;
	}
	
	protected XPanel infoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
		
//		thePanel.add("North", new OneValueView(data, "group", this));
		
		XPanel keyPanel = new XPanel();
		keyPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		theKey = new CatKey(data, "group", this, CatKey.VERT);
		keyPanel.add(theKey);
		thePanel.add(keyPanel);
		theKey.show(false);		//	private version of show() since hidden component is not laid out
			
		return thePanel;
	}
	
	protected XPanel labelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
	
	public void showGroups(boolean showNotHide) {
		((ColouredDotPlotView)theDotPlot).showGroups(showNotHide);
		theKey.show(showNotHide);
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 2));
			colourCheck = new XCheckbox(translate("Show groups"), this);
			colourCheck.setState(false);
		thePanel.add(colourCheck);
		
			jitterOrStack = new XChoice(this);
			jitterOrStack.addItem(translate("jittered"));
			jitterOrStack.addItem(translate("stacked"));
			jitterOrStack.select(1);
			((ColouredDotPlotView)theDotPlot).stackCrosses(true);
		thePanel.add(jitterOrStack);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == jitterOrStack) {
			((ColouredDotPlotView)theDotPlot).stackCrosses(jitterOrStack.getSelectedIndex() == 1);
			return true;
		}
		else if (target == colourCheck) {
			((ColouredDotPlotView)theDotPlot).showGroups(colourCheck.getState());
			theKey.show(colourCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}