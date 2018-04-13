package inference;

import java.awt.*;

import dataView.*;
import valueList.OneValueView;
import utils.*;


public class TLookupPanel extends XPanel {
	static final private Color kPanelColor = new Color(0x006600);
	
	private TTableDataSet tData;
	private OneValueView tValue;
	
	public TLookupPanel(XApplet applet, String title) {
		tData = new TTableDataSet(0.975);
		
		setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
		Font calculationFont = applet.getBigFont();
		setFont(calculationFont);
		
		if (title != null) {
			XLabel heading = new XLabel(title, XLabel.CENTER, applet);
			heading.setFont(applet.getBigBoldFont());
			heading.setForeground(kPanelColor);
			add(heading);
		}
		
		DFEditPanel dfPanel = new DFEditPanel(tData, applet);
		dfPanel.setForeground(kPanelColor);
		dfPanel.setFont(calculationFont);
		add(dfPanel);
		
		tValue = new OneValueView(tData, "t", applet);
		tValue.setForeground(kPanelColor);
		tValue.setFont(calculationFont);
		add(tValue);
	}
	
}