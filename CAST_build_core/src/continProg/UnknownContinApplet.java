package continProg;

import java.awt.*;

import dataView.*;

import contin.*;


public class UnknownContinApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	
	static final private String MAX_COUNT_PARAM = "maxCount";
	
	private DataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		add("North", modelPanel(data));
		add("Center", dataPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readLabels(getParameter(X_LABELS_PARAM));
		xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
		CatVariable yVar = new CatVariable(getParameter(Y_VAR_NAME_PARAM));
		yVar.readLabels(getParameter(Y_LABELS_PARAM));
		yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private XPanel modelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		UnknownJointView modelView = new UnknownJointView(data, this, "y", "x", 3);
		modelView.setForeground(Color.blue);
		thePanel.add(modelView);
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
		
		int maxCount = Integer.parseInt(getParameter(MAX_COUNT_PARAM));
		
		int leftDigits = 0;
		while (maxCount > 9) {
			leftDigits ++;
			maxCount /= 10;
		}
		
		thePanel.add(new DataJointView(data, this, "y", "x", -leftDigits));
		
		thePanel.add(new DataJointView(data, this, "y", "x", 3));
		
		return thePanel;
	}
}