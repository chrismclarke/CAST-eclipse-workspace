package statisticProg;

import java.awt.*;

import dataView.*;
import utils.*;

import statistic.*;


public class DragCrossSdApplet extends DragCrossRmseApplet {
	
	private XChoice popNotSampChoice;
	private boolean currentPopNotSamp = true;
	
	protected LayoutManager getSummaryLayout() {
		return new FlowLayout(FlowLayout.CENTER, 20, 0);
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(getSummaryLayout());
			
			XPanel innerPanel = new XPanel();
//			innerPanel.setLayout(new BorderLayout(10, 0));
			innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
			
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
					popNotSampChoice = new XChoice(this);
					popNotSampChoice.addItem(translate("Population standard deviation"));
					popNotSampChoice.addItem(translate("Sample standard deviation, s"));
					
				choicePanel.add(popNotSampChoice);
//			innerPanel.add("West", choicePanel);	
			innerPanel.add(choicePanel);	
				
				NumValue maxValue = new NumValue(getParameter(MAX_SUMMARY_PARAM));
				summaryValue = new SsqDeviationsView(data, this, "xEquals/redEquals.png", 7, "y", maxValue, theView);
				summaryValue.setForeground(kSummaryColor);
//			innerPanel.add("Center", summaryValue);
			innerPanel.add(summaryValue);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == popNotSampChoice) {
			boolean newPopNotSamp = popNotSampChoice.getSelectedIndex() == 0;
			if (newPopNotSamp != currentPopNotSamp) {
				currentPopNotSamp = newPopNotSamp;
				theView.setPopNotSamp(newPopNotSamp);
				theView.repaint();
				summaryValue.setPopNotSamp(newPopNotSamp);
//				summaryValue.repaint();
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