package im.jeanfrancois.etsmaps.model.svg;

import com.google.inject.Inject;
import com.kitfox.svg.*;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.Route;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

		// Remove navigation layer from the map
		loadNavigationInfo();
	}

	private void createEdge(NavigationNode first, NavigationNode second) {
		edges.add(new NavigationEdge(first, second));
	}

	private NavigationNode createNode(float[] point) {
		final NavigationNode node = new NavigationNode(point[0], point[1]);
		nodes.add(node);

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
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
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
}
