package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import glmAnova.*;
import exper2.*;


public class SeqFitTermsApplet extends XApplet {
	static final protected String MAX_SSQ_PARAM = "maxSsq";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
//	static final private int kMaxR2Decimals = 4;
	
//	static final private LabelValue kExplainedLabel = new LabelValue("Explained");
//	static final private LabelValue kUnexplainedLabel = new LabelValue("Unexplained");
//	static final private LabelValue kTotalLabel = new LabelValue("Total");
	
	protected DataSet data;
	
	protected String fitKeys[];
	protected String componentKeys[];
	
	protected String componentName[];
	protected String variableName[];
	protected Color componentColor[];
	
	protected NumValue maxSsq;
	protected int maxDF;
	
	private SeqModelsLsView theView;
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
			
		add("Center", displayPanel(data));
			
			XPanel tablePanel = new XPanel();
			tablePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			
				AnovaSeqTableView table = new AnovaSeqTableView(data, this,
										componentKeys, maxSsq, componentName, componentColor, variableName, theView);
				table.setFont(getBigFont());
				table.setLastSeparateX(-1);
				
			tablePanel.add(table);
				
		add("South", tablePanel);
	}
	
	private void readMaxSsqs() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
	}
	
	private DataSet readData() {
		SequentialDataSet data = new SequentialDataSet(this);
		
		data.addCatVariable("xCat", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																																			getParameter(X_LABELS_PARAM));
		
		fitKeys = data.getFitKeys();
		componentKeys = data.getComponentKeys();
		componentName = data.getComponentNames();
		variableName = data.getVariableNames();
		componentColor = data.getComponentColors();
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				CatVariable xVar = (CatVariable)data.getVariable("xCat");
				
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(yVar.name);
				
				scatterPanel.add("Left", yAxis);
				
				theView = new SeqModelsLsView(data, this, xAxis, yAxis, "xCat", "y", fitKeys);
				theView.lockBackground(Color.white);
				theView.setFont(getBigFont());
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
}