package structureProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import imageUtils.*;

import structure.*;


public class GroupedValueApplet extends XApplet {
	private final static String NO_OF_COLS_PARAM = "noOfCols";
	private final static String GROUP_SIDE_PARAM = "groupSide";
	
	static final private Color kDarkRed = new Color(0x660000);
	static final private Color kDarkBlue = new Color(0x0000CC);
	static final private Color kPaleBlue = new Color(0xEEEEFF);
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		boolean groupsOnLeft = getParameter(GROUP_SIDE_PARAM).equals("left");
		
		DataSet data = getData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel listPanel = new XPanel();
			listPanel.setLayout(new BorderLayout(5, 0));
		
				ScrollValueList theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
				theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
				theList.addVariableToList("x", ScrollValueList.RAW_VALUE);
			
				theList.setRetainLastSelection(true);
			
			listPanel.add("Center", theList);
			
				XPanel arrowPanel = new XPanel();
				arrowPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				String arrowGif = "redArrowRight.png";
				arrowPanel.add(new ImageCanvas(arrowGif, this));
			
			listPanel.add(groupsOnLeft ? "West" : "East", arrowPanel);
		
		add(groupsOnLeft ? "East" : "West", listPanel);
		
		add("Center", groupPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("x", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																									getParameter(CAT_LABELS_PARAM));
		return data;
	}
	
	private XPanel groupPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yLabel = new XLabel(yVar.name, XLabel.CENTER, this);
			yLabel.setFont(getBigBoldFont());
			yLabel.setForeground(kDarkBlue);
		thePanel.add(yLabel);
		
		int noOfColumns = Integer.parseInt(getParameter(NO_OF_COLS_PARAM));
		
		CatVariable groupVar = (CatVariable)data.getVariable("x");
		int nGroups = groupVar.noOfCategories();
		for (int i=0 ; i<nGroups ; i++) {
			XPanel groupPanel = new InsetPanel(0, 0, 0, 7);
			groupPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			
			String groupName = groupVar.name + ": " + groupVar.getLabel(i).toString();
			XLabel groupLabel = new XLabel(groupName, XLabel.LEFT, this);
			groupLabel.setFont(getStandardBoldFont());
			groupLabel.setForeground(kDarkRed);
			groupPanel.add(groupLabel);
			
			ConditValuesView groupView = new ConditValuesView(data, this, "y", "x", i, noOfColumns);
			groupView.setRetainLastSelection(true);
			groupView.lockBackground(kPaleBlue);
			groupView.setForeground(kDarkRed);
			groupPanel.add(groupView);
			
			thePanel.add(groupPanel);
		}
		
		return thePanel;
	}
	
}