package cast.core;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;
import javax.imageio.*;

import cast.utils.*;


public class HeadingPanel extends JPanel {
	static final public Color kHeadingBackground = new Color(0x0066FF);
	
	static final private Font largeFont = new Font("SansSerif", Font.BOLD, 40);
	static final private Font smallFont = new Font("SansSerif", Font.PLAIN, 14);
	
	public HeadingPanel(String title1, String title2, String headingDescription) {
		setBackground(kHeadingBackground);
		setLayout(new BorderLayout(0, 10));
		
			JPanel topPanel = new JPanel();
			topPanel.setOpaque(false);
			topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
			topPanel.setLayout(new BorderLayout(20, 0));
			
			try {
				BufferedImage castStar = ImageIO.read(HeadingPanel.class.getClassLoader().getResource("images/castLogo.png"));
				JLabel leftStar = new JLabel(new ImageIcon(castStar));
				topPanel.add("West", leftStar);
				
				JLabel rightStar = new JLabel(new ImageIcon(castStar));
				topPanel.add("East", rightStar);
			} catch (IOException e) {
				System.out.println("Could not read castLogo.png");
			}
			
				JPanel titlePanel = new JPanel();
				titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, -10));
				titlePanel.setOpaque(false);
			
					JLabel title1Label = new JLabel(title1, JLabel.CENTER);
					title1Label.setFont(largeFont);
					title1Label.setForeground(Color.white);
					
				titlePanel.add(title1Label);
				
				if (title2 != null) {
					JLabel title2Label = new JLabel(title2, JLabel.CENTER);
					title2Label.setFont(largeFont);
					title2Label.setForeground(Color.white);
					
					titlePanel.add(title2Label);
				}
				
			topPanel.add("Center", titlePanel);
			
		add("North", topPanel);
		
			JPanel textPanel = new JPanel();
			textPanel.setOpaque(false);
			textPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 16, 30));
			textPanel.setLayout(new BorderLayout(0, 0));
			
				JLabel description = new JLabel(headingDescription, JLabel.CENTER);
				description.setFont(smallFont);
				description.setForeground(Color.white);
			textPanel.add(description);
		
		add("Center", textPanel);
	}
	
}
