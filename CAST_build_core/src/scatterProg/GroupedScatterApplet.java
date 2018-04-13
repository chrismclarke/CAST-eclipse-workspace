package scatterProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;
import coreGraphics.*;

import scatter.*;
import groupedDotPlotProg.*;
import multivar.*;


public class GroupedScatterApplet extends ScatterApplet implements GroupingAppletInterface {
	static final private String RANDOM_JITTER_PARAM = "jitter";
									//		**** for the iris data, we need to jitter the crosses
									//		**** since there are so many superimposed
	static final private String HAS_CONTROLS_PARAM = "hasControls";
	static final private String SHOW_GROUPS_PARAM = "showGroups";
	
	private ColouredScatterView theView;
	private CatKey theKey;
	
	private XCheckbox colourCheck;
	
	private void jitterVariable(NumVariable variable, double step,
																	RandomRectangular jitterGenerator) {
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			nextVal.setValue(nextVal.toDouble() + jitterGenerator.generateOne() * step);
		}
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		String jitterString = getParameter(RANDOM_JITTER_PARAM);
		if (jitterString != null) {
			StringTokenizer st = new StringTokenizer(jitterString);
			double xStep = Double.parseDouble(st.nextToken());
			double yStep = Double.parseDouble(st.nextToken());
			long randomSeed = Long.parseLong(st.nextToken());
			
			RandomRectangular jitterGenerator = new RandomRectangular(1, -0.5, 0.5);
			jitterGenerator.setSeed(randomSeed);
			
			jitterVariable((NumVariable)data.getVariable("y"), yStep, jitterGenerator);
			jitterVariable((NumVariable)data.getVariable("x"), xStep, jitterGenerator);
		}
		
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		return data;
	}
	
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 4));
		
		theKey = new CatKey2(data, "group", this, CatKey2.VERT);
		theKey.setFont(getSmallFont());
		thePanel.add(theKey);
		theKey.show(false);		//	private version of show() since hidden component is not laid out
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		thePanel.add(super.controlPanel(data));
		
		String controlsString = getParameter(HAS_CONTROLS_PARAM);
		if (controlsString != null && controlsString.equals("true")) {
			String showGroupString = getParameter(SHOW_GROUPS_PARAM);
			if (showGroupString == null)
				showGroupString = "Show Groups";
			colourCheck = new XCheckbox(showGroupString, this);
			colourCheck.setState(false);
			thePanel.add(colourCheck);
		}
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ColouredScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "group");
		return theView;
	}
	
	public void showGroups(boolean showNotHide) {
		theView.showGroups(showNotHide);
		theKey.show(showNotHide);
	}

	
	private boolean localAction(Object target) {
		if (target == colourCheck) {
			showGroups(colourCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}