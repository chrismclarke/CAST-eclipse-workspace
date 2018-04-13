package mapProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import coreVariables.*;

import cat.*;
import map.*;


public class PieRegionsApplet extends CircleRegionsApplet {
	static final private String N_VARS_PARAM = "nVars";
	static final private String UNITS_PARAM = "units";
	static final private String MAX_VALUE_PARAM = "maxValue";
	
	static final private Color kOrange = new Color(0xCC9900);
	static final private Color kGreen = new Color(0x009900);
	static final private Color kPieColors[] = {Color.red, Color.blue, kOrange, kGreen};
	
	static final private Color kValueBackground = new Color(0xCCCCFF);
	
	protected String yKey[];
	private Color yColor[];
	
	protected void addNumVariable(DataSet data, String varName, String varKey, String valuesString) {
		data.addNumVariable(varKey, varName, valuesString);
	}	
	
	protected DataSet getData() {
		DataSet data = getMapData();
			
		int nVars = Integer.parseInt(getParameter(N_VARS_PARAM));
		yKey = new String[nVars];
		yColor = new Color[nVars];
		Value catLabel[] = new Value[nVars];
		for (int i=0 ; i<nVars ; i++) {
			yKey[i] = "y" + i;
			String varName = getParameter(VAR_NAME_PARAM + i);
			addNumVariable(data, varName, yKey[i], getParameter(VALUES_PARAM + i));
			yColor[i] = kPieColors[i];
			catLabel[i] = new LabelValue(varName);
		}
		
		CatVariable dummyCatVar = new CatVariable("Key");
		dummyCatVar.setLabels(catLabel);
		data.addVariable("dummyKeys", dummyCatVar); 
		
		TotalVariable totalVar = new TotalVariable("Total", data, yKey);
		data.addVariable("size", totalVar);
		
		return data;
	}
	
	protected ShadedCirclesMapView getMap(DataSet data) {
		PieMapView mapView = new PieMapView(data, this, "region");
		
		mapView.setCatDisplayKey("colour", kCountryColours);
		mapView.setPieVars(yKey, yColor);
		mapView.setCircleSizeVariable("size", kMaxCircleRadius * 2 / 3);
		return mapView;
	}
	
	protected XPanel keyPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(10, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			CatKey3View theKey = new CatKey3View(data, this, "dummyKeys");
				theKey.setCatColour(yColor);
		thePanel.add(theKey);
		
		thePanel.lockBackground(kValueBackground);
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new InsetPanel(10, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 3));
		
			OneValueView labelView = new OneValueView(data, "label", this);
		thePanel.add(labelView);
		
		NumValue maxValue = new NumValue(getParameter(MAX_VALUE_PARAM));
		
		String unitsString = getParameter(UNITS_PARAM);
		for (int i=0 ; i<yKey.length ; i++) {
			OneValueView valView = new OneValueView(data, yKey[i], this, maxValue);
			valView.setUnitsString(unitsString);
			thePanel.add(valView);
		}
		
		thePanel.lockBackground(kValueBackground);
		return thePanel;
	}
	
	protected XPanel radiusPanel(DataSet data) {
		XPanel thePanel = super.radiusPanel(data);
		radiusSlider.setValue(67);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
			
		thePanel.add("West", valuePanel(data));
		
		thePanel.add("Center", radiusPanel(data));
			
		thePanel.add("East", keyPanel(data));
		
		return thePanel;
	}
	
}