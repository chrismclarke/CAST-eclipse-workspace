package cast.utils;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class FileFinder {
	
	static final private String kBadCastFolderMessage = "The customisation program cannot continue since you have not chosen a CAST folder.";
	static final private String kNoExerciseXmlFolderMessage = "There is no folder with the XML exercises definitions in CAST/core/exercises.";
	static final private String kBadPluginFolderMessage = "You have not chosen a folder for a Moodle CAST question-type plug-in.";
	static final private String kBadVariationFileMessage = "You have not chosen an XML file (with extention \".xml\").";
	
	static public File getCastFolder(JFrame startFrame) {
		boolean capsLockDown = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		File defaultCastDir = new File(".");		//	for when program is run from CAST folder
		if (!isValidCastDir(defaultCastDir))
			defaultCastDir = new File("../..", "CAST");		//	for when program is run from executable in project folder
		
		if (!capsLockDown && isValidCastDir(defaultCastDir))
			return defaultCastDir;
		else {
			File castDir = queryCastFolder(startFrame, defaultCastDir);
			if (castDir != null)
				return castDir;
		}
		
		showErrorMessage(kBadCastFolderMessage);
		return null;
	}
	
	static public File queryCastFolder(Component caller, File defaultFolder) {
		if (!defaultFolder.exists())
			defaultFolder = null;
		
		JFileChooser fc = new JFileChooser(defaultFolder);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select CAST folder");
		fc.setFileHidingEnabled(true);

//	Show open dialog; this method does not return until the dialog is closed
		int result = fc.showOpenDialog(caller);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				File castDir = fc.getSelectedFile();
				
				if (isValidCastDir(castDir))
					return castDir;
				else
					break;
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
			default:
				break;
		}
		return null;
	}
	
	static public boolean isValidCastDir(File castDir) {
		try {
			/*
			String[] filenames = castDir.getCanonicalPath().split(File.separator);
			if (!castDir.isDirectory() || !filenames[filenames.length - 1].startsWith("CAST"))
				return false;
*/
			
			if (!castDir.isDirectory() || !castDir.getCanonicalFile().getName().startsWith("CAST"))
				return false;
			
			File coreDir = new File(castDir, "core");
			if (!coreDir.isDirectory() || !coreDir.exists())
				return false;
			
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	
	static public File getExerciseXmlFolder(File castDir, JFrame startFrame) {
		File coreDir = new File(castDir, "core");
		File exercisesDir = new File(coreDir, "exercises");
		File exerciseXmlDir = new File(exercisesDir, "xml");
		if (exerciseXmlDir.exists() && exerciseXmlDir.isDirectory()) {
			File topicsFile = new File(exerciseXmlDir, "topics.xml");
			if (topicsFile.exists())
				return exerciseXmlDir;
		}
		
		showErrorMessage(kNoExerciseXmlFolderMessage);
		return null;
	}
	
	
	static public File getTestPluginFolder(JFrame startFrame) {
		boolean capsLockDown = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		File defaultPluginDir = new File("../MoodleTests", "cast");		//	for when program is run from CAST folder
		if (!isValidCastDir(defaultPluginDir))
			defaultPluginDir = new File("../../MoodleTests", "cast");		//	for when program is run from executable in project folder
		
		if (!capsLockDown && isValidCastDir(defaultPluginDir))
			return defaultPluginDir;
		else {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Select folder for CAST question-type plug-in");
			fc.setFileHidingEnabled(true);

//	Show open dialog; this method does not return until the dialog is closed
			int result = fc.showOpenDialog(startFrame);
		
			switch (result) {
				case JFileChooser.APPROVE_OPTION:
					File pluginDir = fc.getSelectedFile();
					if (isValidPluginDir(pluginDir))
						return pluginDir;
					else
						break;
				case JFileChooser.CANCEL_OPTION:
				case JFileChooser.ERROR_OPTION:
				default:
					break;
			}
		}
		
		showErrorMessage(kBadPluginFolderMessage);
		return null;
	}
	
	static private boolean isValidPluginDir(File pluginDir) {
		String filename = pluginDir.getName();
		if (!pluginDir.isDirectory() || !filename.equals("cast"))
			return false;
		
		File questionDir = new File(pluginDir, "questionData");
		if (!questionDir.exists())
			return false;
		
		return true;
	}
	
	
	static public File getCustomVariationsFile(JFrame startFrame, File previousFile) {
		JFileChooser fc = new JFileChooser(previousFile);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Pick XML file with question variations");
		fc.setFileHidingEnabled(true);

		int result = fc.showOpenDialog(startFrame);		// Show open dialog; this method does not return until the dialog is closed
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				File pageFile = fc.getSelectedFile();
				
				String filename = pageFile.getName();
				if (!filename.endsWith(".xml"))
					break;
				return pageFile;
				
			case JFileChooser.CANCEL_OPTION:
				return null;
			case JFileChooser.ERROR_OPTION:
			default:
				break;
		}
		
		showErrorMessage(kBadVariationFileMessage);
		return null;
	}
	
	
	
	static private void showErrorMessage(String message) {
		JOptionPane jop = new JOptionPane();
		JOptionPane.showMessageDialog(jop, message);
	}
	
	static public PrintWriter createUTF8Writer(File f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF8"));
		return new PrintWriter(bw);
	}
	
}
