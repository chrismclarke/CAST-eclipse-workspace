package axis;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import dataView.*;


abstract public class NumCatAxis extends Axis {
	protected Vector labels;
	public double minOnAxis, maxOnAxis;				//		only relevant for numerical axes
	public int noOfCats;									//		only relevant for categ axes
	
	private NumValue labelMin, labelStep;
	private boolean foundLabelSizes = false;
	
	static final public boolean RAW_VALUES = true;
	static final public boolean TRANS_VALUES = false;
	protected boolean rawNotTransValues = RAW_VALUES;
	
	static final public int kDefaultTransLabelDigits = 3;
	private int transLabelDigits = kDefaultTransLabelDigits;
	
	static final private double invLog10 = 1.0 / Math.log(10.0);
	
	private final Font superscriptFont;
	
	private Vector extraValues = null;
	
//---------------------------------------------
	
	static final public int LEFT_ALIGN = 0;
	static final public int RIGHT_ALIGN = 1;
	
	protected int valHeight;
	static private final int kValueBorder = 2;
	static private final int kSuperBaseline = 3;
	static private final int kSubBaseline = 2;
	
	static final private String kTenString = "10";
	
	private String kSimpleLogString, kLogString, noTransformString;
	
//---------------------------------------------
	
	static private int countIntegersBetween(double min, double max, int step) {
		return (int)Math.round(Math.floor(max / step)) - (int)Math.round(Math.ceil(min / step)) + 1;
	}
	
	static public String neatNumAxisLabels(double axisMin, double axisMax, int minLabels) {
		int decimals = 0;
		double factor = 1.0;
		double minScaled = axisMin;
		double maxScaled = axisMax;
		int integersBetween = countIntegersBetween(minScaled, maxScaled, 1);
//		System.out.println("integers between " + minScaled + " and " + maxScaled + " = " + integersBetween);
		while (integersBetween >= 10 * minLabels) {
			factor *= 10.0;
			decimals -= 1;
			minScaled /= 10.0;
			maxScaled /= 10.0;
			integersBetween = countIntegersBetween(minScaled, maxScaled, 1);
//			System.out.println("/= 10: integers between " + minScaled + " and " + maxScaled + " = " + integersBetween);
		}
		
		while (integersBetween < minLabels) {
			factor /= 10.0;
			decimals += 1;
			minScaled *= 10.0;
			maxScaled *= 10.0;
			integersBetween = countIntegersBetween(minScaled, maxScaled, 1);
//			System.out.println("*= 10: integers between " + minScaled + " and " + maxScaled + " = " + integersBetween);
		}
							//	between minLabels and 10*minLabels labels with step=10^k
		double step =  1.0;
		if (countIntegersBetween(minScaled, maxScaled, 5) >= minLabels)
			step = 5.0;
		else if (countIntegersBetween(minScaled, maxScaled, 2) >= minLabels)
			step = 2.0;
		
		double firstScaled = Math.ceil(minScaled / step) * step;
		
		if (decimals < 0)
			decimals = 0;
		
		axisMin = Math.floor(axisMin / factor) * factor;
		axisMax = Math.ceil(axisMax / factor) * factor;
		
		return new NumValue(axisMin, decimals) + " " + new NumValue(axisMax, decimals) + " " + new NumValue(firstScaled * factor, decimals) + " " + new NumValue(step * factor, decimals);
	}
	
//---------------------------------------------
	
	public NumCatAxis(XApplet applet) {
		super(applet);
		labels = new Vector(16);
		superscriptFont = applet.getSmallFont();
		noTransformString = applet.translate("none");
		kSimpleLogString = applet.translate("log");
		kLogString = kSimpleLogString + "(y)";
	}
	
	public Vector getLabels() {
		return labels;
	}
	
	public void setCatLabels(CatVariableInterface variable) {
		labels.removeAllElements();
		noOfCats = variable.noOfCategories();
		for (int index=0 ; index<noOfCats ; index++) {
			Value catLabel = variable.getLabel(index);
			AxisLabel nextAxisLabel = new AxisLabel(catLabel, (index + 0.5) / noOfCats);
			labels.addElement(nextAxisLabel);
		}
		foundLabelSizes = false;
		repaint();
	}
	
	public void setLabelLabels(LabelVariable variable) {
		labels.removeAllElements();
		noOfCats = variable.noOfValues();
		for (int index=0 ; index<noOfCats ; index++) {
			Value catLabel = variable.valueAt(index);
			AxisLabel nextAxisLabel = new AxisLabel(catLabel, (index + 0.5) / noOfCats);
			labels.addElement(nextAxisLabel);
		}
		foundLabelSizes = false;
		repaint();
	}
	
	public int getLabelDecimals() {
		if (labelMin == null || labelStep == null)
			return 0;
		else
			return Math.max(labelMin.decimals, labelStep.decimals);
	}
	
	protected void addAxisLabel(Value val, double position) {
		AxisLabel theLabel = new AxisLabel(val, position);
		labels.addElement(theLabel);
	}
	
	private void initNumLabelValues() throws AxisException {
		labels.removeAllElements();
		if (labelStep == null || labelMin == null)
			return;
		
//		double axisRange = maxOnAxis - minOnAxis;
//		if (labelStep.value < axisRange / 16)		//		no more than 17 labels
//			throw new AxisException(AxisException.FORMAT_ERROR);
//																//		we need more labels for extended boiling-point scale
		
		NumValue nextExtra = null;
		Enumeration e = null;
		if (extraValues != null) {
			e = extraValues.elements();
			if (e.hasMoreElements())
				nextExtra = (NumValue)e.nextElement();
		}
		
		int decimals = getLabelDecimals();
		double label = labelMin.toDouble();
		double step = labelStep.toDouble();
		NumValue nextStepVal = ((label >= minOnAxis) && (label <= maxOnAxis))
										? new NumValue(label, decimals) : null;
		
		while (nextStepVal != null || nextExtra != null) {
			if (nextStepVal == null || (nextExtra != null && nextExtra.toDouble() <= nextStepVal.toDouble())) {
				addAxisLabel(nextExtra, 0.0);
				if (e.hasMoreElements())
					nextExtra = (NumValue)e.nextElement();
				else
					nextExtra = null;
			}
			else {
				addAxisLabel(nextStepVal, 0.0);
				label += step;
				if (label <= maxOnAxis)
					nextStepVal = new NumValue(label, decimals);
				else
					nextStepVal = null;
			}
		}
		
		setNumLabelPositions();
		foundLabelSizes = false;
	}
	
	private void initNumLabelPositions() throws AxisException {
		labels.removeAllElements();
		if (labelStep == null || labelMin == null)
			return;
		
		double axisRange = maxOnAxis - minOnAxis;
//		if (labelStep.value < axisRange / 16)		//		no more than 17 labels
//			throw new AxisException(AxisException.FORMAT_ERROR);
//																//		we need more labels for extended boiling-point scale
			
		if (labelMin.toDouble() >= minOnAxis) {
			double label = labelMin.toDouble();
			double step = labelStep.toDouble();
			while (label <= maxOnAxis) {
				addAxisLabel(null, (label - minOnAxis) / axisRange);
				label += step;
			}
		}
		setNumLabelValues();
	}
	
	private void setNumLabelPositions() {		//		assumes raw values have already been set for labels
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = ((NumValue)nextLabel.label).toDouble();
			double transLabel = (powerIndex == kOnePowerIndex) ? labelValue
										: (powerIndex == kZeroPowerIndex) ? Math.log(labelValue) * invLog10
										: (powerIndex > kZeroPowerIndex) ? Math.pow(labelValue, power)
										: - Math.pow(labelValue, power);
			nextLabel.position = (transLabel - minPower) / powerRange;
		}
	}
	
	private void setNumLabelValues() {		//		assumes positions have already been set for labels
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double value = minPower + (maxPower - minPower) * nextLabel.position;
			if (powerIndex == kOnePowerIndex)
				nextLabel.label = new NumValue(value, Math.max(labelMin.decimals,
																							labelStep.decimals));
			else {
				double tempVal = Math.abs(value);
				int decimals = transLabelDigits;
				while (tempVal >= 1.0 && decimals > 0) {
					decimals--;
					tempVal /= 10.0;
				}
				nextLabel.label = new NumValue(value, decimals);
			}
		}
		foundLabelSizes = false;
	}
	
	public void readNumLabels(String labelInfo) {
		extraValues = null;
		StringTokenizer theLabels = new StringTokenizer(labelInfo);
		
		try {
			if (!theLabels.hasMoreTokens())
				throw new AxisException(AxisException.FORMAT_ERROR);
			String minString = theLabels.nextToken();
			
			minOnAxis = Double.parseDouble(minString);
			if (minOnAxis < 0.0) {
				minPowerIndex = kOnePowerIndex;
				maxPowerIndex = kOnePowerIndex;
			}
			else if (minOnAxis == 0.0)
				minPowerIndex = kZeroPowerIndex + 1;
			
			if (!theLabels.hasMoreTokens())
				throw new AxisException(AxisException.FORMAT_ERROR);
			String maxString = theLabels.nextToken();
			maxOnAxis = Double.parseDouble(maxString);
			if (maxOnAxis <= minOnAxis)
				throw new AxisException(AxisException.FORMAT_ERROR);
			
			labels.removeAllElements();
			setPowerIndex(kOnePowerIndex);
			
			try {
				if (!theLabels.hasMoreTokens())
					throw new AxisException(AxisException.FORMAT_ERROR);
				String labelString = theLabels.nextToken();
				labelMin = new NumValue(labelString);
				
				if (!theLabels.hasMoreTokens())
					throw new AxisException(AxisException.FORMAT_ERROR);
				String stepString = theLabels.nextToken();
				labelStep = new NumValue(stepString);
				
			} catch (NumberFormatException e) {
				labelMin = labelStep = null;
			} catch (AxisException e) {
				labelMin = labelStep = null;
			}
			
			if (theLabels.hasMoreTokens()) {
				extraValues = new Vector(5);
				while (theLabels.hasMoreTokens())
					extraValues.addElement(new NumValue(theLabels.nextToken()));
			}
			
			initNumLabelValues();
		} catch (NumberFormatException e) {
			setDefaultNumAxis();
		} catch (AxisException e) {
			setDefaultNumAxis();
		}
		repaint();
	}
	
	public void setFakeNumLabels(double max, NumValue step) {
																	//	assumes min & 1st label are zero
		labels.removeAllElements();
		
		double label = 0.0;
		while (label <= max) {
			addAxisLabel(new NumValue(label, step.decimals), label / max);
			label += step.toDouble();
		}
		
		repaint();
	}
	
	private void setDefaultNumAxis() {
		System.err.println("Axis incorrectly specified; 0-1 axis used.");
		minOnAxis = 0.0;
		maxOnAxis = 1.0;
		minPowerIndex = kZeroPowerIndex + 1;
		labels.removeAllElements();
	}
	
	public void resetLabelSizes() {
		foundLabelSizes = false;
	}
	
	public void findLabelSizes() {
		if (foundLabelSizes)
			return;
		Graphics g = getGraphics();
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			nextLabel.labelWidth = nextLabel.label.stringWidth(g);
		}
		setFontInfo(g);
		
		foundLabelSizes = true;
	}
	
	public int numValToPosition(double theValue) throws AxisException {
		if (Double.isInfinite(theValue)) {
			if (theValue < 0.0)
				throw new AxisException(AxisException.TOO_LOW_ERROR);
			else
				throw new AxisException(AxisException.TOO_HIGH_ERROR);
		}
		int thePosition = numValToRawPosition(theValue);
		if (thePosition < 0)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (thePosition > axisLength)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else
			return thePosition;
	}
	
	public double numValToDoublePos(double theValue) throws AxisException {
		if (Double.isInfinite(theValue)) {
			if (theValue < 0.0)
				throw new AxisException(AxisException.TOO_LOW_ERROR);
			else
				throw new AxisException(AxisException.TOO_HIGH_ERROR);
		}
		double thePosition = numValToRawDoublePos(theValue);
		if (thePosition < -0.4999)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (thePosition > axisLength + 0.4999)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else
			return thePosition;
	}
	
	public int numValToRawPosition(double theValue) {
		return (int)Math.round(numValToRawDoublePos(theValue));
	}
	
	public double numValToRawDoublePos(double theValue) {
		double transValue = (powerIndex == kOnePowerIndex) ? theValue
										: (powerIndex == kZeroPowerIndex) ? Math.log(theValue) * invLog10
										: (powerIndex > kZeroPowerIndex) ? Math.pow(theValue, power)
										: - Math.pow(theValue, power);
		
		return (axisLength - 1) * (transValue - minPower) / powerRange;
	}
	
	public int numTransValToRawPosition(double transValue) {
		double realPosition = (axisLength - 1) * (transValue - minPower) / powerRange;
		return (int)Math.round(realPosition);
	}
	
	public double positionToNumVal(int thePosition) throws AxisException {
		if (thePosition < 0)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (thePosition > axisLength)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else {
			double basicVal = minPower + thePosition * (maxPower - minPower) / (axisLength - 1);
			return (powerIndex == kOnePowerIndex) ? basicVal
										: (powerIndex == kZeroPowerIndex) ? Math.pow(10.0, basicVal)
										: (powerIndex > kZeroPowerIndex) ? Math.pow(basicVal, 1.0 / power)
										: Math.pow(- basicVal, 1.0 / power);
		}
	}
	
	public int catValToPosition(int catIndex) {
		return catValToPosition(catIndex, noOfCats);
	}
	
	public int catValToPosition(int catIndex, int nCats) {
		return (int)Math.round((axisLength - 1) * (catIndex + 0.5) / nCats);
	}
	
	public int positionToCatVal(int thePosition) {
		if (thePosition < 0)
			return 0;
		else if (thePosition >= axisLength)
			return noOfCats - 1;
		else
			return (thePosition * noOfCats) / axisLength;
	}
	
//---------------------------------------------------------------------
	
	static final protected int kZeroPowerIndex = 200;
	static final protected int kOnePowerIndex = 300;
	static final private int kMaxPowerIndex = 500;
	private int minPowerIndex = 0;
	private int maxPowerIndex = kMaxPowerIndex;
	private int powerIndex = kOnePowerIndex;		//	 0 => k = -2.0,
																//	200 => k = 0.0,
																//	500 => k = 3.0
	private double power = 1.0;
	
	protected double minPower = 0.0;
	protected double maxPower = 0.0;
	protected double powerRange = 0.0;
	
	private int transformPos[] = null;
	
	protected double translateIndexToPower(int powerIndex) {
		return (powerIndex - kZeroPowerIndex) * 0.01;
	}
	
	public void setPower(double power) {
		setPowerIndex(kZeroPowerIndex + (int)Math.round(100.0 * power));
	}
	
	public void setPowerIndex(int powerIndex) {
		if (powerIndex < minPowerIndex || powerIndex > maxPowerIndex)
			return;
		
		this.powerIndex = powerIndex;
		power = translateIndexToPower(powerIndex);
		switch (powerIndex) {
			case kOnePowerIndex:
				minPower = minOnAxis;
				maxPower = maxOnAxis;
				break;
			case kZeroPowerIndex:
				minPower = Math.log(minOnAxis) * invLog10;
				maxPower = Math.log(maxOnAxis) * invLog10;
				break;
			default:
				if (powerIndex > kZeroPowerIndex) {
					minPower = Math.pow(minOnAxis, power);
					maxPower = Math.pow(maxOnAxis, power);
				}
				else {
					minPower = -Math.pow(minOnAxis, power);
					maxPower = -Math.pow(maxOnAxis, power);
				}
		}
		powerRange = maxPower - minPower;
		if (rawNotTransValues)
			setNumLabelPositions();
		else {
			setNumLabelValues();
			findLabelSizes();
		}
		repaint();
		
		if (linkedData != null)
			linkedData.transformedAxis(this);
	}
	
	public void setTransValueDisplay(boolean rawNotTransValues) {
		this.rawNotTransValues = rawNotTransValues;
		try {
			if (rawNotTransValues) {
				initNumLabelValues();
				findLabelSizes();
			}
			else
				initNumLabelPositions();
		} catch (AxisException e) {
			setDefaultNumAxis();
		}
		repaint();
	}
	
	public int getPowerIndex() {
		return powerIndex;
	}
	
	private double transform(double value, int powerIndex) {
		return (powerIndex == kOnePowerIndex) ? value
										: (powerIndex == kZeroPowerIndex) ? Math.log(value) * invLog10
										: (powerIndex < kZeroPowerIndex) ? -Math.pow(value, (powerIndex - kZeroPowerIndex) * 0.01)
										: Math.pow(value, (powerIndex - kZeroPowerIndex) * 0.01);
	}
	
	public double transform(double value) {
		return transform(value, powerIndex);
	}
	
	public double inverseTransform(double value) {
		if (powerIndex == kOnePowerIndex)
			return value;
		else if (powerIndex == kZeroPowerIndex)
			return Math.pow(10.0, value);
		else if (powerIndex < kZeroPowerIndex)
			return Math.pow(-value, 100.0 / (powerIndex - kZeroPowerIndex));
		else
			return Math.pow(value, 100.0 / (powerIndex - kZeroPowerIndex));
	}
	
	private void setupTransformPos() {
		transformPos = new int[maxPowerIndex - minPowerIndex + 1];
		for (int i=minPowerIndex ; i<=maxPowerIndex ; i++) {
			double transValue = transform((minOnAxis + maxOnAxis) * 0.5, i);
			double transMin = transform(minOnAxis, i);
			double transMax = transform(maxOnAxis, i);
		
			transformPos[i - minPowerIndex] = (int)Math.round((axisLength - 1) * (transValue - transMin) / (transMax - transMin));
		}
	}
	
	public int findDragPos() {
		if (transformPos == null)
			setupTransformPos();
		return transformPos[powerIndex - minPowerIndex];
	}
	
	public int powerFromDragPos(int pos) {
		if (transformPos == null)
			setupTransformPos();
		for (int i=1 ; i<=maxPowerIndex - minPowerIndex ; i++)
			if (pos > transformPos[i]) {
				if (pos - transformPos[i] < transformPos[i-1] - pos)
					return minPowerIndex + i;
				else
					return minPowerIndex + i-1;
			}
		return maxPowerIndex;
	}
	
	public NumValue getPower() {
		return getPower(power);
	}
	
	public NumValue getPower(double power) {
		return new NumValue(power, 2);
	}
	
	public void setTransLabelDigits(int transLabelDigits) {
		this.transLabelDigits = transLabelDigits;
		repaint();
	}

//-----------------------------------------------------------------------------------
//	The following variables and routines are only for dragging transformation
//-----------------------------------------------------------------------------------
	
	private DataSet linkedData = null;
	private boolean canDragTransform = false;
	
	public void setLinkedData(DataSet linkedData, boolean canDragTransform) {
		this.linkedData = linkedData;
		this.canDragTransform = canDragTransform;
	}

//-----------------------------------------------------------------------------------
	
	static private final int kHitSlop = 4;
	private int hitOffset;
	protected boolean selectedVal = false;
	
	protected boolean canDrag() {
		return canDragTransform;
	}
	
	abstract protected int getAxisPosition(int xPos, int yPos);
																				//		either returns xPos or yPos
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int valPos = findDragPos();
		
		int hitPos = getAxisPosition(x, y) - lowBorderUsed;
		int hitOffset = hitPos - valPos;
		if (hitOffset > kHitSlop || hitOffset < -kHitSlop)
			return null;
		else
			return new TransformDragPosInfo(getPowerIndex(), hitOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		int hitPos = getAxisPosition(x, y) - lowBorderUsed - hitOffset;
		int newTransIndex = powerFromDragPos(hitPos);
		return new TransformDragPosInfo(newTransIndex);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			selectedVal = true;
			hitOffset = ((TransformDragPosInfo)startPos).hitOffset;
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedVal = false;
			repaint();
		}
		else {
			selectedVal = true;
			TransformDragPosInfo dragPos = (TransformDragPosInfo)toPos;
			int newPowerIndex = dragPos.transformIndex;
			setPowerIndex(newPowerIndex);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedVal = false;
		repaint();
	}
	
	

//-----------------------------------------------------------------------------------
	
	protected int powerWidth(Graphics g) {
		return powerWidth(g, powerIndex);
	}
	
	protected int maxPowerWidth(Graphics g) {
		int theWidth =  Math.max(powerWidth(g, minPowerIndex),
								Math.max(powerWidth(g, maxPowerIndex), powerWidth(g, kOnePowerIndex)));
		if (minPowerIndex <= kZeroPowerIndex)
			theWidth = Math.max(theWidth, powerWidth(g, kZeroPowerIndex));
		return theWidth;
	}
	
	private int powerWidth(Graphics g, int thePowerIndex) {
		int result = 0;
		FontMetrics fm = g.getFontMetrics();
		switch (thePowerIndex) {
			case kZeroPowerIndex:
				result = fm.stringWidth(kLogString);
			case kOnePowerIndex:
				result = fm.stringWidth(noTransformString);
			default:
				{
					String yString = (thePowerIndex > kZeroPowerIndex) ? "y" : "-y";
					int yWidth = fm.stringWidth(yString);
					Font oldFont = g.getFont();
					g.setFont(superscriptFont);
					int powerWidth = getPower(translateIndexToPower(thePowerIndex)).stringWidth(g);
					g.setFont(oldFont);
					result = yWidth + powerWidth;
				}
		}
		return result + 2 * kValueBorder;
	}
	
	protected void drawPower(Graphics g, int boxLeft, int boxTop) {
		int textBaseline = boxTop + valHeight - 2 - descent;
		int valWidth = powerWidth(g);
		
		g.setColor(Color.red);
		g.drawRect(boxLeft, boxTop, valWidth - 1, valHeight - 1);
		
		g.setColor(Color.white);
		g.fillRect(boxLeft + 1, boxTop + 1, valWidth - 2, valHeight - 2);
		
		g.setColor(Color.red);
		switch (getPowerIndex()) {
			case kZeroPowerIndex:
				g.drawString(kLogString, boxLeft + kValueBorder, textBaseline);
				break;
			case kOnePowerIndex:
				g.drawString(noTransformString, boxLeft + kValueBorder, textBaseline);
				break;
			default:
				{
					String yString = (getPowerIndex() > kZeroPowerIndex) ? "y" : "-y";
					g.drawString(yString, boxLeft + kValueBorder, textBaseline);
					FontMetrics fm = g.getFontMetrics();
					int yWidth = fm.stringWidth(yString);
					
					Font oldFont = g.getFont();
					g.setFont(superscriptFont);
					NumValue powerString = getPower();
//					int powerWidth = powerString.stringWidth(g);
					powerString.drawRight(g, boxLeft + kValueBorder + yWidth, textBaseline - kSuperBaseline);
					g.setFont(oldFont);
				}
		}
	}
	
	public int maxNamePowerWidth(Graphics g) {
		int nameWidth =  axisName.stringWidth(g);
		
		if (!(this instanceof TransAxisInterface) || minOnAxis < 0.0)
			return nameWidth;
		else if (minOnAxis == 0.0) {
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			int superWidth = g.getFontMetrics().stringWidth("3.00");
			g.setFont(oldFont);
			
			return nameWidth + superWidth;
		}
		else {
			FontMetrics fm = g.getFontMetrics();
			int logWidth = fm.stringWidth(kSimpleLogString + "()");
			int negWidth = fm.stringWidth("-()");
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			int tenWidth = g.getFontMetrics().stringWidth(kTenString);
			int superWidth = g.getFontMetrics().stringWidth("-2.00");
			g.setFont(oldFont);
			return nameWidth + Math.max(logWidth + tenWidth, negWidth + superWidth);
		}
	}
	
	public int drawNamePower(Graphics g, int horiz, int baseline, int alignment,
																									int maxWidth) {
		FontMetrics fm = g.getFontMetrics();
		int rightPos = horiz + maxWidth;
		int nameWidth = axisName.stringWidth(g);
		
		if (power == 0.0) {
			if (alignment == RIGHT_ALIGN) {
				int width = nameWidth + fm.stringWidth(kSimpleLogString + "()");
				Font oldFont = g.getFont();
				g.setFont(superscriptFont);
				width += g.getFontMetrics().stringWidth(kTenString);
				g.setFont(oldFont);
				horiz = rightPos - width;
			}
			
			g.drawString(kSimpleLogString, horiz, baseline);
			horiz += fm.stringWidth(kSimpleLogString);
			
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			g.drawString(kTenString, horiz, baseline + kSubBaseline);
			horiz += g.getFontMetrics().stringWidth(kTenString);
			g.setFont(oldFont);
			
			g.drawString("(" + axisName.toString() + ")", horiz, baseline);
		}
		else if (power == 1.0)
				axisName.drawRight(g, (alignment == RIGHT_ALIGN)
															? rightPos - nameWidth : horiz, baseline);
		else if (alignment == RIGHT_ALIGN) {
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			NumValue powerString = getPower();
			int powerWidth = powerString.stringWidth(g);
			powerString.drawRight(g, rightPos - powerWidth, baseline - kSuperBaseline);
			g.setFont(oldFont);
			axisName.drawRight(g, rightPos - powerWidth - nameWidth, baseline);
		}
		else {
			String leftString = axisName.toString();
			if (power < 0.0)
				leftString = "-(" + leftString;
			String rightString = (power < 0.0) ? ")" : "";
			int leftWidth = fm.stringWidth(leftString);
//			int rightWidth = fm.stringWidth(rightString);
			
			g.drawString(leftString, horiz, baseline);
			horiz += leftWidth;
			
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			NumValue powerString = getPower();
			powerString.drawRight(g, horiz, baseline - kSuperBaseline);
			horiz += powerString.stringWidth(g);
			g.setFont(oldFont);
			
			g.drawString(rightString, horiz, baseline);
		}
		
		return rightPos;
	}
	
	protected void setValHeight() {
		Graphics g = getGraphics();
		g.setFont(superscriptFont);
		int superAscent = kSuperBaseline + g.getFontMetrics().getAscent();
		
		valHeight = Math.max(ascent, superAscent) + descent + 2 * kValueBorder;
	}

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		if (canDragTransform)
			requestFocus();
		super.mousePressed(e);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (canDragTransform && (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN))
			setPowerIndex(getPowerIndex() + 1);
		else if (canDragTransform && (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP))
			setPowerIndex(getPowerIndex() - 1);
	}
	
}