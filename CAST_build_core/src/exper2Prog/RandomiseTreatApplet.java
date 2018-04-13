package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;
import formula.*;


public class RandomiseTreatApplet extends XApplet {
	static final private String NO_OF_DESIGNS_PARAM = "noOfDesigns";
	static final private String DESIGN_NAME_PARAM = "designName";
	static final private String TREATMENTS_PARAM = "treatments";
	static final private String TREATMENT_NAMES_PARAM = "treatmentNames";
	static final private String UNITS_PARAM = "unitTable";
	static final private String REP_TABLE_NAMES_PARAM = "repTableNames";
	static final private String REPLICATES_PARAM = "replicates";
	
	private DataSet data;
	
	private RandomiseTreatView theView;
	private ReplicateTable repTable;
	
	private XButton randomiseButton;
	private XChoice designChoice = null;
	private int currentDesign = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		
		int nDesigns = Integer.parseInt(getParameter(NO_OF_DESIGNS_PARAM));
		if (nDesigns > 1)
			add("North", designChoicePanel(nDesigns));
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		CatVariable xVar = new CatVariable("Treatments");
		xVar.readLabels(getParameter(TREATMENT_NAMES_PARAM));
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			LabelValue l = (LabelValue)xVar.getLabel(i);
			l.label = MText.expandText(l.label);
		}
		xVar.readValues(getParameter(TREATMENTS_PARAM));
		data.addVariable("x", xVar);
		
		return data;
	}
	
	private XPanel designChoicePanel(int nDesigns) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			designChoice = new XChoice(translate("Replicates"), XChoice.VERTICAL_CENTER, this);
			designChoice.addItem(getParameter(DESIGN_NAME_PARAM));
			for (int i=2 ; i<=nDesigns ; i++)
				designChoice.addItem(getParameter(DESIGN_NAME_PARAM + i));
		
		thePanel.add(designChoice);
		
			repTable = new ReplicateTable(getParameter(REP_TABLE_NAMES_PARAM),
																						getParameter(REPLICATES_PARAM));
		thePanel.add(repTable);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			randomiseButton = new XButton(translate("Randomise treatments"), this);
		
		thePanel.add(randomiseButton);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(UNITS_PARAM));
			int unitRows = Integer.parseInt(st.nextToken());
			int unitCols = Integer.parseInt(st.nextToken());
			theView = new RandomiseTreatView(data, this, "x", unitRows, unitCols);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == randomiseButton) {
			theView.animatePermutation();
			randomiseButton.setText(translate("Randomise again"));
			randomiseButton.invalidate();
			validate();
			return true;
		}
		else if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesign) {
				currentDesign = newChoice;
				CatVariable xVar = (CatVariable)data.getVariable("x");
				if (newChoice == 0) {
					xVar.readValues(getParameter(TREATMENTS_PARAM));
					repTable.setReplicates(getParameter(REPLICATES_PARAM));
				}
				else {
					xVar.readValues(getParameter(TREATMENTS_PARAM + (newChoice + 1)));
					repTable.setReplicates(getParameter(REPLICATES_PARAM + (newChoice + 1)));
				}
				theView.setFrame(0);
				randomiseButton.setText("Randomise treatments");
				randomiseButton.invalidate();
				validate();
				repTable.repaint();
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