package ebook;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebookStructure.*;


class TestInformation {
	int randomSeed, activityId, hash, reducedMinutes;
	int mode;
	boolean manualToken;
	String token;
	
	TestInformation(int randomSeed, int mode, int activityId, String token, int hash, int reducedMinutes, boolean manualToken) {
		this.randomSeed = randomSeed;
		this.mode = mode;
		this.activityId = activityId;
		this.token = token;
		this.hash = hash;
		this.reducedMinutes = reducedMinutes;
		this.manualToken = manualToken;
	}
}

class Token {
	int randomSeed, activityId, hash, reducedMinutes=-1;
	boolean formatOk = true;
	
	Token(String token) {
		if (token.length() <= 16)
				formatOk = false;
		else
			try {
				int hashIndex = token.indexOf("#");
				if (hashIndex > 16) {
					reducedMinutes = Integer.parseInt(token.substring(hashIndex + 1));
					token = token.substring(0, hashIndex);
				}
				activityId = Integer.parseInt(token.substring(16));
				
				String seedString = "";
				String hashString = "";
				for (int i=0 ; i<8 ; i++) {
					seedString += token.substring(i*2, i*2 + 1);
					hashString += token.substring(i*2 + 1, i*2 + 2);
				}
				randomSeed = Integer.parseInt(seedString, 16);
				hash = Integer.parseInt(hashString, 16);
				hash = hash ^ randomSeed;
			} catch (NumberFormatException e) {
				formatOk = false;
			}
	}
}

public class TestSetupDialog extends JDialog {
	static final private Color kGreyColor = new Color(0xAAAAAA);
	static final private String kServerPattern = "<test [^>]*moodleServer=['\"](.*?)['\"]";
	
	static public TestInformation getTestInfo(Frame parent, File testXmlFile) {
		TestSetupDialog dialog = new TestSetupDialog(parent, testXmlFile);

		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		dialog.dispose();
		BrowserFrame.hideBrowserFrame();

		if (dialog.ok) {
			int mode = dialog.practiceMode ? TestFrame.PRACTICE_MODE : TestFrame.TEST_MODE;
			return new TestInformation(dialog.randomSeed, mode, dialog.activityId, dialog.token, dialog.hash, dialog.reducedMinutes, dialog.manualToken);
		}
		else
			return null;
	}
	
	
	static public String changeToHtml(String s, int width) {
		return "<html><div style='width:" + width + "px;'>" + s + "</div></html>";
	}
	
	
	static public int getTestFileHash(File testXmlFile) {
		String xmlFile = XmlHelper.getFileAsString(testXmlFile);
		int hash = xmlFile.hashCode();
		if (hash < 0)
			hash = -hash;
		return hash;
	}
	
//	-------------------------------------------------
	
	private File testXmlFile;
	
	private String hashString, testName, serverPath;
	
	private JButton cancelButton;
	private BigButton practiceButton, getTokenButton, manualTokenButton;
	
	private TextField manualTokenEdit;
	
	private int randomSeed, activityId, hash, reducedMinutes;
	private boolean practiceMode, manualToken = false;
	private String token;
	private boolean ok = false;
	
	public TestSetupDialog(Frame parent, File testXmlFileParam) {
		super(parent, "", true);
		this.testXmlFile = testXmlFileParam;
		getContentPane().setBackground(Color.white);
		
		String testString = XmlHelper.getFileAsString(testXmlFile);
		Pattern testNamePattern = Pattern.compile(BannerNavigation.kTestNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher testNameMatcher = testNamePattern.matcher(testString);
		if (testNameMatcher.find()) {
			testName = testNameMatcher.group(1);
			setTitle("Setup for test \"" + testName + "\"");
		}
		
		Pattern serverPattern = Pattern.compile(kServerPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher serverMatcher = serverPattern.matcher(testString);
		if (serverMatcher.find())
			serverPath = serverMatcher.group(1);
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
			JPanel hashPanel = new JPanel();
			hashPanel.setOpaque(false);
			hashPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			hashPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
				hashString = Integer.toHexString(getTestFileHash(testXmlFile));
				while (hashString.length() < 8)
					hashString = "0" + hashString;
				JLabel hashLabel = new JLabel("(" + hashString + ")", JLabel.LEFT);
				hashLabel.setForeground(kGreyColor);
			hashPanel.add(hashLabel);
		add(hashPanel);
		
			JPanel practicePanel = new JPanel();
			practicePanel.setOpaque(false);
			Border underlineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
			Border spacingBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
			practicePanel.setBorder(BorderFactory.createCompoundBorder(underlineBorder, spacingBorder));
			practicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
				
				practiceButton = new BigButton("Run test in practice mode", 20, 12);
				practiceButton.addMouseListener(new MouseAdapter() {
																		public void mouseClicked(MouseEvent e) {
																			if (practiceButton.isEnabled()) {
																				practiceMode = true;
																				ok = true;
																				setVisible(false);
																			}
																		}
																	});

			practicePanel.add(practiceButton);
			
				JLabel practiceComment = new JLabel(changeToHtml("If you run the test in this way, your results will not be saved.", 400));
			practicePanel.add(practiceComment);
			
		add(practicePanel);
		
			JPanel moodleTokenPanel = new JPanel();
			moodleTokenPanel.setOpaque(false);
			Border underlineBorder2 = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
			Border spacingBorder2 = BorderFactory.createEmptyBorder(20, 20, 20, 20);
			moodleTokenPanel.setBorder(BorderFactory.createCompoundBorder(underlineBorder2, spacingBorder2));
			moodleTokenPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
			
				JLabel moodleComment = new JLabel(changeToHtml("To run the test and save your results, you must obtain"
																+ " a token from Moodle. The easiest way is to click the button below.", 400));
			moodleTokenPanel.add(moodleComment);
				
				getTokenButton = new BigButton("Get token", 20, 12);
				getTokenButton.addMouseListener(new MouseAdapter() {
																		public void mouseClicked(MouseEvent e) {
																			if (getTokenButton.isEnabled()) {
																				String testFileString = HtmlHelper.getFileAsString(testXmlFile);
		
																				Pattern courseNamePattern = Pattern.compile("course='(.*?)'", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
																				Matcher courseNameMatcher = courseNamePattern.matcher(testFileString);
																				if (!courseNameMatcher.find())
																					throw new RuntimeException("Bad format for test XML file");
																				String courseName = courseNameMatcher.group(1);
																				
																				String url = serverPath + TestFrame.kMoodlePathToModule
																													+ "/start_test.php?filehash=" + hashString
																													+ "&coursename=" + courseName;
																				String heading = "Finding token to start test...";
																				BrowserFrame.showBrowserFrame(testName, heading, url, TestSetupDialog.this);
																				setButtonEnable(false);
																			}
																		}
																	});

			moodleTokenPanel.add(getTokenButton);
			
				JLabel moodleComment2 = new JLabel(changeToHtml("<strong>Warning</strong>: Do not click this button until you are ready to start"
				                             + " the test. Timing for the test will start as soon as you get the token.", 400));
			moodleTokenPanel.add(moodleComment2);
			
		add(moodleTokenPanel);
		

			JPanel manualTokenPanel = new JPanel();
			manualTokenPanel.setOpaque(false);
			manualTokenPanel.setBorder(BorderFactory.createCompoundBorder(underlineBorder2, spacingBorder2));
			manualTokenPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
			
				JLabel manualComment = new JLabel(changeToHtml("If you cannot get CAST to correctly communicate with Moodle"
																+ " to start the test, you will need to manually obtain a token from Moodle"
																+ " and paste it into the box below. (The test result will need to be pasted"
																+ " back into Moodle before it expires for the mark to be saved.)", 400));
			manualTokenPanel.add(manualComment);
				
				JPanel tokenEditPanel = new JPanel();
				tokenEditPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				tokenEditPanel.setOpaque(false);
					JLabel tokenLabel = new JLabel("Token for starting test: ");
				tokenEditPanel.add(tokenLabel);
					manualTokenEdit = new TextField(20);
				tokenEditPanel.add(manualTokenEdit);
				
			manualTokenPanel.add(tokenEditPanel);
				
				manualTokenButton = new BigButton("Use token", 20, 12);
				manualTokenButton.addMouseListener(new MouseAdapter() {
																		public void mouseClicked(MouseEvent e) {
																			if (manualTokenButton.isEnabled()) {
																				manualToken = true;
																				startTest(manualTokenEdit.getText());
																			}
																		}
																	});

			manualTokenPanel.add(manualTokenButton);
			
				JLabel manualComment2 = new JLabel(changeToHtml("<strong>Warning</strong>: When the test finishes, you will need to"
				                             + " paste the encoded result back into Moodle fairly quickly.", 400));
			manualTokenPanel.add(manualComment2);
			
		add(manualTokenPanel);

			
			JPanel cancelPanel = new JPanel();
			cancelPanel.setOpaque(false);
			cancelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			cancelPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
			
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = false;
																					setVisible(false);
																				}
																		});
			cancelPanel.add(cancelButton);
			
		add(cancelPanel);
		
		pack();
		setResizable(false);
	}
	
	public void startTest(String token) {
		practiceMode = false;
		if (checkToken(token, testXmlFile)) {
			ok = true;
			setVisible(false);
		}
		else
			JOptionPane.showMessageDialog(TestSetupDialog.this, "Bad test token.", "Error!", JOptionPane.ERROR_MESSAGE);
	}
	
	private boolean checkToken(String token, File testXmlFile) {
		int fileHashCode = getTestFileHash(testXmlFile);
		
		Token parsedToken = new Token(token);
		if (parsedToken.formatOk && parsedToken.hash == fileHashCode) {
			activityId = parsedToken.activityId;
			randomSeed = parsedToken.randomSeed;
			hash = parsedToken.hash;
			this.token = token;
			reducedMinutes = parsedToken.reducedMinutes;
			return true;
		}
		else
			return false;
	}
	
	public void setButtonEnable(boolean enable) {
		cancelButton.setEnabled(enable);
		practiceButton.setEnabled(enable);
		getTokenButton.setEnabled(enable);
		manualTokenButton.setEnabled(enable);
	}
	
}
