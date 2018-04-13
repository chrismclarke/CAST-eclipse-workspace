package ssq;

import java.awt.*;

import dataView.*;
import utils.*;



public class ComponentsPanel extends XPanel implements ComponentPanelInterface {
	private int componentType[];
	
	private ComponentPlotPanel componentPanel[];
	private ComponentPlotPanel currentSelectedComponent;
	
	private DataWithComponentView mainDataView;
	private ComponentEqnPanel equationPanel;
	
	public ComponentsPanel(DataSet data, String axisInfo, String[] componentName,
									String[] componentKey, Color[] componentColor, int[] componentType,
									int selectedIndex, DataWithComponentView mainDataView, 
									ComponentEqnPanel equationPanel, boolean showSD, XApplet applet) {
		this.mainDataView = mainDataView;
		this.equationPanel = equationPanel;
		this.componentType = componentType;
		componentPanel = new ComponentPlotPanel[componentKey.length];
		
		for (int i=0 ; i<componentKey.length ; i++)
			componentPanel[i] = createPanel(data, axisInfo, componentKey, componentName,
														componentColor, selectedIndex, showSD, applet, i);
		
		setLayout(new BorderLayout(0, 0));
		add("Center", createPanels(componentPanel, 0, applet));
		
		currentSelectedComponent = componentPanel[selectedIndex];
		
		repaint();
	}
	
	private XPanel createPanels(ComponentPlotPanel[] componentPanel, int firstIndex, XApplet applet) {
		if (firstIndex == componentPanel.length - 1)
			return componentPanel[firstIndex];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(1.0 / (componentPanel.length - firstIndex), 0));
		thePanel.add(ProportionLayout.LEFT, componentPanel[firstIndex]);
		thePanel.add(ProportionLayout.RIGHT, createPanels(componentPanel, firstIndex + 1, applet));
		
		return thePanel;
	}
	
	private ComponentPlotPanel createPanel(DataSet data, String axisInfo, String[] componentKey,
									String[] componentName, Color[] componentColor, int selectedIndex,
									boolean showSD, XApplet applet, int currentIndex) {
		return new ComponentPlotPanel(data, axisInfo, componentKey[currentIndex],
										componentColor[currentIndex], componentName[currentIndex],
										selectedIndex == currentIndex, showSD, ComponentPlotPanel.SHOW_HEADING, this, applet);
	}
	
	public void actionComponentSelected(Component c) {
		ComponentPlotPanel newSelection = (ComponentPlotPanel)c;
		
		if (currentSelectedComponent != newSelection) {
			currentSelectedComponent = newSelection;
			int selectedIndex = 0;
			for (int i=0 ; i<componentPanel.length ; i++) {
				componentPanel[i].setSelectedComponent(newSelection);
				if (componentPanel[i] == newSelection)
					selectedIndex = i;
			}
			mainDataView.changeComponentDisplay(componentType[selectedIndex]);
			if (equationPanel != null)
				equationPanel.highlightComponent(selectedIndex);
		}
	}
}