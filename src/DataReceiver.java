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
	private final String portName = "COM3"; //Rename to the actual COM port represented by your USB serial port
	private SerialPort serialPort;
	private OutputStream output;
	private String beamOn = "254";
	private String beamOff = "255";
	private String separator = "0";
	
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
			System.err.println("Error: could not find port " + portName);
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
					Thread.sleep(1); // Give the window time to respond to input so it can be closed
				}
			}
		}
		
		@Override
		protected void process(List<String> coords) {
			for (String point : coords) {
				point = point.trim();
				if (point.equals(String.valueOf(true))) {
					send(beamOn);
					windowOutput.append(point + "\n");
				} else if (point.equals(String.valueOf(false))) {
					send(String.valueOf(beamOff));
					windowOutput.append(point + "\n");
				} else {
					String[] values = point.split(",");
					if ((values.length == 2) && ! (values[0].equals(beamOn) || 
							values[0].equals(beamOff) || 
							values[0].equals(separator) || 
							values[1].equals(beamOn) || 
							values[1].equals(beamOff) || 
							values[1].equals(separator))) {
						send(separator);
						send(values[0]);
						send(values[1]);
						windowOutput.append(point + "\n");
					}
				}
			}
		}
		
		private void send(String data) {
			try {
				System.out.println("Sending " + data + " to Arduino");
				output.write(Byte.parseByte(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
