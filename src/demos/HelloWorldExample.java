package demos;

import javax.swing.JFrame;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.nodes.PText;

public class HelloWorldExample extends JFrame {
        
        private HelloWorldExample() {
                final PCanvas canvas = new PCanvas();
                final PText text = new PText("Hello World");
                final PText anothertext = new PText("Hello again");
                canvas.getLayer().addChild(text);
                canvas.getLayer().addChild(anothertext);
                
                SimpleEllipseNode enode = new SimpleEllipseNode();
                canvas.getLayer().addChild(enode);
                add(canvas);

                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setSize(600, 400);
                setVisible(true);
        }

        public static void main(String[] args) {
                new HelloWorldExample();
        }
}
