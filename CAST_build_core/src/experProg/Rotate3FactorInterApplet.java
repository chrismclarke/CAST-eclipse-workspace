package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;


public class Rotate3FactorInterApplet extends RotateThreeFactorLSApplet {
	static final private String INTERACTION_PARAM = "interaction";
	
	private XCheckbox interactCheck;
	private int interact1, interact2;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		add("Center", rotatePanel(data));
		add("East", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		FactorsModel model = (FactorsModel)data.getVariable("model");
		
			StringTokenizer st = new StringTokenizer(getParameter(INTERACTION_PARAM));
			interact1 = Integer.parseInt(st.nextToken());
			interact2 = Integer.parseInt(st.nextToken());
		model.setInteractionVariables(interact1, interact2);
		
		return data;
	}
	
	protected XPanel effectPanel(DataSet data) {
		XPanel effectPanel = super.effectPanel(data);
		
			String factor1Key = "treat" + (interact1+1);
			String factor2Key = "treat" + (interact2+1);
			interactCheck = new XCheckbox(data.getVariable(factor1Key).name + "-" + translate("by")
																						+ "-" + data.getVariable(factor2Key).name, this);
			interactCheck.setState(false);
			interactCheck.disable();
			fitInteraction = false;
		effectPanel.add(interactCheck);
			
		return effectPanel;
	}
	
	private void checkInteraction() {
		if (fitInteraction) {
			factorCheck[interact1].disable();
			factorCheck[interact2].disable();
		}
		else {
			factorCheck[interact1].enable();
			factorCheck[interact2].enable();
		}
		if (fitFactor[interact1] && fitFactor[interact2])
			interactCheck.enable();
		else
			interactCheck.disable();
	}
	
	private boolean localAction(Object target) {
		if (target == interactCheck) {
			fitInteraction = interactCheck.getState();
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", fitFactor, fitInteraction);
			data.variableChanged("model");
			checkInteraction();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what)) {
			checkInteraction();
			return true;
		}
		return localAction(evt.target);
	}
}