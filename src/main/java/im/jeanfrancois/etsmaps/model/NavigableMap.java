package im.jeanfrancois.etsmaps.model;

import java.util.List;


/**
 * Map interface that contains all the necessary information to
 * determine the landmarks in a loaded map and navigating between them.
 *
 * @author jfim
 */
public interface NavigableMap {
	public List<? extends Landmark> getLandmarks();
	public Route getRouteBetweenLandmarks(Landmark origin, Landmark destination);
}
