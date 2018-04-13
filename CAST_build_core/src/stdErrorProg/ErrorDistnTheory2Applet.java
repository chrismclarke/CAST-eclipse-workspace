package stdErrorProg;

import java.awt.*;


public class ErrorDistnTheory2Applet extends ErrorDistnTheoryApplet {
//																		Only shows error distribution (not population)

	public void setupApplet() {
		showError = getParameter(ERROR_AXIS_PARAM) != null;
		isMedianTarget = (getParameter(TARGET_PARAM) != null) && getParameter(TARGET_PARAM).equals("median");
		data = getData();
		
		setLayout(new BorderLayout(10, 0));
		
			saveDistnView(null);			//		no population view
		add("North", sampleSizePanel(true));
		add("Center", errorPanel(data));
		
		setTheoryParameters(data);
	}
}