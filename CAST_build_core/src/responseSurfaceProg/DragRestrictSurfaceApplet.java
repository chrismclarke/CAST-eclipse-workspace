package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import responseSurface.*;


public class DragRestrictSurfaceApplet extends DragQuadSurfaceApplet {
	static final private String START_DRAG_PARAM = "startDrag";
	
	static final private String kZeroList = "0.0 0.0 0.0 0.0 0.0 0.0";
	
	static final private String[] kModelExplanKey = {"xModel", "zModel"};
	
	private XCheckbox dataModelCheck;
	
	private XPanel eqnPanel;
	private CardLayout eqnPanelLayout;
	
	protected XPanel topPanel(DataSet data) {
		eqnPanel = new XPanel();
		eqnPanelLayout = new CardLayout();
		eqnPanel.setLayout(eqnPanelLayout);
		
		eqnPanel.add("blank", new XPanel());
		
		eqnPanel.add("eqn", super.topPanel(data));
		eqnPanelLayout.show(eqnPanel, "blank");
		
			boolean[] showParameter = {true, true, true, true, true, true};
			theEqn.setDrawParameters(showParameter);
		
		return eqnPanel;
	}
	
	protected void addDataValues(DataSet data) {
			yName = getParameter(Y_VAR_NAME_PARAM);
			String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", yName, yValues);
		
			xName = getParameter(X_VAR_NAME_PARAM);
			String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", xName, xValues);
		
			zName = getParameter(Z_VAR_NAME_PARAM);
			String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", zName, zValues);
		
		data.addNumVariable("yModel", "Y model", kZeroList);
		data.addNumVariable("xModel", "X model", kZeroList);
		data.addNumVariable("zModel", "Z model", kZeroList);
		
		setupAnchorValues(data);
	}
	
	private void setupAnchorValues(DataSet data) {
		double distinctX[] = new double[3];
		double distinctZ[] = new double[3];
		int nDistinctX = 0;
		int nDistinctZ = 0;
		
		int n[][] = new int[3][3];
		double sy[][] = new double[3][3];
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumVariable zVar = (NumVariable)data.getVariable("z");
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ze = zVar.values();
		while (ye.hasMoreValues() && xe.hasMoreValues() && ze.hasMoreValues()) {
			double y = ye.nextDouble();
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			int ix = nDistinctX;
			for (int i=0 ; i<nDistinctX ; i++)
				if (x == distinctX[i])
					ix = i;
			if (ix == nDistinctX)
				distinctX[nDistinctX ++] = x;
				
			int jz = nDistinctZ;
			for (int j=0 ; j<nDistinctZ ; j++)
				if (z == distinctZ[j])
					jz = j;
			if (jz == nDistinctZ)
				distinctZ[nDistinctZ ++] = z;
			
			n[ix][jz] ++;
			sy[ix][jz] += y;
		}
		
		NumVariable dragYVar = (NumVariable)data.getVariable("yModel");
		NumVariable dragXVar = (NumVariable)data.getVariable("xModel");
		NumVariable dragZVar = (NumVariable)data.getVariable("zModel");
		
		int index = 0;
		for (int ix=0 ; ix<3 ; ix++)
			for (int jz=0 ; jz<3 ; jz++)
				if (n[ix][jz] > 0) {
					((NumValue)dragXVar.valueAt(index)).setValue(distinctX[ix]);
					((NumValue)dragZVar.valueAt(index)).setValue(distinctZ[jz]);
					((NumValue)dragYVar.valueAt(index)).setValue(sy[ix][jz] / n[ix][jz]);
					index ++;
				}
											//	assumes that exactly 5 of the n[ix][jz] are not 0
		
		StringTokenizer st = new StringTokenizer(getParameter(X_AXIS_INFO_PARAM));
		double xAxisMin = Double.parseDouble(st.nextToken());
		st = new StringTokenizer(getParameter(Z_AXIS_INFO_PARAM));
		double zAxisMin = Double.parseDouble(st.nextToken());
		double zAxisMax = Double.parseDouble(st.nextToken());
		
		double startY = Double.parseDouble(getParameter(START_DRAG_PARAM));
		((NumValue)dragXVar.valueAt(index)).setValue(xAxisMin);
		((NumValue)dragZVar.valueAt(index)).setValue((zAxisMin + zAxisMax) / 2.0);
		((NumValue)dragYVar.valueAt(index)).setValue(startY);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		addDataValues(data);
		addModel(data, kModelExplanKey, "yModel", "model");
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			DragRestrictSurfaceView localView = new DragRestrictSurfaceView(data, this, xAxis, yAxis, zAxis, "model",
												explanKey, "y", kModelExplanKey, "yModel");
			localView.resetModel(Double.NaN);
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			localView.setCrossSize(DataView.LARGE_CROSS);
			localView.setNeverDimCross(true);
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			dataModelCheck = new XCheckbox(translate("Show best model"), this);
		thePanel.add(dataModelCheck);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataModelCheck) {
			DragRestrictSurfaceView dragView = (DragRestrictSurfaceView)theView;
			dragView.setShowSurface(dataModelCheck.getState());
			data.variableChanged("model");
			
			eqnPanelLayout.show(eqnPanel, dataModelCheck.getState() ? "eqn" : "blank");
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