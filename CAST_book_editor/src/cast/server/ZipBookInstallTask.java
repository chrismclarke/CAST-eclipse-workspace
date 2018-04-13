package cast.server;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;


class InstallStatus {
	String fileName = null;;
	int noOfFiles = 0;
	
	InstallStatus(int noOfFiles) {
		this.noOfFiles = noOfFiles;
	}
	
	InstallStatus(String fileName) {
		this.fileName = fileName;
	}
}


public class ZipBookInstallTask extends SwingWorker<Object, InstallStatus> {

  static final public void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }
	
	static protected void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while searching files");
		}
	}
	
	
	private File fileWithZip, booksDir;
	private ZipBookInstallPanel installPanel;
	
	private File currentFile = null;
	
	public ZipBookInstallTask(File fileWithZip, File booksDir, ZipBookInstallPanel installPanel) {
		this.fileWithZip = fileWithZip;
		this.installPanel = installPanel;
		this.booksDir = booksDir;
	}
	
	public Object doInBackground() throws Exception {
    try {
      ZipFile zipFile = new ZipFile(fileWithZip);
			
      Enumeration entries = zipFile.entries();
			
      ZipEntry firstEntry = (ZipEntry)entries.nextElement();
			String bookDirName = firstEntry.getName();
			File bookDir = new File(booksDir, bookDirName);
			if (bookDir.exists()) {
				JOptionPane.showMessageDialog(installPanel, "This e-book cannot be installed since a book called \""
																	+ bookDirName + "\" already exists.", "Error!", JOptionPane.ERROR_MESSAGE);
				zipFile.close();
				return null;
			}
			bookDir.mkdir();

			publish(new InstallStatus(zipFile.size() - 1));
			
      while(entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();
				failIfInterrupted();
				publish(new InstallStatus(entry.getName()));
				
        currentFile = new File(booksDir.getAbsolutePath() + File.separator + entry.getName());
        
        if (entry.isDirectory()) {
          currentFile.mkdirs();
          continue;
        } else {
          currentFile.getParentFile().mkdirs();
          currentFile.createNewFile();
        }

        copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(currentFile)));
				currentFile = null;
      }

      zipFile.close();
    } catch (IOException ioe) {
      System.err.println("Unhandled zip extraction exception:");
      ioe.printStackTrace();
    }
		
		return null;
	}

	public void done() {
		if (isCancelled())
			installPanel.setCancelled();
		else
			installPanel.setFinished();
	}
	
	
	protected void process(final java.util.List<InstallStatus> chunks) {
		for (final InstallStatus status : chunks) {
			if (status.fileName == null)
				installPanel.updateForStart(status.noOfFiles);
			else
				installPanel.updateForNewFile("Installing " + status.fileName);
		}
	}
}