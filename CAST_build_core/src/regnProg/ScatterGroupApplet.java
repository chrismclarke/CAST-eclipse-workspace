package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;

import regnView.*;


public class ScatterGroupApplet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Z_KEYS_PARAM = "zKeys";
	
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	
	static final private String kVarNameSuffix = "VarName";
	static final private String kValuesSuffix = "Values";
	static final private String kCatLabelsSuffix = "CatLabels";
	
	private String zKey[];
	private ScatterGroupsView theView;
	
	private XPanel catVarPanel;
	private CardLayout catVarPanelLayout;
	private XChoice catVarChoice;
	private int currentCatVarChoice = 0;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 3));
		add("Center", displayPanel(data));
		add("East", keyPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null)
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(Z_KEYS_PARAM));
		int nz = st.countTokens();
		zKey = new String[nz];
		for (int i=0 ; i<nz ; i++) {
			zKey[i] = st.nextToken();
			data.addCatVariable(zKey[i], getParameter(zKey[i] + kVarNameSuffix),
									getParameter(zKey[i] + kValuesSuffix), getParameter(zKey[i] + kCatLabelsSuffix));
		}
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				horizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			scatterPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			scatterPanel.add("Left", vertAxis);
			
				theView = new ScatterGroupsView(data, this, horizAxis, vertAxis, "x", "y");
				theView.lockBackground(Color.white);
				theView.setRetainLastSelection(true);
			scatterPanel.add("Center", theView);
		
		thePanel.add("Center", scatterPanel);
		
			XLabel yNameLabel = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yNameLabel.setFont(horizAxis.getFont());
		
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null) {
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			labelPanel.add(new XLabel(labelName, XLabel.LEFT, this));
			
				OneValueView labelView = new OneValueView(data, "label", this);
				labelView.setNameDraw(false);
			labelPanel.add(labelView);
			
			thePanel.add(labelPanel);
		}
			
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel catLabel = new XLabel(translate("Colours show") + ":", XLabel.CENTER, this);
				catLabel.setFont(getStandardBoldFont());
			labelPanel.add(catLabel);
			
				catVarChoice = new XChoice(this);
				for (int i=0 ; i<zKey.length ; i++)
					catVarChoice.addItem(data.getVariable(zKey[i]).name);
			labelPanel.add(catVarChoice);
			
		thePanel.add(labelPanel);
		
			catVarPanel = new XPanel();
			catVarPanelLayout = new CardLayout();
			catVarPanel.setLayout(catVarPanelLayout);
			
			for (int i=0 ; i<zKey.length ; i++) {
				XPanel oneKeyPanel = new XPanel();
				oneKeyPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				CatKey catKey = new CatKey(data, zKey[i], this, CatKey.VERT);
				catKey.setCanSelectGroups(true);
				oneKeyPanel.add(catKey);
				catVarPanel.add(zKey[i], oneKeyPanel);
			}
		
		thePanel.add(catVarPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == catVarChoice) {
			int newChoice = catVarChoice.getSelectedIndex();
			if (newChoice != currentCatVarChoice) {
				currentCatVarChoice = newChoice;
				
				catVarPanelLayout.show(catVarPanel, zKey[newChoice]);
				
				theView.setActiveCatVariable(zKey[newChoice]);
				theView.repaint();
				
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}