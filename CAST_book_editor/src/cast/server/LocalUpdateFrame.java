package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


//public class LocalUpdateFrame extends JFrame {
public class LocalUpdateFrame extends JDialog {
	private File castLocalDir;
	private AllDates localDates, serverDates;
	
	private JButton updateButton, cancelButton;
	private JLabel cancelLabel;
	private CastProgressBar stageProgressBar, itemProgressBar;
	
	private LocalUpdateTask updateTask;
	
	private int noOfStages, currentStageIndex, currentItemIndex;
	
	public boolean finishedUpdate = false;
	
	public LocalUpdateFrame(File castLocalDir, AllDates localDates, AllDates serverDates,
																																	final JFrame callingFrame) {
		super(callingFrame, "Update CAST to its latest version", true);
		this.castLocalDir = castLocalDir;
		this.localDates = localDates;
		this.serverDates = serverDates;
		
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		contentPanel.setBorder(padding);
		setContentPane(contentPanel);

		setLayout(new BorderLayout(0, 20));
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));				
				
				updateButton = new JButton("Update CAST");
				updateButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							updateButton.setEnabled(false);
																							updateCast();
																							cancelButton.setEnabled(true);
																							cancelLabel.setForeground(getBackground());		//	to hide it
																						}
																				});
			topPanel.add(updateButton);
		
		add("North", topPanel);
			
			JPanel reportPanel = new JPanel();
			reportPanel.setLayout(new BorderLayout(0, 20));
			
				JPanel progressPanel = new JPanel();
				progressPanel.setLayout(new GridLayout(0, 1, 0, 20));
				
					stageProgressBar = new CastProgressBar("Update stage");
					
				progressPanel.add(stageProgressBar);
				
					itemProgressBar = new CastProgressBar("Item progress");
					
				progressPanel.add(itemProgressBar);
					
			reportPanel.add("Center", progressPanel);
			
		add("Center", reportPanel);
		
			JPanel cancelPanel = new JPanel();
			cancelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
			
				cancelButton = new JButton("Cancel update");
				cancelButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							cancelLabel.setText("Cancelling...");
																							updateTask.cancel(true);
																							updateButton.setEnabled(true);
																							cancelButton.setEnabled(false);
																							cancelLabel.setForeground(Color.blue);
																						}
																				});
				cancelButton.setEnabled(false);
				
			cancelPanel.add(cancelButton);
				
				cancelLabel = new JLabel("Cancelling...", JLabel.LEFT);
				cancelLabel.setForeground(getBackground());				//	to make it invisible but get it laid out large enough
				cancelLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
				
			cancelPanel.add(cancelLabel);
		
		add("South", cancelPanel);
		
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											if (updateTask == null)
												dispose();
											else
												JOptionPane.showMessageDialog(LocalUpdateFrame.this, "Files are downloading. Cancel the download before closing this window.",
																											"Error!", JOptionPane.ERROR_MESSAGE);
										}
									});
		pack();
		setLocationRelativeTo(callingFrame);
		setVisible(true);
		toFront();
	}
	
	
	private void updateCast() {
		updateTask = new LocalUpdateTask(castLocalDir, this, localDates, serverDates);
		updateTask.execute();
	}
	
	public void setFinished() {
		stageProgressBar.setValue(noOfStages, "Finished");
		itemProgressBar.clear();
		updateButton.setEnabled(true);
		cancelButton.setEnabled(false);
		updateTask = null;
		finishedUpdate = true;
		
		dispose();
	}
	
	public void setCancelled() {
		updateButton.setEnabled(true);
		cancelButton.setEnabled(false);
		updateTask = null;
		cancelLabel.setText("Cancelled");
	}
	
	public void updateForStart(int noOfStages) {
		this.noOfStages = noOfStages;
		currentStageIndex = -1;
		
		stageProgressBar.initialise(noOfStages, "");
		itemProgressBar.initialise(1, "");
	}
	
	public void updateForNewStage(String stageName, int noOfItems) {
		currentStageIndex ++;
		
		stageProgressBar.setValue(currentStageIndex, stageName);
		currentItemIndex = -1;
		itemProgressBar.initialise(noOfItems, "");
	}
	
	public void updateForNewItem(String itemName) {
		currentItemIndex ++;
		itemProgressBar.setValue(currentItemIndex, itemName);
	}
	
	public void updateForNewFile(String itemName) {
		itemProgressBar.setValue(itemName);
	}
}
