package im.jeanfrancois.etsmaps.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Document me!
 *
 * @author jfim
 */
@Singleton
public class StatusDisplayerComponent extends JPanel implements StatusDisplayer {
    private JLabel reasonLabel = new JLabel(" ");
    private JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));

    @Inject
    public StatusDisplayerComponent() {
        add(busyLabel);
        add(reasonLabel);
    }

    @Override
    public void setBusy(boolean busy) {
        busyLabel.setBusy(busy);
    }

    @Override
    public void setStatus(String status) {
        reasonLabel.setText(status);
    }
}
