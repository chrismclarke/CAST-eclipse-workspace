package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import valueList.*;

import regnView.*;


public class ExtraVarsApplet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Z_KEYS_PARAM = "zKeys";
	
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	
	static final private String kVarNameSuffix = "VarName";
	static final private String kValuesSuffix = "Values";
	static final private String kCatLabelsSuffix = "CatLabels";
	
	private String zKey[];
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new ProportionLayout(0.6, 10, ProportionLayout.VERTICAL));
		add(ProportionLayout.TOP, displayPanel(data));
		add(ProportionLayout.BOTTOM, valueListPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		LinearModel model = new LinearModel("model", data, "x");
		model.setLSParams("y", 9, 9, 9);
		data.addVariable("model", model);
		
		StringTokenizer st = new StringTokenizer(getParameter(Z_KEYS_PARAM));
		int nz = st.countTokens();
		zKey = new String[nz];
		for (int i=0 ; i<nz ; i++) {
			zKey[i] = st.nextToken();
			String labelString = getParameter(zKey[i] + kCatLabelsSuffix);
			if (labelString == null)
				data.addNumVariable(zKey[i], getParameter(zKey[i] + kVarNameSuffix),
																												getParameter(zKey[i] + kValuesSuffix));
			else
				data.addCatVariable(zKey[i], getParameter(zKey[i] + kVarNameSuffix),
																						getParameter(zKey[i] + kValuesSuffix), labelString);
		}
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				horizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			scatterPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			scatterPanel.add("Left", vertAxis);
			
				DataView theView = new LSScatterView(data, this, horizAxis, vertAxis, "x", "y", "model");
				theView.setRetainLastSelection(true);
				theView.lockBackground(Color.white);
			scatterPanel.add("Center", theView);
		
		thePanel.add("Center", scatterPanel);
		
			XLabel yNameLabel = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yNameLabel.setFont(horizAxis.getFont());
		
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
	
	private ScrollValueList valueListPanel(DataSet data) {
//		ScrollImages.loadScroll(this);
		ScrollValueList theList = new ScrollValueList(data, this, true);
		theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
		theList.addVariableToList("x", ScrollValueList.RAW_VALUE);
		
		for (int i=0 ; i<zKey.length ; i++)
			theList.addVariableToList(zKey[i], ScrollValueList.RAW_VALUE);
		
		theList.setRetainLastSelection(true);
		
		return theList;
	}
	
}