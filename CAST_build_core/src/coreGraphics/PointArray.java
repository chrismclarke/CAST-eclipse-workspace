package coreGraphics;

public class PointArray {
	public int nPoints = 0;
	public double x[];
	public double y[];
	public PointArray(int maxPoints) {
		x = new double[maxPoints];
		y = new double[maxPoints];
	}
	
	public void reset() {
		nPoints = 0;
	}
	
	public void addPoint(double x, double y) {
		this.x[nPoints] = x;
		this.y[nPoints++] = y;
	}
}
