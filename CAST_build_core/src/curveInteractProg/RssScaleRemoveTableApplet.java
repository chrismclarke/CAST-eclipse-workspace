package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import imageUtils.*;

import curveInteract.*;


public class RssScaleRemoveTableApplet extends TestRemoveTableApplet {
	static final private String SSQ_AXIS_LABELS_PARAM = "ssqAxisLabels";
	
	private double getTotalSsq(DataSet data, String yKey) {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		return (syy - sy * sy / n);
	}
	
	protected XPanel rightPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XLabel rssLabel = new XLabel(translate("Resid ssq"), XLabel.CENTER, this);
			rssLabel.setFont(getStandardBoldFont());
		thePanel.add("North", rssLabel);
		
			StringTokenizer st = new StringTokenizer(getParameter(SSQ_AXIS_LABELS_PARAM));
			int noOfLabels = st.countTokens();
			NumValue axisLabel[] = new NumValue[noOfLabels];
			for (int i=0 ; i<noOfLabels ; i++)
				axisLabel[i] = new NumValue(st.nextToken());
		
			double maxRss = getTotalSsq(data, "y");
			CoreComponentVariable residVar = (CoreComponentVariable)data.getVariable("resid");
			double minRss = residVar.getSsq();
			
			ResidSsqScaleView theView = new ResidSsqScaleView(summaryData, this, "rss", minRss, maxRss, axisLabel);
		
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			if (!onlyDeleteOne) {
				removeAllButton = new XButton(translate("Remove all"), this);
				buttonPanel.add(removeAllButton);
			
				addAllButton = new XButton(translate("Add all"), this);
				buttonPanel.add(addAllButton);
			}
			
		thePanel.add("Center", buttonPanel);
			
			XPanel rssPanel = new XPanel();
			rssPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				OneValueImageView ssqView = new OneValueImageView(summaryData, "rss", this, "xEquals/residualSsqBlack.png", 13, maxSsq);
				ssqView.setHighlightSelection(false);
				ssqView.setFont(getBigFont());
			rssPanel.add(ssqView);
			
		thePanel.add("East", rssPanel);
		return thePanel;
	}
}