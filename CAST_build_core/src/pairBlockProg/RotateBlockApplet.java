package pairBlockProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multiRegn.*;
import pairBlock.*;


public class RotateBlockApplet extends XApplet {
	static final private String INITIAL_ROTATION_PARAM = "initialRotation";
	
	private TwoTreatDataSet data;
	
	private RotateBlockView theView;
	
	private XCheckbox showBlocksCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 4));
		add("Center", rotatePanel(data));
		add("South", controlPanel(data));
	}
	
	private TwoTreatDataSet readData() {
		TwoTreatDataSet data = new TwoTreatDataSet(this);
		
		return data;
	}
	
	private XPanel controlPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
		thePanel.add(RotateButton.createRotationPanel(theView, this));
		
			showBlocksCheck = new XCheckbox(translate("Colour different blocks"), this);
		thePanel.add(showBlocksCheck);
			
		return thePanel;
	}
	
	private XPanel rotatePanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable xVar = (CatVariable)data.getVariable("x");
			D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setCatScale(xVar);
			D3Axis yAxis = new D3Axis(data.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(data.getYAxisInfo());
			CatVariable zVar = (CatVariable)data.getVariable("z");
			D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setCatScale(zVar);
			
			theView = new RotateBlockView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "ls");
			theView.setColouring(RotateBlockView.ALL_BLACK);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		
		String initialRotationString = getParameter(INITIAL_ROTATION_PARAM);
		if (initialRotationString != null) {
			StringTokenizer theAngles = new StringTokenizer(initialRotationString);
			int roundDens = Integer.parseInt(theAngles.nextToken());
			int ofDens = Integer.parseInt(theAngles.nextToken());
			theView.rotateTo(roundDens, ofDens);
		}
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showBlocksCheck) {
			boolean showBlocks = showBlocksCheck.getState();
			theView.setColouring(showBlocks ? RotateBlockView.SHOW_COLOURS
																													: RotateBlockView.ALL_BLACK);
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}