package graphics3D;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import images.*;


public class Rotate3DView extends DataView {
	
	static final protected boolean BACK_AXIS = true;
	static final protected boolean FRONT_AXIS = false;
	
	static final protected int IGNORE_OPAQUE = 0;	//		always draw in bold colours
	static final protected int USE_OPAQUE = 1;		//		draw dimmed if items might be obscured

	static final private int SMALL_ROTATE_RADIUS = 50;
	static final private int BIG_ROTATE_RADIUS = 80;
	
	static final private int kDragSlop = 350;
	static final private int kMaxWait = 30000;		//		30 seconds for hand images
	
	static private boolean hasCursors = findCursorClass();
	static boolean findCursorClass() {
		try {
			@SuppressWarnings("unused")
			Class cursorClass = Class.forName("java.awt.Cursor");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
	
//	static public boolean canClip = false;				//	for testing
	static public boolean canClip = findClipMethod();
	static boolean findClipMethod() {
		try {
			Class graphicsClass = Class.forName("java.awt.Graphics");
			@SuppressWarnings("unused")
			java.lang.reflect.Method m = graphicsClass.getMethod("getClip", new Class[0]);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	static private Cursor openHandCursor = null, closedFistCursor = null;
	
	protected D3Axis xAxis, yAxis, zAxis;
	protected String xKey, yKey, zKey;
	private RotateMap coreMap;
	protected RotateMap map;
	
	private int hitRadius = SMALL_ROTATE_RADIUS;
	
	protected boolean initialised = false;
	
	private boolean doingRotate = false;
	private int currentCursor = hasCursors ? Cursor.DEFAULT_CURSOR : 0;
	
	protected boolean drawData = true;
	
	public Rotate3DView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
																													String xKey, String yKey, String zKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
		this.xKey = xKey;
		this.yKey = yKey;
		this.zKey = zKey;
		coreMap = new RotateMap(30, 30);
		loadCursors(applet);
		addComponentListener(this);
	}
	
	private void loadCursors(XApplet applet) {
		if (openHandCursor == null || closedFistCursor == null) {
//			String suffix = ".gif";
//			int centerOffset = 8;
			boolean supports32BitCursor = Toolkit.getDefaultToolkit().getBestCursorSize(32, 32).getWidth() == 32;
			String suffix = supports32BitCursor ? "_32.png" : ".png";
			int centerOffset = supports32BitCursor ? 16 : 8;
			
			Image openHandImage = CoreImageReader.getImage("hand_open" + suffix);
			Image closedFistImage = CoreImageReader.getImage("hand_closed" + suffix);
			
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(openHandImage, 0);
			tracker.addImage(closedFistImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
			
//			System.out.println("openHandImage = " + openHandImage + ", offset = " + centerOffset);
			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			if (openHandImage != null)
				openHandCursor = toolkit.createCustomCursor(openHandImage , new Point(centerOffset, centerOffset), "open hand");
			else
				openHandCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			
			if (openHandImage != closedFistImage)
				closedFistCursor = toolkit.createCustomCursor(closedFistImage , new Point(centerOffset, centerOffset), "closed fist");
			else
				closedFistCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		}
	}
	
	public void setBigHitRadius() {
		hitRadius = BIG_ROTATE_RADIUS;
	}
	
	public void setInitialRotation(double roundDens, double ofDens) {
		coreMap.setAngles(roundDens, ofDens);
	}
	
	public void changeVariables(String xKey, String yKey, String zKey) {
		if (xKey != null && !xKey.equals(this.xKey))
			this.xKey = xKey;
		if (yKey != null && !yKey.equals(this.yKey))
			this.yKey = yKey;
		if (zKey != null && !zKey.equals(this.zKey))
			this.zKey = zKey;
		repaint();
	}
	
	public void rotateTo(double roundDens, double ofDens) {
		coreMap.setAngles(roundDens, ofDens);
		repaint();
	}
	
	public void setDrawData(boolean drawData) {
		this.drawData = drawData;
	}
	
	public RotateMap getCurrentMap() {
		return map;
	}
	
	protected Point getScreenPoint(double x, double y, double z, Point thePoint) {
		if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z))
			return null;
		
		double xFract = xAxis.numValToPosition(x);
		double yFract = yAxis.numValToPosition(y);
		double zFract = zAxis.numValToPosition(z);
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	private Point getCentre() {
		return translateToScreen(coreMap.mapH3DGraph(0.5, 0.5, 0.5), coreMap.mapV3DGraph(0.5, 0.5, 0.5), null);
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			coreMap.setDimensions(getSize(), 0, 0);			//		********* change *********
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected void drawDragCircle(Graphics g) {
		if (!doingRotate && !hasCursors) {
			g.setColor(Color.pink);
			Point centre = getCentre();
			for (int i=0 ; i<3 ; i++)
				g.drawOval(centre.x - hitRadius + i, centre.y - hitRadius + i,
															2 * (hitRadius - i), 2 * (hitRadius - i));
			g.setColor(getForeground());
		}
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawData) {
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			NumVariable zVariable = (NumVariable)getVariable(zKey);
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			ValueEnumeration ze = zVariable.values();
			Point crossPos = null;
			
			FlagEnumeration fe = getSelection().getEnumeration();
			g.setColor(Color.red);
			while (xe.hasMoreValues() && ye.hasMoreValues() && ze.hasMoreValues()) {
				boolean selected = fe.nextFlag();
				double x = xe.nextDouble();
				double y = ye.nextDouble();
				double z = ze.nextDouble();
				if (selected) {
					crossPos = getScreenPoint(x, y, z, crossPos);
					if (crossPos != null)
						drawCrossBackground(g, crossPos);
				}
			}
			
			g.setColor(getForeground());
			xe = xVariable.values();
			ye = yVariable.values();
			ze = zVariable.values();
			while (xe.hasMoreValues() && ye.hasMoreValues() && ze.hasMoreValues()) {
				crossPos = getScreenPoint(xe.nextDouble(), ye.nextDouble(), ze.nextDouble(), crossPos);
				if (crossPos != null)
					drawCross(g, crossPos);
			}
		}
	}
	
	protected void drawAxes(Graphics g, boolean backNotFront, int colourType) {
		if (map.yAxisBehind() == backNotFront)
			yAxis.draw(g, map, this, colourType, true);
		if (map.xAxisBehind() == backNotFront)
			xAxis.draw(g, map, this, colourType, true);
		if (map.zAxisBehind() == backNotFront)
			zAxis.draw(g, map, this, colourType, true);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		return null;
	}
	
	protected void drawForeground(Graphics g) {
	}
	
	protected void drawContents(Graphics g) {
		Polygon shadePolygon = drawShadeRegion(g);
		
		if (canClip && (shadePolygon !=  null))
			try {
				drawData(g, IGNORE_OPAQUE);
				Shape oldClip = g.getClip();
//				if (g instanceof Graphics2D)					//		Not in current CodeWarrior
//					((Graphics2D)g).clip(shadePolygon);
//				else
					g.setClip(shadePolygon);
				drawAxes(g, BACK_AXIS, D3Axis.SHADED);
				drawData(g, USE_OPAQUE);
				g.setClip(oldClip);
			}
			catch (Exception e) {	//		g.setClip() is not always implemented for Polygon class
				canClip = false;
				drawData(g, USE_OPAQUE);
			}
		else
			drawData(g, USE_OPAQUE);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		map = new RotateMap(coreMap);
		
		drawDragCircle(g);
		
		drawAxes(g, BACK_AXIS, D3Axis.BACKGROUND);
		drawContents(g);
		drawAxes(g, FRONT_AXIS, D3Axis.FOREGROUND);
		
		drawForeground(g);
	}

//-----------------------------------------------------------------------------------
	
	protected void setArrowCursor() {
		if (hasCursors) {
			currentCursor = Cursor.DEFAULT_CURSOR;
			setCursor(Cursor.getPredefinedCursor(currentCursor));
		}
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		return new DragPosInfo(x, y);
	}
	
	private boolean inDragCircle(int x, int y) {
		if (!initialised) {
			coreMap.setDimensions(getSize(), 0, 0);			//		********* change *********
			initialised = true;
		}
		Point centre = getCentre();
		return (centre.x - x) * (centre.x - x)
							+ (centre.y - y) * (centre.y - y) <= hitRadius * hitRadius;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (inDragCircle(x, y))
			return getPosition(x, y);
		else
			return null;
	}
	
	private DragPosInfo startDrag;
	
	protected boolean startDrag(PositionInfo startInfo) {
		stopAutoRotation();
		startDrag = (DragPosInfo)startInfo;
		coreMap.startDrag();
		doingRotate = true;
		if (hasCursors)
			setCurrentCursor(Cursor.MOVE_CURSOR);
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingRotate = false;
			repaint();
		}
		else {
			doingRotate = true;
			DragPosInfo toDrag = (DragPosInfo)toPos;
			coreMap.dragRotate(toDrag.x - startDrag.x, toDrag.y - startDrag.y);
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingRotate = false;
		if (hasCursors && endPos != null) {
			int newCursor = inDragCircle(((DragPosInfo)endPos).x, ((DragPosInfo)endPos).y)
															? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR;
			setCurrentCursor(newCursor);
		}
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		if (!doingDrag && hasCursors) {
			int x = e.getX();
			int y = e.getY();
			int newCursor = inDragCircle(x, y) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR;
			setCurrentCursor(newCursor);
		}
	}
	
	private void setCurrentCursor(int cursorType) {
		if (currentCursor != cursorType) {
//			System.out.println("Changing cursor to " + cursorType);
			currentCursor = cursorType;
			Cursor c = (cursorType == Cursor.HAND_CURSOR) ? openHandCursor
											: (cursorType == Cursor.MOVE_CURSOR) ? closedFistCursor
											: Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
			setCursor(c);
		}
	}

//-----------------------------------------------------------------------------------
	
	private RotateThread rotateThread = null;
	
	public void rotateOneDegree() {
		rotateTo(coreMap.getTheta1() + 1.0, coreMap.getTheta2());
	}
	
	public void animateRotateTo(double roundDens, double ofDens) {
		stopAutoRotation();
		rotateThread = new RotateThread(this, coreMap.getTheta1(), coreMap.getTheta2(), roundDens, ofDens);
		rotateThread.start();
	}
	
	public void customRotate(RotateThread customRotateThread) {
		stopAutoRotation();
		customRotateThread.setInitialRotation(coreMap.getTheta1(), coreMap.getTheta2());
		rotateThread = customRotateThread;
		rotateThread.start();
	}
	
	public void startAutoRotation() {
		stopAutoRotation();
		rotateThread = new RotateThread(this);
		rotateThread.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stopAutoRotation() {
		if (rotateThread != null) {
			rotateThread.stop();
			rotateThread = null;
		}
	}
	
	public void clearRotateThread() {
		rotateThread = null;
	}

//-----------------------------------------------------------------------------------

	public void componentResized(ComponentEvent e) {
		initialised = false;
	}
}
	
