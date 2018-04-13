package regn;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

class Horiz2Axis extends HorizAxis {
	private double widestMinOnAxis, widestMaxOnAxis;
	
	public Horiz2Axis(XApplet applet) {
		super(applet);
	}
	
	public void readNumLabels(String labelInfo) {
		super.readNumLabels(labelInfo);
		widestMinOnAxis = minOnAxis;
		widestMaxOnAxis = maxOnAxis;
	}
	
	public void findAxisWidth() {
		double currentMinOnAxis = minOnAxis;
		double currentMaxOnAxis = maxOnAxis;
		minOnAxis = widestMinOnAxis;
		maxOnAxis = widestMaxOnAxis;
		
		super.findAxisWidth();
		
		minOnAxis = currentMinOnAxis;
		maxOnAxis = currentMaxOnAxis;
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		double currentMinOnAxis = minOnAxis;
		double currentMaxOnAxis = maxOnAxis;
		minOnAxis = widestMinOnAxis;
		maxOnAxis = widestMaxOnAxis;
		
		super.findLengthInfo(availableLength, minLowBorder, minHighBorder);
		
		minOnAxis = currentMinOnAxis;
		maxOnAxis = currentMaxOnAxis;
	}
}

class Vert2Axis extends VertAxis {
	private double widestMinOnAxis, widestMaxOnAxis;
	
	public Vert2Axis(XApplet applet) {
		super(applet);
	}
	
	public void readNumLabels(String labelInfo) {
		super.readNumLabels(labelInfo);
		widestMinOnAxis = minOnAxis;
		widestMaxOnAxis = maxOnAxis;
	}
	
	public void findAxisWidth() {
		double currentMinOnAxis = minOnAxis;
		double currentMaxOnAxis = maxOnAxis;
		minOnAxis = widestMinOnAxis;
		maxOnAxis = widestMaxOnAxis;
		
		super.findAxisWidth();
		
		minOnAxis = currentMinOnAxis;
		maxOnAxis = currentMaxOnAxis;
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		double currentMinOnAxis = minOnAxis;
		double currentMaxOnAxis = maxOnAxis;
		minOnAxis = widestMinOnAxis;
		maxOnAxis = widestMaxOnAxis;
		
		super.findLengthInfo(availableLength, minLowBorder, minHighBorder);
		
		minOnAxis = currentMinOnAxis;
		maxOnAxis = currentMaxOnAxis;
	}
}


public class ZoomSlider extends XPanel {
//	private XApplet applet;
	private XNoValueSlider theSlider;
	private int zoomSteps;
	
	private DataView controlledView, controlledView2;
	private VertAxis yAxis, yAxis2;
	private HorizAxis xAxis, xAxis2;
	
	private double xScale[][] = new double[2][];
	private double yScale[][] = new double[2][];
	
	public ZoomSlider(XApplet applet, int zoomSteps) {
//		this.applet = applet;
		this.zoomSteps = zoomSteps;
		setLayout(new BorderLayout());
		theSlider = new XNoValueSlider(applet.translate("Data"), applet.translate("Extrapolate"), null, 0, zoomSteps, 0, applet);
		add("Center", theSlider);
	}
	
	public void setControlledView(DataView controlledView) {
		if (this.controlledView == null)
			this.controlledView = controlledView;
		else
			controlledView2 = controlledView;
	}
	
	private double getProportion() {
		return getProportion(theSlider.getValue());
	}
	
	private double getProportion(int val) {
		return (zoomSteps - val) / (double)zoomSteps;
	}
	
	private double[] readMinMax(String extremeInfo) {
		StringTokenizer valueTokens = new StringTokenizer(extremeInfo);
		
		double result[] = new double[2];
		result[0] = Double.parseDouble(valueTokens.nextToken());
		result[1] = Double.parseDouble(valueTokens.nextToken());
		
		return result;
	}
	
	public VertAxis createVertAxis(DataSet data, String yAxisInfo, String yExtremeInfo, XApplet applet) {
		if (yAxis == null) {
			yAxis = new Vert2Axis(applet);
			yAxis.readNumLabels(yAxisInfo);
			
			yScale[0] = new double[2];
			yScale[0][0] = yAxis.minOnAxis;
			yScale[0][1] = yAxis.maxOnAxis;
			yScale[1] = readMinMax(yExtremeInfo);
			return yAxis;
		}
		else {
			yAxis2 = new Vert2Axis(applet);
			yAxis2.readNumLabels(yAxisInfo);
			return yAxis2;
		}
	}
	
	public HorizAxis createHorizAxis(DataSet data, String xAxisInfo, String xExtremeInfo, XApplet applet) {
		if (xAxis == null) {
			xAxis = new Horiz2Axis(applet);
			xAxis.readNumLabels(xAxisInfo);
			xScale[0] = new double[2];
			xScale[0][0] = xAxis.minOnAxis;
			xScale[0][1] = xAxis.maxOnAxis;
			xScale[1] = readMinMax(xExtremeInfo);
			return xAxis;
		}
		else {
			xAxis2 = new Horiz2Axis(applet);
			xAxis2.readNumLabels(xAxisInfo);
			return xAxis2;
		}
	}
	
	public void setScaleFraction (double p) {
		double xMin = xScale[0][0] * (1 - p) + xScale[1][0] * p;
		double xMax = xScale[0][1] * (1 - p) + xScale[1][1] * p;
		double yMin = yScale[0][0] * (1 - p) + yScale[1][0] * p;
		double yMax = yScale[0][1] * (1 - p) + yScale[1][1] * p;
		
		xAxis.minOnAxis = xMin;
		xAxis.maxOnAxis = xMax;
		xAxis.setPowerIndex(xAxis.getPowerIndex());
//		xAxis.findAxisWidth();			//		to reset staggered
		xAxis.repaint();
		if (xAxis2 != null) {
			xAxis2.minOnAxis = xMin;
			xAxis2.maxOnAxis = xMax;
			xAxis2.setPowerIndex(xAxis.getPowerIndex());
//			xAxis2.findAxisWidth();			//		to reset staggered
			xAxis2.repaint();
		}
		
		yAxis.minOnAxis = yMin;
		yAxis.maxOnAxis = yMax;
		yAxis.setPowerIndex(yAxis.getPowerIndex());
		yAxis.repaint();
		if (yAxis2 != null) {
			yAxis2.minOnAxis = yMin;
			yAxis2.maxOnAxis = yMax;
			yAxis2.setPowerIndex(yAxis.getPowerIndex());
			yAxis2.repaint();
		}
		
		controlledView.setCrossSize(p > 0.7 ? DataView.LARGE_CROSS : 
										p > 0.4 ? DataView.MEDIUM_CROSS : DataView.SMALL_CROSS);
		controlledView.repaint();
		if (controlledView2 != null) {
			controlledView2.setCrossSize(p > 0.7 ? DataView.LARGE_CROSS : 
											p > 0.4 ? DataView.MEDIUM_CROSS : DataView.SMALL_CROSS);
			controlledView2.repaint();
		}
	}

	
	private boolean localAction(Object target) {
		if (target == theSlider) {
			setScaleFraction(getProportion());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}