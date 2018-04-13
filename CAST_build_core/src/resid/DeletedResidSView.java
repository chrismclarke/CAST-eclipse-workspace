package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import images.*;


public class DeletedResidSView extends DeletedResidView {
	static final private Color kDarkGreen = new Color(0x006600);
	
	static final private String kDelSdImageName = "multiRegn/deletedSD.gif";
	static final private String kSdImageName = "multiRegn/sd.gif";
	static final private int kSdImageWidth = 32;
//	static final private int kSdImageHeight = 21;
	static final private int kSdImageAscent = 17;
	
	private boolean showDeletedSD = false;
	private Image sdImage, deletedSdImage = null;
	private int sdLeft = 0;
	private int sdUp = 0;
	
	public DeletedResidSView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lsKey,
						String deletedLSKey, int sdLeft, int sdUp) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lsKey, deletedLSKey);
		this.sdLeft = sdLeft;
		this.sdUp = sdUp;
		
		MediaTracker mt = new MediaTracker(this);
			sdImage = CoreImageReader.getImage(kSdImageName);
			deletedSdImage = CoreImageReader.getImage(kDelSdImageName);
		mt.addImage(sdImage, 0);
		mt.addImage(deletedSdImage, 0);
	
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image: " + e);
		}
	}
	
	public void setShowDeletedSD(boolean showDeletedSD) {
		this.showDeletedSD = showDeletedSD;
	}
	
	protected void drawBackground(Graphics g, int selectedIndex) {
		if (showDeletedSD && selectedIndex <= 0)
			return;
		
		g.setColor(kDarkGreen);
		LinearModel fullLS = (LinearModel)getVariable(lsKey);
		LinearModel deletedLS = (LinearModel)getVariable(deletedLSKey);
		if (showDeletedSD && deletedLS.setDeletedIndex(selectedIndex))
			deletedLS.updateLSParams(yKey);
		LinearModel ls = showDeletedSD ? deletedLS : fullLS;
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		for (int i=0 ; i<xVar.noOfValues() ; i++)
				drawDeletedResid(g, ls, xVar, i);
		g.setColor(getForeground());
		
//		NumVariable yVar = (NumVariable)getVariable(yKey);
//		NumVariable antiLeverageVar = (NumVariable)getVariable("antiLeverage");
//		for (int i=0 ; i<xVar.noOfValues() ; i++) {
//			if (deletedLS.setDeletedIndex(i))
//				deletedLS.updateLSParams(yKey);
//			double x = xVar.valueAt(i).value;
//			double delFit = deletedLS.evaluateMean(x);
//			double y = yVar.valueAt(i).value;
//			double fullFit = fullLS.evaluateMean(x);
//			double antiLeverage = antiLeverageVar.valueAt(i).value;
//			
//			System.out.println("x = " + x + ", y = " + y + ", delFit = " + delFit
//										+ ", fullFit = " + fullFit + ", antiLeverage = " + antiLeverage);
//			System.out.println("    e = " + (y - fullFit) + ", eDel = " + (y - delFit)
//										+ ", e2 + " + ((y - fullFit) / (antiLeverage * antiLeverage)));
//		}
		
		super.drawBackground(g, selectedIndex);
		
		g.setColor(kDarkGreen);
		int sdTop = getSize().height - sdUp;
		g.drawImage(showDeletedSD ? deletedSdImage : sdImage, sdLeft, sdTop, this);
		NumValue sd = ls.evaluateSD();
		sd.drawRight(g, sdLeft + kSdImageWidth, sdTop + kSdImageAscent);
		
		g.setColor(getForeground());
	}
}
	
