package cast.utils;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;


public class CastProgressBar extends JPanel {
	
	private JLabel itemLabel;
	private JProgressBar progressBar;
	
	public CastProgressBar(String barTitle) {
		Border blackline = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(blackline, barTitle);
		title.setTitleJustification(TitledBorder.CENTER);
		setBorder(title);
		
		setLayout(new BorderLayout(0, 0));
		
			itemLabel = new JLabel(" ", JLabel.LEFT);
		add("North", itemLabel);
			
			progressBar = new JProgressBar();
			progressBar.setEnabled(false);
		add("Center", progressBar);
	}
	
	public void initialise(int maxCount, String initialText) {
		progressBar.setMaximum(maxCount);
		progressBar.setValue(0);
		progressBar.setEnabled(true);
		itemLabel.setText(initialText);
	}
	
	public void setValue(int count, String itemName) {
		progressBar.setValue(count);
		itemLabel.setText(itemName);
	}
	
	public void setValue(String itemName) {
		itemLabel.setText(itemName);
	}
	
	public void clear() {
		progressBar.setMaximum(1);
		progressBar.setValue(1);
		progressBar.setEnabled(false);
		itemLabel.setText(" ");
	}
	
	public void setDone(String endText) {
		progressBar.setValue(progressBar.getMaximum());
		itemLabel.setText(endText);
	}
	
}
