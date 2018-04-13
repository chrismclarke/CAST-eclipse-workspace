package cast.bookEditor;

import java.awt.datatransfer.*;

import javax.swing.*;


public class ElementTransferHandler extends TransferHandler {
	public Transferable createTransferable(JComponent c) {
		if (c instanceof ElementTitle)
			return ((ElementTitle)c).getDomElement();
		else
			return null;
	}
	
	public int getSourceActions(JComponent c) {
		if (c instanceof ElementTitle)
			return TransferHandler.COPY;
		else
			return TransferHandler.NONE;
	}
}