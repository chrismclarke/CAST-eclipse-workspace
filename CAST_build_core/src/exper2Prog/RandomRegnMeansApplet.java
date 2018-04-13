package exper2Prog;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import random.*;

import exper2.*;


public class RandomRegnMeansApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String X_VALUES_PARAM = "xValues";
	
	static final private String REGN_PARAMS_PARAM = "regnParams";
	static final private String SHOW_RESID_PARAM = "showResiduals";
	
//	static final private Color kRssBackgroundColor = new Color(0xFFEEBB);
//	static final private Color kConstraintLabelColor = new Color(0x990000);
//	static final private Color kCheckBackground = new Color(0xDDDDEE);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private XButton sampleButton;
	
	public void setupApplet() {
		data = readData();
		summaryData = new SummaryDataSet(data, "error");
		takeSample();
		
		setLayout(new BorderLayout(0, 0));
		
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable xCatVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xCatVar.readLabels(getParameter(X_LABELS_PARAM));
			xCatVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("xCat", xCatVar);
		
		data.addNumVariable("xNum", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
			LinearModel model = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "xNum",
																															getParameter(REGN_PARAMS_PARAM));
		data.addVariable("model", model);
		
			int n = xCatVar.noOfValues();
			RandomNormal errorGenerator = new RandomNormal(n, 0.0, 1.0, 3.0);
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGenerator, 9);
			errorVar.generateNextSample();
		data.addVariable("error", errorVar);
		
			ResponseVariable yVar = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM), data,
																																"xNum", "error", "model", 9);
		data.addVariable("y", yVar);
		
			String[] xKey = {"xCat"};
			MultipleRegnModel lsModel = new MultipleRegnModel(translate("Least squares"), data, xKey);
			int nParams = lsModel.noOfParameters();
			int[] bDecs = new int[nParams];			//	not used so leave as zero
			lsModel.setLSParams("y", null, bDecs, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
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
			
			scatterPanel.add("Left", yAxis);
					
				CoreOneFactorView theView = new CoreOneFactorView(data, this, xAxis, yAxis, "xCat", "y", "ls");
				theView.lockBackground(Color.white);
				String showResidParam = getParameter(SHOW_RESID_PARAM);
				if (showResidParam != null && showResidParam.equals("false"))
					theView.setShowResiduals(false);
			
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private void takeSample() {
		summaryData.takeSample();
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		ls.updateLSParams("y");
		data.variableChanged("y");
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			sampleButton = new XButton(translate("Repeat experiment"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}