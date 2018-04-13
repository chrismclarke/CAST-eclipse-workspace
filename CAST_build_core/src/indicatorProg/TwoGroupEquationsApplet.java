package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import indicator.*;


public class TwoGroupEquationsApplet extends CoreTwoGroupApplet {
	static final private String SHORT_NAMES_PARAM = "shortNames";
	static final private String CAN_SHOW_PARALLEL_PARAM = "canShowParallel";
	
	static final private double[] kCommonLineConstraints = {Double.NaN, Double.NaN, 0.0, 0.0};
	static final private double[] kEqualSlopeConstraints = {Double.NaN, Double.NaN, Double.NaN, 0.0};
	static final private double[] kNoConstraints = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
	
	static final private Color kEqnBackground = new Color(0xFFEEBB);
	static final private Color kGroupLabelColor = new Color(0x000066);
	
	private XChoice modelChoice;
	private int currentModelIndex = 0;
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 12));
			
			String parallelString = getParameter(CAN_SHOW_PARALLEL_PARAM);
			boolean canShowParallel = (parallelString == null) || (parallelString.equals("true"));
			
			modelChoice = new XChoice(this);
			modelChoice.addItem(translate("Common line"));
			modelChoice.addItem(translate("Separate lines"));
			if (canShowParallel)
				modelChoice.addItem(translate("Equal slopes"));
			
		thePanel.add(modelChoice);
		
			XPanel eqnPanel = new InsetPanel(10, 5);
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 10));
			
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			String yName = st.nextToken();
			String xName = st.nextToken();
			
			CatVariable groupVar = (CatVariable)data.getVariable("z");
			for (int i=0 ; i<2 ; i++) {
				XPanel groupPanel = new XPanel();
				groupPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
					XLabel groupLabel = new XLabel(groupVar.getLabel(i).toString(), XLabel.LEFT, this);
					groupLabel.setFont(getBigBoldFont());
					groupLabel.setForeground(kGroupLabelColor);
				
				groupPanel.add(groupLabel);
				
					GroupEquationView eqn = new GroupEquationView(data, this, "ls", yName, xName,
																		maxParam[0], maxParam[0], maxParam[1], maxParam[1], i);
					eqn.setFont(getBigFont());
				
				groupPanel.add(eqn);
				
				eqnPanel.add(groupPanel);
			}
		
			eqnPanel.lockBackground(kEqnBackground);
			
		thePanel.add(eqnPanel);
		
		setModel(0);
		
		return thePanel;
	}
	
	private void setModel(int modelIndex) {
		double constraints[] = (modelIndex == 0) ? kCommonLineConstraints
													: (modelIndex == 1) ? kNoConstraints
													: kEqualSlopeConstraints;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("ls");
		model.updateLSParams("y", constraints);
		
		data.variableChanged("ls");
	}
	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newChoice = modelChoice.getSelectedIndex();
			if (newChoice != currentModelIndex) {
				currentModelIndex = newChoice;
				
				setModel(newChoice);
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