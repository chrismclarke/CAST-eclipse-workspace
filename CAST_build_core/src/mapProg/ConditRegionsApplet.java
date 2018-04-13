package mapProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;

import map.*;


public class ConditRegionsApplet extends ColourRegionsApplet {
//	private final static String REGION_VAR_NAME_PARAM = "regionVarName";
	
	private boolean isCat[];
	private boolean isNum[];
	
//	private XChoice catVarChoice;
//	private int currentCatVarIndex = 0;
	
	private XChoice conditOption;
	private int currentConditOption = 0;
	private XChoice conditChoice;
	private int currentCondit = 0;
	
	private XPanel conditPanel;
	private CardLayout conditPanelLayout;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		isCat = new boolean[noOfVars];
		isNum = new boolean[noOfVars];
		
		for (int i=0 ; i<noOfVars ; i++) {
			isCat[i] = (data.getVariable(yKey[i]) instanceof CatVariable);
			isNum[i] = !isCat[i];
		}
		
		return data;
	}
	
	protected ShadedMapView getMap(DataSet data) {
		int initNumIndex = getVarIndex(0, isNum);
//		int initCatIndex = getVarIndex(0, isCat);
		
		ShadedMapView mapView = new ShadedMapView(data, this, "region", null, 0);
		setMapKey(mapView, initNumIndex);

		return mapView;
	}
	
	private int getVarIndex(int keyIndex, boolean[] isActive) {
		int initIndex = 0;
		for (int i=0 ; i<noOfVars ; i++)
			if (isActive[i]) {
				if (initIndex == keyIndex)
					return i;
				else
					initIndex ++;
			}
		return -1;
	}
	
	protected void setSelectedColumns(ScrollValueList theList) {
		theList.setSelectedCols(getVarIndex(0, isNum) + 1, getVarIndex(0, isCat) + 1);
	}
	
/*
	private XChoice getCatVariableChoice(DataSet data) {
		XChoice theChoice = new XChoice(this);
		for (int i=0 ; i<noOfVars ; i++)
			if (isCat[i])
				theChoice.addItem(data.getVariable(yKey[i]).name);
		return theChoice;
	}
*/
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
		thePanel.add(variableChoicePanel(data, yKey, isNum, yMinString, yMaxString));
		thePanel.add(conditCatPanel(data));
		
		return thePanel;
	}
	
	private XPanel conditCatPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
		
			String conditKey = yKey[getVarIndex(0, isCat)];
			CatVariable conditVar = (CatVariable)data.getVariable(conditKey);
			conditOption = new XChoice(this);
			conditOption.addItem("All values of " + conditVar.name);
			conditOption.addItem("Only for " + conditVar.name + "...");
		thePanel.add(conditOption);
		
			conditPanel = new XPanel();
			conditPanelLayout = new CardLayout();
			conditPanel.setLayout(conditPanelLayout);
			
			conditPanel.add("all", new XPanel());
				
				XPanel conditChoicePanel = new XPanel();
				conditChoicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
					conditChoice = new XChoice(this);
					for (int i=0 ; i<conditVar.noOfCategories() ; i++)
						conditChoice.addItem(conditVar.getLabel(i).toString());
			
				conditChoicePanel.add(conditChoice);
			
			conditPanel.add("condit", conditChoicePanel);
			conditPanelLayout.show(conditPanel, "all");
		
		thePanel.add(conditPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == variableChoice) {
			int newChoice = variableChoice.getSelectedIndex();
			if (newChoice != currentVarChoice) {
				currentVarChoice = newChoice;
				int numIndex = getVarIndex(newChoice, isNum);
				int catIndex = getVarIndex(0, isCat);
				setMapKey(theMap, numIndex);
				theList.setSelectedCols(numIndex + 1, catIndex + 1);
				keyPanelLayout.show(keyPanel, yKey[numIndex]);
			}
			return true;
		}
		else if (target == conditOption) {
			int newOption = conditOption.getSelectedIndex();
			if (newOption != currentConditOption) {
				currentConditOption = newOption;
				if (newOption == 0) {
					conditPanelLayout.show(conditPanel, "all");
					theMap.setCondit(null, -1);
				}
				else {
					conditPanelLayout.show(conditPanel, "condit");
					int catIndex = getVarIndex(0, isCat);
					theMap.setCondit(yKey[catIndex], currentCondit);
				}
			}
			return true;
		}
		else if (target == conditChoice) {
			int newChoice = conditChoice.getSelectedIndex();
			if (newChoice != currentCondit) {
				currentCondit = newChoice;
				int catIndex = getVarIndex(0, isCat);
				theMap.setCondit(yKey[catIndex], newChoice);
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