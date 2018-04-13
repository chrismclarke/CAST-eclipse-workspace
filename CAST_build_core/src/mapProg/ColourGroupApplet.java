package mapProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import cat.*;
import map.*;


public class ColourGroupApplet extends XApplet {
	static final private String REGION_VAR_NAME_PARAM = "regionVarName";
	static final private String REGION_OUTLINES_PARAM = "regionOutlines";
//	static final private String Y_SCALE_EXTREMES_PARAM = "yScaleExtremes";
	
	static final private String NO_OF_SCALES_PARAM = "noOfScales";
	static final private String SCALE_PARAM = "scale";
//	static final private String SCALE_NAME_PARAM = "scaleName";
	
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kCatColours[][] =  {
															{Color.red, Color.blue},
															{Color.yellow, Color.red, Color.blue},
															{new Color(0xFF9900), new Color(0xCC0033), new Color(0x3300CC)},
															{Color.red, kDarkGreen, Color.darkGray, Color.yellow, Color.blue},
															{new Color(0xFF9900), new Color(0xDD4422), new Color(0xCC0033), new Color(0x880088), new Color(0x3300CC)}
															};
															
	static final private Color kNumColours[][] = {
															{Color.red, Color.blue},
															{Color.yellow, Color.red, Color.blue},
															{new Color(0xFF9900), new Color(0xCC0033), new Color(0x3300CC)}
															};
	
	private int noOfVars;
	private String[] yKey;
	private String[] scaleMinString, scaleMaxString;
	private double[] scaleMin, scaleMax;
	private Color[][] scaleColors;
	private String[] scaleName;
	
	private ShadedMapView theMap;
	private XChoice variableChoice;
	private int currentVarChoice;
	
	private XPanel keyPanel;
	private CardLayout keyPanelLayout;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mapPanel = new XPanel();
			mapPanel.setLayout(new MapLayout());
			
				theMap = getMap(data);
				theMap.lockBackground(Color.white);
			mapPanel.add("xxx", theMap);
			
		add("Center", mapPanel);
		
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RegionVariable regions = new RegionVariable(getParameter(REGION_VAR_NAME_PARAM));
			regions.readValues(getParameter(REGION_OUTLINES_PARAM));
		data.addVariable("region", regions);
		
		String labelNameString = getParameter(LABEL_NAME_PARAM);
		if (labelNameString != null)
			data.addLabelVariable("label", labelNameString, getParameter(LABELS_PARAM));
			
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		noOfVars = Integer.parseInt(getParameter(NO_OF_SCALES_PARAM));
		yKey = new String[noOfVars];
		scaleColors = new Color[noOfVars][];
		scaleName = new String[noOfVars];
		scaleMinString = new String[noOfVars];
		scaleMaxString = new String[noOfVars];
		scaleMin = new double[noOfVars];
		scaleMax = new double[noOfVars];
		
//		int yIndex = 0;
		for (int i=0 ; i<noOfVars ; i++) {
			StringTokenizer st0 = new StringTokenizer(getParameter(SCALE_PARAM + i), "#");
			scaleName[i] = st0.nextToken();
			StringTokenizer st = new StringTokenizer(st0.nextToken());
			
			String numCatString = st.nextToken();
			int scaleIndex = Integer.parseInt(st.nextToken());
			if (numCatString.equals("num")) {
				yKey[i] = "y";
				scaleColors[i] = kNumColours[scaleIndex];
				scaleMinString[i] = st.nextToken();
				scaleMaxString[i] = st.nextToken();
				scaleMin[i] = Double.parseDouble(scaleMinString[i]);
				scaleMax[i] = Double.parseDouble(scaleMaxString[i]);
			}
			else {
				scaleColors[i] = kCatColours[scaleIndex];
				int nBoundaries = scaleColors[i].length - 1;
				NumValue boundaries[] = new NumValue[nBoundaries];
				for (int j=0 ; j<nBoundaries ; j++)
					boundaries[j] = new NumValue(st.nextToken());
				
				String varKey = "y" + i;
				yKey[i] = varKey;
				GroupedVariable yGrouped = new GroupedVariable(varKey, data, "y", boundaries, false);
				data.addVariable(varKey, yGrouped);
			}
		}
		
		return data;
	}
	
	protected ShadedMapView getMap(DataSet data) {
		ShadedMapView mapView = new ShadedMapView(data, this, "region");
		setMapKey(mapView, 0);
		return mapView;
	}
	
	protected void setMapKey(ShadedMapView mapView, int keyIndex) {
		if (scaleMinString[keyIndex] == null)
			mapView.setCatDisplayKey(yKey[keyIndex], scaleColors[keyIndex]);
		else
			mapView.setNumDisplayKey(yKey[keyIndex], scaleMin[keyIndex], scaleMax[keyIndex], scaleColors[keyIndex]);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
				
				variableChoice = new XChoice(this);
				for (int i=0 ; i<noOfVars ; i++)
					variableChoice.addItem(scaleName[i]);
			
			leftPanel.add(variableChoice);
				
				OneValueView labelView = new OneValueView(data, "label", this);
				labelView.addEqualsSign();
			leftPanel.add(labelView);
				
				OneValueView yView = new OneValueView(data, "y", this);
				yView.addEqualsSign();
			leftPanel.add(yView);
		
		thePanel.add(leftPanel);
		
			keyPanel = new XPanel();
			keyPanelLayout = new CardLayout();
			keyPanel.setLayout(keyPanelLayout);
			for (int i=0 ; i<noOfVars ; i++) {
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				CoreVariable yVar = data.getVariable(yKey[i]);
				if (yVar instanceof CatVariable) {
					CatKey3View theKey = new CatKey3View(data, this, yKey[i]);
					theKey.setCatColour(scaleColors[i]);
					theKey.setShowHeading(false);
					innerPanel.add(theKey);
				}
				else {
					NumKeyView theKey = new NumKeyView(data, this, scaleMinString[i], scaleMaxString[i], scaleColors[i]);
					innerPanel.add(theKey);
				}
				keyPanel.add(String.valueOf(i), innerPanel);
			}
				
			keyPanelLayout.show(keyPanel, "0");
			
		thePanel.add(keyPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == variableChoice) {
			int newChoice = variableChoice.getSelectedIndex();
			if (newChoice != currentVarChoice) {
				currentVarChoice = newChoice;
				setMapKey(theMap, newChoice);
				keyPanelLayout.show(keyPanel, String.valueOf(newChoice));
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