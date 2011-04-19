package im.jeanfrancois.etsmaps.model.svg;

import com.kitfox.svg.Text;
import com.kitfox.svg.Tspan;
import im.jeanfrancois.etsmaps.model.Landmark;

import java.util.ArrayList;
import java.util.List;


/**
 * SVG implementation of landmarks.
 *
 * @author jfim
 */
public class SvgLandmark implements Landmark {
	private String name;
	private Text textSvgNode;
	private float x;
	private float y;

	public SvgLandmark(String prefix, Text landmarkText) {
		textSvgNode = landmarkText;

		// Extract the node name from the embedded TSPAN element
		if (textSvgNode.getNumChildren() != 0) {
			List childNodes = textSvgNode.getChildren(new ArrayList());

			for (Object childNode : childNodes) {
				if (childNode instanceof Tspan) {
					name = prefix + ((Tspan) childNode).getText();
				}
			}
		}

		// If we couldn't extract the text from the embedded TSPAN element, load the contents of the TEXT element
		if (name == null) {
			List contents = textSvgNode.getContent();

			for (Object content : contents) {
				if (content instanceof String) {
					name = prefix + content;
				}
			}
		}

		x = textSvgNode.getShape().getBounds().x;
		y = textSvgNode.getShape().getBounds().y;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
