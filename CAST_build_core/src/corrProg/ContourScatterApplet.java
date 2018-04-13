package corrProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import imageUtils.OneValueImageView;
import coreVariables.*;
import coreGraphics.*;
import imageUtils.*;

import corr.*;


public class ContourScatterApplet extends ScatterApplet {
	static final private String DATA_NAMES_PARAM = "dataNames";
	static final private String CORR_EQN_PARAM = "corrEqn";
	
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		NumVariable x = (NumVariable)data.getVariable("x");
		NumVariable y = (NumVariable)data.getVariable("y");
		ProductVariable xy = new ProductVariable(x.name + y.name, data, "x", "y");
		data.addVariable("xy", xy);
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new ContourScatterView(data, this, theHorizAxis, theVertAxis, "x", "y");
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		thePanel.add(new ImageCanvas("corr/zy.gif", this));
		return thePanel;
	}
	
	private CorrelationView getCorrelationView(DataSet data) {
		String corrTypeString = getParameter(CORR_EQN_PARAM);
		if (corrTypeString == null || corrTypeString.equals("sample"))
			return new CorrelationView(data, "x", "y", CorrelationView.DRAW_FORMULA, this);
		else
			return new Correlation2View(data, "x", "y", CorrelationView.DRAW_FORMULA, this);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL));
		
		XPanel xLabelPanel = new XPanel();
		xLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		xLabelPanel.add(new ImageCanvas("corr/zx.gif", this));
		thePanel.add(xLabelPanel);
		
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		topPanel.add(new OneValueImageView(data, "x", this, "corr/zxEquals.gif", 9));
		topPanel.add(new OneValueImageView(data, "y", this, "corr/zyEquals.gif", 9));
		topPanel.add(new OneValueImageView(data, "xy", this, "corr/zxzyEquals.gif", 9));
		thePanel.add(topPanel);
		
		XPanel midPanel = new XPanel();
		midPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		midPanel.add(getCorrelationView(data));
		thePanel.add(midPanel);
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		dataSetChoice = new XChoice(this);
		LabelEnumeration theValues = new LabelEnumeration(getParameter(DATA_NAMES_PARAM));
		while (theValues.hasMoreElements())
			dataSetChoice.addItem((String)theValues.nextElement());
		dataSetChoice.select(0);
		bottomPanel.add(dataSetChoice);
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newDataSet = dataSetChoice.getSelectedIndex();
			if (newDataSet != currentDataSet) {
				currentDataSet = newDataSet;
				NumVariable x = (NumVariable)data.getVariable("x");
				NumVariable y = (NumVariable)data.getVariable("y");
				String xDataParam = (newDataSet == 0) ? X_VALUES_PARAM : "x" + (newDataSet+1) + "Values";
				String yDataParam = (newDataSet == 0) ? Y_VALUES_PARAM : "y" + (newDataSet+1) + "Values";
				String xDataValues = getParameter(xDataParam);
				String yDataValues = getParameter(yDataParam);
				
				x.readValues(xDataValues);
				y.readValues(yDataValues);
				synchronized (data) {
					data.variableChanged("x");
					data.variableChanged("y");
				}
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