package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

//import scatterProg.*;
import loess.*;


class WindowWidthSlider extends XSlider {
	static private int translateWidth(int width, boolean onlyOdd) {
		return onlyOdd ? (width - 3) / 2 : width - 2;
	}
	
	private boolean onlyOdd;
	
//	public WindowWidthSlider(XApplet applet, int maxWidth, int initialWidth) {
//		this(applet, maxWidth, initialWidth, false);
//	}
	
	public WindowWidthSlider(XApplet applet, int maxWidth, int initialWidth, boolean onlyOdd) {
		super(applet.translate("Min"), applet.translate("Max"), applet.translate("Window width") + " = ", 0, translateWidth(maxWidth, onlyOdd),
																						translateWidth(initialWidth, onlyOdd), applet);
		this.onlyOdd = onlyOdd;
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getWindowWidth(val), 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
	
	protected int getWindowWidth() {
		return getWindowWidth(getValue());
	}
	
	protected int getWindowWidth(int val) {
		return onlyOdd ? val * 2 + 3 : val + 2;
	}
}

public class LoessApplet extends ScatterApplet {
	static final private String WINDOW_WIDTH_PARAM = "windowWidth";
	
	private ScatterLoessDragView theView;
	private WindowWidthSlider wSlider;
	private int maxWindowWidth, initialWindowWidth;
	private boolean onlyOdd = false;
	
	public void setupApplet() {
		String windowWidthParam = getParameter(WINDOW_WIDTH_PARAM);
		StringTokenizer st = new StringTokenizer(windowWidthParam);
		maxWindowWidth = Integer.parseInt(st.nextToken());
		if (maxWindowWidth < 2) {
			StringTokenizer xst = new StringTokenizer(getParameter(X_VALUES_PARAM));
			maxWindowWidth = xst.countTokens();
		}
		initialWindowWidth = Integer.parseInt(st.nextToken());
		if (initialWindowWidth < 2)
			initialWindowWidth = maxWindowWidth / 3;
			
		if (st.hasMoreTokens())
			onlyOdd = st.nextToken().equals("odd");
		
		super.setupApplet();
	}
	
	protected XPanel controlPanel(DataSet data) {
		int nSliderValues = onlyOdd ? (maxWindowWidth - 3) / 2 : maxWindowWidth - 2;
		int leftRightMargin = (nSliderValues > 40) ? 40
													: (nSliderValues > 20) ? 80
													: 130;
		
		XPanel thePanel = new InsetPanel(leftRightMargin, 0);
		thePanel.setLayout(new BorderLayout());
		
		wSlider = new WindowWidthSlider(this, maxWindowWidth, initialWindowWidth, onlyOdd);
		
		thePanel.add("Center", wSlider);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		LoessSmoothVariable loessVar = new LoessSmoothVariable("Loess smooth", data, "x", "y");
		data.addVariable("loess", loessVar);
		loessVar.setAxes(theHorizAxis, theVertAxis);
		loessVar.initialise(initialWindowWidth);
		
		theView = new ScatterLoessDragView(data, this, theHorizAxis, theVertAxis, "x", "y", "loess");
		return theView;
	}
	
	private boolean localAction(Object target) {
		if (target == wSlider) {
			LoessSmoothVariable loessVar = (LoessSmoothVariable)data.getVariable("loess");
			loessVar.initialise(wSlider.getWindowWidth());
			data.variableChanged("loess");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}