package mapProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;

import map.*;


public class CirclePopnApplet extends CircleRegionsApplet {
	private XCheckbox circleCheck;
	private XChoice circleVarChoice;
	private int currentCircleVarIndex = 0;
	
	protected XPanel circlePanel;
	protected CardLayout circlePanelLayout;
	
	protected DataSet getData() {
		DataSet data = getMapData();
			
		data.addNumVariable("area", getParameter(AREA_VAR_NAME_PARAM), getParameter(AREA_VALUES_PARAM));
		data.addNumVariable("popn", getParameter(POPN_VAR_NAME_PARAM), getParameter(POPN_VALUES_PARAM));
		
		return data;
	}
	
	protected ShadedCirclesMapView getMap(DataSet data) {
		ShadedCirclesMapView mapView = new ShadedCirclesMapView(data, this, "region");
		mapView.setCatDisplayKey("colour", kCountryColours);
		
		mapView.setCircleSizeVariable(null, 0);
		mapView.setCatVarCircleColor("colour", kCountryColours);
		return mapView;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XLabel countryLabel = new XLabel(data.getVariable("label").name, XLabel.LEFT, this);
			countryLabel.setFont(getBigFont());
		thePanel.add(countryLabel);
			
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.setNameDraw(false);
			labelView.setFont(getBigFont());
		thePanel.add(labelView);
		
		return thePanel;
	}
	
	protected XPanel radiusPanel(DataSet data) {
		circlePanel = new XPanel();
		circlePanelLayout = new CardLayout();
		circlePanel.setLayout(circlePanelLayout);
			
		circlePanel.add("hide", new XPanel());
			
		circlePanel.add("show", super.radiusPanel(data));
		
			radiusSlider.setValue(100);
		
		return circlePanel;
	}
	
	private XPanel circleChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
			circleCheck = new XCheckbox(translate("Show circles"), this);
		thePanel.add(circleCheck);
			
			circleVarChoice = new XChoice(this);
			circleVarChoice.addItem(translate("Area") + " = " + data.getVariable("area").name);
			circleVarChoice.addItem(translate("Area") + " = " + data.getVariable("popn").name);
			circleVarChoice.disable();
		thePanel.add(circleVarChoice);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("West", circleChoicePanel(data));
		
		thePanel.add("Center", radiusPanel(data));
			
		thePanel.add("East", valuePanel(data));
		
		return thePanel;
	}
	
	protected void adjustCircles() {
		String sizeKey = (currentCircleVarIndex == 0) ? "area": "popn";
		theMap.setCircleSizeVariable(sizeKey, radiusSlider.getValue() * kMaxCircleRadius / 100);
	}
	
	private boolean localAction(Object target) {
		if (target == circleCheck) {
			if (circleCheck.getState()) {
				circleVarChoice.enable();
				circlePanelLayout.show(circlePanel, "show");
				adjustCircles();
			}
			else {
				circleVarChoice.disable();
				circlePanelLayout.show(circlePanel, "hide");
				theMap.setCircleSizeVariable(null, 0);
			}
			
			theMap.repaint();
			
			return true;
		}
		else if (target == circleVarChoice) {
			int newChoice = circleVarChoice.getSelectedIndex();
			if (newChoice != currentCircleVarIndex) {
				currentCircleVarIndex = newChoice;
				
				adjustCircles();
				theMap.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
	
}