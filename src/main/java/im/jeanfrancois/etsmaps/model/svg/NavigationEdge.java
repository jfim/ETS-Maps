package im.jeanfrancois.etsmaps.model.svg;

import java.awt.geom.Point2D;

/**
 * Graph edge used for navigation.
 *
 * @author jfim
 */
public class NavigationEdge {
	private NavigationNode first;
	private NavigationNode second;
	private float length;

	public NavigationEdge(NavigationNode first, NavigationNode second) {
		this.first = first;
		this.second = second;

		float dx = first.getX() - second.getX();
		float dy = first.getY() - second.getY();
		length = (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	public NavigationNode getFirst() {
		return first;
	}

	public float getLength() {
		return length;
	}

	public NavigationNode getSecond() {
		return second;
	}

	public float getSquaredDistanceFromPoint(float pointX, float pointY) {
		// http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
		// u = ((x3-x1)(x2-x1) + (y3-y1)(y2-y1)) / ((x2-x1)(x2-x1)+(y2-y1)(y2-y1))

        Point2D.Float closestPoint = getClosestPointOnEdge(pointX, pointY);
        float dx = (float) (closestPoint.getX() - pointX);
        float dy = (float) (closestPoint.getY() - pointY);
		return (dx * dx) + (dy * dy);
	}

    public Point2D.Float getClosestPointOnEdge(float pointX, float pointY) {
        final float firstX = first.getX();
        final float firstY = first.getY();
        final float secondX = second.getX();
        final float secondY = second.getY();

        float dx21 = secondX - firstX;
        float dy21 = secondY - firstY;
        float dx31 = pointX - firstX;
        float dy31 = pointY - firstY;

        float u = ((dx31 * dx21) + (dy31 * dy21)) / ((dx21 * dx21) +
                (dy21 * dy21));

        // Clamp to [0;1]
        if (u < 0.0f) {
            u = 0.0f;
        } else if (1.0f < u) {
            u = 1.0f;
        }

        float closestX = firstX + (u * dx21);
        float closestY = firstY + (u * dy21);

        return new Point2D.Float(closestX, closestY);
    }
}
