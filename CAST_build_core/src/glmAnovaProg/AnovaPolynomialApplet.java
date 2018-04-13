package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import coreVariables.*;
import glmAnova.*;


public class AnovaPolynomialApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_CENTER_PARAM = "xCenter";		// not necessarily mean
	
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String POLY_DEGREE_PARAM = "polyDegree";
	
	static final private String CENTERED_X_AXIS_INFO_PARAM = "xCenteredAxis";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	private double xOffset;
	protected int polyDegree;
	private DataSet data;
	
	private String variableName[];
	private String componentKeys[];
	private String componentName[];
	private Color componentColor[];
	
	private NumValue maxSsq, maxMsq, maxF;
	@SuppressWarnings("unused")
	private int maxDF;
	
	protected PolyScatterView dataView;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("Center", displayPanel(data));
		add("South", anovaTablePanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			xOffset = Double.parseDouble(getParameter(X_CENTER_PARAM));
			ScaledVariable centredXVar = new ScaledVariable(xVar.name, xVar, "x", -xOffset,
																															1.0, xVar.getMaxDecimals());
		data.addVariable("centredX", centredXVar);
		
		polyDegree = Integer.parseInt(getParameter(POLY_DEGREE_PARAM));
		
//		String fitKeys[] = new String[polyDegree];
		
		for (int i=0 ; i<=polyDegree ; i++) {
			String lsKey = "ls" + i;
			PolynomialModel lsModel = new PolynomialModel(lsKey, data, "centredX", i + 1);
			int bDecs[] = new int[i + 1];				// can be all zero
			lsModel.setLSParams("y", bDecs, 0);
			data.addVariable(lsKey, lsModel);
			
			String fitKey = "fit" + i;
			FittedValueVariable fitVar = new FittedValueVariable(fitKey, data, "centredX",
																																lsKey, 0);
			data.addVariable(fitKey, fitVar);
		}
		
		variableName = new String[polyDegree];
		componentKeys = new String[polyDegree + 2];
		componentName = new String[polyDegree + 2];
		componentColor = new Color[polyDegree + 2];
		
		componentKeys[0] = "Total";
		componentName[0] = translate("Total");
		componentColor[0] = Color.black;
		data.addVariable(componentKeys[0], new BasicComponentVariable(componentName[0], data,
																	"centredX", "y", "ls1", BasicComponentVariable.TOTAL, 9));
		
		componentKeys[polyDegree + 1] = "Residual";
		componentName[polyDegree + 1] = translate("Residual");
		componentColor[polyDegree + 1] = Color.red;
		data.addVariable(componentKeys[polyDegree + 1], new BasicComponentVariable(
										componentName[polyDegree + 1], data, "centredX", "y", "ls" + polyDegree,
										BasicComponentVariable.RESIDUAL, 9));
		
		componentKeys[1] = "explained1";
		variableName[0] = componentName[1] = translate("Linear");
		componentColor[1] = Color.blue;
		data.addVariable(componentKeys[1], new BasicComponentVariable(componentName[1], data,
														"centredX", "y", "ls1", BasicComponentVariable.EXPLAINED, 9));
		
		for (int i=1 ; i<polyDegree ; i++) {
			componentKeys[i + 1] = "explained" + (i + 1);
			variableName[i] = componentName[i + 1] = (i == 1) ? translate("Quadratic") : (i == 2)
																											? translate("Cubic") : (translate("Power") + " " + (i + 1));
			componentColor[i + 1] = Color.blue;
			data.addVariable(componentKeys[i + 1], new SeqComponentVariable(componentName[i + 1],
																								data, "fit" + (i + 1), "fit" + i, 9));
		}
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 2));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				MultiHorizAxis xAxis = new MultiHorizAxis(this, 2);
				xAxis.readNumLabels(getParameter(CENTERED_X_AXIS_INFO_PARAM));
				xAxis.readExtraNumLabels(getParameter(X_AXIS_INFO_PARAM));
				xAxis.setStartAlternate(1);			//	displays correct x values but uses centered ones
				xAxis.setAxisName(data.getVariable("x").name);
			innerPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			innerPanel.add("Left", yAxis);
			
				dataView = new PolyScatterView(data, this, xAxis,
												yAxis, "centredX", "y", "ls" + polyDegree, "ls" + (polyDegree - 1));
				dataView.lockBackground(Color.white);
			innerPanel.add("Center", dataView);
		
		thePanel.add("Center", innerPanel);
		
			XLabel yNameLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
			yNameLabel.setFont(getStandardBoldFont());
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
	
	protected XPanel anovaTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			AnovaSeqTableView table = new AnovaSeqTableView(data, this,
										componentKeys, maxSsq, componentName, componentColor, variableName, null);
			table.setFont(getBigFont());
			table.setShowTests(true, maxMsq, maxF);
			table.setLinkedView(dataView);
		
		thePanel.add("Center", table);
		return thePanel;
	}
}