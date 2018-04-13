package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import randomStat.*;

import distribution.*;


public class BinomialDistnApplet extends XApplet {
	static final private String N_PARAM = "n";
	
	public void setupApplet() {
		String nParamString = getParameter(N_PARAM);
		StringTokenizer st = new StringTokenizer(nParamString);
		int maxN = Integer.parseInt(st.nextToken());
		int startN = Integer.parseInt(st.nextToken());
		
		setLayout(new BorderLayout());
		add("Center", new BinomialDistnPanel(this, maxN, startN, ProportionLayout.HORIZONTAL,
																						DiscreteProbView.DRAG_PROB));
	}
}