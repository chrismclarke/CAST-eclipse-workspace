package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import factorial.*;


public class FactorialEffectsApplet extends AnovaTableOnlyApplet {
	static final protected String LONG_NAMES_PARAM = "longNames";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 20));
		
		add("North", displayPanel(data));
		
		add("Center", effectsPanel(data));
	}
	
	
	protected XPanel effectsPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yVariateName = new XLabel(translate("Effects on mean") + " " + yVar.name, XLabel.CENTER, this);
			yVariateName.setForeground(Color.blue);
			yVariateName.setFont(getStandardBoldFont());
			
		thePanel.add("North", yVariateName);
			
			StringTokenizer st = new StringTokenizer(getParameter(LONG_NAMES_PARAM));
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new ProportionLayout(0.5, 5));
				
				topPanel.add(ProportionLayout.LEFT, oneEffectPanel(data, "model", 0, st.nextToken()));
				topPanel.add(ProportionLayout.RIGHT, oneEffectPanel(data, "model", 1, st.nextToken()));
				
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new ProportionLayout(0.5, 5));
				
				bottomPanel.add(ProportionLayout.LEFT, oneEffectPanel(data, "model", 2, st.nextToken()));
				
				bottomPanel.add(ProportionLayout.RIGHT, oneEffectPanel(data, "model", 3, st.nextToken()));
				
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
			
		thePanel.add("Center", mainPanel);
			
		return thePanel;
	}
	
	private XPanel oneEffectPanel(DataSet data, String modelKey, int horizIndex,
																																								String xName) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				MultiFactorModel model = (MultiFactorModel)data.getVariable(modelKey);
				String mainEffectKeys[] = model.getTermKeys()[0];
				CatVariable horizVar = (CatVariable)data.getVariable(mainEffectKeys[horizIndex]);
				HorizAxis xAxis = new HorizAxis(this);
//				int nXCats = horizVar.noOfCategories();
				xAxis.setCatLabels(horizVar);
				if (xName == null)
					xName = horizVar.name;
				xAxis.setAxisName(xName);
			
			scatterPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				yAxis.readNumLabels(labelInfo);
			
			scatterPanel.add("Left", yAxis);
				
				FactorialEffectView theView = new FactorialEffectView(data, this, "y", mainEffectKeys,
																													modelKey, xAxis, yAxis, horizIndex);
				theView.setFont(getSmallFont());
				theView.lockBackground(Color.white);
			
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
		
		return thePanel;
	}
}