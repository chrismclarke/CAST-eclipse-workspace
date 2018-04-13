package cast.core;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


abstract public class GroupHeadingPanel extends JPanel {
	static final private int kLabelGap = 10;
	static final private int kDownArrowWidth = 16;
	static private int kDownTopGap = 4;
	static final private int kDownArrowHeight = 12;
	
	static final private int kRightArrowWidth = 12;
	static private int kRightTopGap = 2;
	static final private int kRightArrowHeight = 16;
	
	private Font labelFont, labelLinkFont;
	private Color stdColor, hoverColor;
	
	private JPanel linkedPanel;
	private JLabel theLabel;
	
	private boolean canExpand = false;
	
	public GroupHeadingPanel(String heading, Font labelFont, Font labelLinkFont, Color stdColorParam, Color hoverColorParam) {
		this.labelFont = labelFont;
		this.labelLinkFont = labelLinkFont;
		stdColor = stdColorParam;
		hoverColor = hoverColorParam;
		
		if (labelFont.getSize() > 18) {
			kDownTopGap += 2;
			kRightTopGap += 2;
		}
		
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		
		theLabel = new JLabel(heading, JLabel.LEFT);
		theLabel.setForeground(stdColor);
		theLabel.setFont(labelFont);
			
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setOpaque(false);
		add(theLabel);
			
		addMouseListener(new MouseListener() {
							public void mouseReleased(MouseEvent e) {}
							public void mousePressed(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {
								if (canExpand) {
									theLabel.setForeground(stdColor);
									repaint();
								}
							}
							public void mouseEntered(MouseEvent e) {
								if (canExpand) {
									theLabel.setForeground(hoverColor);
									repaint();
								}
							}
							public void mouseClicked(MouseEvent e) {
								if (canExpand) {
									doClickAction();
									theLabel.setForeground(stdColor);
									repaint();
								}
							}
						});
	}
	
	public void setExpanding(boolean canExpand) {
		this.canExpand = canExpand;
		theLabel.setFont(canExpand ? labelLinkFont : labelFont);
		setCursor(canExpand ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
																			: Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}
	
	public void setLinkedPanel(JPanel linkedPanel) {
		this.linkedPanel = linkedPanel;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		super.paintComponent(g);
		if (canExpand) {
			int labelWidth = theLabel.getSize().width;
			
			boolean linkVisible = linkedPanel.isVisible();
			int x[], y[];
			if (linkVisible) {
				int[] x2 = {labelWidth + kLabelGap, labelWidth + kLabelGap + kDownArrowWidth, labelWidth + kLabelGap + kDownArrowWidth / 2};
				int[] y2 = {kDownTopGap, kDownTopGap, kDownTopGap + kDownArrowHeight};
				x = x2;
				y = y2;
			}
			else {
				int[] x2 = {labelWidth + kLabelGap, labelWidth + kLabelGap + kRightArrowWidth, labelWidth + kLabelGap};
				int[] y2 = {kRightTopGap, kRightTopGap + kRightArrowHeight / 2, kRightTopGap + kRightArrowHeight};
				x = x2;
				y = y2;
			}
			g.setColor(theLabel.getForeground());
			g.fillPolygon(x, y, 3);
			g.setColor(stdColor);
			g.drawPolygon(x, y, 3);
		}
	}
	
	abstract protected void doClickAction();
}
