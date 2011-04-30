package im.jeanfrancois.etsmaps.ui.svg;

import com.google.inject.Inject;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import im.jeanfrancois.etsmaps.ExceptionDisplayer;
import im.jeanfrancois.etsmaps.model.Landmark;
import im.jeanfrancois.etsmaps.model.Leg;
import im.jeanfrancois.etsmaps.model.Route;
import im.jeanfrancois.etsmaps.model.svg.SvgLandmark;
import im.jeanfrancois.etsmaps.model.svg.SvgNavigableMap;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;


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
    private boolean debugStuffVisible;
    private boolean stillOnDefaultTransform = true;

    private SvgLandmark origin;
    private SvgLandmark destination;

    private BufferedImage markerA = null;
    private BufferedImage markerB = null;

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
                stillOnDefaultTransform = false;

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
                stillOnDefaultTransform = false;

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

        try {
            markerA = ImageIO.read(SvgMapComponent.class.getClassLoader().getResource("marker-a.png"));
            markerB = ImageIO.read(SvgMapComponent.class.getClassLoader().getResource("marker-b.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void setDebugStuffVisible(boolean debugStuffVisible) {
        this.debugStuffVisible = debugStuffVisible;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        //g.clearRect(0, 0, getWidth(), getHeight());

        if (DEBUG) {
            logger.debug("Repainting");
        }

        AffineTransform initialTransform = g2d.getTransform();
        AffineTransform combinedTransform = new AffineTransform(initialTransform);
        combinedTransform.concatenate(transform);

        g2d.setTransform(combinedTransform);

        try {
            diagram.render(g2d);
        } catch (SVGException e) {
            exceptionDisplayer.displayException(e, this);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (debugStuffVisible) {
            map.drawLandmarks(g2d);
            map.drawNavigation(g2d);
        }

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(routeShape);

        final double scaleX = transform.getScaleX();
        if(origin != null) {
            g2d.drawImage(markerA, (int)(origin.getX() - 32 / scaleX), (int)(origin.getY() - 54 / scaleX), (int)(68 / scaleX), (int)(68 / scaleX), null);
        }
        if(destination != null) {
            g2d.drawImage(markerB, (int)(destination.getX() - 32 / scaleX), (int)(destination.getY() - 54 / scaleX), (int)(68 / scaleX), (int)(68 / scaleX), null);
        }

        g2d.setTransform(initialTransform);
        if (debugStuffVisible) {
            g2d.setColor(Color.BLACK);
            final double translateX = transform.getTranslateX();
            final double translateY = transform.getTranslateY();

            g2d.drawString("S " + Double.toString(scaleX), 0, 16);
            g2d.drawString("X " + Double.toString(translateX), 0, 32);
            g2d.drawString("Y " + Double.toString(translateY), 0, 48);

            System.out.println("S " + scaleX + " X " + translateX + " Y " + translateY);
            System.out.println("W " + getWidth() + " H " + getHeight());
        }
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        repaint();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if(stillOnDefaultTransform) {
            float xScaleFactor = width / diagram.getWidth();
            float yScaleFactor = height / diagram.getHeight();
            float scaleFactor = Math.min(xScaleFactor, yScaleFactor);

            transform = new AffineTransform();
            transform.scale(scaleFactor, scaleFactor);
        }
        repaint();
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        repaint();
    }

    @Override
    public void setLocation(Point p) {
        super.setLocation(p);    //To change body of overridden methods use File | Settings | File Templates.
        repaint();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);    //To change body of overridden methods use File | Settings | File Templates.
        repaint();
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);    //To change body of overridden methods use File | Settings | File Templates.
        repaint();
    }

    public void setOrigin(Landmark origin) {
        this.origin = (SvgLandmark) origin;
        repaint();
    }

    public void setDestination(Landmark destination) {
        this.destination = (SvgLandmark) destination;
        repaint();
    }
}
