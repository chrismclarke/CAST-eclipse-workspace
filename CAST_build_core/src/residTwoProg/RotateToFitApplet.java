package residTwoProg;

import java.awt.*;

import axis.*;
import utils.*;
import dataView.*;
import coreGraphics.*;
import models.*;
import graphics3D.*;
import multiRegn.*;
import resid.*;
import residTwo.*;


public class RotateToFitApplet extends XApplet {
	static final private String FIT_AXIS_INFO_PARAM = "fitAxis";
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	static final private NumValue kZeros[] = {kZero, kZero, kZero};
	
	private MultiRegnDataSet data;
	
	private D3Axis xAxis, yAxis, zAxis;
	private ColoredXZView d3View;
	
	private MultiHorizAxis horizAxis;
	private MultiVertAxis vertAxis;
	private ScatterView scatterView;
	
	private XChoice yChoice, xChoice;
	private int currentYChoice, currentXChoice;
	
//	private XButton rotateToFitButton;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.5, 10));
		
		add(ProportionLayout.LEFT, leftPanel(data));
		add(ProportionLayout.RIGHT, rightPanel(data));
	}
	
	private MultiRegnDataSet readData() {
		MultiRegnDataSet data = new MultiRegnDataSet(this);
		
		data.addVariable("fit", new FittedValueVariable("Fitted value", data,
																					MultiRegnDataSet.xKeys, "ls", 9));
		
		data.addVariable("stdResid", new StdResidValueVariable("Standardised resid", data,
																			MultiRegnDataSet.xKeys, "y", "ls", 9));
		
		data.addVariable("zeroModel", new MultipleRegnModel("Zero", data,
																													MultiRegnDataSet.xKeys, kZeros));
		
		return data;
	}
	
	private XPanel rightPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", displayPanel(data));
		
		thePanel.add("South", RotateButton.createRotationPanel(d3View, this));
		
		return thePanel;
	}
	
	private XPanel displayPanel(MultiRegnDataSet data) {
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
		thePanel.add("Center", d3View);
		
		return thePanel;
	}
	
	private XPanel leftPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				yChoice = new XChoice(this);
				yChoice.addItem(data.getVariable("y").name);
				yChoice.addItem(translate("Residual"));
			
			topPanel.add(yChoice);
			
		thePanel.add("North", topPanel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new FixedSizeLayout(250, 0));
			
				XPanel dataPanel = new XPanel();
				dataPanel.setLayout(new AxisLayout());
					
					horizAxis = new MultiHorizAxis(this, 3);
					horizAxis.readNumLabels(data.getXAxisInfo());
					horizAxis.readExtraNumLabels(data.getZAxisInfo());
					horizAxis.readExtraNumLabels(getParameter(FIT_AXIS_INFO_PARAM));
					horizAxis.setChangeMinMax(true);
			
				dataPanel.add("Bottom", horizAxis);
			
					vertAxis = new MultiVertAxis(this, 2);
					vertAxis.readNumLabels(data.getYAxisInfo());
					vertAxis.readExtraNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
					vertAxis.setChangeMinMax(true);
					
				dataPanel.add("Left", vertAxis);
			
					scatterView = new ScatterView(data, this, horizAxis, vertAxis, "x", "y");
					scatterView.lockBackground(Color.white);
				
				dataPanel.add("Center", scatterView);
			innerPanel.add(dataPanel);
			
		thePanel.add("Center", innerPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				xChoice = new XChoice(this);
				xChoice.addItem(data.getVariable("x").name);
				xChoice.addItem(data.getVariable("z").name);
				xChoice.addItem(translate("Fitted value"));
			
			bottomPanel.add(xChoice);
			
		thePanel.add("South", bottomPanel);
		return thePanel;
	}
	
	private double getFitAngle() {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("ls");
		double bx = model.getParameter(1).toDouble();
		double bz = model.getParameter(2).toDouble();
		
		double bxPropn = bx * (xAxis.getMaxOnAxis() - xAxis.getMinOnAxis());
		double bzPropn = bz * (zAxis.getMaxOnAxis() - zAxis.getMinOnAxis());
		
		double angle = Math.atan(bzPropn / bxPropn) * 180 / Math.PI;
		
		return angle;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else if (evt.target == yChoice) {
			int newChoice = yChoice.getSelectedIndex();
			if (newChoice != currentYChoice) {
				currentYChoice = newChoice;
				String newYName = (currentYChoice == 0) ? data.getYVarName() : "Std residual";
				vertAxis.setAlternateLabels(currentYChoice);
				vertAxis.setAxisName(newYName);
				vertAxis.repaint();
				String newYKey = (currentYChoice == 0) ? "y" : "stdResid";
				scatterView.changeVariables(newYKey, null);
				
				yAxis.setNumScale((currentYChoice == 0) ? data.getYAxisInfo()
																								: getParameter(RESID_AXIS_INFO_PARAM));
				yAxis.setLabelName(newYName);
				String model3Key = (currentYChoice == 0) ? "ls" : "zeroModel";
				d3View.changeVariables(null, newYKey, null, model3Key);
			}
			return true;
		}
		else if (evt.target == xChoice) {
			int newChoice = xChoice.getSelectedIndex();
			if (newChoice != currentXChoice) {
				currentXChoice = newChoice;
				horizAxis.setAlternateLabels(newChoice);
				horizAxis.repaint();
				String newHorizKey = (currentXChoice == 0) ? "x"
															: (currentXChoice == 1) ? "z" : "fit";
				scatterView.changeVariables(null, newHorizKey);
			}
			
			double angle = (currentXChoice == 0) ? 0 : (currentXChoice == 1) ? 90 : getFitAngle();
			d3View.animateRotateTo(angle, 0);
			
			return true;
		}
		return false;
	}
}