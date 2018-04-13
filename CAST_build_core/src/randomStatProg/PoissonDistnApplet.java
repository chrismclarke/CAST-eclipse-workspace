package randomStatProg;

import java.awt.*;

import dataView.*;

import distribution.*;
import randomStat.*;


public class PoissonDistnApplet extends XApplet {
	static final private String AXIS_PARAM = "axis";
	static final private String LAMBDA_PARAM = "lambda";
	static final private String BASE_LAMBDA_PARAM = "baseLambda";
	
	public void setupApplet() {
		setLayout(new BorderLayout());
		double baseLambda = Double.parseDouble(getParameter(BASE_LAMBDA_PARAM));
		add("Center", new PoissonDistnPanel(this, getParameter(LAMBDA_PARAM),
												getParameter(AXIS_PARAM), baseLambda, DiscreteProbView.DRAG_PROB));
	}
}