package im.jeanfrancois.etsmaps.model.svg;

import annas.graph.DefaultArc;
import annas.graph.DefaultWeight;
import annas.graph.GraphPath;
import annas.graph.UndirectedGraph;
import annas.graph.util.Dijkstra;
import com.google.inject.Inject;
import com.kitfox.svg.*;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.Leg;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.Route;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * SVG implementation of a navigable map. See the documentation as for
 * SVG-specific annotations that must be added to the SVG file to handle
 * scaling, such as size/orientation/navigation markers.
 *
 * @author fim
 */
public class SvgNavigableMap implements NavigableMap {
	private static final boolean DEBUG = true;
	private static final Logger logger = LoggerFactory.getLogger(SvgNavigableMap.class);
	private static final float MAX_SQUARED_DISTANCE = 0.1f;
	private ArrayList<NavigationEdge> edges = new ArrayList<NavigationEdge>();
	private ArrayList<SvgLandmark> landmarks = new ArrayList<SvgLandmark>();
	private ArrayList<NavigationNode> nodes = new ArrayList<NavigationNode>();
	private ExceptionDisplayer exceptionDisplayer;
	private SVGDiagram diagram;
	private UndirectedGraph<NavigationNode, DefaultArc<NavigationNode>> graph = new UndirectedGraph<NavigationNode, DefaultArc<NavigationNode>>();
	private float unitsPerMetre = 1.0f;

	@Inject
	public SvgNavigableMap(ExceptionDisplayer exceptionDisplayer) {
		this.exceptionDisplayer = exceptionDisplayer;

		// Create the SVG universe to load the SVG files
		SVGUniverse universe = new SVGUniverse();

		// Load a hardcoded diagram
		loadDiagram(universe, "/Bolduc House Floor Plan.svg");

		// Extract landmarks from the map and remove their visual representation from the diagram
		loadLandmarks();

		// Remove scale information from the map
		loadScaleInfo();

		// Remove navigation layer from the map
		loadNavigationInfo();
	}

	private void createEdge(NavigationNode first, NavigationNode second) {
		final NavigationEdge navigationEdge = new NavigationEdge(first, second);
		edges.add(navigationEdge);
		graph.addArc(first, second,
				new DefaultWeight((double) navigationEdge.getLength()));
	}

	private NavigationNode createNode(float[] point) {
		final NavigationNode node = new NavigationNode(point[0], point[1]);
		nodes.add(node);
		graph.addNode(node);

		return node;
	}

	private Group findTopLevelGroup(String groupId) {
		List topLevelElements = diagram.getRoot().getChildren(new ArrayList());

		Group group = null;

		for (Object topLevelElement : topLevelElements) {
			if (topLevelElement instanceof Group) {
				Group evaluatedGroup = (Group) topLevelElement;

				if (evaluatedGroup.getId().equalsIgnoreCase(groupId)) {
					group = evaluatedGroup;

					break;
				}
			}
		}

		return group;
	}

	private NavigationNode getClosestNodeForLandmark(Landmark landmark) {
		SvgLandmark svgLandmark = (SvgLandmark) landmark;

		NavigationNode closestNode = null;
		float closestNodeDistance = Float.MAX_VALUE;

		for (NavigationNode node : nodes) {
			float nodeDistance = node.getSquaredDistanceFrom(svgLandmark.getX(),
					svgLandmark.getY());

			if (nodeDistance < closestNodeDistance) {
				closestNodeDistance = nodeDistance;
				closestNode = node;
			}
		}

		return closestNode;
	}

	public SVGDiagram getDiagram() {
		return diagram;
	}

	public List<? extends Landmark> getLandmarks() {
		return landmarks;
	}

	private NavigationNode getNodeForCoordinates(float[] point) {
		for (NavigationNode node : nodes) {
			if (node.getSquaredDistanceFrom(point[0], point[1]) < MAX_SQUARED_DISTANCE) {
				return node;
			}
		}

		return null;
	}

	public Route getRouteBetweenLandmarks(Landmark origin, Landmark destination) {
		Dijkstra<NavigationNode, DefaultArc<NavigationNode>> dijkstra = new Dijkstra<NavigationNode, DefaultArc<NavigationNode>>(graph);
		final GraphPath<NavigationNode, DefaultArc<NavigationNode>> path = dijkstra.execute(getClosestNodeForLandmark(origin),
				getClosestNodeForLandmark(destination));
		Iterator<NavigationNode> pathIterator = path.getIterator();

		final ArrayList<NavigationNode> navigationNodes = new ArrayList<NavigationNode>();

		while (pathIterator.hasNext()) {
			NavigationNode navigationNode = pathIterator.next();
			navigationNodes.add(navigationNode);
		}

		return new Route() {
			public int getLegCount() {
				return path.size() - 1;
			}

			public Leg getLeg(final int index) {
				return new Leg() {
					public String getDescription() {
						double angle = Math.atan2(
								navigationNodes.get(index + 1).getY() - navigationNodes.get(index).getY(),
								navigationNodes.get(index + 1).getX() - navigationNodes.get(index).getX()
						);

						String heading = "";

						double x = Math.cos(angle);

						// Flip the y coordinate so it is in math coordinates, not screen coordinates
						double y = -Math.sin(angle);

						if (0.5 < y)
							heading = "North";
						else if (y < -0.5)
							heading = "South";

						if (x < -0.5) {
							if (heading.isEmpty())
								heading = "West";
							else
								heading += "west";
						} else if (0.5 < x) {
							if (heading.isEmpty())
								heading = "East";
							else
								heading += "east";
						}

						return heading;
					}

					public float getLengthInMetres() {
						return new NavigationEdge(navigationNodes.get(index),
								navigationNodes.get(index +
										1)).getLength() / unitsPerMetre;
					}

					public Point2D getOrigin() {
						return new Point2D.Float(navigationNodes.get(index)
								.getX(),
								navigationNodes.get(index)
										.getY());
					}

					public Point2D getDestination() {
						return new Point2D.Float(navigationNodes.get(index +
								1)
								.getX(),
								navigationNodes.get(index +
										1)
										.getY());
					}
				};
			}
		};
	}

	private void loadDiagram(SVGUniverse universe, String name) {
		try {
			long start = System.currentTimeMillis();

			if (DEBUG) {
				logger.debug("Loading diagram...");
			}

			diagram = universe.getDiagram(SvgMapComponent.class.getResource(name)
					.toURI(), true);

			if (DEBUG) {
				logger.debug("Loaded diagram in " +
						(System.currentTimeMillis() - start) + " ms");
			}
		} catch (URISyntaxException e) {
			this.exceptionDisplayer.displayException(e);
		}
	}

	private void loadLandmarks() {
		if (DEBUG) {
			logger.debug("Loading landmarks...");
		}

		List<SVGElement> elementsToExplore = new ArrayList<SVGElement>();
		elementsToExplore.add(diagram.getRoot());

		while (!elementsToExplore.isEmpty()) {
			// Get and remove the last element
			final int lastElementIndex = elementsToExplore.size() - 1;

			SVGElement lastElement = elementsToExplore.get(lastElementIndex);
			elementsToExplore.remove(lastElementIndex);

			if (lastElement instanceof Text) {
				try {
					lastElement.getParent().removeChild(lastElement);

					final SvgLandmark landmark = new SvgLandmark((Text) lastElement);
					landmarks.add(landmark);

					if (DEBUG) {
						logger.debug("Loaded landmark " + landmark.getName());
					}
				} catch (SVGElementException e) {
					exceptionDisplayer.displayException(e);
				}
			} else {
				elementsToExplore.addAll(lastElement.getChildren(new ArrayList<SVGElement>()));
			}
		}

		if (DEBUG) {
			logger.debug("Done loading " + landmarks.size() + " landmark(s)");
		}
	}

	private void loadNavigationInfo() {
		if (DEBUG) {
			logger.debug("Loading navigation info...");
		}

		// Find the navigation layer
		Group group = findTopLevelGroup("Navigation");

		if (group == null) {
			return;
		}

		// Remove the navigation layer from the diagram
		try {
			diagram.getRoot().removeChild(group);
		} catch (SVGElementException e) {
			exceptionDisplayer.displayException(e);
		}

		// Extract all POLYLINE and LINE elements
		List<Polyline> polylines = new ArrayList<Polyline>();
		List<Line> lines = new ArrayList<Line>();

		List<SVGElement> elementsToEvaluate = new ArrayList<SVGElement>();
		elementsToEvaluate.addAll(group.getChildren(new ArrayList()));

		while (!elementsToEvaluate.isEmpty()) {
			int lastElementIndex = elementsToEvaluate.size() - 1;
			SVGElement lastElement = elementsToEvaluate.get(lastElementIndex);
			elementsToEvaluate.remove(lastElementIndex);

			if (lastElement instanceof Polyline) {
				polylines.add((Polyline) lastElement);
			} else if (lastElement instanceof Line) {
				lines.add((Line) lastElement);
			} else {
				elementsToEvaluate.addAll(lastElement.getChildren(new ArrayList()));
			}
		}

		// For each polyline
		for (Polyline polyline : polylines) {
			PathIterator iterator = polyline.getShape()
					.getPathIterator(new AffineTransform());
			float[] currentPoint = new float[6];

			// Check if there is a node for the first point
			iterator.currentSegment(currentPoint);

			NavigationNode previousNode = getNodeForCoordinates(currentPoint);

			if (previousNode == null) {
				previousNode = createNode(currentPoint);
			}

			// For each additional point
			while (!iterator.isDone()) {
				iterator.currentSegment(currentPoint);

				// Check if there is a node for the additional point
				NavigationNode currentNode = getNodeForCoordinates(currentPoint);

				if (currentNode == null) {
					currentNode = createNode(currentPoint);
				}

				// Create an edge from the two nodes
				createEdge(previousNode, currentNode);

				previousNode = currentNode;
				iterator.next();
			}
		}

		// For each line
		// Check if there is a node for the first point
		// Check if there is a node for the second point
		// Create an edge from the two nodes
		if (DEBUG) {
			logger.debug("Loaded " + nodes.size() + " node(s) and " +
					edges.size() + " edge(s).");
		}
	}

	private void loadScaleInfo() {
		if (DEBUG) {
			logger.debug("Loading navigation info...");
		}

		// Find the scale layer
		Group group = findTopLevelGroup("Scale_1m");

		if (group == null) {
			return;
		}

		// Remove the navigation layer from the diagram
		try {
			diagram.getRoot().removeChild(group);
		} catch (SVGElementException e) {
			exceptionDisplayer.displayException(e);
		}

		// Get all LINE objects from the later
		List<Line> lines = new ArrayList<Line>();

		List<SVGElement> elementsToEvaluate = new ArrayList<SVGElement>();
		elementsToEvaluate.addAll(group.getChildren(new ArrayList()));

		while (!elementsToEvaluate.isEmpty()) {
			int lastElementIndex = elementsToEvaluate.size() - 1;
			SVGElement lastElement = elementsToEvaluate.get(lastElementIndex);
			elementsToEvaluate.remove(lastElementIndex);

			if (lastElement instanceof Line) {
				lines.add((Line) lastElement);
			} else {
				elementsToEvaluate.addAll(lastElement.getChildren(new ArrayList()));
			}
		}

		if (lines.isEmpty()) {
			return;
		}

		Line line = lines.get(0);

		PathIterator iterator = line.getShape()
				.getPathIterator(new AffineTransform());
		float[] currentPoint = new float[6];

		iterator.currentSegment(currentPoint);

		float x1 = currentPoint[0];
		float y1 = currentPoint[1];

		iterator.next();
		iterator.currentSegment(currentPoint);

		float x2 = currentPoint[0];
		float y2 = currentPoint[1];

		float dx = x2 - x1;
		float dy = y2 - y1;

		unitsPerMetre = (float) Math.sqrt((dx * dx) + (dy * dy));
	}
}
