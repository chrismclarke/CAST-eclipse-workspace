package stemLeafProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import stemLeaf.*;


public class StemAndLeaf2Applet extends XApplet {
	static final private String DISCARD_DIGITS_PARAM = "discardDigits";
	static final private String DATA_NAMES_PARAM = "dataNames";
	
	private String[] dataNames = null;
	private DataSet data;
	
	private boolean hasLabels;
	private XChoice dataSetMenu;
	private int currentDataSet = 0;
	
	private StemAndLeafView theStemAndLeaf;
	private StemLeafValueView valView;
	private OneValueView labelView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		theStemAndLeaf = new StemAndLeafView(data, this, getParameter(STEM_AXIS_PARAM));
		theStemAndLeaf.setFont(getBigFont());
		theStemAndLeaf.setRetainLastSelection(true);
		theStemAndLeaf.lockBackground(Color.white);
		add("South", theStemAndLeaf);
		
		add("Center", valuePanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		int nDataSets = 1;
		String dataNameString = getParameter(DATA_NAMES_PARAM);
		if (dataNameString != null) {
			StringTokenizer st = new StringTokenizer(dataNameString, "#");
			nDataSets = st.countTokens();
			dataNames = new String[nDataSets];
			for (int i=0 ; i<dataNames.length ; i++)
				dataNames[i] = st.nextToken();
		}
		
		String suffix = "";
//		int index = 0;
		for (int i=0 ; i<nDataSets ; i++) {
			data.addNumVariable("y" + suffix, getParameter(VAR_NAME_PARAM + suffix), getParameter(VALUES_PARAM + suffix));
			
			String labelNameString = getParameter(LABEL_NAME_PARAM + suffix);
			String labelValString = getParameter(LABELS_PARAM + suffix);
			hasLabels = (labelNameString != null && labelValString != null);
			if (hasLabels)
				data.addLabelVariable("label" + suffix, labelNameString, labelValString);
			
			suffix = String.valueOf(i + 1);
		}
			
		return data;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		if (dataNames != null) {
			dataSetMenu = new XChoice(this);
			for (int i=0 ; i<dataNames.length ; i++)
				dataSetMenu.addItem(dataNames[i]);
			thePanel.add(dataSetMenu);
		}
		
		if (hasLabels) {
			labelView = new OneValueView(data, "label", this);
			labelView.setHighlightSelection(false);
			labelView.setFont(getBigFont());
			thePanel.add(labelView);
		}
			
			int discardDigits = Integer.parseInt(getParameter(DISCARD_DIGITS_PARAM));
			valView = new StemLeafValueView(data, "y", this, discardDigits);
			valView.setFont(getBigFont());
			
		thePanel.add(valView);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetMenu) {
			int newChoice = dataSetMenu.getSelectedIndex();
			if (newChoice != currentDataSet) {
				currentDataSet = newChoice;
				String suffix = (newChoice == 0) ? "" : String.valueOf(newChoice);
				String yKey = "y" + suffix;
				
				if (labelView != null)
					labelView.setVariableKey(yKey);
				int discardDigits = Integer.parseInt(getParameter(DISCARD_DIGITS_PARAM + suffix));
				valView.setDiscardChars(discardDigits);
				valView.setVariableKey(yKey);
				
				data.variableChanged(yKey);
				
				theStemAndLeaf.setActiveNumVariable(yKey);
//				String axisParam = STEM_AXIS_PARAM;
				theStemAndLeaf.resetAxis(getParameter(STEM_AXIS_PARAM + suffix));
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