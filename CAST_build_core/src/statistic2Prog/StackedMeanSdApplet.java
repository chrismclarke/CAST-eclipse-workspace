package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;

import statistic2.*;


public class StackedMeanSdApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CROSS_SIZE_PARAM = "crossSize";
	static final private String DECIMALS_PARAM = "decimals";
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			axis.setAxisName(data.getVariable("y").name);
			
		add("Bottom", axis);
		
			StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
			int meanDecimals = Integer.parseInt(st.nextToken());
			int sdDecimals = Integer.parseInt(st.nextToken());
			StackedMeanSDView dataView = new StackedMeanSDView(data, this, axis, meanDecimals, sdDecimals);
			int crossSize = DataView.MEDIUM_CROSS;
			String crossSizeString = getParameter(CROSS_SIZE_PARAM);
			if (crossSizeString != null) {
				if (crossSizeString.equals("dot"))
					crossSize = DataView.DOT_CROSS;
				else if (crossSizeString.equals("small"))
					crossSize = DataView.SMALL_CROSS;
				else if (crossSizeString.equals("large"))
					crossSize = DataView.LARGE_CROSS;
			}
			dataView.setCrossSize(crossSize);
			dataView.lockBackground(Color.white);
			dataView.setMeanSDColor(Color.red);
			
		add("Center", dataView);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
}