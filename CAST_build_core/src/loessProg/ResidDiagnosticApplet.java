package loessProg;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import loess.*;


public class ResidDiagnosticApplet extends CoreDiagnosticApplet {
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String INDEX_AXIS_INFO_PARAM = "indexAxis";
	static final private String NSCORE_AXIS_INFO_PARAM = "nscoreAxis";
	
	static final private double kLogFactor = 1.0 / Math.log(10.0);
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		int nVals = xVar.noOfValues();
		double val[] = new double[nVals];
		
		NumVariable logX = new NumVariable("log(" + xVar.name + ")");
		for (int i=0 ; i<nVals ; i++)
			val[i] = Math.log(xVar.doubleValueAt(i)) * kLogFactor;
		logX.setValues(val);
		logX.setDecimals(4);
		data.addVariable("logX", logX);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumVariable logY = new NumVariable("log(" + yVar.name + ")");
		for (int i=0 ; i<nVals ; i++)
			val[i] = Math.log(yVar.doubleValueAt(i)) * kLogFactor;
		logY.setValues(val);
		logY.setDecimals(4);
		data.addVariable("logY", logY);
		
		addSimulationVariables(data, "logX", "logY");
		
		NumVariable index = new NumVariable(translate("Observation order"));
		for (int i=0 ; i<nVals ; i++)
			val[i] = i+1;
		index.setValues(val);
		index.setDecimals(0);
		data.addVariable("index", index);
		
//		System.out.println("index x logX y logY response resid nScore");
//		for (int i=0 ; i<10 ; i++) {
//			System.out.println(index.valueAt(i).toString() + ", "
//						+ xVar.valueAt(i).toString() + ", "
//						+ logX.valueAt(i).toString() + ", "
//						+ yVar.valueAt(i).toString() + ", "
//						+ logY.valueAt(i).toString() + ", "
//						+ response.valueAt(i).toString() + ", "
//						+ resid.valueAt(i).toString() + ", "
//						+ nScore.valueAt(i).toString());
//		}
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																						ProportionLayout.TOTAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			
			topPanel.add("Left", createPlotPanel(data, false, "logX", "response", null,
					getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			topPanel.add("Right", createPlotPanel(data, false, "logX", "resid", null,
					getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		thePanel.add("Top", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			
			bottomPanel.add("Left", createPlotPanel(data, false, "index", "resid", null,
					getParameter(INDEX_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 2));
			bottomPanel.add("Right", createPlotPanel(data, false, "nscore", "resid", null,
					getParameter(NSCORE_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 3));
		thePanel.add("Bottom", bottomPanel);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		switch (plotIndex) {
			case 0:
				return new ScatterView(data, this, theHorizAxis, theVertAxis, "logX", "response");
			case 1:
				return new ScatterView(data, this, theHorizAxis, theVertAxis, "logX", "resid");
			case 2:
				return new JoinedScatterView(data, this, theHorizAxis, theVertAxis, "index", "resid");
			default:
				return new ScatterView(data, this, theHorizAxis, theVertAxis, "nscore", "resid");
		}
	}
}