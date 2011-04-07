package im.jeanfrancois.etsmaps.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
import com.google.inject.Inject;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.Leg;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.Route;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * Navigation panel that contains all the UI controls to search for
 * landmarks and navigate between them.
 *
 * @author jfim
 */
public class NavigationPanel extends JPanel {
	private EventList<Leg> routeLegs = new BasicEventList<Leg>();

	@Inject
	public NavigationPanel(final NavigableMap map, final MapDisplayComponent mapDisplayComponent) {
		setLayout(new MigLayout("wrap 2", "[][grow, fill]", "[][][][][][grow,fill]"));

        add(new JLabel(new ImageIcon(NavigationPanel.class.getClassLoader().getResource("etsmaps-logo.png"))), "span");

		EventList<Landmark> landmarks = new BasicEventList<Landmark>();
		landmarks.addAll(map.getLandmarks());
        EventList<Landmark> sortedLandmarks = new SortedList<Landmark>(landmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark o1, Landmark o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

		add(new JLabel("De"));

		final JComboBox originComboBox = new JComboBox(new EventComboBoxModel<Landmark>(sortedLandmarks));
		add(originComboBox);

        AutoCompleteDecorator.decorate(originComboBox);

		add(new JLabel("Vers"));

		final JComboBox destinationComboBox = new JComboBox(new EventComboBoxModel<Landmark>(sortedLandmarks));
		add(destinationComboBox);

        AutoCompleteDecorator.decorate(destinationComboBox);

		final JButton button = new JButton("Naviguer");
		add(button, "span 2, alignx right");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Route route = map.getRouteBetweenLandmarks((Landmark) originComboBox.getSelectedItem(),
						(Landmark) destinationComboBox.getSelectedItem());

				ArrayList<Leg> legs = new ArrayList<Leg>();

				for (int i = 0; i < route.getLegCount(); ++i) {
					legs.add(route.getLeg(i));
				}

				ReadWriteLock lock = routeLegs.getReadWriteLock();
				lock.writeLock().lock();
				routeLegs.clear();
				routeLegs.addAll(legs);
				lock.writeLock().unlock();

				mapDisplayComponent.overlayRoute(route);
			}
		});

		add(new JLabel("Directions"), "span 2");

		final JTable table = new JTable(new EventTableModel<Leg>(routeLegs,
				new TableFormat<Leg>() {
					public int getColumnCount() {
						return 2;
					}

					public String getColumnName(int i) {
                        if(i == 0)
                            return "Direction";
                        return "Distance";
					}

					public Object getColumnValue(Leg leg, int i) {
						if (i == 0) {
							return leg.getDescription();
						}

						NumberFormat formatter = new DecimalFormat("0.0");
						return formatter.format(leg.getLengthInMetres()) + " m";
					}
				}));
		add(new JScrollPane(table), "width 100%, span 2");
	}
}
