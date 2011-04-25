package demos;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

public class SimpleEllipseNode extends PNode {
    private Ellipse2D ellipse;

    // This nodes uses an internal Ellipse2D to define its shape.
    public Ellipse2D getEllipse() {
            if (ellipse == null) ellipse = new Ellipse2D.Double();
            return ellipse;
    }

    // This method is important to override so that the geometry of 
    // the ellipse stays consistent with the bounds geometry.
    public boolean setBounds(double x, double y, double width, double height) {
            if(super.setBounds(x, y, width, height)) {
                    ellipse.setFrame(x, y, width, height);
                    return true;
            }
            return false;
    }

    // Non rectangular subclasses need to override this method so
    // that they will be picked correctly and will receive the
    // correct mouse events.
    public boolean intersects(Rectangle2D aBounds) {
            return getEllipse().intersects(aBounds);
    }

    // Nodes that override the visual representation of their super
    // class need to override a paint method.
    public void paint(PPaintContext aPaintContext) {
            Graphics2D g2 = aPaintContext.getGraphics(); 
            g2.setPaint(getPaint());
            g2.fill(getEllipse());
    }
}
