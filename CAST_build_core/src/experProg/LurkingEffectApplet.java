package experProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreGraphics.*;
import models.*;
import coreVariables.*;

import exper.*;


public class LurkingEffectApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_COEFF_PARAM = "yCoeffs";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	
	static final private String INDEX_VAR_NAME_PARAM = "indexVarName";
	
	static final private Color kPaleGrayColor = new Color(0xEEEEEE);
	static final private Color kIndexColor = new Color(0x990000);
	static final private NumValue kMaxIndex = new NumValue(999, 0);
	
	private int currentXIndex;
	private XChoice xChoice;
	
	private ResponseNormalView yDistView;
	private DotPlotView xView;
	private DataSet data;
	private VertAxis yAxis;
	private MultiHorizAxis xAxis;
	
	private String xVarName[];
	private String xKey[];
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	private DataSet readData() {
		data = new DataSet();
		
		int nx = 0;
		while (getParameter(X_VAR_NAME_PARAM + nx) != null)
			nx++;
		
		xVarName = new String[nx];
		xKey = new String[nx];
		for (int i=0 ; i<nx ; i++) {
			xVarName[i] = getParameter(X_VAR_NAME_PARAM + i);
			xKey[i] = "x" + i;
			data.addNumVariable(xKey[i], xVarName[i], getParameter(X_VALUES_PARAM + i));
		}
		
			MultipleRegnModel model = new MultipleRegnModel(getParameter(Y_VAR_NAME_PARAM), data, xKey,
																														getParameter(Y_COEFF_PARAM));
		data.addVariable("y", model);
		
		String indexName = getParameter(INDEX_VAR_NAME_PARAM);
		if (indexName != null) {
			NumVariable x0Var = (NumVariable)data.getVariable("x0");
			data.addVariable("index", new IndexVariable(indexName, x0Var.noOfValues()));
		}
		return data;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel indexPanel = new XPanel();
			indexPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																								VerticalLayout.VERT_CENTER, 0));
				OneValueView theIndex = new OneValueView(data, "index", this, kMaxIndex);
				theIndex.setFont(getBigBoldFont());
				theIndex.setForeground(kIndexColor);
			indexPanel.add(theIndex);
			
		thePanel.add("North", indexPanel);
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			labelPanel.add(yVariateName);
			
		thePanel.add("Center", labelPanel);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			xAxis = new MultiHorizAxis(this, xVarName.length);
			xAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM + 0));
			for (int i=1 ; i<xVarName.length ; i++)
				xAxis.readExtraNumLabels(getParameter(X_AXIS_INFO_PARAM + i));
			xAxis.setChangeMinMax(true);
			
		thePanel.add("Bottom", xAxis);
		
			xView = new DotPlotView(data, this, xAxis, 1.0);
			xView.setActiveNumVariable("x0");
			xView.setRetainLastSelection(true);
			xView.lockBackground(kPaleGrayColor);
		thePanel.add("BottomMargin", xView);
		
			yDistView = new ResponseNormalView(data, this, xAxis, yAxis, "y", xKey);
			yDistView.setRetainLastSelection(true);
			yDistView.lockBackground(Color.white);
		thePanel.add("Center", yDistView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			xChoice = new XChoice(this);
			for (int i=0 ; i<xVarName.length ; i++)
				xChoice.addItem(xVarName[i]);
			xChoice.select(0);
			currentXIndex = 0;
			
		thePanel.add(xChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xChoice) {
			int newXIndex = xChoice.getSelectedIndex();
			if (newXIndex != currentXIndex) {
				currentXIndex = newXIndex;
				xAxis.setAlternateLabels(newXIndex);
				xAxis.repaint();
				xView.setActiveNumVariable(xKey[newXIndex]);
				xView.repaint();
				yDistView.repaint();
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