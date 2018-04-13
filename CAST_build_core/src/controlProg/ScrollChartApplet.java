package controlProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;
import random.RandomNormal;


public class ScrollChartApplet extends ControlChartApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	
	private XButton extra1Button, extra5Button, allExtraButton;
	private RandomNormal generator;
	private int decimals = 5;
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		
		String decimalsString = getParameter(DECIMALS_PARAM);
		if (decimalsString != null)
			decimals = Integer.parseInt(decimalsString);
		
		NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
		yVar.setValues(vals);
		yVar.setDecimals(decimals);
		
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected XPanel createProblemView(DataSet data) {
		XPanel thePanel = super.createProblemView(data);
		theView.setFrame(data.getNumVariable().noOfValues());
		return thePanel;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
		
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		OneValueView valueView = new OneValueView(data, "y", this);
		valuePanel.add(valueView);
		controlPanel.add(valuePanel);
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		extra1Button = new RepeatingButton(translate("1 extra"), this);
		buttonPanel.add(extra1Button);
		extra5Button = new RepeatingButton(translate("5 extra"), this);
		buttonPanel.add(extra5Button);
		allExtraButton = new XButton(translate("All extra"), this);
		buttonPanel.add(allExtraButton);
		
		controlPanel.add(buttonPanel);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == extra1Button) {
			generator.setSampleSize(1);
			double vals[] = generator.generate();
			NumVariable yVar = data.getNumVariable();
			yVar.scrollAddValues(vals);
			yVar.setDecimals(decimals);
			synchronized (data) {
				data.variableChanged("y");
				data.setSelection(data.getNumVariable().noOfValues() - 1);
			}
			return true;
		}
		
		if (target == extra5Button) {
			generator.setSampleSize(5);
			double vals[] = generator.generate();
			NumVariable yVar = data.getNumVariable();
			yVar.scrollAddValues(vals);
			yVar.setDecimals(decimals);
			synchronized (data) {
				data.variableChanged("y");
				data.clearSelection();
			}
			return true;
		}
		
		if (target == allExtraButton) {
			generator.setSampleSize(data.getNumVariable().noOfValues());
			double vals[] = generator.generate();
			NumVariable yVar = data.getNumVariable();
			yVar.scrollAddValues(vals);
			yVar.setDecimals(decimals);
			synchronized (data) {
				data.variableChanged("y");
				data.clearSelection();
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