package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import sampling.*;


public class RandomHistoApplet extends RandomBarchartApplet {
	private XButton widerButton, narrowerButton;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(PARAM_PARAM), "#");
		if (st.countTokens() == 1) {
			NormalDistnVariable y = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
			y.setParams(st.nextToken());
			data.addVariable("y", y);
		}
		else {
			NormalDistnVariable y1 = new NormalDistnVariable("y1");
			y1.setParams(st.nextToken());
			data.addVariable("y1", y1);
			
			NormalDistnVariable y2 = new NormalDistnVariable("y2");
			y2.setParams(st.nextToken());
			data.addVariable("y2", y2);
			
			MixtureDistnVariable y = new MixtureDistnVariable(getParameter(VAR_NAME_PARAM), y1, y2);
			y.setParams(st.nextToken());
			data.addVariable("y", y);
		}
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel probLabel = new XLabel(translate("Density"), XLabel.LEFT, this);
		probLabel.setFont(theProbAxis.getFont());
		thePanel.add(probLabel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																					VerticalLayout.VERT_CENTER, 5));
		
		XPanel widthPanel = new XPanel();
		widthPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		narrowerButton = new XButton(translate("Narrower"), this);
		widthPanel.add(narrowerButton);
		widerButton = new XButton(translate("Wider"), this);
		widthPanel.add(widerButton);
		
		thePanel.add(widthPanel);
		
		thePanel.add(super.controlPanel(data));
		
		return thePanel;
	}
	
	private void checkButtonHighlight() {
		int currentGroupingLevel = theView.getGroupingLevel();
		if (currentGroupingLevel > 0)
			narrowerButton.enable();
		else
			narrowerButton.disable();
		if (currentGroupingLevel < theView.getMaxGroupingLevel())
			widerButton.enable();
		else
			widerButton.disable();
	}

	
	private boolean localAction(Object target) {
		if (target == narrowerButton) {
			theView.changeGroupingLevel(RandomHistoView.NARROWER);
			checkButtonHighlight();
			return true;
		}
		else if (target == widerButton) {
			theView.changeGroupingLevel(RandomHistoView.WIDER);
			checkButtonHighlight();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}