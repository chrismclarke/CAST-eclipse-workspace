package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;


import exerciseEstim.*;


public class TestLookup extends XApplet {
	
	private XNumberEditPanel dfEdit;
	private XNumberEditPanel criticalValueEdit;
	private XButton changeDfButton;
	private XButton changeEditButton;
	
	private TLookupPanel lookupPanel;
	
	public void setupApplet() {
		setLayout(new BorderLayout(0, 0));
		
			lookupPanel = new TLookupPanel(this, TLookupPanel.PROB_LOOKUP, TLookupPanel.T_AND_NORMAL);
		add("Center", lookupPanel);
		
		add("South", controlPanel());
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				dfEdit = new XNumberEditPanel("df = ", "10", 3, this);
			topPanel.add(dfEdit);
			
				changeDfButton = new XButton("Change df", this);
			topPanel.add(changeDfButton);
		
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				criticalValueEdit = new XNumberEditPanel("editVal = ", "0.0", 3, this);
			bottomPanel.add(criticalValueEdit);
			
				changeEditButton = new XButton("Change edit", this);
			bottomPanel.add(changeEditButton);
		
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == changeDfButton) {
			double df = dfEdit.getDoubleValue();
			lookupPanel.setTDistnDf(df);
			return true;
		}
		else if (target == changeEditButton) {
			NumValue editVal = criticalValueEdit.getNumValue();
			lookupPanel.setTValue(editVal);
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}