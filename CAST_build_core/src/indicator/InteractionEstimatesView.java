package indicator;

import java.awt.*;

import dataView.*;
import models.*;


public class InteractionEstimatesView extends DataView {
//	static public final String INTERACTION_ESTIMATES = "interactionEstimates";
	
	static final private Color kInteractParamColor = new Color(0x009900);
	
	private String xKey, zKey, modelKey;
//	private NumValue maxParam;
	private boolean[] inestimableParams;
	
	private CatInteractTermRecord interactTerm;
	
	public InteractionEstimatesView(DataSet theData, XApplet applet,
						String xKey, String zKey, String modelKey, NumValue maxParam) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		
		this.xKey = xKey;
		this.zKey = zKey;
		this.modelKey = modelKey;
//		this.maxParam = maxParam;
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		
		interactTerm = new CatInteractTermRecord(xVar, zVar, maxParam, null);
	}
	
	public void setInestimableParams(boolean[] inestimableParams) {
		this.inestimableParams = inestimableParams;
	}
	
	public void paintView(Graphics g) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		int nx = xVar.noOfCategories();
		int nz = zVar.noOfCategories();
		
		int nInteractParams = (nx - 1) * (nz - 1);
		NumValue paramVal[] = new NumValue[nInteractParams];
		for (int i=0 ; i<nInteractParams ; i++)
			paramVal[i] = model.getParameter(nx + nz - 1 + i);
		
		if (inestimableParams != null)
			for (int i=0 ; i<paramVal.length ; i++)
				if (inestimableParams[i])
					paramVal[i] = null;
		
		interactTerm.drawParameters(g, 0, 0, null, paramVal, kInteractParamColor);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		return interactTerm.getMinimumSize(g);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
	
