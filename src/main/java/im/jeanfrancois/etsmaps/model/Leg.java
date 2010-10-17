package im.jeanfrancois.etsmaps.model;

import java.awt.geom.Point2D;

/**
 * A leg is a single direction used to build a path between landmarks.
 * A sample leg might be "Walk 20 metres southwards."
 *
 * @author jfim
 */
public interface Leg {
	public String getDescription();

	public float getLengthInMetres();

	public Point2D getOrigin();

	public Point2D getDestination();
}
