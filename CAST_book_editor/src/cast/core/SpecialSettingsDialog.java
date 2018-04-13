package cast.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


public class SpecialSettingsDialog extends JDialog {
	
	private File castDir;
	private JTextField castServerPath;
	private JCheckBox adminCheckbox;
	private boolean saveSettings = false;
	
	public SpecialSettingsDialog(File defaultCastDir) {
		super((Frame)null, "Special settings for CAST manager", true);
		
		castDir = defaultCastDir;
		
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		contentPanel.setBorder(padding);
		setContentPane(contentPanel);
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, 25));
		
			JPanel castFolderPanel = new JPanel();
			Border blackline = BorderFactory.createLineBorder(Color.black);
			TitledBorder title = BorderFactory.createTitledBorder(blackline, "CAST folder");
			title.setTitleJustification(TitledBorder.LEFT);
			castFolderPanel.setBorder(title);
			castFolderPanel.setLayout(new BorderLayout(10, 0));
			
				final JLabel castFolderPath = new JLabel(""	, JLabel.LEFT);
				setLabelToPath(castDir, castFolderPath);;
			castFolderPanel.add("Center", castFolderPath);
			
				JButton chooseCastFolderButton = new JButton("Choose folder");
				chooseCastFolderButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						File foundDir = FileFinder.queryCastFolder(SpecialSettingsDialog.this, castDir);
																						if (foundDir != null) {
																							castDir = foundDir;
																							setLabelToPath(castDir, castFolderPath);
																						}
																					}
																			});
			castFolderPanel.add("East", chooseCastFolderButton);
		
		add(castFolderPanel);
		
			JPanel castServerPanel = new JPanel();
			blackline = BorderFactory.createLineBorder(Color.black);
			title = BorderFactory.createTitledBorder(blackline, "CAST server URL");
			title.setTitleJustification(TitledBorder.LEFT);
			castServerPanel.setBorder(title);
			castServerPanel.setLayout(new BorderLayout(10, 0));
			
				castServerPath = new JTextField("http://" + Options.kCastDownloadUrl, 50);
				castServerPath.setEnabled(false);
			castServerPanel.add("Center", castServerPath);
		
		add(castServerPanel);
		
			final JPanel administratorPanel = new JPanel();
			blackline = BorderFactory.createLineBorder(Color.black);
			title = BorderFactory.createTitledBorder(blackline, "Advanced");
			title.setTitleJustification(TitledBorder.LEFT);
			administratorPanel.setBorder(title);
			administratorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				adminCheckbox = new JCheckBox("Allow administrator commands");
			administratorPanel.add(adminCheckbox);
			administratorPanel.setVisible(false);
		
		add(administratorPanel);
		
			JPanel closePanel = new JPanel();
			closePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JButton saveButton = new JButton("Continue");
				saveButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						/*
																						try {
																							URLConnection yc = new URL(castServerPath.getText() + "/index.html").openConnection();
																							InputStream is = yc.getInputStream();
																							is.close();
																						} catch (Exception ex) {
																							JOptionPane jop = new JOptionPane();
																							JOptionPane.showMessageDialog(jop, "The server URL is not valid or cannot be accessed");
																							return;
																						}
																						*/
																						saveSettings = true;
																						SpecialSettingsDialog.this.setVisible(false);
																					}
																			});
			closePanel.add(saveButton);
			
				JButton closeButton = new JButton("Cancel");
				closeButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						SpecialSettingsDialog.this.setVisible(false);
																					}
																			});
			closePanel.add(closeButton);
			
		add(closePanel);
		
		
		((JPanel)getContentPane()).getActionMap().put("showAdmin", new AbstractAction() {
																								public void actionPerformed(ActionEvent e) {
																									administratorPanel.setVisible(true);
																									pack();
																									setLocationRelativeTo(null);
																								}
																							});
		
		KeyStroke controlA = KeyStroke.getKeyStroke("control shift released DOWN");
		InputMap inputMap = ((JPanel)getContentPane()).getInputMap();
		inputMap.put(controlA, "showAdmin");
	
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}
	
	private void setLabelToPath(File path, JLabel label) {
		try {
			label.setText(path.getCanonicalPath());
		} catch (IOException ex) {
			label.setText(path.getAbsolutePath());
		}
	}
	
	public boolean doSaveSettings() {
		return saveSettings;
	}
	
	public boolean isAdministratorMode() {
		return adminCheckbox.isSelected();
	}
	
	public File getCastFolder() {
		return castDir;
	}
	
//	public String getServerUrl() {
//		return castServerPath.getText();
//	}
}
