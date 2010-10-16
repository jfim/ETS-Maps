package im.jeanfrancois.etsmaps;

import com.google.inject.Guice;
import com.google.inject.Injector;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;


/**
 * ETS Maps Applet
 *
 * @author jfim
 */
public class EtsMapsApplet extends JApplet {
	public EtsMapsApplet() throws HeadlessException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Don't care
		}
	}

	@Override
	public void init() {
		super.init();

		// Create Guice injector
		Injector injector = Guice.createInjector(new EtsMapsModule());

		ExceptionDisplayer displayer = injector.getInstance(ExceptionDisplayer.class);

		try {
			// Set layout
			setLayout(new MigLayout("", "[grow, fill]", "[grow, fill]"));

			// Add single map display component
			add((JComponent) injector.getInstance(MapDisplayComponent.class));
		} catch (RuntimeException re) {
			displayer.displayException(re);
		}
	}
}
