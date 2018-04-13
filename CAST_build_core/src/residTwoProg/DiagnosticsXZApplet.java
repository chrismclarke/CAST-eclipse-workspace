package residTwoProg;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;

import residProg.*;
import resid.*;
import multiRegn.*;
import residTwo.*;


public class DiagnosticsXZApplet extends DiagnosticsApplet {
	static final private int[] kBDecs = {9, 9, 9};
	
	private D3Axis xAxis, yAxis, zAxis;
	
	protected CoreModelDataSet readData() {
		MultiRegnDataSet data = new MultiRegnDataSet(this);
		
			MultipleRegnModel deletedLS = new MultipleRegnModel("Deleted LS", data,
																																	MultiRegnDataSet.xKeys);
			deletedLS.setLSParams("y", kBDecs, 9);
		data.addVariable("deletedLS", deletedLS);
		
			DeletedSDVariable deletedSD = new DeletedSDVariable("Deleted sd", data, "y",
																																					"deletedLS");
		data.addVariable("deletedSD", deletedSD);
		
			ExtStudentResidVariable tResidVar = new ExtStudentResidVariable("Ext student resid",
																		data, MultiRegnDataSet.xKeys, "y", "ls", "deletedLS", 9);
		data.addVariable("tResid", tResidVar);
		
			FitInfluenceVariable dfitsVar = new FitInfluenceVariable("DFITS", data,
																			MultiRegnDataSet.xKeys, "tResid", "ls", 9);
		data.addVariable("dfits", dfitsVar);
		
			LeverageValueVariable leverageVar = new LeverageValueVariable("Leverage", data,
													MultiRegnDataSet.xKeys, "ls", LeverageValueVariable.LEVERAGE, 9);
		data.addVariable("leverage", leverageVar);
		
		return data;
	}
	
	protected int getNoOfParams() {
		return 3;
	}
	
	protected XPanel scatterContentsPanel(CoreModelDataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(regnData.getXAxisInfo());
		yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(regnData.getYAxisInfo());
		zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(regnData.getZAxisInfo());
		
		ColoredXZView d3View = new ColoredXZView(regnData, this, xAxis, yAxis, zAxis,
																							"ls", MultiRegnDataSet.xKeys, "y");
		d3View.setSelectCrosses(true);
		d3View.lockBackground(Color.white);
		thePanel.add("Center", d3View);
			
//		thePanel.add("South", rotatePanel(d3View));
		
		return thePanel;
	}
	
	protected void changeDataPlot(int dataSetChoice) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		
		xAxis.setNumScale(regnData.getXAxisInfo());
		xAxis.setLabelName(regnData.getXVarName());
		yAxis.setNumScale(regnData.getYAxisInfo());
		yAxis.setLabelName(regnData.getYVarName());
		zAxis.setNumScale(regnData.getZAxisInfo());
		zAxis.setLabelName(regnData.getZVarName());
	}
}