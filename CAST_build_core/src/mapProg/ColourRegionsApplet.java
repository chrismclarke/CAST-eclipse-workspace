package mapProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import cat.*;
import map.*;


public class ColourRegionsApplet extends XApplet {
	private final static String REGION_VAR_NAME_PARAM = "regionVarName";
	private final static String REGION_OUTLINES_PARAM = "regionOutlines";
	private final static String NO_OF_VAR_PARAM = "noOfVariables";
	private final static String MIN_PARAM = "minValue";
	private final static String MAX_PARAM = "maxValue";
	
	static final protected Color kCatColours[] = CatKey3View.kCatColour;
	static final protected Color kNumColours[] = {new Color(0xFF9900), new Color(0xCC0033), new Color(0x3300CC)};
	
	static final private int kListHeight = 200;
	
	protected int noOfVars;
	protected double yMin[], yMax[];
	protected String yMinString[], yMaxString[];
	protected String yKey[];
	
	protected ShadedMapView theMap;
	protected ScrollValueList theList;
	protected XChoice variableChoice;
	protected int currentVarChoice;
	
	protected XPanel keyPanel;
	protected CardLayout keyPanelLayout;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mapPanel = new XPanel();
			mapPanel.setLayout(new MapLayout());
			
				theMap = getMap(data);
//			mapPanel.add("xxx", theMap);
													//	It should work by placing theMap directly in mapPanel
													//	This works in appletViewer but initial display is offset
													//	into left border in Safari and Firefox
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new BorderLayout(0, 0));
					theMap.lockBackground(Color.white);
				innerPanel.add("Center", theMap);
					
			mapPanel.add("xxx", innerPanel);
			
		add("Center", mapPanel);
		
		add("East", controlPanel(data));
		add("South", dataTablePanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RegionVariable regions = new RegionVariable(getParameter(REGION_VAR_NAME_PARAM));
			regions.readValues(getParameter(REGION_OUTLINES_PARAM));
		data.addVariable("region", regions);
		
		String labelNameString = getParameter(LABEL_NAME_PARAM);
		if (labelNameString != null)
			data.addLabelVariable("label", labelNameString, getParameter(LABELS_PARAM));
		
		noOfVars = Integer.parseInt(getParameter(NO_OF_VAR_PARAM));
		yMin = new double[noOfVars];
		yMax = new double[noOfVars];
		yMinString = new String[noOfVars];
		yMaxString = new String[noOfVars];
		yKey = new String[noOfVars];
		for (int i=0 ; i<noOfVars ; i++) {
			yKey[i] = "y" + i;
			yMin[i] = yMax[i] = Double.NaN;
		}
		
		for (int i=0 ; i<noOfVars ; i++) {
			String labelsString = getParameter(CAT_LABELS_PARAM + i);
			if (labelsString == null) {
				data.addNumVariable(yKey[i], getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
				yMinString[i] = getParameter(MIN_PARAM + i);
				yMaxString[i] = getParameter(MAX_PARAM + i);
				StringTokenizer st = new StringTokenizer(yMinString[i]);	//	can have units after value
				yMin[i] = Double.parseDouble(st.nextToken());
				st = new StringTokenizer(yMaxString[i]);									//	can have units after value
				yMax[i] = Double.parseDouble(st.nextToken());
			}
			else
				data.addCatVariable(yKey[i], getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i),
																									labelsString);
		}
		
		return data;
	}
	
	protected ShadedMapView getMap(DataSet data) {
		ShadedMapView mapView = new ShadedMapView(data, this, "region");
		setMapKey(mapView, 0);
		return mapView;
	}
	
	protected void setMapKey(ShadedMapView mapView, int keyIndex) {
		if (Double.isNaN(yMin[keyIndex]))
			mapView.setCatDisplayKey(yKey[keyIndex], kCatColours);
		else
			mapView.setNumDisplayKey(yKey[keyIndex], yMin[keyIndex], yMax[keyIndex], kNumColours);
	}
	
	protected XPanel controlPanel(DataSet data) {
		return variableChoicePanel(data, yKey, null, yMinString, yMaxString);
	}
	
	protected XPanel variableChoicePanel(DataSet data, String[] yKey, boolean[] isActive,
																															String[] yMin, String[] yMax) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			variableChoice = new XChoice(this);
			for (int i=0 ; i<noOfVars ; i++)
				if (isActive == null || isActive[i])
					variableChoice.addItem(data.getVariable(yKey[i]).name);
		
		thePanel.add(variableChoice);
		
			keyPanel = new XPanel();
			keyPanelLayout = new CardLayout();
			keyPanel.setLayout(keyPanelLayout);
			for (int i=0 ; i<noOfVars ; i++)
				if (isActive == null || isActive[i]) {
					XPanel innerPanel = new XPanel();
					innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					CoreVariable yVar = data.getVariable(yKey[i]);
					if (yVar instanceof CatVariable) {
						CatKey3View theKey = new CatKey3View(data, this, yKey[i]);
						theKey.setShowHeading(false);
						innerPanel.add(theKey);
					}
					else {
						NumKeyView theKey = new NumKeyView(data, this, yMinString[i], yMaxString[i], kNumColours);
						innerPanel.add(theKey);
					}
					keyPanel.add(yKey[i], innerPanel);
				}
		
			int initialIndex = 0;
			if (isActive != null)
				for (int i=0 ; i<noOfVars ; i++)
					if (isActive[i]) {
						initialIndex = i;
						break;
					}
				
			keyPanelLayout.show(keyPanel, yKey[initialIndex]);
		thePanel.add(keyPanel);
		
		return thePanel;
	}
	
	private XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, kListHeight));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
				theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
				if (data.getVariable("label") != null)
					theList.addVariableToList("label", ScrollValueList.RAW_VALUE);
				
				for (int i=0 ; i<noOfVars ; i++)
					theList.addVariableToList("y" + i, ScrollValueList.RAW_VALUE);
			
				setSelectedColumns(theList);
				
			mainPanel.add("Center", theList);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	protected void setSelectedColumns(ScrollValueList theList) {
		theList.setSelectedCols(1, -1);
	}

	
	private boolean localAction(Object target) {
		if (target == variableChoice) {
			int newChoice = variableChoice.getSelectedIndex();
			if (newChoice != currentVarChoice) {
				currentVarChoice = newChoice;
				setMapKey(theMap, newChoice);
				theList.setSelectedCols(newChoice + 1, -1);
				keyPanelLayout.show(keyPanel, yKey[newChoice]);
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