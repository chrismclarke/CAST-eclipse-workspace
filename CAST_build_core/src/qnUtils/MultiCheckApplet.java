package qnUtils;

import java.awt.*;

import dataView.*;
import utils.*;


abstract public class MultiCheckApplet extends CheckValueApplet {
	private XButton newDataButton;
	
	protected AxisGenerator axisGenerator;
	protected SampleSizeGenerator sampleSizeGenerator;
	
	abstract protected DataSet createData();
	abstract protected void readAccuracy();
	abstract protected void changeRandomParams(DataSet data);
	abstract protected String[] answerStrings(NumValue answer);
	
	protected DataSet readData() {
		axisGenerator = new AxisGenerator(this);
		sampleSizeGenerator = new SampleSizeGenerator(this);
		
		readAccuracy();
		
		DataSet data = createData();
		
		changeRandomParams(data);
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		newDataButton = new XButton("New Data Set", this);
		thePanel.add(newDataButton);
		return thePanel;
	}
	
	
	private boolean localAction(Object target) {
		if (target == newDataButton) {
			int oldAnswerType = valueEdit.getCurrentAnswer();
			changeRandomParams(getData());
			resetForNewData();
			setLabelText(valueLabel());
			int newAnswerType = valueEdit.getCurrentAnswer();
			if (oldAnswerType != newAnswerType)
				changeForNewAnswerType(newAnswerType);
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