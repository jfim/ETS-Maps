package im.jeanfrancois.etsmaps.ui;

import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.Route;

/**
 * Interface implemented by map display components.
 *
 * @author jfim
 */
public interface MapDisplayComponent {
	public void overlayRoute(Route route);
    public void setDebugStuffVisible(boolean debugStuffVisible);
    public void setOrigin(Landmark origin);
    public void setDestination(Landmark destination);
}
