package im.jeanfrancois.etsmaps;

import com.google.inject.Inject;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.net.URISyntaxException;

/**
 * Swing component that shows a SVG map and allows panning/zooming.
 *
 * @author jfim
 */
public class SvgMapComponent extends JComponent implements MapDisplayComponent {
	private double scaleFactor = 1.0;
	private double xTranslation = 0.0;
	private double yTranslation = 0.0;
	private boolean dirty = true;
	private boolean fastUpdate = false;

	private SVGDiagram diagram;

	private final ExceptionDisplayer exceptionDisplayer;

	private MouseEvent lastMouseEvent = null;

	@Inject
	public SvgMapComponent(ExceptionDisplayer exceptionDisplayer) {
		this.exceptionDisplayer = exceptionDisplayer;

		SVGUniverse universe = new SVGUniverse();
		try {
			diagram = universe.getDiagram(SvgMapComponent.class.getResource("/Map_of_USA_with_state_names.svg").toURI(), true);
		} catch (URISyntaxException e) {
			this.exceptionDisplayer.displayException(e, this);
		}

		final MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastMouseEvent = e;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				xTranslation -= lastMouseEvent.getX() - e.getX();
				yTranslation -= lastMouseEvent.getY() - e.getY();

				System.out.println("Requesting repaint");
				System.out.println("xTranslation = " + xTranslation);
				System.out.println("yTranslation = " + yTranslation);

				fastUpdate = false;
				dirty = true;
				repaint();
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scaleFactor *= Math.pow(1.1, -e.getWheelRotation());
				dirty = true;
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				xTranslation -= lastMouseEvent.getX() - e.getX();
				yTranslation -= lastMouseEvent.getY() - e.getY();

				System.out.println("Requesting repaint");
				System.out.println("xTranslation = " + xTranslation);
				System.out.println("yTranslation = " + yTranslation);

				fastUpdate = true;
				dirty = true;
				repaint();

				lastMouseEvent = e;
			}
		};

		addMouseMotionListener(adapter);
		addMouseListener(adapter);
		addMouseWheelListener(adapter);
	}

	@Override
	public void setSize(Dimension d) {
		dirty = true;
		super.setSize(d);
	}

	@Override
	public void setSize(int width, int height) {
		dirty = true;
		super.setSize(width, height);
	}

	@Override
	public void setBounds(Rectangle r) {
		dirty = true;
		super.setBounds(r);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		dirty = true;
		super.setBounds(x, y, width, height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (dirty) {
			Graphics2D g2d = (Graphics2D) g;
			if(fastUpdate)
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			else
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			AffineTransform transform = new AffineTransform();
			transform.setToScale(scaleFactor, scaleFactor);
			transform.translate(xTranslation, yTranslation);
			System.out.println("Repainting");

			g2d.setTransform(transform);

			try {
				diagram.render(g2d);
			} catch (SVGException e) {
				exceptionDisplayer.displayException(e, this);
			}

			dirty = false;
		}
	}
}
