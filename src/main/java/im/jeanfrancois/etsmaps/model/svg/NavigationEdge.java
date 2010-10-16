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
}
