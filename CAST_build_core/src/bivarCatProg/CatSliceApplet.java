package bivarCatProg;

import java.awt.*;

import dataView.*;
import utils.*;

import multivar.*;
import bivarCat.*;


public class CatSliceApplet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_LABELS_PARAM = "yLabels";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	private XCheckbox sliceCheck;
	private SlicedContinView theView;
	
	private XPanel slicePanel;
	private CardLayout slicePanelLayout;
	
	private SliceSlider sliceSlider;
	private XChoice sliceChoice;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		add("North", displayPanel(data));
		add("Center", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable xCatVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM), Variable.USES_REPEATS);
		xCatVariable.readLabels(getParameter(X_LABELS_PARAM));
		xCatVariable.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xCatVariable);
		
		CatVariable yCatVariable = new CatVariable(getParameter(Y_VAR_NAME_PARAM), Variable.USES_REPEATS);
		yCatVariable.readLabels(getParameter(Y_LABELS_PARAM));
		yCatVariable.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yCatVariable);
		
		CatVariable zCatVariable = new CatVariable(getParameter(Z_VAR_NAME_PARAM), Variable.USES_REPEATS);
		zCatVariable.readLabels(getParameter(Z_LABELS_PARAM));
		zCatVariable.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zCatVariable);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0));
			
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				sliceCheck = new XCheckbox(translate("Slice"), this);
			checkPanel.add(sliceCheck);
		
		thePanel.add(ProportionLayout.LEFT, checkPanel);
		
			CatVariable zVar = (CatVariable)data.getVariable("z");
			slicePanel = new XPanel();
			slicePanelLayout = new CardLayout();
			slicePanel.setLayout(slicePanelLayout);
			
			slicePanel.add("blank", new XPanel());
			
				XPanel sliceChoicePanel = new XPanel();
				if (zVar.noOfCategories() > 2) {
					sliceChoicePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
					String name[] = new String[zVar.noOfCategories()];
					for (int i=0 ; i<name.length ; i++)
						name[i] = zVar.getLabel(i).toString();
					sliceSlider = new SliceSlider(data.getVariable("z").name, 0, name.length-1, name, this);
					
					sliceSlider.setFont(getStandardBoldFont());
					sliceChoicePanel.add(sliceSlider);
				}
				else {
					sliceChoicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						sliceChoice = new XChoice(this);
						sliceChoice.addItem(zVar.getLabel(0).toString());
						sliceChoice.addItem(zVar.getLabel(1).toString());
					sliceChoicePanel.add(sliceChoice);
				}
			theView.setSlice(0);
			slicePanel.add("choice", sliceChoicePanel);
			
		thePanel.add(ProportionLayout.RIGHT, slicePanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
			theView = new SlicedContinView(data, this, "x", "y", "z", 3);
			theView.setFont(getBigFont());
		thePanel.add(theView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sliceCheck) {
			slicePanelLayout.show(slicePanel, sliceCheck.getState() ? "choice" : "blank");
				theView.setSlicing(sliceCheck.getState());
			return true;
		}
		else if (target == sliceSlider) {
			theView.setSlice(sliceSlider.getValue());
			return true;
		}
		else if (target == sliceChoice) {
			theView.setSlice(sliceChoice.getSelectedIndex());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}