package im.jeanfrancois.etsmaps;

import com.google.inject.Guice;
import com.google.inject.Injector;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import im.jeanfrancois.etsmaps.ui.NavigationPanel;
import im.jeanfrancois.etsmaps.ui.StatusDisplayerComponent;
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
        final Injector injector = Guice.createInjector(new EtsMapsModule());

        final ExceptionDisplayer displayer = injector.getInstance(ExceptionDisplayer.class);

        final StatusDisplayerComponent statusDisplayerComponent = injector.getInstance(StatusDisplayerComponent.class);

        // Lay out empty interface
        setLayout(new MigLayout("wrap 2", "[][grow, fill]", "[grow, fill][]"));
        JPanel dummyNavigationPanel = new JPanel();
        JPanel dummyMapDisplayPanel = new JPanel();

        getContentPane().add(dummyNavigationPanel, "width 310");
        getContentPane().add(dummyMapDisplayPanel, "spany 2");
        getContentPane().add(statusDisplayerComponent, "span, alignx left");

        statusDisplayerComponent.setBusy(true);
        statusDisplayerComponent.setStatus("Chargement des cartes...");

        // Load the navigation stuff in another thread
        new Thread() {
            public void run() {
                try {
                    final NavigationPanel navigationPanel = injector.getInstance(NavigationPanel.class);
                    final JComponent mapDisplayPanel = (JComponent) injector.getInstance(MapDisplayComponent.class);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getContentPane().remove(0);
                            getContentPane().add(navigationPanel, "width 310", 0);
                            getContentPane().remove(1);
                            getContentPane().add(mapDisplayPanel, "spany 2", 1);
                            getContentPane().validate();

                            statusDisplayerComponent.setBusy(false);
                        }
                    });
                } catch (RuntimeException re) {
                    displayer.displayException(re);
                }
            }
        }.start();
    }
}
