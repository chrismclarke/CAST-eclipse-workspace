package exper2Prog;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import twoFactor.*;


public class MeansDiagramApplet extends XApplet {
							// ****
							// ****		Can only shows main effects & interaction for X & Z and (optionally) main effects for W
							// ****
	static final private String XZ_INTERACTION_PARAM = "xzInteraction";
	
	static final private String W_VAR_NAME_PARAM = "wVarName";
	static final private String W_VALUES_PARAM = "wValues";
	static final private String W_LABELS_PARAM = "wLabels";
	
	static final private String[] kXZFactorKeys = {"x", "z"};
	static final private String[] kWFactorKey = {"w"};
	
	private boolean hasW = false;
	private TwoFactorDataSet data;
	
	private XCheckbox interactionCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
			
		if (hasW) {
			XPanel effectPanel = new XPanel();
			effectPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
			
			effectPanel.add(ProportionLayout.TOP, wEffectPanel(data));
			effectPanel.add(ProportionLayout.BOTTOM, xzEffectsPanel(data));
			
			add("Center", effectPanel);
		}
		else
			add("Center", xzEffectsPanel(data));
		
		if (getParameter(XZ_INTERACTION_PARAM).equals("check"))
			add("South", controlPanel());
	}
	
	private TwoFactorDataSet readData() {
		TwoFactorDataSet data = new TwoFactorDataSet(this);
		
		if (getParameter(XZ_INTERACTION_PARAM).equals("true")) {
			TwoFactorModel ls = (TwoFactorModel)data.getVariable("ls");
			ls.setModelType(TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, true);
			ls.updateLSParams("y");
		}
		
		String wVarName = getParameter(W_VAR_NAME_PARAM);
		if (wVarName != null) {
			hasW = true;
			data.addCatVariable("w", wVarName, getParameter(W_VALUES_PARAM), getParameter(W_LABELS_PARAM));
			
				GroupsModelVariable lsW = new GroupsModelVariable("LS for W", data, "w");
				lsW.updateLSParams("y");
			data.addVariable("lsW", lsW);
		}
		
		return data;
	}
	
	private XPanel wEffectPanel(TwoFactorDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.75, 0));
		thePanel.add(ProportionLayout.RIGHT, new XPanel());
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.3333, 0));
			leftPanel.add(ProportionLayout.LEFT, new XPanel());
			
			leftPanel.add(ProportionLayout.RIGHT, effectPanel(data, kWFactorKey, 0, "lsW"));
			
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
		return thePanel;
	}
	
	private XPanel xzEffectsPanel(TwoFactorDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		thePanel.add(ProportionLayout.LEFT, effectPanel(data, kXZFactorKeys, 0, "ls"));
		thePanel.add(ProportionLayout.RIGHT, effectPanel(data, kXZFactorKeys, 1, "ls"));
		
		return thePanel;
	}
	
	
	private XPanel effectPanel(TwoFactorDataSet data, String[] factorKeys, int horizIndex, String lsKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				CatVariable xVar = (CatVariable)data.getVariable(factorKeys[horizIndex]);
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
			
			scatterPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = data.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
			
			scatterPanel.add("Left", yAxis);
				
				EffectDiagramView theView = new EffectDiagramView(data, this, "y", factorKeys, lsKey,
																																				xAxis, yAxis, horizIndex);
				theView.lockBackground(Color.white);
			
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			NumVariable yVar = (NumVariable)data.getVariable("y");
			XLabel yVariateName = new XLabel(translate("Mean") + " " + yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		interactionCheck = new XCheckbox(translate("Show interaction"), this);
		thePanel.add(interactionCheck);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == interactionCheck) {
			TwoFactorModel ls = (TwoFactorModel)data.getVariable("ls");
			ls.setModelType(TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, interactionCheck.getState());
			ls.updateLSParams("y");
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}