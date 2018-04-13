package percentile;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;


public class MultiDataSet extends DataSet {
	static final private String DATA_COUNT_PARAM = "noOfDataSets";
	
	static final private String DATA_NAME_PARAM = "dataName";
	static final private String VAR_NAME_PARAM = "varName";
	static final private String LONG_VAR_NAME_PARAM = "longVarName";
	static final private String VALUES_PARAM = "values";
	
	static final private String UNITS_PARAM = "units";
	
	static final private String DESCRIPTION_PARAM = "description";
	
	static final protected String HORIZ_AXIS_INFO_PARAM = "horizAxis";
	
	static final private String FIXED_VALUE_PARAM = "fixedValue";		//	for reference variable
	
	
	private String dataName[];
	private String yVarName[];
	private String longYVarName[];
	
	private String yValueString[];
	private String fixedValueString[];
	
	private String unitsString[];
	
	private String description[];
	
	protected int noOfDataSets;
	protected int currentDataSetIndex = 0;
	
	private MultiHorizAxis yAxis;
	private XTextArea descriptionArea;
	private DataSet referenceData;
	
	public MultiDataSet(XApplet applet) {
		String countString = applet.getParameter(DATA_COUNT_PARAM);
		noOfDataSets = (countString == null) ? 1 : Integer.parseInt(countString);
		
		dataName = readStrings(DATA_NAME_PARAM, applet, noOfDataSets);
		yVarName = readStrings(VAR_NAME_PARAM, applet, noOfDataSets);
		longYVarName = readStrings(LONG_VAR_NAME_PARAM, applet, noOfDataSets);
		
		yValueString = readStrings(VALUES_PARAM, applet, noOfDataSets);
		fixedValueString = readStrings(FIXED_VALUE_PARAM, applet, noOfDataSets);
		
		unitsString = readStrings(UNITS_PARAM, applet, noOfDataSets);
		
		description = readStrings(DESCRIPTION_PARAM, applet, noOfDataSets);
		
		addNumVariable("y", yVarName[0], yValueString[0]);
	}
	
	private String getSuffix(int dataIndex) {
		return (dataIndex == 0) ? "" : String.valueOf(dataIndex + 1);
	}
	
	private String[] readStrings(String param, XApplet applet, int noOfDataSets) {
		String result[] = null;
		if (applet.getParameter(param) != null) {
			result = new String[noOfDataSets];
			for (int i=0 ; i<noOfDataSets ; i++)
				result[i] = applet.getParameter(param + getSuffix(i));
		}
		return result;
	}
	
//---------------------------------------------------------------
	
	public XChoice getDataSetChoice(XApplet applet) {
		if (dataName.length <= 1)
			return null;
		XChoice theChoice = new XChoice(applet);
		for (int i=0 ; i<dataName.length ; i++)
			theChoice.addItem(dataName[i]);
		return theChoice;
	}
	
	public boolean changeDataSet(int dataIndex) {
		if (dataIndex != currentDataSetIndex) {
			currentDataSetIndex = dataIndex;
			
			NumVariable yVar = (NumVariable)getVariable("y");
			yVar.readValues(yValueString[dataIndex]);
			yVar.name = yVarName[dataIndex];
			
			if (descriptionArea != null)
				descriptionArea.setText(currentDataSetIndex);
			if (yAxis != null) {
				yAxis.setAlternateLabels(dataIndex);
				yAxis.setAxisName(longYVarName[dataIndex]);
				yAxis.repaint();
			}
			
			NumVariable refVar = (NumVariable)referenceData.getVariable("ref");
			refVar.setValueAt(new NumValue(fixedValueString[dataIndex]), 0);
			referenceData.valueChanged(0);
			
			variableChanged("y");
			return true;
		}
		else
			return false;
	}
	
	public MultiHorizAxis getHorizYAxis(XApplet applet) {
		yAxis = new MultiHorizAxis(applet, noOfDataSets);
		String yAxisInfo[] = readStrings(HORIZ_AXIS_INFO_PARAM, applet, noOfDataSets);
		yAxis.readNumLabels(yAxisInfo[0]);
		yAxis.setAxisName(longYVarName[0]);
		for (int i=1 ; i<noOfDataSets ; i++)
			yAxis.readExtraNumLabels(yAxisInfo[i]);
		yAxis.setChangeMinMax(true);
		return yAxis;
	}
	
	public XTextArea getDescription(int pixWidth, XApplet applet) {
		descriptionArea = new XTextArea(description, 0, pixWidth, applet);
		descriptionArea.lockBackground(Color.white);
		return descriptionArea;
	}
	
	public DataSet getReferenceData() {
		referenceData = new DataSet();
		referenceData.addNumVariable("ref", "Reference", fixedValueString[0]);
		referenceData.setSelection(0);
		return referenceData;
	}
	
	public String getUnitsString() {
		return unitsString[currentDataSetIndex];
	}
	
	public String[] getAllUnitsStrings() {
		return unitsString;
	}

}