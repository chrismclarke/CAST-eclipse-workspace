package twoGroup;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class GroupSummaryPanel extends XPanel {
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	static final private Color kGreenColor = new Color(0x006600);
	
	static final private String kMaxSummaryString = "999.99";
	static final private String kMaxCountString = "9999";
	
	private GroupSummaryView meanView, sdView;
	
	public GroupSummaryPanel(XApplet applet, GroupsDataSet data, int group, int orientation) {
		GroupSummaryView countView = new GroupSummaryView(data, applet, GroupSummaryView.N, group, kMaxCountString, 0);
		countView.setForeground(Color.black);
		meanView = new GroupSummaryView(data, applet, GroupSummaryView.X_BAR, group, kMaxSummaryString,
												data.getSummaryDecimals());
		meanView.setForeground(Color.blue);
		sdView = new GroupSummaryView(data, applet, GroupSummaryView.S, group, kMaxSummaryString,
												data.getSummaryDecimals());
		sdView.setForeground(kGreenColor);
		
		if (orientation == HORIZONTAL) {
			setLayout(new ProportionLayout(0.333, 4, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
				
			add(ProportionLayout.LEFT, countView);
				
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new ProportionLayout(0.5, 4, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
				rightPanel.add(ProportionLayout.LEFT, meanView);
				rightPanel.add(ProportionLayout.RIGHT, sdView);
			
			add(ProportionLayout.RIGHT, rightPanel);
		}
		else {
			setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
			add(countView);
			add(meanView);
			add(sdView);
		}
	}
	
	public void setDecimals(int meanDecimals, int sdDecimals) {
		meanView.setDecimals(meanDecimals);
		sdView.setDecimals(sdDecimals);
	}
	
	public double getSd() {
		return Double.parseDouble(sdView.getValueString());		//	rounded to displayed value
	}
	
	public double getMean() {
		return Double.parseDouble(meanView.getValueString());		//	rounded to displayed value
	}
}