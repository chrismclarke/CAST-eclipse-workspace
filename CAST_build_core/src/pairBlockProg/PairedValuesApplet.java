package pairBlockProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;
import coreVariables.*;
import valueList.*;

//import test.*;
import pairBlock.*;


public class PairedValuesApplet extends XApplet {
	static final private String RAW_AXIS_INFO_PARAM = "rawAxis";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	static final private String X_FACTOR_NAME_PARAM = "xFactorName";
	static final private String X1_NAME_PARAM = "x1Name";
	static final private String X2_NAME_PARAM = "x2Name";
	static final private String DIFF_NAME_PARAM = "diffName";
	static final private String X1_VALUES_PARAM = "x1Values";
	static final private String X2_VALUES_PARAM = "x2Values";
	static final private String RAW_HEADING_PARAM = "rawHeading";
	static final private String DIFF_HEADING_PARAM = "diffHeading";
	
	private DataSet data;
	
	private PairedDotPlotView pairedView;
	
	private XCheckbox showPairingCheck;
	
	private XPanel diffPanel;
	private CardLayout diffPanelLayout;
	
	public void setupApplet() {
		getData();
		
		setLayout(new BorderLayout(0, 6));
		
		add("South", choicePanel());
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.6, 15, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
			
			mainPanel.add(ProportionLayout.LEFT, rawDataPanel(data));
			mainPanel.add(ProportionLayout.RIGHT, diffDataPanel(data));
			
		add("Center", mainPanel);
	}
	
	private void getData() {
		data = new DataSet();
		
			NumVariable x1Var = new NumVariable(getParameter(X1_NAME_PARAM));
			x1Var.readValues(getParameter(X1_VALUES_PARAM));
		data.addVariable("x1", x1Var);
		
			NumVariable x2Var = new NumVariable(getParameter(X2_NAME_PARAM));
			x2Var.readValues(getParameter(X2_VALUES_PARAM));
		data.addVariable("x2", x2Var);
		
			SumDiffVariable diffVar = new SumDiffVariable(getParameter(DIFF_NAME_PARAM), data,
																														"x2", "x1", SumDiffVariable.DIFF);
		data.addVariable("diff", diffVar);
	}
	
	private XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			showPairingCheck = new XCheckbox(translate("Show pairing"), this);
		thePanel.add(showPairingCheck);
		return thePanel;
	}
	
	private XPanel rawDataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XLabel titleLabel = new XLabel(getParameter(RAW_HEADING_PARAM), XLabel.CENTER, this);
			titleLabel.setFont(getStandardBoldFont());
		
		thePanel.add("North", titleLabel);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				VertAxis vertAxis = new VertAxis(this);
				String labelInfo = getParameter(RAW_AXIS_INFO_PARAM);
				vertAxis.readNumLabels(labelInfo);
			
			dataPanel.add("Left", vertAxis);
			
				CatVariable tempVar = new CatVariable("");
				tempVar.readLabels(getParameter(X1_NAME_PARAM) + " " + getParameter(X2_NAME_PARAM));
				
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.setCatLabels(tempVar);
				String xFactorName = getParameter(X_FACTOR_NAME_PARAM);
				if (xFactorName != null) {
					horizAxis.setAxisName(xFactorName);
					horizAxis.setCenterAxisName(true);
				}
			
			dataPanel.add("Bottom", horizAxis);
			
				pairedView = new PairedDotPlotView(data, this, "x1", "x2", vertAxis, horizAxis, 0.75);
				pairedView.setRetainLastSelection(true);
				pairedView.lockBackground(Color.white);
				
			dataPanel.add("Center", pairedView);
		
		thePanel.add("Center", dataPanel);
		
		return thePanel;
	}
	
	private XPanel allDifferencesPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XLabel titleLabel = new XLabel(getParameter(DIFF_HEADING_PARAM), XLabel.CENTER, this);
			titleLabel.setFont(getStandardBoldFont());
		
		thePanel.add("North", titleLabel);
		
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis vertAxis = new VertAxis(this);
				String labelInfo = getParameter(DIFF_AXIS_INFO_PARAM);
				vertAxis.readNumLabels(labelInfo);
			
			plotPanel.add("Left", vertAxis);
			
				CatVariable tempVar = new CatVariable("");
				tempVar.readLabels(getParameter(DIFF_NAME_PARAM));
				
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.setCatLabels(tempVar);
			
			plotPanel.add("Bottom", horizAxis);
			
				DotPlotView dataView = new CentredDotPlotView(data, this, vertAxis, horizAxis, pairedView, 1.0);
				
				dataView.setRetainLastSelection(true);
				dataView.lockBackground(Color.white);
				dataView.setActiveNumVariable("diff");
				
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
	
	private XPanel oneDifferencePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			String diffNameString = new StringTokenizer(getParameter(DIFF_NAME_PARAM), "#").nextToken();
											//	strips "#' from start and end of string if they are exist
			XLabel diffLabel = new XLabel(diffNameString, XLabel.LEFT, this);
		thePanel.add(diffLabel);
		
			OneValueView diffView = new OneValueView(data, "diff", this);
			diffView.setNameDraw(false);
		thePanel.add(diffView);
		
		return thePanel;
	}
	
	private XPanel diffDataPanel(DataSet data) {
		diffPanel = new XPanel();
			diffPanelLayout = new CardLayout();
		diffPanel.setLayout(diffPanelLayout);
		diffPanel.add("One difference", oneDifferencePanel(data));
			
		diffPanel.add("All differences", allDifferencesPanel(data));
	
		return diffPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showPairingCheck) {
			pairedView.setShowPairing(showPairingCheck.getState());
			pairedView.repaint();
			
			diffPanelLayout.show(diffPanel, showPairingCheck.getState() ? "All differences" : "One difference");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}