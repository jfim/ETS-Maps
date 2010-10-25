package im.jeanfrancois.etsmaps.model.svg;

/**
 * Graph node used for navigation.
 *
 * @author jfim
 */
public class NavigationNode {
	private float x;
	private float y;

	public NavigationNode(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getSquaredDistanceFrom(float otherX, float otherY) {
		float dx = x - otherX;
		float dy = y - otherY;

		return (dx * dx) + (dy * dy);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
