package demos;

import javax.swing.JFrame;


public class GraphEditorTester extends JFrame {
	public GraphEditorTester() {
		setTitle("Piccolo Graph Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphEditor graphEditor = new GraphEditor(500, 500);
		getContentPane().add(graphEditor);
		pack();
		setVisible(true);
	}
	

	public static void main(String args[]) {
		new GraphEditorTester();
	}
}