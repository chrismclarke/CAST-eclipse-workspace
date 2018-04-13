package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class DeletedResidView extends ScatterView {
	
	protected String lsKey, deletedLSKey;
	
	public DeletedResidView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
															String xKey, String yKey, String lsKey, String deletedLSKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lsKey = lsKey;
		this.deletedLSKey = deletedLSKey;
		setRetainLastSelection(true);
	}
	
	public void paintView(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		
		drawBackground(g, selectedIndex);
		super.paintView(g);
	}
	
	protected void drawDeletedResid(Graphics g, LinearModel delModel, NumVariable xVar,
																																						int index) {
		NumValue x = (NumValue)xVar.valueAt(index);
		
		Point pData = getScreenPoint(index, x, null);
		double delMean = delModel.evaluateMean(x);
		
		int vertPos = yAxis.numValToRawPosition(delMean);
		int horizPos = axis.numValToRawPosition(x.toDouble());
		Point pDelFit = translateToScreen(horizPos, vertPos, null);
		g.drawLine(pData.x, pData.y, pDelFit.x, pDelFit.y);
	}
	
	protected void drawBackground(Graphics g, int selectedIndex) {
		LinearModel model = (LinearModel)getVariable(lsKey);
		g.setColor((selectedIndex < 0) ? Color.gray : Color.lightGray);
		model.drawMean(g, this, axis, yAxis);
		
		if (selectedIndex >= 0) {
			LinearModel delModel = (LinearModel)getVariable(deletedLSKey);
			if (delModel.setDeletedIndex(selectedIndex))
				delModel.updateLSParams(yKey);
			g.setColor(Color.blue);
			delModel.drawMean(g, this, axis, yAxis);
			
			g.setColor(Color.red);
			NumVariable xVar = (NumVariable)getVariable(xKey);
			drawDeletedResid(g, delModel, xVar, selectedIndex);
		}
		
		g.setColor(getForeground());
	}
	
}
	
