package mapProg;

import java.awt.*;

import dataView.*;
import utils.*;

import map.*;


public class CircleRegions2Applet extends CircleRegionsApplet {
	protected XChoice densityDisplayChoice;
	protected int currentDisplayChoice = 0;
	protected XChoice circleVarChoice;
	protected int currentCircleChoice = 0;
	
	protected XPanel circlePanel;
	protected CardLayout circlePanelLayout;
	
	protected DataSet getData() {
		DataSet data = super.getData();
			
		data.addNumVariable("area", getParameter(AREA_VAR_NAME_PARAM), getParameter(AREA_VALUES_PARAM));
		data.addNumVariable("popn", getParameter(POPN_VAR_NAME_PARAM), getParameter(POPN_VALUES_PARAM));
		
		return data;
	}
	
	protected ShadedCirclesMapView getMap(DataSet data) {
		ShadedCirclesMapView map = super.getMap(data);
		setHiddenCircles(map);
		return map;
	}
	
	protected XPanel displayTypePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
	
			String densityName = data.getVariable("density").name;
			XLabel choiceLabel = new XLabel("Display " + densityName + " in:", XLabel.LEFT, this);
			choiceLabel.setFont(getStandardBoldFont());
		thePanel.add(choiceLabel);
			
			densityDisplayChoice = new XChoice(this);
			densityDisplayChoice.addItem("Countries");
			densityDisplayChoice.addItem("Circles");
			
		thePanel.add(densityDisplayChoice);
		
		return thePanel;
	}
	
	protected XPanel circleTypePanel(DataSet data) {
		circlePanel = new XPanel();
		circlePanelLayout = new CardLayout();
		circlePanel.setLayout(circlePanelLayout);
			
		circlePanel.add("hide", new XPanel());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
				XPanel varPanel = new XPanel();
				varPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					XLabel varLabel = new XLabel("Circle size:", XLabel.LEFT, this);
					varLabel.setFont(getStandardBoldFont());
				varPanel.add(varLabel);
			
					circleVarChoice = new XChoice(this);
					circleVarChoice.addItem(data.getVariable("area").name);
					circleVarChoice.addItem(data.getVariable("popn").name);
					circleVarChoice.addItem(data.getVariable("size").name);
				varPanel.add(circleVarChoice);
				
			mainPanel.add("North", varPanel);
			
			mainPanel.add("Center", radiusPanel(data));
			
		circlePanel.add("show", mainPanel);
		
		return circlePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
			
		thePanel.add("West", displayTypePanel(data));
		
		thePanel.add("Center", circleTypePanel(data));
		
		return thePanel;
	}
	
	private void setHiddenCircles(ShadedCirclesMapView map) {
		map.setFixedCircleColor(null);
		map.setNumDisplayKey("density", minDensity, maxDensity, kFillColors);
		map.repaint();
	}
	
	protected void adjustCircles() {
		theMap.setNumVarCircleColor("density", minDensity, maxDensity, kFillColors);
		String sizeKey = (currentCircleChoice == 0) ? "area"
										: (currentCircleChoice == 1) ? "popn" : "size";
		theMap.setCircleSizeVariable(sizeKey, radiusSlider.getValue() * kMaxCircleRadius / 100);
	}
	
	private boolean localAction(Object target) {
		if (target == densityDisplayChoice) {
			int newChoice = densityDisplayChoice.getSelectedIndex();
			if (newChoice != currentDisplayChoice) {
				currentDisplayChoice = newChoice;
				
				if (newChoice == 0) {
					circlePanelLayout.show(circlePanel, "hide");
					setHiddenCircles(theMap);
				}
				else {
					circleVarChoice.select(0);
					currentCircleChoice = 0;
					circlePanelLayout.show(circlePanel, "show");
					theMap.setCatDisplayKey("colour", kCountryColours);
					adjustCircles();
				}
			}
			return true;
		}
		else if (target == circleVarChoice) {
			int newChoice = circleVarChoice.getSelectedIndex();
			if (newChoice != currentCircleChoice) {
				currentCircleChoice = newChoice;
				
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