package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;
import coreGraphics.*;
import coreVariables.*;

import indicator.*;


abstract public class CoreTwoGroupApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String INTERACTION_PARAM = "hasInteraction";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String GROUP_EFFECT_NAME_PARAM = "groupEffectName";
	static final private String Y_BOXPLOT_PARAM = "yBoxPlot";
	
	static final private String[] kNoInteractionXKey = {"x", "z"};
	static final private String[] kInteractionXKey = {"x", "z", "xz"};
	
	static final private Color kBoxFill = new Color(0xDDDDDD);
	
	private String[] xKeys = kNoInteractionXKey;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected boolean hasInteraction;
	
	protected String paramName[];
	protected NumValue maxParam[];
	private int paramDecimals[];
	
	public void setupApplet() {
		data = readData();
		summaryData = createSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 5));
			
		add("Center", displayPanel(data));
		add("South", bottomPanel(data));
		add("East", keyPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		String interactionString = getParameter(INTERACTION_PARAM);
		hasInteraction = (interactionString != null) && interactionString.equals("true");
		if (hasInteraction)
			xKeys = kInteractionXKey;
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM),
																												getParameter(Y_VALUES_PARAM));
		
		NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable(xKeys[0], xVar);
		
		CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
		zVar.readLabels(getParameter(Z_LABELS_PARAM));
		zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable(xKeys[1], zVar);
		
		if (hasInteraction)
			data.addVariable(xKeys[2], new CatNumInteractTermVariable("Interaction",
																										data, xKeys[1], xKeys[0], 1));
		
		paramName = new String[xKeys.length + 1];
		paramName[0] = translate("intercept");
		paramName[1] = xVar.name;
			String groupEffectName = getParameter(GROUP_EFFECT_NAME_PARAM);
		paramName[2] = (groupEffectName == null) ? zVar.name : groupEffectName;
		if (hasInteraction)
			paramName[3] = translate("interaction");
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
		maxParam = new NumValue[xKeys.length + 1];
		for (int i=0 ; i<=xKeys.length ; i++)
			maxParam[i] = new NumValue(st.nextToken());
		
		paramDecimals = new int[xKeys.length + 1];
		for (int i=0 ; i<=xKeys.length ; i++)
			paramDecimals[i] = maxParam[i].decimals;
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	private SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
				
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
				xAxis.setAxisName(data.getVariable(xKeys[0]).name);
			dataPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			dataPanel.add("Left", yAxis);
			
				DragParallelLinesView theView;
				if (hasInteraction)
					theView = new DragLinesView(data, this, xAxis, yAxis, xKeys, "y", null, null,
																							"ls", paramDecimals, new NumValue(1.0));
				else
					theView = new DragParallelLinesView(data, this, xAxis, yAxis, xKeys, "y", null,
																										null, "ls", paramDecimals);
				theView.setCanDragHandles(false);
				theView.lockBackground(Color.white);
				
			dataPanel.add("Center", theView);
			
			String boxPlotString = getParameter(Y_BOXPLOT_PARAM);
			if (boxPlotString != null && boxPlotString.equals("true")) {
				BoxView yBoxView = new BoxView(data, this, yAxis);
				yBoxView.setShowOutliers(false);
				yBoxView.setActiveNumVariable("y");
				yBoxView.setFillColor(kBoxFill);
				dataPanel.add("LeftMargin", yBoxView);
			}
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	abstract protected XPanel bottomPanel(DataSet data);
	
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
		
			XLabel zLabel = new XLabel(data.getVariable(xKeys[1]).name, XLabel.LEFT, this);
			zLabel.setFont(getBigFont());
		thePanel.add(zLabel);
			
			CatKey zKey = new CatKey(data, xKeys[1], this, CatKey.VERT);
			zKey.setFont(getBigFont());
		thePanel.add(zKey);
		
		return thePanel;
	}
}