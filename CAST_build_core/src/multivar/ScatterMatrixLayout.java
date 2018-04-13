package multivar;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;


public class ScatterMatrixLayout implements LayoutManager {
	private HorizAxis xAxis[];			//	xAxis[i] is for variable i
	private VertAxis yAxis[];			//	yAxis[i] is for variable i+1
	private DataView graph[][];		//	graph[i,j] is for x-variable i, y-variable j+1
	private XLabel xLabel[], yLabel[];
	private XPanel controlPanel;
	private int noOfVariables, horizBorder, vertBorder;
	
	private int xAxisIndex = 0;
	private int yAxisIndex = 0;
	private int xLabelIndex = 0;
	private int yLabelIndex = 0;
	private int xGraphIndex = 0;
	private int yGraphIndex = 0;
	
	static final private int kLabelAxisGap = 2;
	
	public ScatterMatrixLayout(int noOfVariables, int horizBorder, int vertBorder) {
		this.noOfVariables = noOfVariables;
		this.horizBorder = horizBorder;
		this.vertBorder = vertBorder;
		xAxis = new HorizAxis[noOfVariables - 1];
		yAxis = new VertAxis[noOfVariables - 1];
		xLabel = new XLabel[noOfVariables - 1];
		yLabel = new XLabel[noOfVariables - 1];
		graph = new DataView[noOfVariables - 1][];
		for (int i=0 ; i<noOfVariables - 1 ; i++)
			graph[i] = new DataView[i + 1];
	}
	
	public void addLayoutComponent(String name, Component comp) {
		try {
			if (name.equals("XLabel"))
				xLabel[xLabelIndex ++] = (XLabel)comp;
			else if (name.equals("YLabel"))
				yLabel[yLabelIndex ++] = (XLabel)comp;
			else if (name.equals("XAxis"))
				xAxis[xAxisIndex ++] = (HorizAxis)comp;
			else if (name.equals("YAxis"))
				yAxis[yAxisIndex ++] = (VertAxis)comp;
			else if (name.equals("XPanel"))
				controlPanel = (XPanel)comp;
			else {
				graph[yGraphIndex][xGraphIndex ++] = (DataView)comp;
				if (xGraphIndex > yGraphIndex) {
					xGraphIndex = 0;
					yGraphIndex ++;
				}
			}
		} catch (ClassCastException e) {
			System.err.println("Bad component passed to layout");
		}
	}
	
	public void removeLayoutComponent(Component comp) {
		//		can't remove component from layout
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(300, 300);				//		this probably won't get called by Applet
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(30, 30);				//		this probably won't get called by Applet
	}
	
	public void layoutContainer(Container parent) {
		int maxYLabelWidth = 0;
		for (int i=0 ; i<yLabel.length ; i++)
			if (yLabel[i] != null)
				maxYLabelWidth = Math.max(maxYLabelWidth, yLabel[i].getPreferredSize().width);
		
		int maxXLabelHeight = 0;
		for (int i=0 ; i<xLabel.length ; i++)
			if (xLabel[i] != null)
				maxXLabelHeight = Math.max(maxXLabelHeight, xLabel[i].getPreferredSize().height);
		
		int maxYAxisWidth = 0;
		for (int i=0 ; i<yAxis.length ; i++)
			if (yAxis[i] != null) {
				yAxis[i].findAxisWidth();
				maxYAxisWidth = Math.max(maxYAxisWidth, yAxis[i].getAxisWidth());
			}
		
		Insets insets = parent.getInsets();
		int maxWidth = parent.getSize().width - (insets.left + insets.right);
		int maxHeight = parent.getSize().height - (insets.top + insets.bottom);
		
		int oneGraphWidth = (maxWidth - maxYLabelWidth - maxYAxisWidth - kLabelAxisGap)
																	/ (noOfVariables - 1) - 2 * horizBorder;
		int maxXAxisHeight = 0;
		for (int i=0 ; i<xAxis.length ; i++) {
			xAxis[i].findLabelSizes();
			xAxis[i].setBordersAndLength(horizBorder, horizBorder, oneGraphWidth);
			xAxis[i].findAxisWidth();
			maxXAxisHeight = Math.max(maxXAxisHeight, xAxis[i].getAxisWidth());
		}
		
		int oneGraphHeight = (maxHeight - maxXAxisHeight - maxXLabelHeight - kLabelAxisGap)
																	/ (noOfVariables - 1) - 2 * vertBorder;
		for (int i=0 ; i<yAxis.length ; i++)
			yAxis[i].setBordersAndLength(vertBorder, vertBorder, oneGraphHeight);
		
		int topPos = 0;
		for (int i=0 ; i<yAxis.length ; i++) {
			int leftPos = 0;
			if (yLabel[i] != null) {
				Dimension preferred = yLabel[i].getPreferredSize();
				yLabel[i].setBounds(leftPos, topPos + vertBorder
						+ (oneGraphHeight - preferred.height) / 2, maxYLabelWidth, preferred.height);
			}
			leftPos += maxYLabelWidth + kLabelAxisGap;
			if (yAxis[i] != null) 
				yAxis[i].setBounds(leftPos, topPos, maxYAxisWidth, oneGraphHeight + 2 * vertBorder);
			
			leftPos += maxYAxisWidth;
			for (int j=0 ; j<=i ; j++) {
				Insets border = graph[i][j].getViewBorder();
				graph[i][j].setBounds(leftPos + horizBorder - border.left,
									topPos + vertBorder - border.top,
									oneGraphWidth + border.left + border.right,
									oneGraphHeight + border.top + border.bottom);
				leftPos += oneGraphWidth + 2 * horizBorder;
			}
			topPos += oneGraphHeight + 2 * vertBorder;
		}
		
		int leftPos = maxYLabelWidth + kLabelAxisGap + maxYAxisWidth;
		for (int i=0 ; i<xAxis.length ; i++) {
			xAxis[i].setBounds(leftPos, topPos, oneGraphWidth + 2 * horizBorder, maxXAxisHeight);
			leftPos += oneGraphWidth + 2 * horizBorder;
		}
		topPos += maxXAxisHeight + kLabelAxisGap;
		leftPos = maxYLabelWidth + kLabelAxisGap + maxYAxisWidth;
		for (int i=0 ; i<xAxis.length ; i++) {
			xLabel[i].setBounds(leftPos, topPos, oneGraphWidth + 2 * horizBorder, maxXLabelHeight);
			leftPos += oneGraphWidth + 2 * horizBorder;
		}
		
		if (controlPanel != null) {
			int noOfSquares = (noOfVariables - 1) / 2;
			int maxPanelWidth = noOfSquares * (oneGraphWidth + 2 * horizBorder);
			int maxPanelHeight = noOfSquares * (oneGraphHeight + 2 * vertBorder);
			
//			Dimension size = controlPanel.getPreferredSize();
			controlPanel.setBounds(insets.left + maxWidth - maxPanelWidth, insets.top,
																					maxPanelWidth, maxPanelHeight);
		}
	}
	
	public String toString() {
		return getClass().getName();
	}
}