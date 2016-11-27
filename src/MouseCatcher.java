import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MouseCatcher extends JFrame {
	
	private final int size = 800;
	boolean active = true;
	
	public MouseCatcher() {
		createWindow();
		addMouseListener(new ClickTracker());
		addMouseMotionListener(new MotionTracker());
	}
	
	private void createWindow() {
		setTitle("Remote Laser Controller - Active");
		setSize(size, size);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void toggle(Boolean... value) {
		if (value.length > 0) {
			active = value[0];
		} else {
			active = !active;
		}
		setTitle("Remote Laser Controller - " + (active ? "Active" : "Inactive"));
	}
	
	private void reportPosition() {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		Point window = getLocationOnScreen();
		double x = mouse.getX()-window.getX();
		double y = mouse.getY()-window.getY();
		if (active) {
			System.out.println("(" + x/getWidth() + ", " + y/getHeight() + ")");
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() ->{
			MouseCatcher t = new MouseCatcher();
			t.setVisible(true);
		});
	}
	
	private class ClickTracker implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			toggle();
			if (active) {
				reportPosition();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {
			toggle(false);
		}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
	}
	
	private class MotionTracker implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {
			reportPosition();
			
		}
		
	}
}
