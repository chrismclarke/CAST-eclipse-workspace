package pageStructure;

import java.awt.*;
import javax.swing.*;

import ebook.*;
import ebookStructure.*;

public class PageContentView extends JScrollPane {
	
//	static private boolean foundMargins = false;
//	static private int imgLeft, imgRight, bodyLeft, bodyRight;
	
	@SuppressWarnings("unused")
	private int borderWidth = -1;		//	stores width of window and scrollbar
	
	private BookFrame theBookFrame;
	private JPanel contentPanel;
	private JPanel scrollingPanel;
	
	private int minimumContentWidth;
	
	private class ScrollingPanel extends JPanel implements Scrollable {
		public Dimension getPreferredScrollableViewportSize() {
			Dimension d = getPreferredSize();
//			System.out.println("preferred width = " + d.width + ", minimumContentWidth = " + minimumContentWidth);
			d.width = Math.max(d.width, minimumContentWidth);
			return d;
		}
		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 2;
		}
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return visibleRect.height - 10;
		}
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
	
	public PageContentView(BookFrame theBookFrame) {
		this.theBookFrame = theBookFrame;
		Color bgColor = CoreDrawer.getBackgroundColor(theBookFrame.getPageStyleSheet());
		setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder());
		
		scrollingPanel = new ScrollingPanel();
		scrollingPanel.setLayout(new BorderLayout(0, 0));
		scrollingPanel.setBackground(bgColor);
			
			contentPanel = new JPanel();
			contentPanel.setOpaque(false);
		scrollingPanel.add("Center", contentPanel);
		
		setViewportView(scrollingPanel);
		
		getViewport().setBackground(bgColor);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	}
	
	public void setHtml(DomElement pageElement) {
		scrollingPanel.remove(contentPanel);
		if (pageElement instanceof DomSection)
			contentPanel = new SectionView((DomSection)pageElement, theBookFrame);
		else {
			PageDrawer thePageDrawer = new PageDrawer(pageElement, theBookFrame);
			contentPanel = thePageDrawer.createPanel();
			minimumContentWidth = thePageDrawer.getMinimumWidth();
		}
		scrollingPanel.add("Center", contentPanel);
		
    getVerticalScrollBar().setValue(0);
	}
	
/*
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		theBookFrame.showNormalCursor();
	}
*/
}