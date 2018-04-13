package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;

import ssq.*;
import multiRegn.*;


public class SlopeCorrelApplet extends CoeffCIApplet {
	static final private String XSLOPE_AXIS_INFO_PARAM = "xSlopeAxis";
	static final private String ZSLOPE_AXIS_INFO_PARAM = "zSlopeAxis";
	static final private String MAX_R_PARAM = "maxR";
	
	static final private int kScatterPanelHeight = 280;
	
	protected DataSet readData() {
		data = new AdjustXZCorrDataSet(this);
		
		setCoeffNames();
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		return data;
	}
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error");
		
		maxCoeff = new NumValue[3];
		int decimals[] = new int[3];
		StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
		for (int i=0 ; i<3 ; i++) {
			maxCoeff[i] = new NumValue(st.nextToken());
			decimals[i] = maxCoeff[i].decimals;
		}
			
		summaryData.addVariable("planes", new LSCoeffVariable("Planes", "ls",
																						MultiRegnDataSet.xKeys, "y", null, decimals));
		for (int i=1 ; i<3 ; i++) {
			String coeffKey = "b" + i;
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffName[i], summaryData,
																																						"planes", i));
		}
		
		return summaryData;
	}
	
	protected XPanel getSamplePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
		
			AdjustXZCorrDataSet data2 = (AdjustXZCorrDataSet)data;
			double initialR2 = data2.getInitialXZR2();
			String maxRString = getParameter(MAX_R_PARAM);
			double maxR = Double.parseDouble(maxRString);
			double maxR2 = maxR * maxR;
		thePanel.add("Center", new R2Slider(this, data, "z", "y", summaryData, translate("Correl(X, Z)"),
																											"0.0", maxRString, initialR2, maxR2));
		
		return thePanel;
	}
	
	protected void addDescription(DataSet data, XPanel thePanel) {
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 8, 0, 0);
		thePanel.setLayout(new FixedSizeLayout(999, kScatterPanelHeight));
		
			XPanel innerPanel = new XPanel();
//			innerPanel.setLayout(new ProportionLayout(0.35, 0, ProportionLayout.HORIZONTAL,
//																																	ProportionLayout.TOTAL));
//			
//			innerPanel.add(ProportionLayout.LEFT, corrSliderPanel(data));
//			innerPanel.add(ProportionLayout.RIGHT, slopeScatterPanel(summaryData, "b1", "b2"));
			
			innerPanel.setLayout(new BorderLayout(30, 0));
			
			innerPanel.add("West", bottomLeftPanel(data));
			innerPanel.add("Center", slopeScatterPanel(summaryData, "b1", "b2"));
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel bottomLeftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 20));
		
			XPanel lsEqnPanel = new XPanel();
			lsEqnPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 3));
				XLabel lsLabel = new XLabel(translate("Least squares line") + ": ", XLabel.LEFT, this);
				lsLabel.setFont(getStandardBoldFont());
			lsEqnPanel.add(lsLabel);
			lsEqnPanel.add(new MultiLinearEqnView(data, this, "ls", yVarName, xVarName, maxCoeff, maxCoeff));
		thePanel.add(lsEqnPanel);
		
		thePanel.add(super.getSamplePanel());
		
		return thePanel;
	}
	
	private XPanel slopeScatterPanel(SummaryDataSet summaryData, String vertKey, String horizKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(ZSLOPE_AXIS_INFO_PARAM));
				NumVariable zVar = (NumVariable)summaryData.getVariable(horizKey);
				horizAxis.setAxisName(zVar.name);
			scatterPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(XSLOPE_AXIS_INFO_PARAM));
			scatterPanel.add("Left", vertAxis);
			
				DataView theView = new ScatterView(summaryData, this, horizAxis, vertAxis, horizKey, vertKey);
				theView.lockBackground(Color.white);
			scatterPanel.add("Center", theView);
			
		thePanel.add("Center", scatterPanel);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				NumVariable xVar = (NumVariable)summaryData.getVariable(vertKey);
				XLabel xVariateName = new XLabel(xVar.name, XLabel.LEFT, this);
				xVariateName.setFont(vertAxis.getFont());
				topPanel.add(xVariateName);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
}