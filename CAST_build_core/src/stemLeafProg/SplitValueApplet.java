package stemLeafProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import stemLeaf.*;


public class SplitValueApplet extends XApplet {
	static final private String LEAF_DECIMALS_PARAM = "leafDecimals";
	
	private DataSet data;
	private XNoValueSlider slider;
	private XChoice dataChoice;
	
	private int noOfVariables = 0;
	private int leafDecimals[];
	private SplitValueView fixedView, splitView;
	
	public void setupApplet() {
		DataSet data = readData();
		leafDecimals = new int[noOfVariables];
		StringTokenizer theValues = new StringTokenizer(getParameter(LEAF_DECIMALS_PARAM));
		for (int i=0 ; i<noOfVariables ; i++)
			leafDecimals[i] = Integer.parseInt(theValues.nextToken());
		
		setLayout(new BorderLayout());
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (noOfVariables > 1)
				topPanel.add(dataChoicePanel(data));
		
		add("North", topPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.35, 10));
			
				fixedView = new SplitValueView(data, this, "y0", leafDecimals[0], SplitValueView.FIXED);
				fixedView.lockBackground(Color.white);
			mainPanel.add(ProportionLayout.LEFT, fixedView);
			
				splitView = new SplitValueView(data, this, "y0", leafDecimals[0], SplitValueView.SPLITTING);
				splitView.lockBackground(Color.white);
			mainPanel.add(ProportionLayout.RIGHT, splitView);
			
		add("Center", mainPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.35, 10));
		
			bottomPanel.add(ProportionLayout.LEFT, new XPanel());
		
			slider = new XNoValueSlider(translate("Raw"), translate("Split"), null, 0, SplitValueView.kSplitFrame, 0, this);
			bottomPanel.add(ProportionLayout.RIGHT, slider);
		
		add("South", bottomPanel);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		while (true) {
			String name = getParameter(VAR_NAME_PARAM + noOfVariables);
			String values = getParameter(VALUES_PARAM + noOfVariables);
			if (name == null || values == null)
				break;
			data.addNumVariable("y" + noOfVariables, name, values);
			noOfVariables ++;
		}
		return data;
	}
	
	protected XPanel dataChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		dataChoice = new XChoice(this);
		for (int i=0 ; i<noOfVariables ; i++) {
			CoreVariable v = data.getVariable("y" + i);
			dataChoice.addItem(v.name);
		}
		dataChoice.select(0);
		thePanel.add(dataChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataChoice) {
			int dataIndex = dataChoice.getSelectedIndex();
			synchronized (data) {
				fixedView.setVariable("y" + dataIndex, leafDecimals[dataIndex]);
				splitView.setVariable("y" + dataIndex, leafDecimals[dataIndex]);
				slider.setValue(0);
			}
			return true;
		}
		else if (target == slider) {
			splitView.setFrame(slider.getValue());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}