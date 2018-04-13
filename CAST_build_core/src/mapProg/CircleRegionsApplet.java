package mapProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import map.*;


public class CircleRegionsApplet extends XApplet {
	static final private String REGION_VAR_NAME_PARAM = "regionVarName";
	static final private String REGION_OUTLINES_PARAM = "regionOutlines";
	static final private String REGION_COLORS_PARAM = "regionColors";
	static final protected String SIZE_VAR_NAME_PARAM = "sizeVarName";
	static final protected String SIZE_VALUES_PARAM = "sizeValues";
	static final protected String DENSITY_VAR_NAME_PARAM = "densityVarName";
	static final protected String DENSITY_VALUES_PARAM = "densityValues";
	static final private String DENSITY_EXTREMES_PARAM = "densityExtremes";
	
	static final protected String AREA_VAR_NAME_PARAM = "areaVarName";
	static final protected String AREA_VALUES_PARAM = "areaValues";
	static final protected String POPN_VAR_NAME_PARAM = "popnVarName";
	static final protected String POPN_VALUES_PARAM = "popnValues";
	
	static final protected int kMaxCircleRadius = 60;
	static final protected Color kCountryColours[] = {new Color(0xFBC242), new Color(0xA8866F), new Color(0x7ABE53), new Color(0xFEF789),
																									new Color(0x8BCCB1), new Color(0x7FADC5), new Color(0xF5A482), new Color(0x37AF8A),
																									new Color(0xCBDC4C), new Color(0x006600)};
	
	static final protected Color kFillColors[] = {new Color(0xFF9900), new Color(0xCC0033), new Color(0x3300CC)};
	
	protected DataSet data;
	
	protected ShadedCirclesMapView theMap;
	
	protected XNoValueSlider radiusSlider;
	
	private String minDensityString, maxDensityString;
	protected double minDensity, maxDensity;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 5));
		
			XPanel mapPanel = new XPanel();
			mapPanel.setLayout(new MapLayout());
			
				theMap = getMap(data);
				theMap.lockBackground(Color.white);
			mapPanel.add("xxx", theMap);
			
		add("Center", mapPanel);
		
		add("South", controlPanel(data));
	}
	
	protected DataSet getMapData() {
		DataSet data = new DataSet();
		
			RegionVariable regions = new RegionVariable(getParameter(REGION_VAR_NAME_PARAM));
			regions.readValues(getParameter(REGION_OUTLINES_PARAM));
		data.addVariable("region", regions);
		
		String labelNameString = getParameter(LABEL_NAME_PARAM);
		if (labelNameString != null)
			data.addLabelVariable("label", labelNameString, getParameter(LABELS_PARAM));
			
		data.addCatVariable("colour", "Colour", getParameter(REGION_COLORS_PARAM),
																												getParameter(REGION_COLORS_PARAM));
		return data;
	}
	
	protected DataSet getData() {
		DataSet data = getMapData();
			
		data.addNumVariable("size", getParameter(SIZE_VAR_NAME_PARAM), getParameter(SIZE_VALUES_PARAM));
		data.addNumVariable("density", getParameter(DENSITY_VAR_NAME_PARAM), getParameter(DENSITY_VALUES_PARAM));
		
		return data;
	}
	
	protected ShadedCirclesMapView getMap(DataSet data) {
		ShadedCirclesMapView mapView = new ShadedCirclesMapView(data, this, "region");
		mapView.setCatDisplayKey("colour", kCountryColours);
//		mapView.setFixedCircleColor(Color.red);
		
		StringTokenizer st = new StringTokenizer(getParameter(DENSITY_EXTREMES_PARAM));
		minDensityString = st.nextToken();
		minDensity = Double.parseDouble(minDensityString);
		maxDensityString = st.nextToken();
		maxDensity = Double.parseDouble(maxDensityString);
		
		mapView.setNumVarCircleColor("density", minDensity, maxDensity, kFillColors);
		mapView.setCircleSizeVariable("size", kMaxCircleRadius / 2);
		return mapView;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.addEqualsSign();
			labelView.setFont(getBigFont());
		thePanel.add(labelView);
		
			OneValueView sizeView = new OneValueView(data, "size", this);
			sizeView.addEqualsSign();
			sizeView.setFont(getBigFont());
		thePanel.add(sizeView);
		
			OneValueView densityView = new OneValueView(data, "density", this);
			densityView.addEqualsSign();
			densityView.setFont(getBigFont());
		thePanel.add(densityView);
		
		return thePanel;
	}
	
	protected XPanel radiusPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
	
			radiusSlider = new XNoValueSlider(translate("Small"), translate("Large"), translate("Circle size"), 0, 100, 50, this);
		thePanel.add(radiusSlider);
		
		return thePanel;
	}
	
	protected XPanel densityKeyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
	
			NumKeyView theKey = new NumKeyView(data, this, minDensityString, maxDensityString, kFillColors);
		thePanel.add(theKey);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("Center", radiusPanel(data));
			
		thePanel.add("East", valuePanel(data));
		
		return thePanel;
	}
	
	protected void adjustCircles() {
		theMap.setCircleSizeVariable("size", radiusSlider.getValue() * kMaxCircleRadius / 100);
	}
	
	private boolean localAction(Object target) {
		if (target == radiusSlider) {
			adjustCircles();
			theMap.repaint();
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}