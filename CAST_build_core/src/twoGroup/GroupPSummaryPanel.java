package twoGroup;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class GroupPSummaryPanel extends XPanel {
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
//	static final private Color kGreenColor = new Color(0x006600);
	
	static final private String kMaxSummaryString = "999.99";
	static final private String kMaxCountString = "9999";
	
	public GroupPSummaryPanel(XApplet applet, ContinTableDataSet data, int group,
																								int orientation) {
		GroupSummaryView countView = new GroupSummaryView(data, applet, GroupSummaryView.N, group, kMaxCountString, 0);
		countView.setForeground(Color.black);
		GroupSummaryView propnView = new GroupSummaryView(data, applet, GroupSummaryView.P, group, kMaxSummaryString,
																									data.getSummaryDecimals());
		propnView.setForeground(Color.blue);
		
		if (orientation == HORIZONTAL) {
			setLayout(new ProportionLayout(0.5, 4, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
				
			add(ProportionLayout.LEFT, countView);
			add(ProportionLayout.RIGHT, propnView);
		}
		else {
			setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
			add(countView);
			add(propnView);
		}
	}
}