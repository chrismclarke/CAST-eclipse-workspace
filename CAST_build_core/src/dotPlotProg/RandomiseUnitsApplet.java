package dotPlotProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import random.*;
import coreVariables.*;

import dotPlot.*;


public class RandomiseUnitsApplet extends XApplet {
	static final private String N_UNITS_PARAM = "nUnits";
	static final private String RANDOM_NAME_PARAM = "randomName";
	static final private String RANDOM_DECIMALS_PARAM = "randomDecimals";
	static final private String UNIT_NAME_PARAM = "unitName";
	
	static final private int kFramesPerSec = 40;
	
	private DataSet data;
	
	private XButton generateButton, sortButton;	
	private SortScrollContent theListContent;
	
	public void setupApplet() {
		
		data = getData();
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel randPanel = new XPanel();
			randPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
				SortScrollList theList = new SortScrollList(data, this, ScrollValueList.HEADING);
				theList.addVariableToList("rand", ScrollValueList.RAW_VALUE);
				theList.addVariableToList("unit", ScrollValueList.RAW_VALUE);
				theList.sortByVariable("rand", ScrollValueList.SMALL_FIRST);
				theListContent = (SortScrollContent)theList.getSortContent();
				theListContent.setColourRows(false);
				theList.setFont(getBigFont());
			
			randPanel.add(theList);
			
		add("Center", randPanel);
		
		add("East", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			int nUnits = Integer.parseInt(getParameter(N_UNITS_PARAM));
			int randDecimals = Integer.parseInt(getParameter(RANDOM_DECIMALS_PARAM));
			RandomRectangular generator = new RandomRectangular(nUnits, 0.0, 1.0);
			NumSampleVariable randVar = new NumSampleVariable(getParameter(RANDOM_NAME_PARAM), generator, randDecimals);
			randVar.generateNextSample();
		data.addVariable("rand", randVar);
		
		data.addVariable("unit", new IndexVariable(getParameter(UNIT_NAME_PARAM), nUnits));
		return data;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			generateButton = new XButton(translate("New random numbers"), this);
			
		thePanel.add(generateButton);
		
			sortButton = new XButton(translate("Sort random numbers"), this);
		thePanel.add(sortButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			NumSampleVariable randVar = (NumSampleVariable)data.getVariable("rand");
			randVar.generateNextSample();
			theListContent.setFrame(0);
			sortButton.enable();
			return true;
		}
		else if (target == sortButton) {
			theListContent.animateFrames(0, SortScrollContent.kSortedIndex, kFramesPerSec, null);
			sortButton.disable();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}