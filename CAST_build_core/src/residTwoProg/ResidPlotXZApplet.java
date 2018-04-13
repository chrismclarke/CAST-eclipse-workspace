package residTwoProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;
import multiRegn.*;
import residTwo.*;


public class ResidPlotXZApplet extends XApplet {
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	
	protected MultiRegnDataSet data;
	
	protected XChoice yChoice, xChoice;
	protected int currentYChoice, currentXChoice;
	
	private XCheckbox colorCheck;
	
	protected MultiHorizAxis horizAxis;
	protected MultiVertAxis vertAxis;
	protected YChangeScatterView d2View;
	
	protected ColoredXZView d3View;
	protected D3Axis xAxis, yAxis, zAxis;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																									ProportionLayout.TOTAL));
		
		add(ProportionLayout.LEFT, dataPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 15));
			rightPanel.add("Center", residPanel(data));
			rightPanel.add("South", controlPanel(data));
			
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	protected MultiRegnDataSet readData() {
		MultiRegnDataSet data = new MultiRegnDataSet(this);
		
		data.addVariable("resid", new ResidValueVariable(translate("Residual"), data,
																							MultiRegnDataSet.xKeys, "y", "ls", 9));
		
		return data;
	}
	
	protected XChoice getYChoice(MultiRegnDataSet data) {
		XChoice yChoice = new XChoice(this);
		yChoice.addItem(data.getVariable("y").name);
		yChoice.addItem(translate("Residual"));
		return yChoice;
	}
	
	protected MultiHorizAxis getHorizAxis(MultiRegnDataSet data) {
		MultiHorizAxis horizAxis = new MultiHorizAxis(this, 2);
		horizAxis.readNumLabels(data.getXAxisInfo());
		horizAxis.readExtraNumLabels(data.getZAxisInfo());
		horizAxis.setChangeMinMax(true);
		return horizAxis;
	}
	
	protected String getFakeVertAxisInfo(String axisParam) {
		String partResInfo = getParameter(axisParam);
		StringTokenizer st = new StringTokenizer(partResInfo);
		String minString = st.nextToken();
		String maxString = st.nextToken();
		NumValue max = new NumValue(maxString);
		max.setValue(max.toDouble() + 1.0);
		
		return (minString + " " + maxString + " " + max.toString() + " 1");
	}
	
	protected XPanel scatterPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = getHorizAxis(data);
		thePanel.add("Bottom", horizAxis);
		
			vertAxis = new MultiVertAxis(this, 2);
			vertAxis.readNumLabels(data.getYAxisInfo());
			vertAxis.readExtraNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
			vertAxis.setChangeMinMax(false);
		thePanel.add("Left", vertAxis);
		
			VertAxis dummyVertAxis = new VertAxis(this);
			dummyVertAxis.readNumLabels(getFakeVertAxisInfo(RESID_AXIS_INFO_PARAM));
			dummyVertAxis.setShowUnlabelledAxis(false);
		thePanel.add("Right", dummyVertAxis);
		
			d2View = new YChangeScatterView(data, this, horizAxis, vertAxis, dummyVertAxis, "x", "y", "resid", d3View);
			d2View.setRetainLastSelection(true);
			d2View.lockBackground(Color.white);
			
		thePanel.add("Center", d2View);
		
		return thePanel;
	}
	
	private XPanel residPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				yChoice = getYChoice(data);
			topPanel.add(yChoice);
			
		thePanel.add("North", topPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				xChoice = new XChoice(this);
				xChoice.addItem(data.getVariable("x").name);
				xChoice.addItem(data.getVariable("z").name);
			bottomPanel.add(xChoice);
			
		thePanel.add("South", bottomPanel);
		
		thePanel.add("Center", scatterPanel(data));
		
		return thePanel;
	}
	
	
	protected XPanel rotatePanel(Rotate3DView theView) {
		return RotateButton.createRotationPanel(theView, this);
	}
	
	protected XPanel dataPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		xAxis = new D3Axis(data.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(data.getXAxisInfo());
		yAxis = new D3Axis(data.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(data.getYAxisInfo());
		zAxis = new D3Axis(data.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(data.getZAxisInfo());
		
		d3View = new ColoredXZView(data, this, xAxis, yAxis, zAxis, "ls", MultiRegnDataSet.xKeys, "y");
		d3View.setSelectCrosses(true);
		d3View.lockBackground(Color.white);
//		d3View.setColoredAxis(ColoredXZView.Z_COLOR);
		thePanel.add("Center", d3View);
			
		thePanel.add("South", rotatePanel(d3View));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			colorCheck = new XCheckbox(translate("Color crosses"), this);
		thePanel.add(colorCheck);
		return thePanel;
	}
	
	protected void changeVertVariable() {
		vertAxis.setAlternateLabels(currentYChoice);
		vertAxis.repaint();
		
		d2View.animateChange(currentYChoice == 1, yChoice);
	}
	
	protected void setHorizVariable() {
		String xKey = (currentXChoice == 0) ? "x" : "z";
		d2View.changeVariables(null, xKey);
	}
	
	protected void doShowColors() {
		d3View.setColoredAxis((currentXChoice == 0) ? ColoredXZView.Z_COLOR
																								: ColoredXZView.X_COLOR);
		d3View.repaint();
		d2View.setColorKey((currentXChoice == 0) ? "z" : "x");
		d2View.repaint();
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else if (evt.target == yChoice) {
			int newChoice = yChoice.getSelectedIndex();
			if (newChoice != currentYChoice) {
				currentYChoice = newChoice;
				changeVertVariable();
			}
			return true;
		}
		else if (evt.target == xChoice) {
			int newChoice = xChoice.getSelectedIndex();
			if (newChoice != currentXChoice) {
				currentXChoice = newChoice;
				horizAxis.setAlternateLabels(newChoice);
				horizAxis.repaint();
				if (colorCheck.getState())
					doShowColors();
				d2View.suddenChange(false);		//	to show y on vertical axis
				yChoice.select(0);
				currentYChoice = 0;
				setHorizVariable();
			}
			return true;
		}
		else if (evt.target == colorCheck) {
			boolean showColors = colorCheck.getState();
			if (showColors)
				doShowColors();
			else {
				d3View.setColoredAxis(ColoredXZView.NO_COLOR);
				d2View.setColorKey(null);
				d3View.repaint();
				d2View.repaint();
			}
			return true;
		}
		return false;
	}
}

