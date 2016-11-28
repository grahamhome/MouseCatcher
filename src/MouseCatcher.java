import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MouseCatcher extends JFrame {
	
	private boolean active = false;
	private Socket serverConnection;
	private DataOutputStream out;
	
	
	public MouseCatcher() {
		createWindow();
		openSocket();
		addMouseListener(new ClickTracker());
		addMouseMotionListener(new MotionTracker());
	}
	
	private void createWindow() {
		setTitle("Remote Laser Controller - Inactive");
		setSize(800, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				quit();
			}
		});
		setVisible(true);
	}
	
	private void quit() {
		dispose();
		try {
			serverConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	private void openSocket() {
		try {
			serverConnection = new Socket("localhost", 8008);
			out = new DataOutputStream(serverConnection.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void toggleTracking(Boolean... value) {
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
			String coords = (int)((x/getWidth())*255) + ", " + (int)((y/getHeight())*255) + "\n";
			System.out.println(coords);
			try {
				out.writeBytes(coords);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			new MouseCatcher();
		});
	}
	
	private class ClickTracker implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			toggleTracking();
			if (active) {
				reportPosition();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {
			toggleTracking(false);
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
