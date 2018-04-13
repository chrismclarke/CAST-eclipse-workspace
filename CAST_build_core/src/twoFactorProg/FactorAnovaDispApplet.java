package twoFactorProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import glmAnova.*;
import twoFactor.*;


public class FactorAnovaDispApplet extends FactorAnovaSeqApplet {
	static final private Color kSummaryBackground = new Color(0xDADAE9);
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.6, 5, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			
				XPanel rotatePanel = new XPanel();
				rotatePanel.setLayout(new BorderLayout(5, 0));
		
				rotatePanel.add("Center", displayPanel(data));
				rotatePanel.add("East", eastPanel(data));
				
			topPanel.add(ProportionLayout.LEFT, rotatePanel);
			
				XPanel effectsPanel = new InsetPanel(5, 3);
				effectsPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																																	ProportionLayout.TOTAL));
				
				effectsPanel.add(ProportionLayout.TOP, effectPanel(data, 0));
				effectsPanel.add(ProportionLayout.BOTTOM, effectPanel(data, 1));
				
				effectsPanel.lockBackground(kSummaryBackground);
			topPanel.add(ProportionLayout.RIGHT, effectsPanel);
			
		add("Center", topPanel);
				
			AnovaSeqTableView table = getAnovaTable(data);
			
		add("South", table);
	}
	
	
	private XPanel effectPanel(DataSet data, int horizIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				CatVariable xVar = (CatVariable)data.getVariable(factorKeys[horizIndex]);
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.lockBackground(kSummaryBackground);
//				int nXCats = xVar.noOfCategories();
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
			
			scatterPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				yAxis.readNumLabels(labelInfo);
				yAxis.lockBackground(kSummaryBackground);
			
			scatterPanel.add("Left", yAxis);
				
				EffectDiagramView theView = new EffectDiagramView(data, this, "y", factorKeys, "ls", xAxis, yAxis, horizIndex);
				theView.lockBackground(Color.white);
			
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yVariateName = new XLabel(translate("Mean") + " " + yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
}