package im.jeanfrancois.etsmaps.ui.svg;

import com.google.inject.Inject;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.Leg;
import im.jeanfrancois.etsmaps.model.Route;
import im.jeanfrancois.etsmaps.model.svg.SvgNavigableMap;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;


/**
 * Swing component that shows a SVG map and allows panning/zooming.
 *
 * @author jfim
 */
public class SvgMapComponent extends JComponent implements MapDisplayComponent {
    private static final boolean DEBUG = true;
    private static final Logger logger = LoggerFactory.getLogger(SvgMapComponent.class);
    private Path2D.Float routeShape = new Path2D.Float();
    private final ExceptionDisplayer exceptionDisplayer;
    private SVGDiagram diagram;
    private AffineTransform transform = new AffineTransform();
    private MouseEvent startDragEvent = null;
    private SvgNavigableMap map;

    @SuppressWarnings({"unchecked"})
    @Inject
    public SvgMapComponent(ExceptionDisplayer exceptionDisplayer,
                           SvgNavigableMap map) {
        diagram = map.getDiagram();
        this.exceptionDisplayer = exceptionDisplayer;
        this.map = map;

        final MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                try {
                    int wheelRotation = e.getWheelRotation();
                    Point p = e.getPoint();
                    if (wheelRotation > 0) {
                        Point2D p1 = transformPoint(p);
                        transform.scale(1 / 1.2, 1 / 1.2);
                        Point2D p2 = transformPoint(p);
                        transform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    } else {
                        Point2D p1 = transformPoint(p);
                        transform.scale(1.2, 1.2);
                        Point2D p2 = transformPoint(p);
                        transform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            repaint();
                        }
                    });
                } catch (NoninvertibleTransformException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    if (startDragEvent == null)
                        return;
                    Point dragStartScreen = startDragEvent.getPoint();
                    Point dragEndScreen = e.getPoint();
                    Point2D.Float dragStart = transformPoint(dragStartScreen);
                    Point2D.Float dragEnd = transformPoint(dragEndScreen);
                    double dx = dragEnd.getX() - dragStart.getX();
                    double dy = dragEnd.getY() - dragStart.getY();
                    transform.translate(dx, dy);
                    startDragEvent = e;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            repaint();
                        }
                    });
                } catch (NoninvertibleTransformException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                startDragEvent = e;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startDragEvent = null;
                repaint();
            }
        };

        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });
    }

    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = transform.createInverse();

        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    public void overlayRoute(Route route) {
        routeShape = new Path2D.Float();

        if ((route == null) || (route.getLegCount() == 0)) {
            return;
        }

        Leg leg = route.getLeg(0);
        routeShape.moveTo(leg.getOrigin().getX(), leg.getOrigin().getY());
        routeShape.lineTo(leg.getDestination().getX(),
                leg.getDestination().getY());

        for (int i = 1; i < route.getLegCount(); ++i) {
            leg = route.getLeg(i);
            routeShape.lineTo(leg.getDestination().getX(),
                    leg.getDestination().getY());
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (DEBUG) {
            logger.debug("Repainting");
        }

        g2d.setTransform(transform);

        try {
            diagram.render(g2d);
        } catch (SVGException e) {
            exceptionDisplayer.displayException(e, this);
        }

        // map.drawLandmarks(g2d);

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(routeShape);
    }
}
