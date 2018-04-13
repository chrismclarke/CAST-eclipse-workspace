package ssq;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;


public class ComponentPlotPanel extends XPanel implements ComponentPanelInterface {
	static final private String[] makeStringArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	static final private Color[] makeColorArray(Color c) {
		Color[] cArray = new Color[1];
		cArray[0] = c;
		return cArray;
	}
	
	static final public boolean SHOW_HEADING = true;
	static final public boolean NO_HEADING = false;
	
	static final public boolean SHOW_SD = true;
	static final public boolean NO_SD = false;
	
	static final public boolean SELECTED = true;
	static final public boolean NOT_SELECTED = false;
	
	private String componentKeys[];
	private String componentNames[];
	private Color componentColors[];
	
	private VertAxis axis;
	private ComponentPlotView componentView;
	private XLabel heading;
	
	private XPanel parentPanel;
	
	public ComponentPlotPanel(DataSet data, String axisInfo, String[] componentKeys,
										Color[] componentColors, String componentNamesString, boolean selected,
										boolean showSD, boolean showNameHeading, XPanel parentPanel,
										XApplet applet) {
		this.parentPanel = parentPanel;
		this.componentColors = componentColors;
		
		setComponentKeys(componentKeys, componentNamesString, 0);
		
		setLayout(new BorderLayout());
		
			XPanel dotPanel = new XPanel();
			dotPanel.setLayout(new AxisLayout());
			
				axis = new VertAxis(applet);
				axis.readNumLabels(axisInfo);
			dotPanel.add("Left", axis);
			
				componentView = new ComponentPlotView(data, applet, axis, 1.0, selected, this);
				componentView.setStickyDrag(true);
				componentView.lockBackground(Color.white);
				if (showSD)
					componentView.setShowSD(true);
			dotPanel.add("Center", componentView);
			
		add("Center", dotPanel);
		
		if (showNameHeading) {
			heading = new XLabel(componentNames[0], XLabel.LEFT, applet);
			heading.setFont(axis.getFont());
			add("North", heading);
		}
		
		setComponent(0);
		
		repaint();
	}
	
	public ComponentPlotPanel(DataSet data, String axisInfo, String componentKey,
										Color componentColor, String componentName, boolean selected, boolean showSD,
										boolean showNameHeading, XPanel parentPanel, XApplet applet) {
		this(data, axisInfo, makeStringArray(componentKey), makeColorArray(componentColor),
										componentName, selected, showSD, showNameHeading, parentPanel, applet);
	}
	
	public ComponentPlotView getView() {
		return componentView;
	}
	
	public VertAxis getAxis() {
		return axis;
	}
	
	public void setComponentKeys(String[] componentKeys, String componentNamesString,
																													int activeComponent) {
		this.componentKeys = componentKeys;
		StringTokenizer st = new StringTokenizer(componentNamesString, "#");
		int nameCount = st.countTokens();
		componentNames = new String[nameCount];
		for (int i=0 ; i<nameCount ; i++)
			componentNames[i] = st.nextToken();
		if (componentView != null) {
			componentView.setActiveNumVariable(componentKeys[activeComponent]);
			componentView.repaint();
		}
	}
	
	public void setComponent(int componentIndex) {
			Color c = componentColors[componentIndex];
			axis.setForeground(c);
			componentView.setForeground(c);
			componentView.setActiveNumVariable(componentKeys[componentIndex]);
			
			axis.repaint();
			componentView.repaint();
			
			if (heading != null) {
				heading.setForeground(c);
				heading.setText(componentNames[componentIndex]);
				heading.repaint();
			}
	}
	
	public void actionComponentSelected(Component c) {
		if (parentPanel instanceof ComponentPanelInterface)
			((ComponentPanelInterface)parentPanel).actionComponentSelected(this);
	}
	
	public void setSelectedComponent(ComponentPlotPanel selectedPanel) {
		componentView.setSelected(selectedPanel == this);
	}
	
	public XChoice createComponentChoice(XApplet applet) {
		XChoice componentChoice = new XChoice(applet);
		for (int i=0 ; i<componentNames.length ; i++)
			componentChoice.addItem(componentNames[i]);
		return componentChoice;
	}
}