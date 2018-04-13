package graphics3D;

import java.awt.*;

import dataView.*;
import models.*;


abstract public class Rotate3DPlanesView extends Rotate3DView {
	
	static final private boolean BACK_PLANES = true;
	static final private boolean FRONT_PLANES = false;
	
	static final protected int BEHIND_0_PLANES = 0;
	static final protected int BEHIND_1_PLANES = 1;
	static final protected int BEHIND_2_PLANES = 2;
	
 	protected String model0Key, model1Key;
	protected Color planeAColor, planeBColor;
	
	public Rotate3DPlanesView(DataSet theData, XApplet applet,
							D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
							String model0Key, String model1Key, Color planeAColor, Color planeBColor) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.model0Key = model0Key;
		this.model1Key = model1Key;
		setPlaneColors(planeAColor, planeBColor);
	}
	
	public void setPlaneColors(Color planeAColor, Color planeBColor) {
		this.planeAColor = planeAColor;
		this.planeBColor = planeBColor;
	}
	
	public void setPlaneKeys(String model0Key, String model1Key) {
		this.model0Key = model0Key;
		this.model1Key = model1Key;
	}
	
	abstract protected void drawPlanesData(Graphics g, int planeLevel);
	
	protected MultipleRegnModel getPlaneA() {
		return (MultipleRegnModel)getData().getVariable(model0Key);
	}
	
	protected MultipleRegnModel getPlaneB() {
		if (model1Key == null)
			return null;
		else
			return (MultipleRegnModel)getData().getVariable(model1Key);
	}
	
	protected Color[] getAllShades(Color baseColor, double p) {
		Color c[] = new Color[4];
		c[0] = baseColor;
		c[1] = mixColors(darkenColor(baseColor, 0.2), planeAColor, p);
		c[2] = mixColors(darkenColor(baseColor, 0.2), planeBColor, p);
		c[3] = mixColors(c[1], planeBColor, p);
		return c;
	}
	
	protected Color[] getAllShades(Color baseColor) {
		return getAllShades(baseColor, 0.6);
	}
	
	protected Color getShade(Color[] c, int planeLevel, boolean behindA, boolean behindB,
																												boolean aFurthestBack) {
		if (planeLevel == BEHIND_0_PLANES)
			return c[0];
		else if (planeLevel == BEHIND_1_PLANES) {
			if (behindA && behindB)
				if (aFurthestBack)
					return c[1];
				else
					return c[2];
			else
				return c[0];
		}
		else												 // BEHIND_2_PLANES
			if (behindA && behindB)
				return c[3];
			else if (behindA)
				return c[1];
			else if (behindB)
				return c[2];
			else
				return c[0];
	}
	
	protected double getFitA(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return modelA.evaluateMean(explan);
	}
	
	protected double getFitB(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return (modelB == null) ? modelA.evaluateMean(explan) : modelB.evaluateMean(explan);
	}
	
	private void findCorners(double[] x, double[] z, double[] ya, double[] yb) {
		if (map.zAxisBehind()) {
			x[0] = x[1] = x[4] = xAxis.getMinOnAxis();
			x[2] = x[3] = xAxis.getMaxOnAxis();
		}
		else  {
			x[0] = x[1] = x[4] = xAxis.getMaxOnAxis();
			x[2] = x[3] = xAxis.getMinOnAxis();
		}
		
		if (map.xAxisBehind()) {
			z[0] = z[3] = z[4] = zAxis.getMinOnAxis();
			z[1] = z[2] = zAxis.getMaxOnAxis();
		}
		else  {
			z[0] = z[3] = z[4] = zAxis.getMaxOnAxis();
			z[1] = z[2] = zAxis.getMinOnAxis();
		}
		
		MultipleRegnModel planeA = getPlaneA();
		MultipleRegnModel planeB = getPlaneB();
		double explan[] = new double[2];
		
		for (int i=0 ; i<5 ; i++) {
			explan[0] = x[i];
			explan[1] = z[i];
			ya[i] = getFitA(planeA, planeB, explan);
			yb[i] = getFitB(planeA, planeB, explan);
		}
		
		if (planesCross(ya, yb)) {	//	furthest point may not be in back region when looking between planes
			boolean aFromTop = viewingPlaneFromTop(planeA);
			boolean bFromTop = (planeB == null) ? aFromTop : viewingPlaneFromTop(planeB);
																//	planeB == null for RotateModelBandView
			if (aFromTop != bFromTop) {
				while ((ya[0] < yb[0]) == aFromTop) {
					for (int i=0 ; i<4 ; i++) {
						x[i] = x[i+1];
						z[i] = z[i+1];
						ya[i] = ya[i+1];
						yb[i] = yb[i+1];
					}
					x[4] = x[0];
					z[4] = z[0];
					ya[4] = ya[0];
					yb[4] = yb[0];
				}
			}
		}
	}
	
	protected Point getModelPoint(double[] xVals, MultipleRegnModel model) {
		double fit = model.evaluateMean(xVals);
		return getScreenPoint(xVals[0], fit, xVals[1], null);
	}
	
	protected boolean viewingPlaneFromTop(MultipleRegnModel model) {
		if (model == null)		//	copied from ModelDot3View
			return true;
		
		double minX = xAxis.getMinOnAxis();
		double maxX = xAxis.getMaxOnAxis();
		double minZ = zAxis.getMinOnAxis();
		double maxZ = zAxis.getMaxOnAxis();
		
		double nearX, nearZ;
//		double nearY, y1, y2;
//		double angle = map.getTheta1();
		if (map.zAxisBehind())
			nearX = maxX;
		else
			nearX = minX;
		if (map.xAxisBehind())
			nearZ = maxZ;
		else
			nearZ = minZ;
		
		double x1 = minX;
		double x2 = maxX;
		double z1, z2;
		if (map.zAxisBehind() == map.xAxisBehind()) {
			z1 = maxZ;
			z2 = minZ;
		}
		else {
			z1 = minZ;
			z2 = maxZ;
		}
		
		double xVals[] = new double[2];
		xVals[0] = nearX;
		xVals[1] = nearZ;
		Point nearPos = getModelPoint(xVals, model);
		
		xVals[0] = x1;
		xVals[1] = z1;
		Point pos1 = getModelPoint(xVals, model);
		
		xVals[0] = x2;
		xVals[1] = z2;
		Point pos2 = getModelPoint(xVals, model);
		
		return (pos1.x > pos2.x) == ((pos2.y - pos1.y) * (nearPos.x - pos1.x)
											> (nearPos.y - pos1.y) * (pos2.x - pos1.x));
	}
	
	private Point getCrossPoint(double[] x, double[] z, double[] ya, double[] yb, int startIndex) {
		double crossPtPropn = (yb[startIndex] - ya[startIndex])
						/ (double)(yb[startIndex] - ya[startIndex] - yb[startIndex + 1] + ya[startIndex + 1]);
		double xCross = x[startIndex] * (1 - crossPtPropn) + x[startIndex + 1] * crossPtPropn;
		double zCross = z[startIndex] * (1 - crossPtPropn) + z[startIndex + 1] * crossPtPropn;
		double yCross = ya[startIndex] * (1 - crossPtPropn) + ya[startIndex + 1] * crossPtPropn;
		return getScreenPoint(xCross, yCross, zCross, null);
	}
	
	private void processEdge(double[] x, double[] z, double[] ya, double[] yb,
									int[] aCoordx, int[] aCoordy, int[] bCoordx, int[] bCoordy, int startIndex,
									Polygon aPoly, Polygon bPoly, Polygon clipPoly, boolean backPlanes) {
		boolean aUnderBStart = ya[startIndex] < yb[startIndex];
		boolean aUnderBEnd = ya[startIndex + 1] < yb[startIndex + 1];
		
		if (aUnderBStart == aUnderBEnd) {
			if (aUnderBEnd == backPlanes) {
				clipPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
				aPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
			}	
			else {
				clipPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
				bPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
			}	
		}
		else {
			Point crossPt = getCrossPoint(x, z, ya, yb, startIndex);
			clipPoly.addPoint(crossPt.x, crossPt.y);
			aPoly.addPoint(crossPt.x, crossPt.y);
			bPoly.addPoint(crossPt.x, crossPt.y);
			if (aUnderBEnd == backPlanes) {
				clipPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
				aPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
			}
			else {
				clipPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
				bPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
			}
		}
	}
	
	private void findPolygons(double[] x, double[] z, double[] ya, double[] yb,
									int[] aCoordx, int[] aCoordy, int[] bCoordx, int[] bCoordy,
									Polygon aPoly, Polygon bPoly, Polygon clipPoly, boolean backPlanes) {
		boolean aUnderBStart = ya[0] < yb[0];
		if (aUnderBStart == backPlanes) {
			aPoly.addPoint(aCoordx[0], aCoordy[0]);
			clipPoly.addPoint(aCoordx[0], aCoordy[0]);
		}
		else {
			bPoly.addPoint(bCoordx[0], bCoordy[0]);
			clipPoly.addPoint(bCoordx[0], bCoordy[0]);
		}
		for (int i=0 ; i<4 ; i++)
			processEdge(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, i,
																												aPoly, bPoly, clipPoly, backPlanes);
		if (aUnderBStart == backPlanes) {
			if (bPoly.npoints > 0)
				bPoly.addPoint(bPoly.xpoints[0], bPoly.ypoints[0]);
		}
		else {
			if (aPoly.npoints > 0)
				aPoly.addPoint(aPoly.xpoints[0], aPoly.ypoints[0]);
		}
	}
	
	private boolean planesCross(double[] ya, double[] yb) {
		boolean aBelowAtStart = ya[0] < yb[0];
		for (int i=1 ; i<4 ; i++)
			if ( (ya[i] < yb[i]) != aBelowAtStart)
				return true;
		return false;
	}
	
	private void findCrossPolygons(double[] x, double[] z, double[] ya, double[] yb,
									int[] aCoordx, int[] aCoordy, int[] bCoordx, int[] bCoordy,
									Polygon aPoly, Polygon bPoly, Polygon clipPoly, boolean backPlanes) {
		boolean aBelowAtStart = ya[0] < yb[0];
		if (backPlanes) {
			aPoly.addPoint(aCoordx[0], aCoordy[0]);
			bPoly.addPoint(bCoordx[0], bCoordy[0]);
		}
		for (int i=0 ; i<4 ; i++)
			processCrossEdge(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, i,
																							aPoly, bPoly, clipPoly, aBelowAtStart == backPlanes);
		if (!backPlanes) {
			aPoly.addPoint(aPoly.xpoints[0], aPoly.ypoints[0]);
			bPoly.addPoint(bPoly.xpoints[0], bPoly.ypoints[0]);
		}
		
		if (backPlanes) {
			int iCross = 0;
			int nPoints = aPoly.npoints;
			for (iCross=0 ; iCross<nPoints ; iCross++)
				if (aPoly.xpoints[iCross] == bPoly.xpoints[iCross] && aPoly.ypoints[iCross] == bPoly.ypoints[iCross])
					break;
			for (int i=iCross+1 ; i<nPoints ; i++)
				clipPoly.addPoint(aPoly.xpoints[i], aPoly.ypoints[i]);
			for (int i=0 ; i<=iCross ; i++)
				clipPoly.addPoint(aPoly.xpoints[i], aPoly.ypoints[i]);
			for (int i=iCross-1 ; i>=0 ; i--)
				clipPoly.addPoint(bPoly.xpoints[i], bPoly.ypoints[i]);
			for (int i=nPoints-1 ; i>=iCross ; i--)
				clipPoly.addPoint(bPoly.xpoints[i], bPoly.ypoints[i]);
		}
		else {
			for (int i=0 ; i<aPoly.npoints - 1 ; i++)
				clipPoly.addPoint(aPoly.xpoints[i], aPoly.ypoints[i]);
			for (int i=bPoly.npoints-2 ; i>=0 ; i--)
				clipPoly.addPoint(bPoly.xpoints[i], bPoly.ypoints[i]);
		}
	}
	
	private void processCrossEdge(double[] x, double[] z, double[] ya, double[] yb,
									int[] aCoordx, int[] aCoordy, int[] bCoordx, int[] bCoordy, int startIndex,
									Polygon aPoly, Polygon bPoly, Polygon clipPoly, boolean aBelowRequired) {
		boolean aBelow0 = ya[startIndex] < yb[startIndex];
		boolean aBelow1 = ya[startIndex + 1] < yb[startIndex + 1];
		if (aBelow0 == aBelow1) {
			if (aBelow0 == aBelowRequired) {
				aPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
				bPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
			}
		}
		else {
			Point crossPt = getCrossPoint(x, z, ya, yb, startIndex);
			aPoly.addPoint(crossPt.x, crossPt.y);
			bPoly.addPoint(crossPt.x, crossPt.y);
			
			if (aBelow1 == aBelowRequired) {
				aPoly.addPoint(aCoordx[startIndex + 1], aCoordy[startIndex + 1]);
				bPoly.addPoint(bCoordx[startIndex + 1], bCoordy[startIndex + 1]);
			}
		}
	}
	
	private void drawCrossedPlanes(Graphics g, Polygon aBackPoly, Polygon bBackPoly, Polygon aFrontPoly,
															Polygon bFrontPoly, Polygon backClipPoly, Polygon frontClipPoly) {
		Color aPlaneDimColor = mixColors(planeBColor, darkenColor(planeAColor, 0.2), 0.8);
		Color bPlaneDimColor = mixColors(planeAColor, darkenColor(planeBColor, 0.2), 0.8);
		
		if (canClip)
			try {
				drawPlanesData(g, BEHIND_0_PLANES);
				
				Shape oldClip = g.getClip();
				g.setClip(backClipPoly);
				if (aBackPoly.npoints > 1) {
					g.setColor(planeAColor);
					g.fillPolygon(aBackPoly);
				}
				if (bBackPoly.npoints > 1) {
					g.setColor(planeBColor);
					g.fillPolygon(bBackPoly);
				}
				
				drawAxes(g, BACK_AXIS, D3Axis.SHADED);
				drawPlanesData(g, BEHIND_1_PLANES);
				
				g.setClip(frontClipPoly);
				
				if (aFrontPoly.npoints > 1) {
					g.setColor(planeAColor);
					g.fillPolygon(aFrontPoly);
				}
				if (bFrontPoly.npoints > 1) {
					g.setColor(planeBColor);
					g.fillPolygon(bFrontPoly);
				}
				
				if (aBackPoly.npoints > 1) {
					g.setColor(aPlaneDimColor);
					g.fillPolygon(aBackPoly);
				}
				if (bBackPoly.npoints > 1) {
					g.setColor(bPlaneDimColor);
					g.fillPolygon(bBackPoly);
				}
				drawAxes(g, BACK_AXIS, D3Axis.SHADED);
				drawPlanesData(g, BEHIND_2_PLANES);
				
				g.setClip(oldClip);
			}
			catch (Exception e) {	//		g.setClip() is not always implemented for Polygon class
				canClip = false;
				drawCrossedPlanesWithoutClip(g, aBackPoly, bBackPoly, aFrontPoly,
																					bFrontPoly, backClipPoly, frontClipPoly);
			}
		else
			drawCrossedPlanesWithoutClip(g, aBackPoly, bBackPoly, aFrontPoly,
																					bFrontPoly, backClipPoly, frontClipPoly);
	}
	
	private void drawCrossedPlanesWithoutClip(Graphics g, Polygon aBackPoly, Polygon bBackPoly, Polygon aFrontPoly,
													Polygon bFrontPoly, Polygon backClipPoly, Polygon frontClipPoly) {
		if (aBackPoly.npoints > 1) {
			g.setColor(planeAColor);
			g.fillPolygon(aBackPoly);
		}
		if (bBackPoly.npoints > 1) {
			g.setColor(planeBColor);
			g.fillPolygon(bBackPoly);
		}
		if (aFrontPoly.npoints > 1) {
			g.setColor(planeAColor);
			g.fillPolygon(aFrontPoly);
		}
		if (bFrontPoly.npoints > 1) {
			g.setColor(planeBColor);
			g.fillPolygon(bFrontPoly);
		}
		drawPlanesData(g, BEHIND_2_PLANES);
	}
	
	protected void drawContents(Graphics g) {
		MultipleRegnModel planeA = getPlaneA();
		MultipleRegnModel planeB = getPlaneB();
		
		if (planeA == null && planeB == null)
			drawPlanesData(g, BEHIND_0_PLANES);
		else {
			double x[] = new double[5];
			double z[] = new double[5];
			double ya[] = new double[5];
			double yb[] = new double[5];
			
			findCorners(x, z, ya, yb);
			
			int aCoordx[] = new int[5];
			int aCoordy[] = new int[5];
			int bCoordx[] = new int[5];
			int bCoordy[] = new int[5];
			Point p = null;
			
			for (int i=0 ; i<5 ; i++) {
				p = getScreenPoint(x[i], ya[i], z[i], p);
				aCoordx[i] = p.x;
				aCoordy[i] = p.y;
				p = getScreenPoint(x[i], yb[i], z[i], p);
				bCoordx[i] = p.x;
				bCoordy[i] = p.y;
			}
			
			Polygon aBackPoly = new Polygon();
			Polygon bBackPoly = new Polygon();
			Polygon aFrontPoly = new Polygon();
			Polygon bFrontPoly = new Polygon();
			Polygon backClipPoly = new Polygon();
			Polygon frontClipPoly = new Polygon();
			
			boolean aPlaneFromTop = viewingPlaneFromTop(getPlaneA());
			boolean bPlaneFromTop = (planeB == null) ? aPlaneFromTop : viewingPlaneFromTop(planeB);
																	//	planeB == null for RotateModelBandView
				
			if (aPlaneFromTop == bPlaneFromTop) {
				findPolygons(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, aBackPoly, bBackPoly,
																														backClipPoly, aPlaneFromTop == BACK_PLANES);
				findPolygons(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, aFrontPoly, bFrontPoly,
																													frontClipPoly, aPlaneFromTop == FRONT_PLANES);
				drawCrossedPlanes(g, aBackPoly, bBackPoly, aFrontPoly, bFrontPoly, backClipPoly, frontClipPoly);
			}
			else {
				boolean crossing = planesCross(ya, yb);
				if (crossing) {
					findCrossPolygons(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, aBackPoly, bBackPoly,
																														backClipPoly, BACK_PLANES);
					findCrossPolygons(x, z, ya, yb, aCoordx, aCoordy, bCoordx, bCoordy, aFrontPoly, bFrontPoly,
																													frontClipPoly, FRONT_PLANES);
					drawCrossedPlanes(g, aBackPoly, bBackPoly, aFrontPoly, bFrontPoly, backClipPoly, frontClipPoly);
				}
				else {
					if (canClip)
						try {
							drawPlanesData(g, BEHIND_0_PLANES);
							
							for (int i=0 ; i<5 ; i++) {
								aBackPoly.addPoint(aCoordx[i], aCoordy[i]);
								bFrontPoly.addPoint(bCoordx[i], bCoordy[i]);
							}
							backClipPoly = aBackPoly;
							frontClipPoly = bFrontPoly;
							
							g.setColor(planeAColor);
							g.fillPolygon(aBackPoly);
							
							Shape oldClip = g.getClip();
							g.setClip(backClipPoly);
							drawAxes(g, BACK_AXIS, D3Axis.SHADED);
							drawPlanesData(g, BEHIND_1_PLANES);
							g.setClip(oldClip);
							
							g.setColor(planeBColor);
							g.fillPolygon(bFrontPoly);
							
							g.setClip(frontClipPoly);
							drawAxes(g, BACK_AXIS, D3Axis.SHADED);
							drawPlanesData(g, BEHIND_2_PLANES);
						
							g.setClip(oldClip);
						}
						catch (Exception e) {	//		g.setClip() is not always implemented for Polygon class
							canClip = false;
							g.setColor(planeAColor);
							g.fillPolygon(aBackPoly);
							g.setColor(planeBColor);
							g.fillPolygon(bFrontPoly);
							drawPlanesData(g, BEHIND_2_PLANES);
						}
					else {
						g.setColor(planeAColor);
						g.fillPolygon(aBackPoly);
						g.setColor(planeBColor);
						g.fillPolygon(bFrontPoly);
						drawPlanesData(g, BEHIND_2_PLANES);
					}
				}
			}
		}
	}
}
	
