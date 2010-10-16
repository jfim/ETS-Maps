package im.jeanfrancois.etsmaps.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.google.inject.Inject;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;


/**
 * Navigation panel that contains all the UI controls to search for
 * landmarks and navigate between them.
 *
 * @author jfim
 */
public class NavigationPanel extends JPanel {
	private NavigableMap map;

	@Inject
	public NavigationPanel(NavigableMap map) {
		this.map = map;
		setLayout(new MigLayout("wrap 2"));

		EventList<Landmark> landmarks = new BasicEventList<Landmark>();
		landmarks.addAll(map.getLandmarks());

		add(new JLabel("From"));
		add(new JComboBox(new EventComboBoxModel<Landmark>(landmarks)));
		add(new JLabel("To"));
		add(new JComboBox(new EventComboBoxModel<Landmark>(landmarks)));

		final JButton button = new JButton("Navigate");
		add(button, "span 2");
	}
}
