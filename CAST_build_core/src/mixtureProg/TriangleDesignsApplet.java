package mixtureProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import graphics3D.*;

import multivarProg.*;
import time.IntegerAdjuster;
import mixture.*;


public class TriangleDesignsApplet extends RotateApplet {
	
	static final private int kStartM = 2;
	static final protected NumValue kMaxPropn = new NumValue(1.0, 4);
	static final private String[] xzKeys = {"x", "z"};
	
	protected DataSet data;
	private XChoice designChoice;
	private int currentDesignChoice;
	private IntegerAdjuster mAdjuster;
	
	protected OneValueView xValueView, yValueView, zValueView;
	
	private XPanel designPanel;
	private CardLayout designPanelLayout;
		
	protected DataSet readData() {
		data = super.readData();		//	data values from params are ignored buy must exist
		setCentroidData();
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
			
			theView = new TriangleDesignView(data, this, xAxis, yAxis, zAxis, "y", xzKeys);
			((TriangleDesignView)theView).setSelectCrosses(true);
			theView.lockBackground(Color.white);
			theView.setBigHitRadius();
			theView.setCrossSize(DataView.LARGE_CROSS);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel designPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 10));
			designChoice = new XChoice(this);
			designChoice.addItem(translate("Simplex-centroid design"));
			designChoice.addItem(translate("Simplex-lattice design"));
		thePanel.add(designChoice);
		
			designPanel = new XPanel();
			designPanelLayout = new CardLayout();
			designPanel.setLayout(designPanelLayout);
			
			designPanel.add("blank", new XPanel());
			
				XPanel mAdjusterPanel = new XPanel();
				mAdjusterPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					mAdjuster = new IntegerAdjuster("m = ", 2, 5, kStartM, this);
				
				mAdjusterPanel.add(mAdjuster);
			
			designPanel.add("mAdjuster", mAdjusterPanel);
			designPanelLayout.show(designPanel, "blank");
			
		thePanel.add(designPanel);
		
		return thePanel;
	}
	
	protected XPanel valuesPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																										VerticalLayout.VERT_CENTER, 6));
		
			xValueView = new OneValueView(data, "x", this, kMaxPropn);
			xValueView.setHighlightSelection(false);
			xValueView.setFont(getBigFont());
		thePanel.add(xValueView);
		
			yValueView = new OneValueView(data, "y", this, kMaxPropn);
			yValueView.setHighlightSelection(false);
			yValueView.setFont(getBigFont());
		thePanel.add(yValueView);
		
			zValueView = new OneValueView(data, "z", this, kMaxPropn);
			zValueView.setHighlightSelection(false);
			zValueView.setFont(getBigFont());
		thePanel.add(zValueView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 30));
		
		thePanel.add(designPanel());
		
		thePanel.add(valuesPanel(data));
		return thePanel;
	}
	
	protected void setM(int m) {
		double doubleM = m;
		int n = (m + 2) * (m + 1) / 2;
		double x[] = new double[n];
		double y[] = new double[n];
		double z[] = new double[n];
		
		int dataIndex = 0;
		for (int i=0 ; i<=m ; i++) {
			double xRow = i / doubleM;
			for (int j=0 ; j<=(m-i) ; j++) {
				double yRow = j / doubleM;
				double zRow = 1.0 - xRow - yRow;
				x[dataIndex] = xRow;
				y[dataIndex] = yRow;
				z[dataIndex] = zRow;
				dataIndex ++;
			}
		}
		setDataValues(x, y, z);
	}
	
	protected void setCentroidData() {
		double x[] = new double[7];
		double y[] = new double[7];
		double z[] = new double[7];
		
		x[0] = x[1] = x[2] = 0.0;
		x[3] = x[4] = 0.5;
		x[5] = 1.0;
		
		y[0] = y[3] = y[4] = 0.0;
		y[1] = y[4] = 0.5;
		y[2] = 1.0;
		
		z[2] = z[4] = z[5] = 0.0;
		z[1] = z[3] = 0.5;
		z[0] = 1.0;
		
		x[6] = y[6] = z[6] = 0.333333;
		
		setDataValues(x, y, z);
	}
	
	protected void setDataValues(double[] x, double[] y, double[] z) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.setValues(x);
		xVar.setDecimals(kMaxPropn.decimals);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.setValues(y);
		yVar.setDecimals(kMaxPropn.decimals);
		
		NumVariable zVar = (NumVariable)data.getVariable("z");
		zVar.setValues(z);
		zVar.setDecimals(kMaxPropn.decimals);
		
		data.variableChanged("x");
		data.variableChanged("y");
		data.variableChanged("z");
	}
	
	
	private boolean localAction(Object target) {
		if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesignChoice) {
				currentDesignChoice = newChoice;
				
				designPanelLayout.show(designPanel, (newChoice == 1) ? "mAdjuster" : "blank");
				
				if (newChoice == 1) {
					int m = mAdjuster.getValue();
					setM(m);
				}
				else
					setCentroidData();
			}
			return true;
		}
		else if (target == mAdjuster) {
			int m = mAdjuster.getValue();
			setM(m);
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