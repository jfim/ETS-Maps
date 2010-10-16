package im.jeanfrancois.etsmaps.ui.svg;

import com.google.inject.Inject;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.svg.SvgNavigableMap;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;


/**
 * Swing component that shows a SVG map and allows panning/zooming.
 *
 * @author jfim
 */
public class SvgMapComponent extends JComponent implements MapDisplayComponent {
	private final ExceptionDisplayer exceptionDisplayer;
	private MouseEvent lastMouseEvent = null;
	private SVGDiagram diagram;
	private boolean dirty = true;
	private boolean fastUpdate = false;
	private double scaleFactor = 1.0;
	private double xTranslation = 0.0;
	private double yTranslation = 0.0;

	@SuppressWarnings({"unchecked"})
	@Inject
	public SvgMapComponent(ExceptionDisplayer exceptionDisplayer, SvgNavigableMap map) {
		diagram = map.getDiagram();
		this.exceptionDisplayer = exceptionDisplayer;

		final MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastMouseEvent = e;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				xTranslation -= (lastMouseEvent.getX() - e.getX());
				yTranslation -= (lastMouseEvent.getY() - e.getY());

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
				xTranslation -= (lastMouseEvent.getX() - e.getX());
				yTranslation -= (lastMouseEvent.getY() - e.getY());

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
	protected void paintComponent(Graphics g) {
		if (dirty) {
			Graphics2D g2d = (Graphics2D) g;

			if (fastUpdate) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			} else {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
			}

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
	public void setSize(Dimension d) {
		dirty = true;
		super.setSize(d);
	}

	@Override
	public void setSize(int width, int height) {
		dirty = true;
		super.setSize(width, height);
	}
}
