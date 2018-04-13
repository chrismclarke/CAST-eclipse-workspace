package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import distn.*;

import variance.*;


public class FTailDiagramApplet extends XApplet {
	static final private String F_AXIS_PARAM = "fAxis";
	static final private String DF_PARAM = "df";
	static final private String F_VALUE_PARAM = "fValue";
	static final private String AREA_PROPN_PARAM = "areaProportion";
	
	private DataSet data;
	
	private int numerDF, denomDF;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new AxisLayout());
			
			HorizAxis fAxis = new HorizAxis(this);
			fAxis.readNumLabels(getParameter(F_AXIS_PARAM));
		add("Bottom", fAxis);
		
			FView theView = new FView(data, this, fAxis, "fDistn", "F");
			theView.setDistnLabel(new LabelValue("F(" + numerDF + ", " + denomDF + ")"), Color.gray);
			double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
			theView.setAreaProportion(areaPropn);
			theView.setShowValueArrow(false);
			theView.lockBackground(Color.white);
			
		add("Center", theView);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("F", "F value", getParameter(F_VALUE_PARAM));

			StringTokenizer st = new StringTokenizer(getParameter(DF_PARAM));
			numerDF = Integer.parseInt(st.nextToken());
			denomDF = Integer.parseInt(st.nextToken());
		
		data.addVariable("fDistn", new FDistnVariable("F distn", numerDF, denomDF));
		
		return data;
	}
}