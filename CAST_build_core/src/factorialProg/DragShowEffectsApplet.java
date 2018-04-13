package factorialProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import graphics3D.*;

import factorial.*;


public class DragShowEffectsApplet extends DragFactorialApplet {
	static final protected String EFFECT_PANEL_HEIGHT_PARAM = "effectPanelHeight";
	
	protected XPanel eastPanel(DataSet data) {
		XPanel rotatePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
			rotateButton = new XButton(translate("Spin"), this);
		rotatePanel.add(rotateButton);
		
		if (hasY && !alwaysLS) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 20));
			thePanel.add(rotatePanel);
			
				lsButton = new XButton(translate("Least squares"), this);
			thePanel.add(lsButton);
			
			return thePanel;
		}
		else
			return rotatePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yVariateName = new XLabel(translate("Effects on mean") + " " + yVar.name, XLabel.CENTER, this);
			yVariateName.setForeground(Color.blue);
			yVariateName.setFont(getStandardBoldFont());
			
		thePanel.add("North", yVariateName);
		
			XPanel mainPanel = new XPanel();
			int effectPanelHeight = Integer.parseInt(getParameter(EFFECT_PANEL_HEIGHT_PARAM));
			mainPanel.setLayout(new FixedSizeLayout(50, effectPanelHeight));
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
				
					XPanel topPanel = new XPanel();
					topPanel.setLayout(new ProportionLayout(0.5, 5));
					
					topPanel.add(ProportionLayout.LEFT, effectDisplayPanel(data, "model", 0,
																																getParameter(X_LONG_NAME_PARAM)));
					topPanel.add(ProportionLayout.RIGHT, effectDisplayPanel(data, "model", 1,
																																getParameter(Z_LONG_NAME_PARAM)));
					
				innerPanel.add(ProportionLayout.TOP, topPanel);
				
					XPanel bottomPanel = new XPanel();
					bottomPanel.setLayout(new ProportionLayout(0.5, 5));
					
					bottomPanel.add(ProportionLayout.LEFT, effectDisplayPanel(data, "model", 2,
																																getParameter(W_LONG_NAME_PARAM)));
					
					bottomPanel.add(ProportionLayout.RIGHT, new FactorialEffectPanel(data, "model",
															alwaysLS, "y", FactorialEffectPanel.COLUMNS, translate("Terms in model"), this));
					
				innerPanel.add(ProportionLayout.BOTTOM, bottomPanel);
				
			mainPanel.add(innerPanel);
			
		thePanel.add("Center", mainPanel);
			
		return thePanel;
	}
	
	private XPanel effectDisplayPanel(DataSet data, String modelKey, int horizIndex,
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