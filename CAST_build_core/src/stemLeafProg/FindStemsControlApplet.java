package stemLeafProg;

import java.awt.*;
import java.util.StringTokenizer;

import dataView.*;
import utils.*;


public class FindStemsControlApplet extends XApplet {
	static final private String LIST_APPLET_PARAM = "listNames";
	
	public void setupApplet() {
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		checkButton = new XButton("Check Stems", this);
		add(checkButton);
	}
	
	private boolean localAction(Object target) {
		if (target == checkButton) {
			String listNameString = getParameter(LIST_APPLET_PARAM);
			StringTokenizer st = new StringTokenizer(listNameString);
			while (st.hasMoreTokens()) {
				FindStemsApplet findStemApplet = (FindStemsApplet)getApplet(st.nextToken());
				findStemApplet.checkStems();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
	private XButton checkButton;
}