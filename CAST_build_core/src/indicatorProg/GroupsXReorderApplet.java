package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;

import glmAnovaProg.*;
import indicator.*;


public class GroupsXReorderApplet extends AnovaTableReorderApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String COEFF_DECIMALS_PARAM = "coeffDecimals";
	static final private String JITTER_PARAM = "jitter";
	
	static final private String[] kXKey = {"x", "z"};
	
	private int paramDecimals[];
	
	public void setupApplet() {
		readMaxSsq();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 5));
		
		add("Center", displayPanel(data));
		add("South", createTable(data));
		add("East", keyPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
																										getParameter(Z_LABELS_PARAM));
		
			StringTokenizer st = new StringTokenizer(getParameter(COEFF_DECIMALS_PARAM));
			paramDecimals = new int[st.countTokens()];
			for (int i=0 ; i<paramDecimals.length ; i++)
				paramDecimals[i] = Integer.parseInt(st.nextToken());
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, kXKey);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("lsXZ", lsModel);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
				
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
				xAxis.setAxisName(data.getVariable(kXKey[0]).name);
			dataPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			dataPanel.add("Left", yAxis);
			
				DragParallelLinesView theView = new DragParallelLinesView(data, this, xAxis, yAxis, kXKey, "y", 
									null, null, "lsXZ", paramDecimals);
				theView.setCanDragHandles(false);
				String jitterString = getParameter(JITTER_PARAM);
				if (jitterString != null) {
					StringTokenizer st = new StringTokenizer(jitterString);
					double jitter = Double.parseDouble(st.nextToken());
					long seed = Long.parseLong(st.nextToken());
					theView.setJitter(jitter, seed);
				}
				theView.lockBackground(Color.white);
				
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(50, 0, 30, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
		
			XLabel zLabel = new XLabel(data.getVariable(kXKey[1]).name, XLabel.LEFT, this);
			zLabel.setFont(getBigFont());
		thePanel.add(zLabel);
			
			CatKey zKey = new CatKey(data, kXKey[1], this, CatKey.VERT);
			zKey.setFont(getBigFont());
		thePanel.add(zKey);
		
		return thePanel;
	}
}