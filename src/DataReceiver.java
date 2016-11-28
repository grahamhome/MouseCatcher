import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

@SuppressWarnings("serial")
public class DataReceiver extends JFrame {
	
	private ServerSocket clientConnection;
	private JTextArea windowOutput;
	private BufferedReader input;
	private final String portName = "COM3"; //Rename as appropriate; use "/dev/ttyUSB*" on Linux where * is the serial port number
	private SerialPort serialPort;
	private OutputStream output;
	
	public DataReceiver() {
		createWindow();
		openSocket();
		openSerialPort();
	}
	
	private void createWindow() {
		setTitle("Data Receiver");
		setSize(400, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				quit();
			}
		});
		
		windowOutput = new JTextArea();
		windowOutput.setEditable(false);
		JScrollPane scroll = new JScrollPane(windowOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
		setVisible(true);
	}
	
	private void quit() {
		dispose();
		try {
			input.close();
			clientConnection.close();
			output.close();
			serialPort.close();
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
	
	private void openSerialPort() {
		CommPortIdentifier portID = null;
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();
			if (currentPortID.getName().equals(portName)) {
				portID = currentPortID;
				break;
			}
		}
		if (portID == null) {
			System.out.println("Could not find port " + portName);
			//quit();
		}
		try {
			serialPort = (SerialPort) portID.open(this.getName(), 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			output = serialPort.getOutputStream();
		} catch (Exception e) {
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
			input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			while (true) {
				try {
					String data = input.readLine();
					publish(data + "\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		protected void process(List<String> coords) {
			for (String point : coords) {
				windowOutput.append(point);
				String[] values = point.split(",");
				try {
					output.write(Integer.parseInt(values[0]));
					output.write(Integer.parseInt(values[1]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
