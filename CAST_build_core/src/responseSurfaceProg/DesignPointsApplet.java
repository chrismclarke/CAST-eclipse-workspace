package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import responseSurface.*;
import time.IntegerAdjuster;


public class DesignPointsApplet extends RotateApplet {
	static final private String X_FACTORIAL_VALS_PARAM = "xFactorialVals";
	static final private String Y_FACTORIAL_VALS_PARAM = "yFactorialVals";
	static final private String Z_FACTORIAL_VALS_PARAM = "zFactorialVals";
	
	private IntegerAdjuster centerReplicateAdjuster;
	private XChoice designChoice;
	private int currentDesignIndex = 0;
		
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			String xName = getParameter(X_VAR_NAME_PARAM);
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			String yName = getParameter(Y_VAR_NAME_PARAM);
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			String zName = getParameter(Z_VAR_NAME_PARAM);
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			StringTokenizer st = new StringTokenizer(getParameter(X_FACTORIAL_VALS_PARAM));
			double minX = Double.parseDouble(st.nextToken());
			double maxX = Double.parseDouble(st.nextToken());
			
			st = new StringTokenizer(getParameter(Y_FACTORIAL_VALS_PARAM));
			double minY = Double.parseDouble(st.nextToken());
			double maxY = Double.parseDouble(st.nextToken());
			
			st = new StringTokenizer(getParameter(Z_FACTORIAL_VALS_PARAM));
			double minZ = Double.parseDouble(st.nextToken());
			double maxZ = Double.parseDouble(st.nextToken());
			
			int startFactorialReplicates = 1;
			int startStarReplicates = 0;
			int startCenterReplicates = 0;
			int startBbReplicates = 0;
			
			theView = new SurfaceDesignView(data, this, xAxis, yAxis, zAxis, minX, maxX, minY, maxY, minZ, maxZ,
									startFactorialReplicates, startStarReplicates, startCenterReplicates, startBbReplicates);
			theView.lockBackground(Color.white);
			theView.setCrossSize(DataView.LARGE_CROSS);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 40));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel designLabel = new XLabel(translate("Design") + ":", XLabel.LEFT, this);
				designLabel.setFont(getStandardBoldFont());
			topPanel.add(designLabel);
			
				designChoice = new XChoice(this);
				designChoice.addItem(translate("Factorial"));
				designChoice.addItem(translate("Central composite"));
				designChoice.addItem(translate("Box-Behnken"));
			topPanel.add(designChoice);
			
		thePanel.add(topPanel);
		
			centerReplicateAdjuster = new IntegerAdjuster(translate("Centre points") + ":", 0, 8, 0, this);
		thePanel.add(centerReplicateAdjuster);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		SurfaceDesignView surfaceView = (SurfaceDesignView)theView;
		if (target == centerReplicateAdjuster) {
			surfaceView.setCenterReplicates(centerReplicateAdjuster.getValue());
			theView.repaint();
			return true;
		}
		else if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesignIndex) {
				currentDesignIndex = newChoice;
				
				switch (newChoice) {
					case 0:
						surfaceView.setFactorialReplicates(1);
						surfaceView.setStarReplicates(0);
						surfaceView.setBbReplicates(0);
						break;
					case 1:
						surfaceView.setFactorialReplicates(1);
						surfaceView.setStarReplicates(1);
						surfaceView.setBbReplicates(0);
						break;
					case 2:
						surfaceView.setFactorialReplicates(0);
						surfaceView.setStarReplicates(0);
						surfaceView.setBbReplicates(1);
						break;
				}
				surfaceView.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
	
}