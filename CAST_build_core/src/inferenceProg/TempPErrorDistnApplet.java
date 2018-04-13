package inferenceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class TempPErrorDistnApplet extends XApplet {
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String PROB_INFO_PARAM = "probAxis";
	static final private String K_CONST_PARAM = "kConst";
	
	private DataSet data;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		
		add("Center", errorDistnPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
			v.readLabels(getParameter(CAT_LABELS_PARAM));
			v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", v);
		
			int count[] = v.getCounts();
			int n = v.noOfValues();
			double p = count[0] / (double)n;
			double pSD = Math.sqrt(p * (1 - p) / n);
			
			System.out.println("x/n = " + count[0] + "/" + n + " = " + p + "\npSD = " + pSD);
			
			NormalDistnVariable errorDistn = new NormalDistnVariable("Distn of error");
			errorDistn.setMean(0.0);
			errorDistn.setSD(pSD);
			
			String kString = getParameter(K_CONST_PARAM);
			if (kString != null) {
				double k = Double.valueOf(kString).doubleValue();
				errorDistn.setMinSelection(-k * pSD);
				errorDistn.setMaxSelection(k * pSD);
			}
		
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
	
	private XPanel errorDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, "errorDistn", ERROR_INFO_PARAM);
		thePanel.add("Bottom", horizAxis);
		
		
//			JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, horizAxis, "errorDistn", 0.0);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels(getParameter(PROB_INFO_PARAM));
		probAxis.setShowUnlabelledAxis(false);
		thePanel.add("Left", probAxis);
		
			DistnDensityView dataView = new DistnDensityView(data, this, horizAxis, probAxis, "errorDistn");
			dataView.setActiveNumVariable("dummy");
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
}