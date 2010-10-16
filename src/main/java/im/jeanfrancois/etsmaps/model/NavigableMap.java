package im.jeanfrancois.etsmaps.model;

import im.jeanfrancois.etsmaps.model.Landmark;

import java.util.List;

/**
 * Document me!
 *
 * @author jfim
 */
public interface NavigableMap {
	public List<? extends Landmark> getLandmarks();
}
