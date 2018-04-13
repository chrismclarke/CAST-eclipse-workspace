package formula;

import java.awt.*;

import dataView.*;


public class Bracket extends FormulaPanel {
	static final private int kBracketExtraHeight = 2;
	static final private int kMinHalfStraight = 5;
	static final private int kLeftLabelGap = 3;
	
	static final private int bracketPix[] = {1, 1, 1, 2, 3, 4, 7};
	
	private FormulaPanel val;
	private LabelValue leftLabel = null;			//		Mainly for "Pr(...)"
	private int leftLabelWidth;
	
	private int bracketCurveHeight, curveWidth;
	
	public Bracket(FormulaPanel val, FormulaContext context) {
		super(context);
		
		this.val = val;
		
		val.setParent(this);
		add(val);
	}
	
	public void setLeftLabel(LabelValue leftLabel) {
		this.leftLabel = leftLabel;
	}
	
	public void reinitialise() {
		val.reinitialise();
		super.reinitialise();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			val.initialise(val.getGraphics());
			
			layoutAscent = val.layoutAscent + kBracketExtraHeight;
			layoutDescent = val.layoutDescent + kBracketExtraHeight;
			int halfHeight = (layoutAscent + layoutDescent) / 2;
			
			bracketCurveHeight = 0;
			curveWidth = 0;
			for (int i=0 ; i<bracketPix.length ; i++) {
				if (halfHeight - bracketPix[i] < kMinHalfStraight)
					break;
				curveWidth = i + 1;
				bracketCurveHeight += bracketPix[i];
				halfHeight -= bracketPix[i];
			}
			
			layoutWidth = val.layoutWidth + 2 * (curveWidth + 1);
			if (leftLabel != null) {
				leftLabelWidth = leftLabel.stringWidth(g) + kLeftLabelGap;
				layoutWidth += leftLabelWidth;
			}
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		horizStart += curveWidth;
		if (leftLabel != null)
			horizStart += leftLabelWidth;
		
		val.setBounds(horizStart, baseline - val.layoutAscent, val.layoutWidth,
															val.layoutAscent + val.layoutDescent);
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		if (leftLabel != null) {
			leftLabel.drawRight(g, horizStart, baseline);
			horizStart += leftLabelWidth;
		}
		
		int bracketTop = baseline - layoutAscent;
		int leftArcLeft = horizStart;
		int rightArcRight = horizStart + layoutWidth - leftLabelWidth;
		
		int drawBracketWidth = curveWidth + 1;
		
		int topVert = bracketTop;
		int bottomVert = bracketTop + layoutAscent + layoutDescent - 1;
		int horiz = leftArcLeft + drawBracketWidth - 1;
		for (int i=0 ; i<curveWidth ; i++) {
			int oldTopVert = topVert;
			int oldBottomVert = bottomVert;
			topVert += bracketPix[i];
			bottomVert -= bracketPix[i];
			g.drawLine(horiz, oldTopVert, horiz, topVert - 1);
			g.drawLine(horiz, bottomVert + 1, horiz, oldBottomVert);
			horiz --;
		}
		g.drawLine(leftArcLeft, bracketTop + bracketCurveHeight, leftArcLeft,
									bracketTop + layoutAscent + layoutDescent - bracketCurveHeight - 1);
		
		topVert = bracketTop;
		bottomVert = bracketTop + layoutAscent + layoutDescent - 1;
		horiz = rightArcRight - drawBracketWidth;
		for (int i=0 ; i<curveWidth ; i++) {
			int oldTopVert = topVert;
			int oldBottomVert = bottomVert;
			topVert += bracketPix[i];
			bottomVert -= bracketPix[i];
			g.drawLine(horiz, oldTopVert, horiz, topVert - 1);
			g.drawLine(horiz, bottomVert + 1, horiz, oldBottomVert);
			horiz ++;
		}
		g.drawLine(rightArcRight - 1, bracketTop + bracketCurveHeight, rightArcRight - 1,
									bracketTop + layoutAscent + layoutDescent - bracketCurveHeight - 1);
		
	}
	
	protected double evaluateFormula() {
		return val.evaluateFormula();
	}
}