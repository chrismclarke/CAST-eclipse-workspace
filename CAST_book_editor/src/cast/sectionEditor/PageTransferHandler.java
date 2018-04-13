package cast.sectionEditor;

import java.awt.datatransfer.*;

import javax.swing.*;


public class PageTransferHandler extends TransferHandler {
	public Transferable createTransferable(JComponent c) {
		if (c instanceof CorePagePanel)
			return ((CorePagePanel)c).getPageDom();
		else
			return null;
	}
	
	public int getSourceActions(JComponent c) {
		if (c instanceof CorePagePanel)
			return TransferHandler.COPY;
		else
			return TransferHandler.NONE;
	}
}