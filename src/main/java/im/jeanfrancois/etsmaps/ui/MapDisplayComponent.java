package im.jeanfrancois.etsmaps.ui;

import im.jeanfrancois.etsmaps.model.Route;

/**
 * Interface implemented by map display components.
 *
 * @author jfim
 */
public interface MapDisplayComponent {
	public void overlayRoute(Route route);
}
