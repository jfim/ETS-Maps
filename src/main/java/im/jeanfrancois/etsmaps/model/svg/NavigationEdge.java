package im.jeanfrancois.etsmaps.model.svg;

/**
 * Graph edge used for navigation.
 *
 * @author jfim
 */
public class NavigationEdge {
	private NavigationNode first;
	private NavigationNode second;

	public NavigationEdge(NavigationNode first, NavigationNode second) {
		this.first = first;
		this.second = second;
	}

	public NavigationNode getFirst() {
		return first;
	}

	public NavigationNode getSecond() {
		return second;
	}

	public float getSquaredDistanceFromPoint(float pointX, float pointY) {
		// http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
		// u = ((x3-x1)(x2-x1) + (y3-y1)(y2-y1)) / ((x2-x1)(x2-x1)+(y2-y1)(y2-y1))
		float dx21 = second.getX() - first.getX();
		float dy21 = second.getY() - first.getY();
		float dx31 = pointX - first.getX();
		float dy31 = pointY - first.getY();

		float u = ((dx31 * dx21) + (dy31 * dy21)) / ((dx21 * dx21) +
				(dy21 * dy21));

		// Clamp to [0;1]
		if (u < 0.0f) {
			u = 0.0f;
		} else if (1.0f < u) {
			u = 1.0f;
		}

		float closestX = first.getX() + (u * dx21);
		float closestY = first.getY() + (u * dy21);

		float dx = closestX - pointX;
		float dy = closestY - pointY;

		return (dx * dx) + (dy * dy);
	}
}
