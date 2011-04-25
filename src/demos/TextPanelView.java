package demos;

import javax.swing.JFrame; 

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.nodes.PText;
import org.eclipse.swt.widgets.*;

import edu.umd.cs.piccolox.swt.*;
import org.eclipse.swt.layout.*;


public class TextPanelView extends JFrame{
    public TextPanelView() {
        super();
}

public static void main(String[] args) {
        Display display = new Display ();
        Shell shell = open(display);
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
}

public static Shell open(Display display) {
        final Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        PSWTCanvas canvas = new PSWTCanvas(shell,0);
        
        PSWTText text = new PSWTText("Hello World");
        canvas.getLayer().addChild(text);
        
//        PSWTCanvas acanvas = new PSWTCanvas(shell,1);
//        acanvas.getLayer().addChild(text);
        
        shell.open();
        return shell;
}

}
