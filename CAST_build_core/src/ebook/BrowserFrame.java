package ebook;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import utils.*;


public class BrowserFrame extends JFrame {
	static final private Font kHeadingFont = new Font("Arial", Font.BOLD, 24);
	static final private Color kHeadingBackground = new Color(0xDDDDDD);
	static final private Color kLineColor = new Color(0x999999);
	static final private int kStartWidth = 500;
	static final private int kStartHeight = 800;
	
//	static final private String kStartFile = "start_test";
	
	static private BrowserFrame browserFrame = null;
	
	static public void showBrowserFrame(String testName, String heading, String url, TestSetupDialog setupDialog) {
/*
		try {
		    java.awt.Desktop.getDesktop().browse(new URI(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
*/
		if (browserFrame == null) {
			browserFrame = new BrowserFrame(testName, heading, url, setupDialog);
			browserFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
//			browserFrame.setLocationRelativeTo(parent);
			browserFrame.setVisible(true);
		}
		else {
			browserFrame.changeWebPage(heading, url, setupDialog);
			browserFrame.setVisible(true);
		}
	}
	
	static public void hideBrowserFrame() {
		if (browserFrame != null)
			browserFrame.setVisible(false);
	}
	
//----------------------------------
	
	private JLabel headingLabel;
	private JFXPanel browserPanel;
	private WebView webView;
	private TestSetupDialog setupDialog;
	private BigButton useTokenButton;
	
	private BrowserFrame(String testName, String heading, final String theUrl, TestSetupDialog startDialogParam) {
		super(testName);
		setupDialog = startDialogParam;
		
		setLayout(new BorderLayout(0, 20));
		setBackground(kHeadingBackground);
		setSize(kStartWidth, kStartHeight);
		getContentPane().setBackground(Color.white);
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 20));
			Border spacingBorder = BorderFactory.createEmptyBorder(10, 0, 10, 0);
			Border bottomLineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, kLineColor);
			topPanel.setBorder(BorderFactory.createCompoundBorder(bottomLineBorder, spacingBorder));
			
				headingLabel = new JLabel(heading, JLabel.CENTER);
				headingLabel.setFont(kHeadingFont);
			topPanel.add(headingLabel);
			
				useTokenButton = new BigButton("Use token for test");
				useTokenButton.addMouseListener(new MouseAdapter() {
																		public void mouseClicked(MouseEvent e) {
																					Platform.runLater(new Runnable() {		//	run on the JavaFX thread
																																	public void run() {
																																		copyTokenToDialog();
																																	}
																					});
																		}
																	});
				
				useTokenButton.setVisible(setupDialog != null);
			topPanel.add(useTokenButton);
				
		add("North", topPanel);
		
			browserPanel = new JFXPanel();
			browserPanel.setBackground(Color.white);
		add("Center", browserPanel);

		Platform.runLater(new Runnable() {		//	run on the JavaFX thread
														public void run() {
															initFX(browserPanel);
															webView.getEngine().load(theUrl);
														}
		});
		
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
				if (setupDialog != null)
					setupDialog.setButtonEnable(true);
        setVisible(false);
      }
    });
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	private void initFX(JFXPanel browserPanel) {
		// This method is invoked on the JavaFX thread
		webView = new WebView();
		browserPanel.setScene(new Scene(webView));
	}
	
	public void changeWebPage(String heading, final String newUrl, TestSetupDialog setupDialog) {
		headingLabel.setText(heading);
		useTokenButton.setVisible(setupDialog != null);
		this.setupDialog = setupDialog;

		Platform.runLater(new Runnable() {		//	run on the JavaFX thread
														public void run() {
															webView.getEngine().load(newUrl);
														}
		});
	}
	
/*
	private boolean isStartFile(String urlString) {
		int lastPhp = urlString.lastIndexOf(".php");
		int lastSlash = urlString.substring(0, lastPhp).lastIndexOf("/");
		String fileName = urlString.substring(lastSlash + 1, lastPhp);
		return fileName.equals(kStartFile);
	}
*/
	
	private void copyTokenToDialog() {		//	 on JavaFx thread
		@SuppressWarnings("unused")
		String currentUrl = (String)webView.getEngine().executeScript("window.location.href");
		String token = (String)webView.getEngine().executeScript("window.token");
		if ("undefined".equals(token)) {
			SwingUtilities.invokeLater(new Runnable() {		//	back on Swing thread
										public void run() {
											JOptionPane.showMessageDialog(BrowserFrame.this, "You must login to find a token before you can start the test.",
																																			"Error!", JOptionPane.ERROR_MESSAGE);
										}
			});
		}
		else {
			setupDialog.setButtonEnable(true);
			setupDialog.startTest(token);
			setVisible(false);
		}
	}
	
/*
	private void reloadPage() {
		JSObject window = (JSObject) webView.getEngine().executeScript("window");
		window.call("reload", new Boolean(true));
	}
*/
	
}