import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class DataReceiver extends JFrame {
	
	private ServerSocket clientConnection;
	private JTextArea output;
	private BufferedReader inputReader;
	
	public DataReceiver() {
		createWindow();
		openSocket();
	}
	
	private void createWindow() {
		setTitle("Coordinate Receiver");
		setSize(400, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				quit();
			}
		});
		
		output = new JTextArea();
		output.setEditable(false);
		JScrollPane scroll = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
		setVisible(true);
	}
	
	private void quit() {
		dispose();
		try {
			inputReader.close();
			clientConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	private void openSocket() {
		try {
			clientConnection = new ServerSocket(8008);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void listen() {
		new DataReader().execute();		
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			DataReceiver dr = new DataReceiver();
			dr.listen();
		});	
	}
	
	private class DataReader extends SwingWorker<Void, String> {

		@Override
		protected Void doInBackground() throws Exception {
			Socket connectionSocket = clientConnection.accept();
			while (true) {
				try {
					inputReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					publish(inputReader.readLine() + "\n");
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		protected void process(List<String> coords) {
			for (String coord : coords) {
				output.append(coord);
			}
		}
	}
}
