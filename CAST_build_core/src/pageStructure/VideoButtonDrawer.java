package pageStructure;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ebook.*;
import ebookStructure.*;


public class VideoButtonDrawer extends CoreDrawer {
	private String buttonNameString;
	private BookFrame theBookFrame;
	
	public VideoButtonDrawer(String buttonNameString, BookFrame theBookFrame) {
		this.buttonNameString = buttonNameString;
		this.theBookFrame = theBookFrame;
	}
	
	
	public JPanel createPanel() {
		JPanel buttonPanel	= new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		
			BigButton videoButton = new BigButton(buttonNameString);
			videoButton.addMouseListener(new MouseAdapter() {
									public void mouseClicked(MouseEvent e) {
										theBookFrame.showPageVersion(DomElement.VIDEO_VERSION);
									}
								});
			
		buttonPanel.add(videoButton);
			
		return buttonPanel;
	}
}
