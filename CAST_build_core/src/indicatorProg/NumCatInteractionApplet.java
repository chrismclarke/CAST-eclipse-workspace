package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;
import coreVariables.*;

import indicator.*;


public class NumCatInteractionApplet extends XApplet {
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	static final protected String Z_LABELS_PARAM = "zLabels";
	static final private String X_AXIS_PARAM = "xAxis";
	static final protected String Y_AXIS_PARAM = "yAxis";
	static final private String MAX_PARAM_PARAM = "maxParam";
														//	intercept, slope, cat effect, interaction effect
	static final private String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	static final private String GROUP_EFFECT_NAME_PARAM = "groupEffectName";
	static final private String INTERACTION_NAME_PARAM = "interactionName";
	static final private String SSQ_TYPE_PARAM = "ssqType";
	
	static final private Color kTableBackground = new Color(0xD6E1FF);
	
	static final private int kInteractionHierarchy[][] = {null, null, {0,1}};
																		//	does not include intercept so first X is index 0
	
	protected String[] xKeys;
	
	private DataSet data;
	
	private String termName[];
	protected int nxPerTerm[];
	protected NumValue maxParam[];
	protected int paramDecimals[];
	
//	private NumValue maxType3Ssq, maxDf, maxMeanSsq, maxF;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 5));
			
		add("Center", displayPanel(data));
		add("South", tablePanel(data));
		add("East", keyPanel(data));
	}
	
	protected void setupParams(Variable xVar, Variable zVar) {
		termName = new String[3];
		termName[0] = xVar.name;
			String groupEffectName = getParameter(GROUP_EFFECT_NAME_PARAM);
		termName[1] = (groupEffectName == null) ? zVar.name : groupEffectName;
			String interactionName = getParameter(INTERACTION_NAME_PARAM);
		termName[2] = (interactionName == null) ? translate("Interaction") : interactionName;
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
		maxParam = new NumValue[xKeys.length + 1];
		for (int i=0 ; i<4 ; i++)
			maxParam[i] = new NumValue(st.nextToken());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM),
																												getParameter(Y_VALUES_PARAM));
		
		NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
		CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
		zVar.readLabels(getParameter(Z_LABELS_PARAM));
		zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		int nzCats = zVar.noOfCategories();
		
		xKeys = new String[1 + nzCats];
		xKeys[0] = "x";
		xKeys[1] = "z";
		for (int i=1 ; i<nzCats ; i++) {
			xKeys[i + 1] = "xz" + i;
			data.addVariable(xKeys[i + 1], new CatNumInteractTermVariable(xKeys[i + 1],
																													data, "z", "x", i));
		}
		
		setupParams(xVar, zVar);
		
		nxPerTerm = new int[3];
		nxPerTerm[0] = nxPerTerm[1] = 1;
		nxPerTerm[2] = nzCats - 1;
		
		paramDecimals = new int[2 * nzCats];
		paramDecimals[0] = maxParam[0].decimals;
		paramDecimals[1] = maxParam[1].decimals;
		for (int i=1 ; i<nzCats ; i++) {
			paramDecimals[i + 1] = maxParam[2].decimals;
			paramDecimals[i + nzCats] = maxParam[3].decimals;
		}
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
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
			
				DragLinesView theView = new DragLinesView(data, this, xAxis, yAxis, xKeys, "y",
																						null, null, "ls", paramDecimals, new NumValue(1.0));
				theView.setCanDragHandles(false);
				theView.lockBackground(Color.white);
				
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 7);
			innerPanel.setLayout(new BorderLayout(0, 0));

				StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
				NumValue maxType3Ssq = new NumValue(st.nextToken());
				NumValue maxDf = new NumValue(st.nextToken());
				NumValue maxMeanSsq = new NumValue(st.nextToken());
				NumValue maxF = new NumValue(st.nextToken());
				
				Type3SsqTableView testTable;
				String ssqTypeParam = getParameter(SSQ_TYPE_PARAM);
				if (Integer.parseInt(ssqTypeParam) == 3)
					testTable = new Type3SsqTableView(data, this, "ls", "y", maxType3Ssq,
										maxDf, maxMeanSsq, maxF, termName, nxPerTerm, kInteractionHierarchy);
				else
					testTable = new Type1SsqTableView(data, this, "ls", "y", maxType3Ssq,
										maxDf, maxMeanSsq, maxF, termName, nxPerTerm, kInteractionHierarchy);
				
			innerPanel.add("Center", testTable);
		
			innerPanel.lockBackground(kTableBackground);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
		
			CatVariable zVar = (CatVariable)data.getVariable(xKeys[1]);
			XLabel zLabel = new XLabel(zVar.name, XLabel.LEFT, this);
			zLabel.setFont(getBigFont());
		thePanel.add(zLabel);
			
			CatKey zKey = new CatKey(data, xKeys[1], this, CatKey.VERT);
			zKey.setFont(getBigFont());
		thePanel.add(zKey);
		
		return thePanel;
	}
}