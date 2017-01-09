import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;

public class MouseCatcher {

	private static JFrame window;
	private static Socket serverConnection;
	private static DataOutputStream output;

	private static void createWindow() {
		window = new JFrame();
		window.setSize(800, 800);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				try { serverConnection.close(); } catch (IOException e) {}
			}
		});
		window.setVisible(true);
	}

	private static void openSocket() {
		window.setTitle("Remote Laser Controller");
		boolean connected = false;
		do {
			try {
				serverConnection = new Socket("localhost", 8008);
				output = new DataOutputStream(serverConnection.getOutputStream());
				connected = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!connected);
	}

	public static void main(String[] args) throws Exception {
		createWindow();
		openSocket();
		window.addMouseMotionListener(new MouseAdapter() {
			private static final byte UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
			private int prevX, prevY;

			@Override
			public void mouseDragged(MouseEvent e) {
				send(e.getY() < prevY ? UP : DOWN);
				prevY = e.getY();
				send(e.getX() < prevX ? LEFT : RIGHT);
				prevX = e.getX();
			}

			private void send(byte data) {
				System.out.println("Sending " + data + " to server");
				try {
					output.writeByte(data);
					output.flush();
				} catch (IOException e) {
				}
			}
		});
	}
}
