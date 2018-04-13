package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.net.ftp.*;

import cast.core.*;
import cast.utils.*;


public class UpdateServerFrame extends JFrame {
	
	private JButton uploadButton;
	private JCheckBox noManagerCheck, noCoreJavaCheck;
	private JLabel finishedLabel;
	private CastProgressBar uploadStageProgress, uploadItemProgress;
	private File castSourceDir;
	
	private JTextField serverNameEdit, userNameEdit, castPathEdit;
	private JPasswordField passwordEdit;
	
	private UpdateServerTask uploadTask;
	
	public UpdateServerFrame(File castSourceDir) {
		super("Update server CAST from local CAST folder");
		this.castSourceDir = castSourceDir;
		
		setLayout(new BorderLayout(0, 10));
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, 5));
				
				JPanel ftpServerPanel = new JPanel();
				ftpServerPanel.setLayout(new BorderLayout(0, 0));
					JLabel serverTitle = new JLabel("FTP server:");
					serverNameEdit = new JTextField(Options.kCastUploadServer, 25);
					serverNameEdit.setEnabled(false);
					serverTitle.setLabelFor(serverNameEdit);
				
				ftpServerPanel.add("West", serverTitle);
				ftpServerPanel.add("Center", serverNameEdit);
				
			topPanel.add(ftpServerPanel);
				
				JPanel castPathPanel = new JPanel();
				castPathPanel.setLayout(new BorderLayout(0, 0));
					JLabel pathTitle = new JLabel("Path to CAST on server:");
					castPathEdit = new JTextField(Options.kCastUploadPath, 25);
					castPathEdit.setEnabled(false);
					pathTitle.setLabelFor(castPathEdit);
				
				castPathPanel.add("West", pathTitle);
				castPathPanel.add("Center", castPathEdit);
				
			topPanel.add(castPathPanel);
				
				JPanel userNameServerPanel = new JPanel();
				userNameServerPanel.setLayout(new BorderLayout(0, 0));
					JLabel userNameTitle = new JLabel("User name:");
					userNameEdit = new JTextField("", 25);
					userNameTitle.setLabelFor(userNameEdit);
				
				userNameServerPanel.add("West", userNameTitle);
				userNameServerPanel.add("Center", userNameEdit);
				
			topPanel.add(userNameServerPanel);
				
				JPanel ftpPasswordPanel = new JPanel();
				ftpPasswordPanel.setLayout(new BorderLayout(0, 0));
					JLabel passwordTitle = new JLabel("FTP password:");
					passwordEdit = new JPasswordField("", 25);
					passwordTitle.setLabelFor(passwordEdit);
				
				ftpPasswordPanel.add("West", passwordTitle);
				ftpPasswordPanel.add("Center", passwordEdit);
				
			topPanel.add(ftpPasswordPanel);
				
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 0));
					noManagerCheck = new JCheckBox("Ignore changes to Start_CAST.jar");
					noManagerCheck.setSelected(true);
				buttonPanel.add(noManagerCheck);
					noCoreJavaCheck = new JCheckBox("Ignore changes to coreCAST.jar");
					noCoreJavaCheck.setSelected(true);
				buttonPanel.add(noCoreJavaCheck);
					
					uploadButton = new JButton("Upload to server...");
					uploadButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											finishedLabel.setText("Finding book contents...");
											uploadButton.setEnabled(false);
											uploadFiles(!noManagerCheck.isSelected(), !noCoreJavaCheck.isSelected());
										}
									});
				buttonPanel.add(uploadButton);
			topPanel.add(buttonPanel);
			
		add("North", topPanel);
		
			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new FixedSizeLayout(400, 40));
				finishedLabel = new JLabel("", Label.LEFT);
				finishedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
				finishedLabel.setForeground(Color.blue);
			messagePanel.add(finishedLabel);
		add("Center", messagePanel);
			
			JPanel progressPanel = new JPanel();
			progressPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, 10));
			
				uploadStageProgress = new CastProgressBar("Upload stage");
			progressPanel.add(uploadStageProgress);
			
				uploadItemProgress = new CastProgressBar("Item progress");
			progressPanel.add(uploadItemProgress);
			
		add("South", progressPanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
	}
	
	
	private void uploadFiles(boolean uploadTheManagerJar, boolean uploadJavaJar) {
		HashSet<String> bookDirs = new HashSet<String>();
		
		File coreDir = new File(castSourceDir, "core");
		File booksDir = new File(coreDir, "bk");
		File[] bookDir = booksDir.listFiles(new FilenameFilter() {
																						public boolean accept(File dir, String name) {
																							return new File(dir, name).isDirectory();
																						}
																					});
		for (int i=0 ; i<bookDir.length ; i++)
			bookDirs.add(bookDir[i].getName());
			
		FTPClient ftpClient = createFtpClient();
		if (ftpClient == null) {
			finishedLabel.setText("Error!!!  Could not connect to FTP server.");
			return;
		}
		
		finishedLabel.setText("Uploading ...");
		
		uploadTask = new UpdateServerTask(castSourceDir, uploadTheManagerJar, uploadJavaJar,
																	"http://" + Options.kCastDownloadUrl, bookDirs, ftpClient, finishedLabel,
																	uploadStageProgress, uploadItemProgress, this);
		uploadTask.execute();
	}
	
	private FTPClient createFtpClient() {
		String serverName = serverNameEdit.getText();
		String castPath = castPathEdit.getText();
		String userName = userNameEdit.getText();
		@SuppressWarnings("deprecation")
		String password = passwordEdit.getText();
		
		FTPClient ftpClient = new FTPClient();
		
		try{
			ftpClient.connect(serverName);
			if (!ftpClient.login(userName, password))
				throw new IOException("Supplied wrong credentials to FTP Server");
			
			ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.setBufferSize(1024000);
			
			int lastSlashIndex = castPath.lastIndexOf("/");
			String castFolderName = castPath.substring(lastSlashIndex + 1);
			if (lastSlashIndex > 0) {
				String castParentPath = castPath.substring(0, lastSlashIndex);
				ftpClient.changeWorkingDirectory(castParentPath);
			}
			
			FTPFile[]	directories = ftpClient.listDirectories();
			boolean directoryExists = false;
			for (int i=0 ; i<directories.length ; i++)
				if (directories[i].getName().equals(castFolderName)) {
					directoryExists = true;
					break;
				}
			if (!directoryExists)
				ftpClient.makeDirectory(castFolderName);
			
			ftpClient.changeWorkingDirectory(castFolderName);
			
			return ftpClient;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void enableUploadButton() {
		uploadButton.setEnabled(true);
	}
}
