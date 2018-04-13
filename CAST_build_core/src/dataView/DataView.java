package dataView;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import utils.*;
import axis.*;

@SuppressWarnings("deprecation")
abstract public class DataView extends BufferedCanvas implements Runnable, Observer, ComponentListener {
	
	static final public Color mixColors(Color c1, Color c2, double p1) {
		if (p1 > 1.0)
			p1 = 1.0;
		else if (p1 < 0.0)
			p1 = 0.0;
			
		int m1 = (int)Math.round(p1 * 256);
		int m2 = 256 - m1;
		return new Color((c1.getRed() * m1 + c2.getRed() * m2) / 256,
								(c1.getGreen() * m1 + c2.getGreen() * m2) / 256,
								(c1.getBlue() * m1 + c2.getBlue() * m2) / 256);
 	}
	
	static final public Color mixColors(Color c1, Color c2) {
		return mixColors(c1, c2, 0.5);
	}
	
	static final public Color darkenColor(Color c, double p) {
													//	p=1 is black, p=0 is original c
		return mixColors(Color.black, c, p);
 	}
	
	static final public Color dimColor(Color c, double p) {
													//	p=1 is white, p=0 is original c
		return mixColors(Color.white, c, p);
 	}
	
	static final public Color desatColor(Color c, double p) {
		int greyLevel = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
		Color grey = new Color(greyLevel, greyLevel, greyLevel);
		return mixColors(grey, c, p);
 	}
	
	static final public Color getShadedColor(Color c) {
		return mixColors(c, Color.lightGray, 0.5);
 	}
	
	static final private double kControlPropn = 1.0 / 4;
	
//--------------------------------------------------------------------------

	private Insets border;
	
	public DataView(DataSet data, XApplet applet, Insets border) {
		super(applet);
		
		this.border = border;
		
		currentFrame = 0;
		
		this.data = data;
		data.addObserver(this);
	}
	
	public void setViewBorder(Insets border) {
		this.border = border;
	}
	
	public Insets getViewBorder() {
		return border;
	}

//-----------------------------------------------------------------------------------
	
	private DataSet data = null;
	private String activeNumVariableKey = null;
	private String activeCatVariableKey = null;
	protected String activeDistnVariableKey = null;
	
	public void setActiveNumVariable(String key) {
		activeNumVariableKey = key;
	}
	
	public void setActiveCatVariable(String key) {
		activeCatVariableKey = key;
	}
	
	public void setActiveDistnVariable(String key) {
		activeDistnVariableKey = key;
	}
	
	public String getActiveNumKey() {
		return (activeNumVariableKey == null) ? data.getDefaultNumVariableKey()
															: activeNumVariableKey;
	}
	
	public String getActiveCatKey() {
		return (activeCatVariableKey == null) ? data.getDefaultCatVariableKey()
															: activeCatVariableKey;
	}
	
	protected Flags getSelection() {
		return data.getSelection();
	}
	
	protected CoreVariable getVariable(String key) {
		return data.getVariable(key);
	}
	
	protected NumVariable getNumVariable() {
		if (activeNumVariableKey != null)
			return (NumVariable)data.getVariable(activeNumVariableKey);
		else
			return data.getNumVariable();
	}
	
	protected CatVariable getCatVariable() {
		if (activeCatVariableKey != null)
			return (CatVariable)data.getVariable(activeCatVariableKey);
		else
			return data.getCatVariable();
	}
	
	protected DistnVariable getDistnVariable() {
		if (activeDistnVariableKey != null)
			return (DistnVariable)data.getVariable(activeDistnVariableKey);
		else
			return data.getDistnVariable();
	}
	
	protected LabelVariable getLabelVariable() {
		return data.getLabelVariable();
	}
	
	protected DataSet getData() {
		return data;
	}

//-----------------------------------------------------------------------------------
	
	public void corePaint(Graphics g) {
		synchronized (data) {
			paintView(g);
		}
	}
	
	abstract public void paintView(Graphics g);
	
	public void reinitialiseAfterTransform() {
	}

//-----------------------------------------------------------------------------------
	
	public final static int DOT_CROSS = 0;
	public final static int SMALL_CROSS = 1;
	public final static int MEDIUM_CROSS = 2;
	public final static int LARGE_CROSS = 3;
	public final static int HUGE_CROSS = 4;

	private int crossSize = MEDIUM_CROSS;
	
	public void setCrossSize(int crossSize) {
		if (crossSize >= DOT_CROSS && crossSize <= HUGE_CROSS)
			this.crossSize = crossSize;
	}
	
	protected int getCrossSize() {
		return crossSize;
	}
	
	protected int getCrossPix() {
		if (getApplet().usesBigFonts() && crossSize >= MEDIUM_CROSS)
			return crossSize + 1;
		else
			return crossSize;
	}
	
	protected void drawCross(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
		g.drawLine(thePos.x - crossPix, thePos.y - crossPix,
										thePos.x + crossPix, thePos.y + crossPix);
		g.drawLine(thePos.x - crossPix, thePos.y + crossPix,
										thePos.x + crossPix, thePos.y - crossPix);
	}
	
	protected void drawBoldCross(Graphics g, Point thePos) {
		drawCross(g, thePos);
		thePos.x --;
		drawCross(g, thePos);
		thePos.x += 2;
		drawCross(g, thePos);
		thePos.x --;
		thePos.y --;
		drawCross(g, thePos);
		thePos.y += 2;
		drawCross(g, thePos);
		thePos.y --;
	}
	
	protected void drawPlus(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
		g.drawLine(thePos.x - crossPix, thePos.y,
										thePos.x + crossPix, thePos.y);
		g.drawLine(thePos.x, thePos.y - crossPix,
										thePos.x, thePos.y + crossPix);
	}
	
	protected void drawSquare(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
		g.drawRect(thePos.x - crossPix, thePos.y - crossPix,
										2 * crossPix, 2 * crossPix);
	}
	
	protected void drawCircle(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
//		g.drawOval(thePos.x - crossPix, thePos.y - crossPix,
//										2 * crossPix + 1, 2 * crossPix + 1);
		g.drawOval(thePos.x - crossPix, thePos.y - crossPix,
										2 * crossPix, 2 * crossPix);
	}
	
	protected void drawBlob(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
		g.fillOval(thePos.x - crossPix, thePos.y - crossPix,
										2 * crossPix + 1, 2 * crossPix + 1);
	}
	
	protected void drawDiamond(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
		g.drawLine(thePos.x, thePos.y - crossPix,
										thePos.x + crossPix, thePos.y);
		g.drawLine(thePos.x + crossPix, thePos.y,
										thePos.x, thePos.y + crossPix);
		g.drawLine(thePos.x, thePos.y + crossPix,
										thePos.x - crossPix, thePos.y);
		g.drawLine(thePos.x - crossPix, thePos.y,
										thePos.x, thePos.y - crossPix);
	}
	
	private static final Color shapeColor[] = {null, Color.red, Color.blue, Color.green};
	
	protected Color getCrossColor(int markIndex) {
		int colourIndex = markIndex % shapeColor.length;
		if (colourIndex != 0)
			return shapeColor[colourIndex];
		else
			return getForeground();
	}
	
	public void drawMark(Graphics g, Point thePos, int markIndex) {
		int shapeIndex = markIndex % 5;
		int colourIndex = markIndex % shapeColor.length;
		Color oldColor = null;
		if (colourIndex != 0) {
			oldColor = g.getColor();
			g.setColor(shapeColor[colourIndex]);
		}
		switch (shapeIndex) {
			case 0:
				drawCross(g, thePos);
				break;
			case 1:
				drawCircle(g, thePos);
				break;
			case 2:
				drawPlus(g, thePos);
				break;
			case 3:
				drawSquare(g, thePos);
				break;
			case 4:
				drawDiamond(g, thePos);
				break;
		}
		if (colourIndex != 0)
			g.setColor(oldColor);
	}
	
	protected void drawCrossBackground(Graphics g, Point thePos) {
		int crossPix = getCrossPix();
//		if (crossSize >= DOT_CROSS && crossSize <= LARGE_CROSS) {
			g.fillRect(thePos.x - crossPix - 1, thePos.y - crossPix - 1,
											crossPix * 2 + 3, crossPix * 2 + 3);
//		}
	}
	
	//-----------------------------------------------------------------------------------
	
	private boolean trimCurve = true;
	
	public void setTrimCurve(boolean trimCurve) {
		this.trimCurve = trimCurve;
	}
	
	protected void drawCurve(Graphics g, double[] x, double[] y, NumCatAxis xAxis, NumCatAxis yAxis,
													 boolean extendToOrigin) {
		// if xyAxis is null, xy[] are assumed to be proportions
		Graphics2D g2d = (Graphics2D)g;
		GeneralPath gp = new GeneralPath();
		
		Point2D.Double startPt = null;
		int startIndex = -1;
		while (startPt == null) {
			startIndex ++;
			startPt = convertToScreen(x[startIndex], y[startIndex], xAxis, yAxis, null);
			//	ignore when y-coord is infinite (possible for log-likelihood)
		}
		if (extendToOrigin) {
			double slop = (xAxis.maxOnAxis - xAxis.minOnAxis) * 0.01;
			Point2D.Double originPt = convertToScreen(xAxis.minOnAxis - slop, 0.0, xAxis, yAxis, null);
			gp.moveTo(originPt.getX(), originPt.getY());
			gp.lineTo(startPt.getX(), startPt.getY());
		}
		else
			gp.moveTo(startPt.getX(), startPt.getY());
		
		addSmoothPointsToPath(x, y, xAxis, yAxis, gp, startIndex, startPt);
		
		g2d.draw(gp);
	}
	
	protected void drawCurve(Graphics g, double[] x, double[] y, NumCatAxis xAxis, NumCatAxis yAxis) {
		drawCurve(g, x, y, xAxis, yAxis, false);
	}
	
	protected void fillCurve(Graphics g, double[] x, double[] y, NumCatAxis xAxis, NumCatAxis yAxis) {
		Graphics2D g2d = (Graphics2D)g;
		GeneralPath gp = new GeneralPath();
		
		Point2D.Double originPt = convertToScreen(x[0], 0.0, xAxis, yAxis, null);
		gp.moveTo(originPt.getX(), originPt.getY());
		Point2D.Double startPt = convertToScreen(x[0], y[0], xAxis, yAxis, null);
		gp.lineTo(startPt.getX(), startPt.getY());
		
		addSmoothPointsToPath(x, y, xAxis, yAxis, gp, 0, startPt);
		
		Point2D.Double lastPt = convertToScreen(x[x.length - 1], 0.0, xAxis, yAxis, null);
		gp.lineTo(lastPt.getX(), lastPt.getY());
		gp.lineTo(originPt.getX(), originPt.getY());
		
		g2d.fill(gp);
	}
	
	private void addSmoothPointsToPath(double[] x, double[] y, NumCatAxis xAxis, NumCatAxis yAxis,
																				 GeneralPath gp, int startIndex, Point2D.Double startPt) {
		Point2D.Double prevPt = null;
		Point2D.Double thisPt = startPt;
		int i = startIndex + 1;
		Point2D.Double nextPt = convertToScreen(x[i], y[i], xAxis, yAxis, null);
		double thisDx = (nextPt.getX() - thisPt.getX());
		double thisDy = (nextPt.getY() - thisPt.getY());
		
		if (trimCurve)
			while (Math.abs(thisPt.getY() - nextPt.getY()) < 1.0 && i < x.length - 1) {
				i ++;
				nextPt = convertToScreen(x[i], y[i], xAxis, yAxis, nextPt);
			}
		
//		double thisGradient = thisDy / thisDx;
		
		do {									//	i is index of endpoint of segment on entry
			prevPt = thisPt;
			thisPt = nextPt;
			double prevDx = thisDx;
			double prevDy = thisDy;
//			double prevGradient = thisGradient;
			
			if (i == x.length - 1) {
				thisDx = (thisPt.getX() - prevPt.getX());
				thisDy = (thisPt.getY() - prevPt.getY());
				i++;
			}
			else {
				i++;
				nextPt = convertToScreen(x[i], y[i], xAxis, yAxis, null);
//				System.out.println("nextPt: " + x[i] + ", " + y[i] + ", " + nextPt.getX() + ", " + nextPt.getY());
				if (nextPt == null)
					break;
				thisDx = (nextPt.getX() - prevPt.getX());		//	should these be nextPt - thisPt ?
				thisDy = (nextPt.getY() - prevPt.getY());
				
				if (trimCurve)
					while ((i < x.length - 1) && Math.abs(nextPt.getY() - thisPt.getY()) < 1.0) {
						i ++;
						nextPt = convertToScreen(x[i], y[i], xAxis, yAxis, null);
					};
			}
			
			//				gp.lineTo(thisPt.getX(), thisPt.getY());
			double xStep = thisPt.getX() - prevPt.getX();
			double c1_x = prevPt.getX() + xStep * kControlPropn;
			double c1_y = prevPt.getY() + xStep * prevDy / prevDx * kControlPropn;
			double c2_x = thisPt.getX() - xStep * kControlPropn;
			double c2_y = thisPt.getY() - xStep * thisDy / thisDx * kControlPropn;
			//				g.setColor(Color.red);
			//				drawBlob(g, new Point((int)Math.round(c1_x), (int)Math.round(c1_y)));
			//				g.setColor(Color.blue);
			//				drawBlob(g, new Point((int)Math.round(c2_x), (int)Math.round(c2_y)));
			//				System.out.println((prevPt.getX() + prevDx / 3) + ", " + (thisPt.getX() - thisDx / 3) + ", " + thisPt.getX());
			//				g.setColor(Color.black);
			//				drawBlob(g, new Point((int)Math.round(thisPt.getX()), (int)Math.round(thisPt.getY())));
			gp.curveTo(c1_x, c1_y, c2_x, c2_y, thisPt.getX(), thisPt.getY());
		} while (i < x.length);
	}
	
	protected Point2D.Double convertToScreen(double x, double y, NumCatAxis xAxis, NumCatAxis yAxis,
																										Point2D.Double thePoint) {							//	can be off-axis
		double xPos = (xAxis == null) ? (x * getSize().width) : xAxis.numValToRawDoublePos(x);
		double yPos = (yAxis == null) ? (y * getSize().height) : yAxis.numValToRawDoublePos(y);
		if (Double.isInfinite(xPos) || Double.isInfinite(yPos))
			return null;
		else
			return translateToScreenD2(xPos, yPos, thePoint);
	}
	
	public Point2D.Double translateToScreenD2(double horiz, double vert, Point2D.Double thePoint) {
		vert = getSize().height - vert;
		if (border != null) {
			horiz += border.left;
			vert -= border.bottom;
		}
		if (thePoint == null)
			thePoint = new Point2D.Double();
		
		thePoint.setLocation(horiz, vert);
		return thePoint;
	}

//-----------------------------------------------------------------------------------
	
	public Point translateToScreen(int horiz, int vert, Point thePoint) {
		vert = getSize().height - 1 - vert;
		if (border != null) {
			horiz += border.left;
			vert -= border.bottom;
		}
		if (thePoint == null)
			thePoint = new Point(horiz, vert);
		else {
			thePoint.x = horiz;
			thePoint.y = vert;
		}
		return thePoint;
	}
	
	protected Point translateFromScreen(int x, int y, Point thePoint) {
		y = getSize().height - 1 - y;
		if (border != null) {
			x -= border.left;
			y -= border.bottom;
		}
		if (thePoint == null)
			thePoint = new Point(x, y);
		else {
			thePoint.x = x;
			thePoint.y = y;
		}
		return thePoint;
	}

//-----------------------------------------------------------------------------------
	
	private int endFrame, framesPerSec, currentFrame;
	private Thread runner = null;
	private XSlider animationController = null;
	
	public int getCurrentFrame() {
		return currentFrame;
	}
	
	public void animateFrames(int theStartFrame, int noOfFrames, int theFramesPerSec,
																					XSlider animationController) {
		pause();
		currentFrame = theStartFrame;
		getApplet().frameChanged(this);
		endFrame = theStartFrame + noOfFrames;
		framesPerSec = theFramesPerSec;
		this.animationController = animationController;
		restart();
	}
	
	protected void drawNextFrame() {		//		control chart selects final value instead
		repaint();
	}
	
	public void setInitialFrame(int frameToShow) {		//	for use before being added to XApplet
																										//	since getApplet() would throw runtime error
		currentFrame = frameToShow;
		endFrame = currentFrame;
	}
	
	public void setFrame(int frameToShow) {
		pause();
		currentFrame = frameToShow;
		endFrame = currentFrame;
		if (getApplet() != null)
			getApplet().frameChanged(this);
		drawNextFrame();
	}
	
	public void setFrame(int frameToShow, XSlider animationController) {
		pause();
		currentFrame = frameToShow;
		endFrame = currentFrame;
		getApplet().frameChanged(this);
		updateSliderValue(animationController);
		drawNextFrame();
	}
	
	private void updateSliderValue(XSlider controller) {
		if (controller != null) {
			if (currentFrame >= 0 && currentFrame <= controller.getMaxValue()
																	&& currentFrame != controller.getValue()) {
				controller.setPostEvents(false);
				controller.setValue(currentFrame);
												//		don't let XSlider post event which would cause
												//		setFrame() to be called again
				controller.setPostEvents(true);
			}
		}
	}
	
	public void reset() {
		setFrame(0);
	}
	
	public void restart() {
		if (runner == null && currentFrame != endFrame) {
			runner = new Thread(this);
			runner.start();
		}
	}
	
	public void pause() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
	}
	
	public void run() {
		try {
			for ( ; currentFrame != endFrame
						; currentFrame = ((currentFrame<endFrame) ? (currentFrame+1) : (currentFrame-1))) {
					getApplet().frameChanged(this);
					drawNextFrame();
					updateSliderValue(animationController);
					Thread.sleep(1000 / framesPerSec);
				}
			getApplet().frameChanged(this);
			updateSliderValue(animationController);
			drawNextFrame();
		} catch (InterruptedException e) {
			System.out.println("Animation interrupted: " + e);
		}
		animationController = null;
		runner = null;
	}

//-----------------------------------------------------------------------------------
	
	public void update(Observable o, Object arg) {		//	method to receive messages from DataSet
		DataChangeMessage theMessage = (DataChangeMessage) arg;
		Graphics g = getGraphics();
		checkAliasing(g);
		
		synchronized(data) {
			switch (theMessage.messageType) {
				case DataChangeMessage.CHANGE_SELECTION:
					doChangeSelection(g);
					break;
				case DataChangeMessage.CHANGE_VALUE:
					doChangeValue(g, theMessage.index);
					break;
				case DataChangeMessage.CHANGE_VARIABLE:
					doChangeVariable(g, (String)theMessage.object);
					break;
				case DataChangeMessage.TRANSFORMED_AXIS:
					doTransformView(g, (NumCatAxis)theMessage.object);
					break;
				case DataChangeMessage.ADDED_VALUES:
					doAddValues(g, theMessage.index);
					break;
			}
		}
	}
	
	protected void doChangeSelection(Graphics g) {
		repaint();
	}
	
	final protected void doInvertItem(Graphics g, int index) {	//****	No longer used
	}
	
	protected void doChangeValue(Graphics g, int index) {
		repaint();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		repaint();							//		override this if instant redraw is required
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
										//		override this if view depends on this axis
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
		repaint();							//		override this if instant redraw is required
	}

//-----------------------------------------------------------------------------------
	
	protected boolean retainLastSelection = false;
	
	public void setRetainLastSelection(boolean retainLastSelection) {
		this.retainLastSelection = retainLastSelection;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else if (startInfo instanceof IndexPosInfo) {
			int hitIndex = ((IndexPosInfo)startInfo).itemIndex;
			getData().setSelection(hitIndex);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (endPos != null && !retainLastSelection)
			getData().clearSelection();
	}

	public void mousePressed(MouseEvent e) {
		if (data != null) {
			pause();
			super.mousePressed(e);
		}
	}

//-----------------------------------------------------------------------------------
		
	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		return new Dimension(20, 20);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
}