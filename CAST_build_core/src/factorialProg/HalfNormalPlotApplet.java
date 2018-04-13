package factorialProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import factorial.*;


public class HalfNormalPlotApplet extends AnovaTableOnlyApplet {
	static final protected String EFFECT_AXIS_INFO_PARAM = "effectAxis";
	static final protected String Z_AXIS_INFO_PARAM = "zAxis";
	
	private HalfNormalPlotView effectView;
	
	private XCheckbox centrePointCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 20));
		
		add("North", displayPanel(data));
		
		add("Center", bottomPanel(data));
	}
	
	protected FactorialDataSet readData() {
		FactorialDataSet data = super.readData();
		MultiFactorModel model = (MultiFactorModel)data.getVariable("model");
		if (model instanceof CentrePointFactorialModel)
			((CentrePointFactorialModel)model).setUseCentrePoints(false);
		
		String[][] allTermKeys = model.getTermKeys();
		int[][] fullModelTerms = new int[1][allTermKeys[0].length];
		for (int i=0 ; i<fullModelTerms[0].length ; i++)
			fullModelTerms[0][i] = i;
		
		MultiFactorModel fullModel = new MultiFactorModel("Full model", data, allTermKeys,
																														fullModelTerms);
		data.addVariable("fullModel", fullModel);
		
		return data;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		MultiFactorModel model = (MultiFactorModel)data.getVariable("model");
		if (model instanceof CentrePointFactorialModel) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(20, 0));
			thePanel.add("Center", halfNormalPlotPanel(data));
			
				XPanel checkPanel = new XPanel();
				checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																												VerticalLayout.VERT_CENTER, 0));
				
					centrePointCheck = new XCheckbox(translate("Use centre points"), this);
				checkPanel.add(centrePointCheck);
			
			thePanel.add("East", checkPanel);
			
			return thePanel;
		}
		else {
			XPanel thePanel = new InsetPanel(70, 5, 70, 0);
			thePanel.setLayout(new BorderLayout(0, 0));
				
			thePanel.add("Center", halfNormalPlotPanel(data));
				
			return thePanel;
		}
	}
	
	protected XPanel halfNormalPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XLabel effectTitle = new XLabel(translate("Half normal quantile"), XLabel.LEFT, this);
		thePanel.add("North", effectTitle);
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				VertAxis zAxis = new VertAxis(this);
				zAxis.readNumLabels(getParameter(Z_AXIS_INFO_PARAM));
			
			scatterPanel.add("Left", zAxis);
			
				HorizAxis effectAxis = new HorizAxis(this);
				effectAxis.readNumLabels(getParameter(EFFECT_AXIS_INFO_PARAM));
				effectAxis.setAxisName(translate("Effect"));
			
			scatterPanel.add("Bottom", effectAxis);
				
				effectView = new HalfNormalPlotView(data, this, "y", "fullModel", "model", effectAxis, zAxis);
				effectView.setCrossSize(DataView.LARGE_CROSS);
				effectView.setFont(getSmallFont());
				effectView.lockBackground(Color.white);
			
			scatterPanel.add("Center", effectView);
				
		thePanel.add("Center", scatterPanel);
			
		return thePanel;
	}
	
	public void modelChanged() {
		effectView.repaint();
	}
	
	private boolean localAction(Object target) {
		if (target == centrePointCheck) {
			CentrePointFactorialModel model = (CentrePointFactorialModel)data.getVariable("model");
			model.setUseCentrePoints(centrePointCheck.getState());
			data.variableChanged("model");
			effectView.repaint();
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}