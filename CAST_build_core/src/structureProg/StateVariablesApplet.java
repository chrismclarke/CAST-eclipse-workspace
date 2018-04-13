package structureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;

import structure.*;


public class StateVariablesApplet extends XApplet {
	static final private String VAR_KEYS_PARAM = "varKeys";
	static final private String X_COORDS_PARAM = "xCoords";
	static final private String Y_COORDS_PARAM = "yCoords";
	static final private String STATE_NAMES_PARAM = "stateNames";
	
	private String varKeys[];
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
			ScrollValueList theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			for (int i=0 ; i<varKeys.length ; i++)
				theList.addVariableToList(varKeys[i], ScrollValueList.RAW_VALUE);
			
			theList.setRetainLastSelection(true);
		add("Center", theList);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add("Center", new StateVariablesView(data, this, theList, varKeys, "stateInfo/"));
			
			USAStateView stateView = new USAStateView(data, this, "x", "y");
			stateView.lockBackground(Color.white);
			stateView.setRetainLastSelection(true);
			topPanel.add("West", stateView);
		add("North", topPanel);
	}
	
	private DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(VAR_KEYS_PARAM));
		int nVars = 1;
		while (st.hasMoreTokens()) {
			st.nextToken();
			nVars ++;
		}
		varKeys = new String[nVars];
		varKeys[0] = "state";
		st = new StringTokenizer(getParameter(VAR_KEYS_PARAM));
		nVars = 1;
		while (st.hasMoreTokens()) {
			varKeys[nVars] = st.nextToken();
			nVars ++;
		}
		
		DataSet data = new DataSet();
		
		data.addLabelVariable("state", "State", getParameter(STATE_NAMES_PARAM));
		data.addNumVariable("x", "xCoord", getParameter(X_COORDS_PARAM));
		data.addNumVariable("y", "yCoord", getParameter(Y_COORDS_PARAM));
		
		for (int i=1 ; i<varKeys.length ; i++)
			data.addNumVariable(varKeys[i], getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
		
		return data;
	}
}