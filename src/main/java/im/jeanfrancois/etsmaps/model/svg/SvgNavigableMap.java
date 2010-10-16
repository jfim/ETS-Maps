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
	private ArrayList<SvgLandmark> landmarks = new ArrayList<SvgLandmark>();
	private ExceptionDisplayer exceptionDisplayer;
	private SVGDiagram diagram;

	@Inject
	public SvgNavigableMap(ExceptionDisplayer exceptionDisplayer) {
		// Create the SVG universe to load the SVG files
		SVGUniverse universe = new SVGUniverse();

		// Load a hardcoded diagram
		try {
			diagram = universe.getDiagram(SvgMapComponent.class.getResource("/Bolduc House Floor Plan.svg")
					.toURI(), true);
		} catch (URISyntaxException e) {
			this.exceptionDisplayer.displayException(e);
		}

		// Extract landmarks from the map and remove their visual representation from the diagram
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

	public SVGDiagram getDiagram() {
		return diagram;
	}

	public List<? extends Landmark> getLandmarks() {
		return landmarks;
	}

	public Route getRouteBetweenLandmarks(Landmark origin, Landmark destination) {
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
	}
}
