package mixtureProg;

import java.awt.*;

import dataView.*;
import valueList.*;
import graphics3D.*;

import mixture.*;


public class PyramidDesignsApplet extends TriangleDesignsApplet {
	static final protected String U_VAR_NAME_PARAM = "uVarName";
	static final protected String U_VALUES_PARAM = "uValues";
	
//	static final private String kAxisInfo = "-0.1 1.3 0.0 0.2";
	static final private String kAxisInfo = "0 1.2 2 1";
		
	protected DataSet readData() {
		DataSet data = super.readData();		//	data values from params are ignored buy must exist
		String uValues = getParameter(U_VALUES_PARAM);
		if (uValues != null)
			data.addNumVariable("u", getParameter(U_VAR_NAME_PARAM), uValues);
		
		setCentroidData();
		return data;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			D3Axis xAxis = new D3Axis("", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(kAxisInfo);
			xAxis.setShow(false);
			D3Axis yAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(kAxisInfo);
			yAxis.setShow(false);
			D3Axis zAxis = new D3Axis("", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kAxisInfo);
			zAxis.setShow(false);
			
			theView = new DesignPyramidView(data, this, xAxis, yAxis, zAxis, "y", "x", "z", "u");
			theView.lockBackground(Color.white);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.setBigHitRadius();
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel valuesPanel(DataSet data) {
		XPanel thePanel = super.valuesPanel(data);
		
		xValueView.setForeground(DesignPyramidView.kSColor);
		yValueView.setForeground(DesignPyramidView.kRColor);
		zValueView.setForeground(DesignPyramidView.kTColor);
		
			OneValueView uValueView = new OneValueView(data, "u", this, kMaxPropn);
			uValueView.setHighlightSelection(false);
			uValueView.setForeground(DesignPyramidView.kUColor);
		thePanel.add(uValueView);
		return thePanel;
	}
	

	protected void setM(int m) {
		int n = 0;
		for (int i=0 ; i<=m ; i++)
		 n += (m - i + 2) * (m - i + 1) / 2;
		
		double r[] = new double[n];
		double s[] = new double[n];
		double t[] = new double[n];
		double u[] = new double[n];
		
		double doubleM = m;
		int dataIndex = 0;
		for (int i=0 ; i<=m ; i++) {
			double rRow = i / doubleM;
			for (int j=0 ; j<=(m-i) ; j++) {
				double sRow = j / doubleM;
				for (int k=0 ; k<=(m-i-j) ; k++) {
					double tRow = k / doubleM;
					double uRow = 1.0 - rRow - sRow - tRow;
					r[dataIndex] = rRow;
					s[dataIndex] = sRow;
					t[dataIndex] = tRow;
					u[dataIndex] = uRow;
					dataIndex ++;
				}
			}
		}
		setDataValues(r, s, t, u);
	}

	
	protected void setCentroidData() {
		double r[] = {1, 0, 0, 0, .5, .5, .5,  0,  0,  0, .333, .333, .333,    0, .25};
		double s[] = {0, 1, 0, 0, .5,  0,  0, .5, .5,  0, .333, .333,    0, .333, .25};
		double t[] = {0, 0, 1, 0,  0, .5,  0, .5,  0, .5, .333,    0, .333, .333, .25};
		double u[] = {0, 0, 0, 1,  0,  0, .5,  0, .5, .5,    0, .333, .333, .333, .25};
		
		setDataValues(r, s, t, u);
	}
	
	protected void setDataValues(double[] r, double[] s, double[] t, double[] u) {
		NumVariable uVar = (NumVariable)data.getVariable("u");
		if (uVar ==  null)
			return;				//	First called by TriangleDesignsApplet before uVar is created
		
		super.setDataValues(r, s, t);
		
		uVar.setValues(u);
		uVar.setDecimals(kMaxPropn.decimals);
		
		data.variableChanged("u");
	}
	
}