package catProg;

import java.util.*;

import dataView.*;
import valueList.*;

import cat.*;


public class CreateContinTableApplet extends CreateFreqTableApplet {
	static final private String VARIABLES_PARAM = "variables";
	
	protected int noOfVariables, rowCatIndex, colCatIndex;
	
	protected CatValueScrollList theScrollList;
	
	protected DataSet readData() {
		StringTokenizer st = new StringTokenizer(getParameter(VARIABLES_PARAM));
		noOfVariables = Integer.parseInt(st.nextToken());
		rowCatIndex = Integer.parseInt(st.nextToken());
		colCatIndex = Integer.parseInt(st.nextToken());
		
		DataSet data = new DataSet();
		for (int i=1 ; i<=noOfVariables ; i++) {
			String labelsString = getParameter(CAT_LABELS_PARAM + i);
			if (labelsString == null)
				data.addNumVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
			else
				data.addCatVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i), labelsString);
		}
		return data;
	}
	
	protected CoreCreateTableView createTableView(DataSet data) {
		return new CreateContinTableView(data, this, "y" + (rowCatIndex + 1), "y" + (colCatIndex + 1));
	}
	
	protected XPanel valueListPanel(DataSet data, CoreCreateTableView freqTable) {
//		ScrollImages.loadScroll(this);
		
		theScrollList = new CatValueScrollList(data, this, ScrollValueList.HEADING, freqTable);
		
		for (int i=1 ; i<=noOfVariables ; i++)
			theScrollList.addVariableToList("y" + i, ScrollValueList.RAW_VALUE);
		theScrollList.setSelectedCols(rowCatIndex, colCatIndex);
		
		return theScrollList;
	}
	
	protected int numberCompleted() {
		return theScrollList.numberCompleted();
	}
	
	protected void doReset() {
		if (theScrollList != null)
			theScrollList.resetList();
		resetButton.disable();
		completeButton.disable();
	}
	
	protected void doCompleteTable() {
		if (theScrollList != null)
			theScrollList.completeTable();
		completeButton.disable();
	}
}