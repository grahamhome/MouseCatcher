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
		toggleTracking(false);
		addMouseListener(new ClickTracker());
		addMouseMotionListener(new MotionTracker());
		
	}
	
	private void createWindow() {
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
		setTitle("Remote Laser Controller - Connecting To Server...");
		boolean connected = false;
		do {
			try {
				serverConnection = new Socket("localhost", 8008);
				out = new DataOutputStream(serverConnection.getOutputStream());
				connected = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!connected);
	}
	
	private void toggleTracking(Boolean... value) {
		if (value.length > 0) {
			if (active != value[0]) {
				active = value[0];
				send(String.valueOf(active));
			}
		} else {
			active = !active;
			send(String.valueOf(active));
		}
		setTitle("Remote Laser Controller - " + (active ? "Active" : "Inactive"));
		
	}
	
	private void reportPosition() {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		Point window = getLocationOnScreen();
		double x = mouse.getX()-window.getX();
		double y = mouse.getY()-window.getY();
		if (active) {
			String coords = (int)((x/getWidth())*255) + ", " + (int)((y/getHeight())*255);
			send(coords);
		}
	}
	
	private void send(String data) {
		try {
			System.out.println("Sending " + data + " to server");
			out.writeBytes(data + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
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
