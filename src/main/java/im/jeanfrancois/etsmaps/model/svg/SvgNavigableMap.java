package im.jeanfrancois.etsmaps.model.svg;

import com.google.inject.Inject;
import com.kitfox.svg.*;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.Route;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;

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
	private SVGDiagram diagram;
	private ExceptionDisplayer exceptionDisplayer;

	@Inject
	public SvgNavigableMap(ExceptionDisplayer exceptionDisplayer) {

		SVGUniverse universe = new SVGUniverse();

		try {
			diagram = universe.getDiagram(SvgMapComponent.class.getResource("/Bolduc House Floor Plan.svg")
					.toURI(), true);
		} catch (URISyntaxException e) {
			this.exceptionDisplayer.displayException(e);
		}

		// FIXME Test: Remove all text from the diagram
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
				} catch (SVGElementException e) {
					exceptionDisplayer.displayException(e);
				}
			} else {
				elementsToExplore.addAll(lastElement.getChildren(new ArrayList<SVGElement>()));
			}
		}
	}

	public List<? extends Landmark> getLandmarks() {
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
	}

	public Route getRouteBetweenLandmarks(Landmark origin, Landmark destination) {
		// TODO Implement this method
		throw new RuntimeException("Unimplemented method!");
	}

	public SVGDiagram getDiagram() {
		return diagram;
	}
}
