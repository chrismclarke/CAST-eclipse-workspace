package pageStructure;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebook.*;
import ebookStructure.*;


public class NextButtonDrawer extends CoreDrawer {
	static final private int kBaseEndFontSize = 24;
	static final private int kButtonFontSize = 18;
	static final private int kCopyrightFontSize = 13;
	
	static final private String kCopyrightString = "\u00a92000-2017 W. Douglas Stirling, Massey University";
	static final private Color kCopyrightColor = new Color(0x999999);
	
	static final private Color kButtonColor = new Color(0xCC0000);
	static final private Color kStdButtonBackground = Color.white;
	static final private Color kMouseoverButtonBackground = new Color(0xCCCCCC);
	
	private BookFrame theWindow;
	private String buttonTitle;
	private DomElement nextElement;
	
	public NextButtonDrawer(DomElement theElement, BookFrame theBookFrame) {
		theWindow = theBookFrame;
		nextElement = theElement.nextElement();
		if (nextElement == null)
			buttonTitle = null;
		else {
			boolean isModule = theBookFrame.getEbook().isModule();
			String chapterString = isModule ? "Section" : "Chapter";
			if (nextElement instanceof DomChapter)
				buttonTitle = ((theElement instanceof DomBook) ? "First " : "Next ") + chapterString;
			else if (nextElement instanceof DomSection)
				buttonTitle = (theElement instanceof DomChapter) ? "First Section" : "Next Section";
			else
				buttonTitle = (theElement instanceof DomSection) ? "First Page" : "Next Page";
			buttonTitle = theWindow.translate(buttonTitle);
		}
	}
	
	
	private class ArrowBorder extends AbstractBorder {
		static final private int kTopBottomGap = 8;
		static final private int kArrowHeadWidth = 25;
		static final private int kTextLeftBorder = 20;
		static final private int kTextRightBorder = 15;
		static final private int kTextTopBorder = 1;
		static final private int kTextBottomBorder = 1;
		
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
																										RenderingHints.VALUE_ANTIALIAS_ON);
		
		public boolean isBorderOpaque() {
			return false;
		}
		
		public Insets getBorderInsets(Component c, Insets insets) {
			return getBorderInsets(c);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(kTopBottomGap + 1 + kTextTopBorder, kTextLeftBorder + 1,
												kTopBottomGap + 1 + kTextBottomBorder, kTextRightBorder + kArrowHeadWidth + 1);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(hints);
			g.setColor(Color.black);
			
			int xCoord[] = {0, width - kArrowHeadWidth, width - kArrowHeadWidth, width - 1, width - kArrowHeadWidth,
																width - kArrowHeadWidth, 0};
			int yCoord[] = {kTopBottomGap, kTopBottomGap, 0, height / 2, height - 1, height - kTopBottomGap,
																height - kTopBottomGap};
			Polygon arrowOutline = new Polygon(xCoord, yCoord, xCoord.length);
			
			Area outerArrow = new Area(new Polygon(xCoord, yCoord, xCoord.length));
			Insets insets = getBorderInsets(c);
			Area innerRect = new Area(new Rectangle(insets.left, insets.top, width - insets.left - insets.right,
																							height - insets.top - insets.bottom));
			outerArrow.subtract(innerRect);
			
			g2.setColor(c.getBackground());
			g2.fill(outerArrow);
			g2.setColor(Color.black);
			g2.draw(arrowOutline);
		}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel	= new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			
			JPanel buttonPanel	= new JPanel();
			buttonPanel.setOpaque(false);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
			
			if (buttonTitle == null) {
					JPanel endPanel = new JPanel();
					endPanel.setOpaque(false);
					endPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					
						Font endFont = new Font(kSanSerifFontName, Font.BOLD, scaledSize(kBaseEndFontSize));
						JLabel endLabel1 = new JLabel(theWindow.translate("BookEndText1"), JLabel.LEFT);
						endLabel1.setFont(endFont);
					endPanel.add(endLabel1);
						JLabel endLabel2 = new JLabel(theWindow.translate("BookEndText2"), JLabel.LEFT);
						endLabel2.setFont(endFont);
					endPanel.add(endLabel2);
				buttonPanel.add(endPanel);
			}
			else {
				final JLabel nextButton = new JLabel(buttonTitle, JLabel.LEFT) {
															public void paintComponent(Graphics g) {
																g.setColor(getBackground());
																Insets border = getBorder().getBorderInsets(this);
																g.fillRect(border.left, border.top, getSize().width - border.left - border.right,
																							getSize().height - border.top - border.bottom);
																g.setColor(getForeground());
																super.paintComponent(g);
															}
														};
				nextButton.setBorder(new ArrowBorder());
				Font buttonFont = new Font(kSanSerifFontName, Font.BOLD, scaledSize(kButtonFontSize));
				nextButton.setFont(buttonFont);
				nextButton.setForeground(kButtonColor);
				nextButton.setBackground(kStdButtonBackground);
				
				nextButton.addMouseListener(new MouseListener() {
									public void mouseReleased(MouseEvent e) {}
									public void mousePressed(MouseEvent e) {}
									public void mouseExited(MouseEvent e) {
										nextButton.setBackground(kStdButtonBackground);
									}
									public void mouseEntered(MouseEvent e) {
										nextButton.setBackground(kMouseoverButtonBackground);
									}
									public void mouseClicked(MouseEvent e) {
										theWindow.showPage(nextElement, BookFrame.FROM_TOC);
									}
								});
				nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				
				buttonPanel.add(nextButton);
			}
		thePanel.add(buttonPanel);
		
			JPanel copyrightPanel = new JPanel();
			copyrightPanel.setOpaque(false);
			copyrightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			copyrightPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 20));
			
				JLabel copyrightLabel = new JLabel(kCopyrightString, JLabel.LEFT);
				Font copyrightFont = new Font(kSerifFontName, Font.PLAIN, scaledSize(kCopyrightFontSize));
				copyrightLabel.setFont(copyrightFont);
				copyrightLabel.setForeground(kCopyrightColor);
				copyrightLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				copyrightLabel.addMouseListener(new MouseAdapter() {  
												public void mouseClicked(MouseEvent e) {
													theWindow.showNamedPage("aboutCast3");
												}  
										});
			copyrightPanel.add(copyrightLabel);
			
		thePanel.add(copyrightPanel);
		
		return thePanel;
	}
}
