package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;

import exerciseSD.*;


public class TempErrorDistnApplet extends XApplet {
	static final private String DATA_INFO_PARAM = "dataAxis";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String MEAN_SD_DECIMALS_PARAM = "meanSdDecimals";
	
	static final private Color kCrossColor = new Color(0x000099);
	
	private DataSet data;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, dataPanel(data));
		add(ProportionLayout.BOTTOM, errorDistnPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", yVar);
		
			ValueEnumeration ye = yVar.values();
			double sy = 0.0;
			double syy = 0.0;
			int n = 0;
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sy += y;
				syy += y * y;
				n ++;
			}
			double yMean = sy / n;
			double ySD = Math.sqrt((syy - sy * yMean) / (n-1));
			double errorSD = ySD / Math.sqrt(n);
			
			System.out.println("yMean = " + yMean + "\nySD = " + ySD + "\nyErrorSD = " + errorSD);
			
			NormalDistnVariable errorDistn = new NormalDistnVariable("Distn of error");
			errorDistn.setMean(0.0);
			errorDistn.setSD(errorSD);
		
		data.addVariable("errorDistn", errorDistn);
		
		NumVariable dummyVar = new NumVariable("dummy");
		data.addVariable("dummy", dummyVar);
		
		return data;
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedDotPlotView dataView;
			String meanSdDecimalString = getParameter(MEAN_SD_DECIMALS_PARAM);
			if (meanSdDecimalString == null) {
				dataView = new StackedDotPlotView(data, this, theHorizAxis);
				dataView.setForeground(kCrossColor);
			}
			else {
				dataView = new StackMeanSdView(data, this, theHorizAxis, "y", Integer.parseInt(meanSdDecimalString));
				dataView.setForeground(DataView.dimColor(kCrossColor, 0.6));
			}
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel errorDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "errorDistn", ERROR_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, theHorizAxis, "errorDistn", 0.0);
			dataView.setActiveNumVariable("dummy");
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
}