package ssq;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;


public class ComponentEqnPanel extends Binary {
	static final private int kMaxSsqs = 10;
	
	static public int combineComponents(int[] componentType) {
											//	For highlighting several components
		int combinedType = 0;
		for (int i=0 ; i<componentType.length ; i++) {
			if (componentType[i] < 0 || componentType[i] >= kMaxSsqs)
				throw new RuntimeException("Error! Index of component greater than 10");
			combinedType = combinedType * kMaxSsqs + componentType[i];
		}
		return combinedType;
	}
	
	static private FormulaPanel oneComponent(ValueView compView, Image compImage,
																	int imageWidth, int imageHeight, FormulaContext context) {
		if (compImage == null)
			return new SummaryValue(compView, context);
		else
			return new SummaryValue(compView, compImage, imageWidth, imageHeight, context);
	}
	
	static private FormulaPanel sumComponents(int startIndex, OneValueView[] compView,
																			Image[] compImage, Color[] compColor, int imageWidth,
																			int imageHeight, FormulaContext context) {
		FormulaContext compContext = context.getRecoloredContext(compColor[startIndex]);
		FormulaPanel head = oneComponent(compView[startIndex], compImage[startIndex],
																	imageWidth, imageHeight, compContext);
		if (startIndex == compView.length - 1)
			return head;
		else {
			FormulaPanel tail = sumComponents(startIndex + 1, compView, compImage,
																									compColor, imageWidth, imageHeight, context);
			return new Binary(Binary.PLUS, head, tail, context);
		}
	}
	
	static final private OneValueView[] createViews(DataSet data, String[] compKey,
																									NumValue maxValue, FormulaContext context) {
		XApplet applet = context.getApplet();
		OneValueView compView[] = new OneValueView[compKey.length];
		for (int i=0 ; i<compKey.length ; i++) {
			compView[i] = new OneValueView(data, compKey[i], applet, maxValue);
			compView[i].setNameDraw(false);
			compView[i].setHighlightSelection(i == 0);
			compView[i].setHighlightBackground(Color.yellow);
		}
		return compView;
	}
	
	private OneValueView compView[];
	
	public ComponentEqnPanel(OneValueView[] compView, Image[] compImage,
														Color[] compColor, int imageWidth, int imageHeight, FormulaContext context) {
		super(EQUALS, oneComponent(compView[0], compImage[0], imageWidth, imageHeight, context.getRecoloredContext(compColor[0])),
					sumComponents(1, compView, compImage, compColor, imageWidth, imageHeight, context), context);
		this.compView = compView;
	}
	
	public ComponentEqnPanel(DataSet data, String[] compKey, NumValue maxValue,
												Image[] compImage, Color[] compColor, int imageWidth, int imageHeight,
												FormulaContext context) {
		this(createViews(data, compKey, maxValue, context), compImage, compColor, imageWidth,
																																				imageHeight, context);
	}
	
	public void highlightComponent(int componentType) {
		if (componentType == 0) {
			compView[0].setHighlightSelection(true);
			compView[0].repaint();
			
			for (int i=1 ; i<compView.length ; i++) {
				compView[i].setHighlightSelection(false);
				compView[i].repaint();
			}
		}			//		Cannot highlight component 0 (Total ssq) at same time as any others
		else
			for (int i=0 ; i<compView.length ; i++) {
				boolean highlight = false;
				int tempType = componentType;
				while (tempType > 0) {
					if (tempType % kMaxSsqs == i)
						highlight = true;
					tempType /= kMaxSsqs;
				}
				
				compView[i].setHighlightSelection(highlight);
				compView[i].repaint();
			}
	}
}