package im.jeanfrancois.etsmaps.model;

/**
 * A route is a path between two landmarks and is composed of one or more legs.
 *
 * @author jfim
 */
public interface Route {
	public int getLegCount();
	public Leg getLeg(int index);
}
