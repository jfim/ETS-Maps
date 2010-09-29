package im.jeanfrancois.etsmaps;

import com.google.inject.Singleton;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import java.awt.*;
import java.util.logging.Level;

/**
 * Utility class to centralize exception display management.
 *
 * @author jfim
 */
@Singleton
public class ExceptionDisplayer {
	public void displayException(Throwable t, Component parent) {
		t.printStackTrace();

		JXErrorPane errorPane = new JXErrorPane();
		ErrorInfo errorInfo = new ErrorInfo("Error", "An unhandled exception has occurred during program execution.", null, "No category", t, Level.SEVERE, null);
		errorPane.setErrorInfo(errorInfo);
		JXErrorPane.createDialog(parent, errorPane).setVisible(true);
	}

	public void displayException(Throwable t) {
		displayException(t, null);
	}
}
