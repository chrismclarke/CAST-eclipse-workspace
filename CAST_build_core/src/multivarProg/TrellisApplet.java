package multivarProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import coreVariables.*;


public class TrellisApplet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	
	static final private String SLICE_BOUNDARIES_PARAM = "sliceBoundary";	//	must be exactly 3 boundaries
	static final private String SLICE_LABELS_PARAM = "sliceLabels";				//	must be exactly 4 categories (slices)
	
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL));
			topPanel.add(ProportionLayout.LEFT, slicePanel(data, 0));
			topPanel.add(ProportionLayout.RIGHT, slicePanel(data, 1));
			
		add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL));
			bottomPanel.add(ProportionLayout.LEFT, slicePanel(data, 2));
			bottomPanel.add(ProportionLayout.RIGHT, slicePanel(data, 3));
			
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			NumVariable zVar = new NumVariable(getParameter(Z_VAR_NAME_PARAM));
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
			StringTokenizer st = new StringTokenizer(getParameter(SLICE_BOUNDARIES_PARAM));
			double boundary[] = new double[st.countTokens()];
			for (int i=0 ; i<boundary.length ; i++)
				boundary[i] = Double.parseDouble(st.nextToken());
			CatVariable sliceVar = new GroupedNumVariable(zVar.name, data, "z", boundary);
			sliceVar.readLabels(getParameter(SLICE_LABELS_PARAM));
		data.addVariable("slice", sliceVar);
		
		int nSlices = sliceVar.noOfCategories();
		
		for (int i=0 ; i<nSlices ; i++) {
			FilterNumVariable ySlice = new FilterNumVariable(yVar.name, data, "y", "slice");
			ySlice.setFilterIndex(i);
			data.addVariable("y" + i, ySlice);
		}
		
		return data;
	}
	
	private HorizAxis createHorizAxis() {
		HorizAxis axis = new HorizAxis(this);
		axis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
		return axis;
	}
	
	private VertAxis createVertAxis() {
		VertAxis axis = new VertAxis(this);
		axis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		return axis;
	}
	
	private XLabel createSliceLabel(DataSet data, int sliceIndex) {
		CatVariable zVar = (CatVariable)data.getVariable("slice");
		XLabel label = new XLabel(zVar.getLabel(sliceIndex).toString(), XLabel.CENTER, this);
		label.setFont(getBigBoldFont());
		return label;
	}
	
	private XPanel createPlot(DataSet data, int sliceIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
		thePanel.add("North", yLabel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis xAxis = createHorizAxis();
				xAxis.setAxisName(data.getVariable("x").name);
			mainPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = createVertAxis();
			mainPanel.add("Left", yAxis);
			
				String yKey = "y" + sliceIndex;
				ScatterView theView = new ScatterView(data, this, xAxis, yAxis, "x", yKey);
				theView.lockBackground(Color.white);
			mainPanel.add("Center", theView);
			
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	private XPanel slicePanel(DataSet data, int sliceIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("North", createSliceLabel(data, sliceIndex));
		thePanel.add("Center", createPlot(data, sliceIndex));
		return thePanel;
	}
}