package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;

import stdError.*;


public class PopnSeVsSdApplet extends ErrorDistnTheoryApplet {
	
	static final protected Color kParamStatBackground = new Color(0xEDF2FF);
	
	public void setupApplet() {
		showError = getParameter(ERROR_AXIS_PARAM) != null;
		data = getData();
		
		setLayout(new BorderLayout());
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																									ProportionLayout.TOTAL));
				dataPanel.add(ProportionLayout.LEFT, distributionPanel(data, "model", kPopnColor,
																					kNormalPopnColor, getParameter(AXIS_INFO_PARAM)));
				dataPanel.add(ProportionLayout.RIGHT, distributionPanel(data, "meanErrorDistn",
															kErrorColor, kNormalErrorColor, getParameter(ERROR_AXIS_PARAM)));
		
		add("Center", dataPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
				innerPanel.add(summaryPanel(data));
				innerPanel.add(sampleSizePanel(false));
				
			bottomPanel.add(innerPanel);
		
		add("South", bottomPanel);
		
		setTheoryParameters(data);
	}
	
	private XPanel summaryPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(12, 5);
		thePanel.setLayout(new GridBagLayout());
		
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			
			Insets topInsets = new Insets(0, 10, 0, 10);
			Insets leftInsets = new Insets(10, 0, 10, 10);
			Insets spaceAround = new Insets(10, 10, 10, 10);
			
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.gridx = 1;
			c.gridy = 0;
			c.insets = topInsets;
//			c.ipady = 10;
//			c.ipadx = 20;
			XLabel sdLabel = new XLabel(translate("Standard deviation"), XLabel.CENTER, this);
			sdLabel.setFont(getStandardBoldFont());
		thePanel.add(sdLabel, c);
		
			c.gridx = 2;
			XLabel seLabel = new XLabel(translate("Standard error"), XLabel.CENTER, this);
			seLabel.setFont(getStandardBoldFont());
			seLabel.setForeground(Color.red);
		thePanel.add(seLabel, c);
			
			c.anchor = GridBagConstraints.EAST;
			c.weightx = 0.0;
			c.weighty = 1.0;
			c.gridx = 0;
			c.gridy = 1;
			c.insets = leftInsets;
//			c.ipadx = 0;
			XLabel theoryLabel = new XLabel(translate("Theory"), XLabel.LEFT, this);
			theoryLabel.setFont(getStandardBoldFont());
		thePanel.add(theoryLabel, c);
			
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.gridx = 1;
			c.gridy = 1;
			c.insets = spaceAround;
//			c.ipadx = 20;
			StDevnValueView sdView = new StDevnValueView(data, this, "model", true, modelMean);
			thePanel.add(sdView, c);
		
			c.gridx = 2;
			c.gridy = 1;
			StdErrorValueView seView = new StdErrorValueView(data, this, "meanErrorDistn", false, modelMean);
			thePanel.add(seView, c);
		
		thePanel.lockBackground(kParamStatBackground);
		return thePanel;
	}
	
}