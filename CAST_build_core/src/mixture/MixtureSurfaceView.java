package mixture;

import java.awt.*;

import dataView.*;
import coreGraphics.*;
import graphics3D.*;


public class MixtureSurfaceView extends Rotate3DView {
	
	static final public int SURFACE = 0;
	static final public int GRID = 1;
	static final public int CONTOURS = 2;
	
	static final private String k100Percent = "100% ";
//	static final private int kHorizLabelOffset = 6;
//	static final private int kVertLabelOffset = 4;
	
	static final private double kInvRootThree = 1.0 / Math.sqrt(3.0);
	
	static final private Color kBaseColor = new Color(0xDDDDDD);
	static final private Color kBaseFrameColor = new Color(0x999999);
	
	static final private int kLabelVertGap = 5;
	static final private int kLabelHorizGap = 5;
	
	private String modelKey, yKey;
	private String[] explanKey;
	
	static final private int kAxisSteps = 20;
//	static final private int kAxisSteps = 3;
//	static final private double kEpsilon = 0.01;	// offset for working out whether above surface
	
//	static final private Color kDarkGray = new Color(0x333333);
	
	private int drawType = SURFACE;
	
	private double[] fixedContours = null;
	
	private int[][] horizGridCoord;
	private int[][] vertGridCoord;
	
	private MixtureGridDrawer surfaceDrawer;
	private ContourControlView contourControl;
	
	public MixtureSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.modelKey = modelKey;
		this.yKey = yKey;
		this.explanKey = explanKey;
		
		surfaceDrawer = new MixtureGridDrawer(kAxisSteps);
	
		horizGridCoord = new int[kAxisSteps + 1][];
		vertGridCoord = new int[kAxisSteps + 1][];
		for (int i=0 ; i<=kAxisSteps ; i++) {
			horizGridCoord[i] = new int[kAxisSteps + 1 - i];
			vertGridCoord[i] = new int[kAxisSteps + 1 - i];
		}
	}
	
	public void setDrawType(int drawType) {
		this.drawType = drawType;
	}
	
	public void setColourMap(ColourMap colourMap) {
		surfaceDrawer.setColourMap(colourMap);
	}
	
	public void setContourControl(ContourControlView contourControl) {
		this.contourControl = contourControl;
	}
	
	public void setFixedContours(double[] fixedContours) {
		this.fixedContours = fixedContours;
	}
	
	private double getContourValue() {
		if (contourControl == null)
			return Double.NaN;
		else
			return contourControl.getContourValue();
	}
	
	public MixtureGridDrawer getSurfaceDrawer() {
		return surfaceDrawer;
	}
	
	protected MixtureModel getModel() {
		return (modelKey == null) ? null : (MixtureModel)getVariable(modelKey);
	}
	
	public boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			MixtureModel model = (MixtureModel)getModel();
			if (model == null)
				initialised = false;
			else
				surfaceDrawer.findFittedValues(model);
		
			return true;
		}
		return false;
	}
	
	private Point getScreenPoint(double r, double s, double t, double y, Point p) {
		double x = r;
		double z = kInvRootThree * (r + 2 * s);
		return getScreenPoint(x, y, z, p);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		MixtureModel model = (MixtureModel)getModel();
		if (model == null)
			return null;
		
		initialise(g);
		
		Point p = null;
		
		for (int i=0 ; i<=kAxisSteps ; i++) {
			double r = i / (double)kAxisSteps;
			for (int j=0 ; j<=kAxisSteps-i ; j++) {
				double s = j / (double)kAxisSteps;
				double t = 1.0 - r - s;
				p = getScreenPoint(r, s, t, surfaceDrawer.fitValue[i][j], p);
				horizGridCoord[i][j] = p.x;
				vertGridCoord[i][j] = p.y;
			}
		}
		surfaceDrawer.setGridCoords(horizGridCoord, vertGridCoord);
		
//		if (drawType == GRID) {
//			surfaceDrawer.frameSurface(g);
//			surfaceDrawer.drawContour(g, model, getContourValue());
//		}
//		else
		if (drawType == SURFACE) {
			double theta1 = map.getTheta1();
			int frontIndex = (theta1 < 90) || (theta1 >= 330) ? 1
												: theta1 < 210 ? 2
												: 0;
			int backIndex = (theta1 < 30) || (theta1 >= 270) ? 2
												: theta1 < 150 ? 0
												: 1;
			
			surfaceDrawer.shadeSurface(g, model, getContourValue(), frontIndex, backIndex);
		}
		else {		//	drawType == CONTOURS
			g.setColor(Color.lightGray);
			surfaceDrawer.outlineSurface(g);
			
			if (fixedContours != null) {
				for (int i=0 ; i<fixedContours.length ; i++) {
					g.setColor(surfaceDrawer.getColourMap().getColour(fixedContours[i]));
					surfaceDrawer.drawContour(g, model, fixedContours[i]);
				}
			}
			double dragContour = getContourValue();
			if (!Double.isNaN(dragContour)) {
					g.setColor(Color.black);
					surfaceDrawer.drawContour(g, model, dragContour);
			}
		}
		return null;
	}

	
	protected Point getModelPoint(double[] rstVals, MixtureModel model) {
		double fit = model.evaluateMean(rstVals);
		return getScreenPoint(rstVals[0], rstVals[1], rstVals[2], fit, null);
	}
	
	protected NumVariable getYDataVar() {
		return (NumVariable)getVariable(yKey);
	}
	
	protected NumVariable getRDataVar() {
		return (NumVariable)getVariable(explanKey[0]);
	}
	
	protected NumVariable getSDataVar() {
		return (NumVariable)getVariable(explanKey[1]);
	}
	
	protected NumVariable getTDataVar() {
		return (NumVariable)getVariable(explanKey[2]);
	}
	
	private void drawWithinScreen(Graphics g, String vertexName, int nameLength, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		x = Math.max(0, Math.min(getSize().width - nameLength, x));
		y = Math.max(fm.getAscent(), Math.min(getSize().height - fm.getDescent(), y));
		g.drawString(vertexName, x, y);
	}
	
	private void draw100PercentLabel(Graphics g, Point v, String vertexName,
																										Point v1, Point v2) {
//		boolean topV = v.y < v1.y && v.y < v2.y;
		boolean bottomV = v.y > v1.y && v.y > v2.y;
		boolean leftV = v.x < v1.x && v.x < v2.x;
		boolean rightV = v.x > v1.x && v.x > v2.x;
		
		FontMetrics fm = g.getFontMetrics();
		vertexName = k100Percent + vertexName;
		int width = fm.stringWidth(vertexName);
		if (bottomV)
			drawWithinScreen(g, vertexName, width, v.x - width / 2,
																					v.y + fm.getAscent() + kLabelVertGap);
		else if (leftV)
			drawWithinScreen(g, vertexName, width, v.x - width - kLabelHorizGap,
																					v.y + fm.getAscent() / 2);
		else if (rightV)
			drawWithinScreen(g, vertexName, width, v.x + kLabelHorizGap,
																					v.y + fm.getAscent() / 2);
		else		// topV or other
			drawWithinScreen(g, vertexName, width, v.x - width / 2,
																					v.y - fm.getDescent() - kLabelVertGap);
	}
	
	protected void drawAxes(Graphics g, boolean backNotFront, int colourType) {
		if (map.getTheta2() > 180 || backNotFront && colourType == D3Axis.BACKGROUND) {
			g.setColor(kBaseColor);
			int xCoord[] = new int[4];
			int yCoord[] = new int[4];
			Point pR = getScreenPoint(1.0, 0.0, 0.0, yAxis.getMinOnAxis(), null);
			xCoord[0] = xCoord[3] = pR.x;
			yCoord[0] = yCoord[3] = pR.y;
			Point pS = getScreenPoint(0.0, 1.0, 0.0, yAxis.getMinOnAxis(), null);
			xCoord[1] = pS.x;
			yCoord[1] = pS.y;
			Point pT = getScreenPoint(0.0, 0.0, 1.0, yAxis.getMinOnAxis(), null);
			xCoord[2] = pT.x;
			yCoord[2] = pT.y;
			g.fillPolygon(xCoord, yCoord, 4);
			g.setColor(kBaseFrameColor);
			g.drawPolygon(xCoord, yCoord, 4);
			
			g.setColor(getForeground());
			draw100PercentLabel(g, pR, getRDataVar().name, pS, pT);
			draw100PercentLabel(g, pS, getSDataVar().name, pR, pT);
			draw100PercentLabel(g, pT, getTDataVar().name, pR, pS);
		}
		super.drawAxes(g, backNotFront, colourType);
	}

	protected void drawData(Graphics g, int shadeHandling) {
		if (!drawData)
			return;
		
//		MixtureModel model = (MixtureModel)getModel();
		
		Point crossPos = null;
		double rst[] = new double[3];
		
		ValueEnumeration ye = getYDataVar().values();
		ValueEnumeration re = getRDataVar().values();
		ValueEnumeration se = getSDataVar().values();
		ValueEnumeration te = getTDataVar().values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			rst[0] = re.nextDouble();
			rst[1] = se.nextDouble();
			rst[2] = te.nextDouble();
			crossPos = getScreenPoint(rst[0], rst[1], rst[2], y, crossPos);
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}
	
	
//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(modelKey));
			initialised = false;
		super.doChangeVariable(g, key);
	}

}
	
