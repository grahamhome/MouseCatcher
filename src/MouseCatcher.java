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
public class MouseCatcher {
	
	private JFrame window;
	private boolean active = false;
	private Socket serverConnection;
	private DataOutputStream out;

	public MouseCatcher() {
		createWindow();
		openSocket();
		toggleTracking(false);
		window.addMouseListener(new ClickTracker());
		window.addMouseMotionListener(new MotionTracker());
	}

	private void createWindow() {
		window = new JFrame();
		window.setSize(800, 800);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				quit();
			}
		});
		window.setVisible(true);
	}

	private void quit() {
		window.dispose();
		try {
			serverConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	private void openSocket() {
		window.setTitle("Remote Laser Controller - Connecting To Server...");
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
		window.setTitle("Remote Laser Controller - " + (active ? "Active" : "Inactive"));
	}

	private void reportPosition() {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		Point windowLoc = window.getLocationOnScreen();
		double x = mouse.getX()-windowLoc.getX();
		double y = mouse.getY()-windowLoc.getY();
		if (active) {
			String coords = (int)((x/window.getWidth())*255) + ", " + (int)((y/window.getHeight())*255);
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
		EventQueue.invokeLater(() -> new MouseCatcher());
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
