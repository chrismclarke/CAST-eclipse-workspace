package ssq;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

public class DataWithComponentsPanel extends XPanel {
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	private DataSet data;
	private String xKey, yKey;
	
	private DataWithComponentView theView;
	private HorizAxis theHorizAxis;
	private VertAxis theVertAxis;
	private XLabel yVariateName;
	
	public DataWithComponentsPanel(XApplet applet) {
	}
	
	public void setupPanel(DataSet data, String xKey, String yKey, String lsKey,
									String modelKey, int initialComponentDisplay, XApplet applet) {
		this.data = data;
		this.xKey = xKey;
		this.yKey = yKey;
		
		setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				
				CoreVariable xVar = data.getVariable(xKey);
				boolean noHorizAxis = (xVar instanceof CatVariable) && (((CatVariable)xVar).noOfCategories() == 1);
				if (!noHorizAxis) {
					theHorizAxis = createHorizAxis(data, xKey, applet);
					scatterPanel.add("Bottom", theHorizAxis);
				}
				
				theVertAxis = createVertAxis(data, applet);
				scatterPanel.add("Left", theVertAxis);
				
				theView = createDataView(data, theHorizAxis, theVertAxis,
										xKey, yKey, lsKey, modelKey, initialComponentDisplay, applet);
				theView.setStickyDrag(true);
				theView.lockBackground(Color.white);
				
				scatterPanel.add("Center", theView);
				
			add("Center", scatterPanel);
			
			CoreVariable yVar = data.getVariable(yKey);
			yVariateName = new XLabel(yVar.name, XLabel.LEFT, applet);
			yVariateName.setFont(theVertAxis.getFont());
			add("North", yVariateName);
		
		repaint();
	}
	
	public void setupPanel(DataSet data, String xKey, String yKey, String lsKey,
																		String modelKey, XApplet applet) {
		setupPanel(data, xKey, yKey, lsKey, modelKey, BasicComponentVariable.TOTAL, applet);
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new DataWithComponentView(data, applet, theHorizAxis, theVertAxis,
																xKey, yKey, lsKey, modelKey, initialComponentDisplay);
	}
	
	public DataWithComponentView getView() {
		return theView;
	}
	
	private HorizAxis createHorizAxis(DataSet data, String xKey, XApplet applet) {
		HorizAxis axis = new HorizAxis(applet);
		
		CoreVariable xVar = data.getVariable(xKey);
		if (xVar instanceof NumVariable) {
			String labelInfo = applet.getParameter(X_AXIS_INFO_PARAM);
			axis.readNumLabels(labelInfo);
		}
		else
			axis.setCatLabels((CatVariable)xVar);
		axis.setAxisName(xVar.name);
		return axis;
	}
	
	private VertAxis createVertAxis(DataSet data, XApplet applet) {
		VertAxis axis = new VertAxis(applet);
		String labelInfo = applet.getParameter(Y_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		return axis;
	}
	
	public void changeAxes(String xAxisInfo, String yAxisInfo) {
		if (xKey != null) {
			CoreVariable xVar = data.getVariable(xKey);
			if (xAxisInfo != null)
				theHorizAxis.readNumLabels(xAxisInfo);
			else
				theHorizAxis.setCatLabels((CatVariable)xVar);
			theHorizAxis.setAxisName(xVar.name);
			theHorizAxis.repaint();
		}
		
		theVertAxis.readNumLabels(yAxisInfo);
		theVertAxis.repaint();
		CoreVariable yVar = data.getVariable(yKey);
		yVariateName.setText(yVar.name);
	}
}