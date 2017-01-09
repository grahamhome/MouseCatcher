import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Enumeration;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class DataReceiver {

	private final static String portName = "/dev/ttyS88";
	private static SerialPort serialPort;
	private static OutputStream output;

	private static void openSerialPort() {
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
			serialPort = (SerialPort) portID.open(DataReceiver.class.toString(), 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			output = serialPort.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ServerSocket clientConnection = new ServerSocket(8008);
		openSerialPort();
		InputStream input = clientConnection.accept().getInputStream();
		int data;
		while((data = input.read()) != -1) {
			System.out.println("Sending " + data + " to Arduino");
			output.write((byte)data);
		}
		clientConnection.close();
	}
}
