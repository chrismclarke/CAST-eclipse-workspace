package ebook;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.View;

public class CustomTabbedPaneUI extends BasicTabbedPaneUI {
	static final private Color kSelectedBackground = new Color(0xEEEEEE);
	static final private Color kUnselectedBackground = new Color(0xCCCCCC);
	
//	private Color selectColor;
//	private Color deSelectColor;
	private int inclTab = 4;
//	private int anchoFocoV = inclTab;
	private int anchoFocoH = 4;
	private int anchoCarpetas = 18;
	private Polygon shape;
	
	private Color tabBackgroundColor;

	public static ComponentUI createUI(JComponent c) {
		return new CustomTabbedPaneUI();
	}
	
	public void setTabBackground(Color c) {
		tabBackgroundColor = c;
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
//		selectColor = new Color(250, 192, 192);
//		deSelectColor = new Color(197, 193, 168);
		tabAreaInsets.right = anchoCarpetas;
		contentBorderInsets.top = 0;
	}

	@Override
	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
					//	Assumes that tabPlacement == TOP
		if (tabBackgroundColor != null)
			g.setColor(tabBackgroundColor);
		g.fillRect(0, 0, tabPane.getWidth(), tabPane.getHeight());
		
		if (runCount > 1) {
			int lines[] = new int[runCount];
			for (int i = 0; i < runCount; i++) {
				lines[i] = rects[tabRuns[i]].y + maxTabHeight;
			}
			Arrays.sort(lines);
			int fila = runCount;
			for (int i = 0; i < lines.length - 1; i++, fila--) {
				Polygon carp = new Polygon();
				carp.addPoint(0, lines[i]);
				carp.addPoint(tabPane.getWidth() - 2 * fila - 2, lines[i]);
				carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + 3);
				if (i < lines.length - 2) {
					carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i + 1]);
					carp.addPoint(0, lines[i + 1]);
				} else {
					carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + rects[selectedIndex].height);
					carp.addPoint(0, lines[i] + rects[selectedIndex].height);
				}
				carp.addPoint(0, lines[i]);
				g.setColor(hazAlfa(fila));
				g.fillPolygon(carp);
				g.setColor(darkShadow.darker());
				g.drawPolygon(carp);
			}
		}
		super.paintTabArea(g, tabPlacement, selectedIndex);
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		int xp[] = null;
		int yp[] = null;
		
		xp = new int[]{x, x, x + 3, x + w - inclTab - 6, x + w - inclTab - 2, x + w - inclTab, x + w - inclTab, x};
		yp = new int[]{y + h, y + 3, y, y, y + 1, y + 3, y + h, y + h};
		
		shape = new Polygon(xp, yp, xp.length);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(isSelected ? kSelectedBackground : kUnselectedBackground);
		g2d.fillPolygon(shape);
		if (isSelected) {
			g2d.setColor(Color.black);
			g2d.drawPolygon(shape);
		}
	}

	@Override
	protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
		super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
		g.setFont(font);
		View v = getTextViewForTab(tabIndex);
		if (v != null) {
			// html
			v.paint(g, textRect);
		}
		else {
			// plain text
			int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
			if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
				g.setColor(tabPane.getForegroundAt(tabIndex));
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
			}
			else { // tab disabled
				g.setColor(Color.BLACK);
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
				g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x - 1, textRect.y + metrics.getAscent() - 1);
			}
		}
	}

	@Override
	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
		return 20 + inclTab + super.calculateTabWidth(tabPlacement, tabIndex, metrics);
	}

	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
		if (tabPlacement == LEFT || tabPlacement == RIGHT) {
			return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
		}
		else {
			return anchoFocoH + super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
		}
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
	}

	@Override
	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
//		if (tabPane.hasFocus() && isSelected) {
//			g.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
//			g.drawPolygon(shape);
//		}
	}

	protected Color hazAlfa(int fila) {
		int alfa = 0;
		if (fila >= 0) {
			alfa = 50 + (fila > 7 ? 70 : 10 * fila);
		}
		return new Color(0, 0, 0, alfa);
	}
}