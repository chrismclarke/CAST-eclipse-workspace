package residProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;

import regn.*;
import resid.*;


public class ParamInfluenceApplet extends FitInfluenceApplet {
	static final private String INT_CHANGE_AXIS_PARAM = "intChangeAxis";
	static final private String SLOPE_CHANGE_AXIS_PARAM = "slopeChangeAxis";
	static final private String SHORT_Y_NAME_PARAM = "shortYVarName";
	static final private String SHORT_X_NAME_PARAM = "shortXVarName";
	
	static final private Color kEqnBackground = new Color(0xDDDDFF);
	
//	private DataSet data;
	
	private XChoice paramChoice;
	private int currentParam = 0;
	
	private LinearEquationView fullEqn;
	private DeletedLinEquationView deletedEqn;
	
	private XPanel diffPanel;
	private CardLayout diffPanelLayout;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
			DeletedChangeVariable intChangeVar = new DeletedChangeVariable(translate("Change in intercept") + " =",
													data, "y", "x", "ls", "deletedLS", DeletedChangeVariable.INTERCEPT,
													maxIntercept.decimals);
		data.addVariable("intChange", intChangeVar);
		
			DeletedChangeVariable slopeChangeVar = new DeletedChangeVariable(translate("Change in slope") + " =",
													data, "y", "x", "ls", "deletedLS", DeletedChangeVariable.SLOPE,
													maxSlope.decimals);
		data.addVariable("slopeChange", slopeChangeVar);
		
		return data;
	}
	
	protected boolean showFitOnDataPlot() {
		return false;
	}
	
	protected XPanel influencePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				paramChoice = new XChoice(this);
				paramChoice.addItem(translate("Change in intercept"));
				paramChoice.addItem(translate("Change in slope"));
			choicePanel.add(paramChoice);
		
		thePanel.add("North", choicePanel);
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				NumVariable xVar = (NumVariable)data.getVariable("x");
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				influenceVertAxis = new MultiVertAxis(this, 2);
				influenceVertAxis.setChangeMinMax(true);
				influenceVertAxis.readNumLabels(getParameter(INT_CHANGE_AXIS_PARAM));
				influenceVertAxis.readExtraNumLabels(getParameter(SLOPE_CHANGE_AXIS_PARAM));
			plotPanel.add("Left", influenceVertAxis);
			
				influenceView = new HiliteOneResidualView(data, this, horizAxis, influenceVertAxis, "x", "intChange", null);
				influenceView.setRetainLastSelection(true);
				influenceView.lockBackground(Color.white);
			plotPanel.add("Center", influenceView);
		
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel mainPanel = new InsetPanel(10, 3);
			mainPanel.setLayout(new BorderLayout(8, 0));
			
				XPanel leftPanel = new XPanel();
				leftPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 4));
				
					XLabel fullLabel = new XLabel(translate("Full model") + ":", XLabel.RIGHT, this);
					fullLabel.setFont(getStandardBoldFont());
				leftPanel.add(fullLabel);
				
					XLabel deletedLabel = new XLabel(translate("Deleted model") + ":", XLabel.RIGHT, this);
					deletedLabel.setFont(getStandardBoldFont());
				leftPanel.add(deletedLabel);
				
			mainPanel.add("West", leftPanel);
			
				XPanel eqnPanel = new XPanel();
				eqnPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_SPACED, 4));
				
					String yName = getParameter(SHORT_Y_NAME_PARAM);
					String xName = getParameter(SHORT_X_NAME_PARAM);
					fullEqn = new LinearEquationView(data, this, "ls", yName, xName,
																					maxIntercept, maxIntercept, maxSlope, maxSlope);
					fullEqn.setSelectedParamIndex(0);
				eqnPanel.add(fullEqn);
				
					deletedEqn = new DeletedLinEquationView(data, this, "deletedLS", "y", yName, xName, maxIntercept, maxSlope);
					deletedEqn.setSelectedParamIndex(0);
					
				eqnPanel.add(deletedEqn);
				
			mainPanel.add("Center", eqnPanel);
			
				XPanel rightPanel = new InsetPanel(30, 0, 0, 0);
				rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
					diffPanel = new XPanel();
					diffPanelLayout = new CardLayout();
					diffPanel.setLayout(diffPanelLayout);
					
						XPanel intPanel = new XPanel();
						intPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
							OneValueView intChangeView = new OneValueView(data, "intChange", this, maxIntercept);
						intPanel.add(intChangeView);
						
					diffPanel.add("interceptChange", intPanel);
					
						XPanel slopePanel = new XPanel();
						slopePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
							OneValueView slopeChangeView = new OneValueView(data, "slopeChange", this, maxSlope);
						slopePanel.add(slopeChangeView);
						
					diffPanel.add("slopeChange", slopePanel);
					diffPanelLayout.show(diffPanel, "interceptChange");
					
				rightPanel.add(diffPanel);
				
			mainPanel.add("East", rightPanel);
		
			mainPanel.lockBackground(kEqnBackground);
		thePanel.add(mainPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == paramChoice) {
			int newChoice = paramChoice.getSelectedIndex();
			if (newChoice != currentParam) {
				currentParam = newChoice;
				influenceVertAxis.setAlternateLabels(newChoice);
				influenceView.changeVariables(newChoice == 0 ? "intChange" : "slopeChange", "x");
				
				fullEqn.setSelectedParamIndex(newChoice);
				fullEqn.repaint();
				deletedEqn.setSelectedParamIndex(newChoice);
				deletedEqn.repaint();
				
				diffPanelLayout.show(diffPanel, newChoice == 0 ? "interceptChange" : "slopeChange");
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