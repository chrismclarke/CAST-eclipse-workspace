package percentile;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import exercise2.*;

//import boxPlot.*;


public class CumFunctInverseView extends CumFunctDotPlotView {
//	static public final String CUM_FUNCT_INVERSE = "cumFunctInverse";
	
	static final public Color kPercentArrowColor = new Color(0x009900);
	
	public CumFunctInverseView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																				DataSet refData, String refKey, VertAxis cumProbAxis) {
		super(theData, applet, theAxis, refData, refKey, PropnRangeView.LESS_EQUAL, cumProbAxis);
	}
	
	protected double getXReferenceValue() {
		double prob = super.getXReferenceValue() / 100.0;		//	reference value is percentage
		
		NumValue sortedData[] = getNumVariable().getSortedData();
		return PercentileInfo.evaluatePercentile(sortedData, prob, cumEvaluateType);
	}
	
	protected void drawDragLine(Graphics g, int refHoriz, int topBorder) {
		NumVariable refVar = getReferenceVariable();
		double prob = refVar.doubleValueAt(0) / 100.0;		//	reference value is percentage
		int probPos = cumProbAxis.numValToRawPosition(prob);
		int probVert = translateToScreen(0, probPos, null).y;
			
		if (doingDrag) {
			g.setColor(Color.black);
			g.drawLine(0, probVert, refHoriz, probVert);
			g.setColor(Color.red);
			g.drawLine(0, probVert - 1, refHoriz, probVert - 1);
			g.drawLine(0, probVert + 1, refHoriz, probVert + 1);
		}
	}
	
	protected void drawCumArrow(Graphics g, NumValue[] sortedData, double ref, int refHorizPos) {
		NumVariable refVar = getReferenceVariable();
		double prob = refVar.doubleValueAt(0) / 100.0;		//	reference value is percentage
		int probPos = cumProbAxis.numValToRawPosition(prob);
		Point p = translateToScreen(refHorizPos, probPos, null);
		
		g.setColor(Color.red);
		g.drawLine(0, p.y, p.x - 1, p.y);
		for (int i=1 ; i<5 ; i++)
			g.drawLine(p.x - 1 - i, p.y - i, p.x - 1 - i, p.y + i);
		g.setColor(kPercentArrowColor);
		g.drawLine(p.x, p.y, p.x, getSize().height - getViewBorder().bottom);
	}
	
	protected void drawBottomPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		if (allowDrag) {
			g.setColor(kPercentArrowColor);
			Point p = translateToScreen(refHorizPos, 0, null);
			g.drawLine(p.x, p.y, p.x, getSize().height);
			for (int i=1 ; i<5 ; i++)
				g.drawLine(p.x - i, getSize().height - i - 1, p.x + i, getSize().height - i - 1);
			
			g.setColor(getForeground());
		}
		
		super.drawBottomPanel(g, yVar, ref, refHorizPos);
	}

//-----------------------------------------------------------------------------------
	
	protected int getMinMouseMove() {
		return 1;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		NumVariable refVar = getReferenceVariable();
		double propn = refVar.doubleValueAt(0) / 100.0;
		int propnVertPos = cumProbAxis.numValToRawPosition(propn);
		int propnVert = translateToScreen(0, propnVertPos, null).y;
		
		if (Math.abs(y - propnVert) > kHitSlop)
			return null;
		else
			return new VertDragPosInfo(y, 0, y - propnVert);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0 || hitPos.y + hitOffset >= cumProbAxis.getAxisLength())
			return null;
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		doingDrag = true;
		repaint();
		((DragMultiVertAxis)cumProbAxis).setDragValue((NumValue)getReferenceVariable().valueAt(0));
		((DragMultiVertAxis)cumProbAxis).setAlternateLabels(2);
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			if (fromPos == null)
				doingDrag = true;
			NumVariable refVar = getReferenceVariable();
			NumValue refValue = (NumValue)(refVar.valueAt(0));
			
			double newRef = 0.0;
			VertDragPosInfo newPos = (VertDragPosInfo)toPos;
			
			try {
				newRef = cumProbAxis.positionToNumVal(newPos.y + hitOffset) * 100.0;
				if (refValue.decimals == 0)
					newRef = Math.rint(newRef);
				else
					newRef = Math.rint(10.0 * newRef) / 10.0;
			} catch (AxisException ex) {
				return;
			}
			
			if (refValue.toDouble() != newRef) {
				refValue.setValue(newRef);
				refData.valueChanged(0);
				getData().variableChanged(getData().getDefaultNumVariableKey());
				((DragMultiVertAxis)cumProbAxis).setDragValue((NumValue)getReferenceVariable().valueAt(0));
			}
			
			if (getApplet() instanceof ExerciseApplet)
				((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
		((DragMultiVertAxis)cumProbAxis).setAlternateLabels(1);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		NumVariable refVar = getReferenceVariable();
		NumValue refValue = (NumValue)(refVar.valueAt(0));
		double step = (refValue.decimals == 0) ? 1.0 : 0.1;
		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN) {
			refValue.setValue(Math.max(0.0, refValue.toDouble() - step));
			refData.valueChanged(0);
			getData().variableChanged(getData().getDefaultNumVariableKey());		}
		else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP) {
			refValue.setValue(Math.min(100.0, refValue.toDouble() + step));
			refData.valueChanged(0);
			getData().variableChanged(getData().getDefaultNumVariableKey());
		}
	}
	
}
	
