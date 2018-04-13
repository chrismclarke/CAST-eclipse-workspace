package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import cat.*;
import graphics.*;


public class BivarBarApplet extends RotateBarApplet {
	static final private String N_SERIES_PARAM = "nSeries";
	static final private String Y_NAME_PARAM = "yName";
	static final private String X_CLUSTER_PARAM = "xCluster";
	static final private String Z_CLUSTER_PARAM = "zCluster";
	static final private String PROPN_AXIS_NAME_PARAM = "propnAxisName";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxisInfo";
	
	static final private String kDepthAxisInfo = "0.05 0.95 2 1";
	static final private String kZAxisInfo = "0.05 0.95 2 1";
	
	private String yKeys[];
	private Color catRowColor[];
	
//	private XButton clusterByXButton, clusterByZButton;
	private XCheckbox showPropnsCheck;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		int nSeries = Integer.parseInt(getParameter(N_SERIES_PARAM));
		yKeys = new String[nSeries];
		catRowColor = new Color[nSeries];
		LabelValue seriesLabels[] = new LabelValue[nSeries];
		for (int i=0 ; i<nSeries ; i++) {
			yKeys[i] = "y" + i;
			catRowColor[i] = CatKey3View.kCatColour[i];
			String seriesName = getParameter(VAR_NAME_PARAM + i);
			seriesLabels[i] = new LabelValue(seriesName);
			data.addNumVariable(yKeys[i], seriesName, getParameter(VALUES_PARAM + i));
		}
		
			CatVariable keyVar = new CatVariable("Key");
			keyVar.setLabels(seriesLabels);
		data.addVariable("key", keyVar);
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
		data.addVariable("label", labelVar);
		
		yName = getParameter(Y_NAME_PARAM);
		
		return data;
	}
	
	protected BarChartRotateView getBarChartView(DataSet theData, D3Axis xAxis, D3Axis yAxis,
													D3Axis zAxis, String labelKey, String yName, double initialDiameter) {
		return new BivarBarChartView(theData, this, xAxis, yAxis, zAxis, labelKey, yKeys,
							catRowColor, yName, initialDiameter, getParameter(PROPN_AXIS_INFO_PARAM),
							getParameter(PROPN_AXIS_NAME_PARAM));
	}
	
	protected String getXAxisInfo() {
		return kDepthAxisInfo;
	}
	
	protected String getZAxisInfo() {
		return kZAxisInfo;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		CatKey3View keyView = new CatKey3View(data, this, "key");
		thePanel.add(keyView);
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			rotatePanel.add(new RotateButton(RotateButton.XYZ_BLANK_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
		thePanel.add(rotatePanel);
			
			XPanel clusterPanel = new XPanel();
			clusterPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			clusterPanel.add(new RotateCustomButton(getParameter(X_CLUSTER_PARAM), theView, this));
			clusterPanel.add(new RotateCustomButton(getParameter(Z_CLUSTER_PARAM), theView, this));
			
		thePanel.add(clusterPanel);
		
			showPropnsCheck = new XCheckbox(translate("Show proportions"), this);
		thePanel.add(showPropnsCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showPropnsCheck) {
			((BivarBarChartView)theView).setShowProportions(showPropnsCheck.getState());
			theView.repaint();
			
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