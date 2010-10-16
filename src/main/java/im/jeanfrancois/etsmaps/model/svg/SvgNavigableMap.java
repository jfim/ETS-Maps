package im.jeanfrancois.etsmaps.model.svg;

import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.Route;

import java.util.List;


/**
 * SVG implementation of a navigable map. See the documentation as for
 * SVG-specific annotations that must be added to the SVG file to handle
 * scaling, such as size/orientation/navigation markers.
 *
 * @author jfim
 */
public class SvgNavigableMap implements NavigableMap {
	public List<? extends Landmark> getLandmarks() {
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
	}

	public Route getRouteBetweenLandmarks(Landmark origin, Landmark destination) {
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
	}
}
