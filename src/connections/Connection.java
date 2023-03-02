package connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import controller.P2P;
import view.*;

public class Connection {

	private final P2P p2p;
	private final String ip;
	private Socket socket;
	private HealthCareConnection hcc;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private long lastTimeReceivedMessage;
	private Thread thread;
	public boolean runState;
	
	public Connection(P2P p2p, String ip) {
		this.p2p = p2p;
		this.ip = ip;
		thread = new Thread(this::run);
	}

	//Metodos de inicializacion y control

	public void stopConnection() {
		System.err.println("Parando Connector");
		killSocket();
	}

	public void killSocket() {
		try {
			runState = false;
			if(hcc!=null)
				hcc.stopHCC();
			if(socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			in = null;
			out = null;
			socket = null;
			System.err.println("Matando el socket: " + ip);
		}
	}

	public boolean isOk() {
		return socket!=null && !socket.isClosed();
	}

	public String getIp() {
		return ip;
	}

	public void setSocket(Socket socket) {
		if(!isOk() && ip.equals(socket.getInetAddress().getHostAddress())) {
			this.socket = socket;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				updateTimeReceivedMessage();
				runState = true;
				if(!thread.isAlive()) {
					thread = new Thread(this::run);
					thread.start();
				}
				hcc = new HealthCareConnection(this, 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void run() {
		while(runState) {
			if(isOk()) {
				receive();
			}
		}
	}

	public long getTimeReceivedMessage() {
		return lastTimeReceivedMessage;
	}

	//Metodo de envio de mensajes

	public void send(String packageInfo) {
		if(isOk()) {
			send(socket.getInetAddress().getHostAddress(), packageInfo);
		}
	}

	public void send(String destinationIp, String packageInfo) {
		if(destinationIp != null && !P2P.isValidIp(destinationIp))
			return;
		if(destinationIp == null) {
			destinationIp = "*";
		}
		if(isOk()) {
			Frame frame = new Frame();
			frame.setFrameType(Frame.FrameType.MESSAGE);
			frame.setHeader(2, socket.getLocalAddress().getHostAddress(), destinationIp);
			frame.setPayload(packageInfo);
			sendFrame(frame);
		}
	}

	public void sendFrame(Frame frame) {
		if(isOk()) {
			try {
				out.writeObject(frame);
			} catch (IOException e) {
				e.printStackTrace();
				killSocket();
			}
		}
	}

	//Metodo de recepcion de mensajes

	private void receive() {
		if(isOk()) {
			try {
				Frame frame = (Frame)in.readObject();
				updateTimeReceivedMessage();
				handleFrame(frame);
			} catch (Exception e) {
				System.err.println("Error al recibir, mantando socket.");
				killSocket();
			}
		}
	}

	//Metodo de manejo de mensajes

	private void handleFrame(Frame frame) {
		String myIp = socket.getLocalAddress().getHostAddress();
		System.err.flush();
		switch (frame.getFrameType()) {
			case MESSAGE -> {
				// Si el paquete es nuestro. Lo matamos
				if (frame.getSourceIP().equals(myIp)) return;
				// En caso de que el paquete vaya dirigido a todos o a nosotros. Enviar el payload y la ip de origen al controlador para tratarlo.
				if (frame.getTargetIP().equals(myIp) || frame.getTargetIP().equals("*")) {
					p2p.pushMessage(frame.getSourceIP(), frame.getPayload());
				}
				// Reenviarlo solo en caso de que el paquete no sea para nostros y su ttl no sea 0
				else if (!frame.decrementTTL()) {
					p2p.resend(ip, frame);
				}
			}
			case PING -> {
				// Consideramos viene directamente de este peer
				Frame response = new Frame();
				response.setFrameType(Frame.FrameType.PONG);
				response.setHeader(1, myIp, ip);
				System.out.println("Conexion enviando Ping: " + ip);
				sendFrame(response);
			}
			case PONG -> System.out.println("Conexión recibiendo Pong: " + socket.getInetAddress().getHostAddress());
		}
	}


	public void doPing() {
		if(isOk()) {
			Frame ping = new Frame();
			ping.setFrameType(Frame.FrameType.PING);
			ping.setHeader(1, socket.getLocalAddress().getHostAddress(), ip);
			sendFrame(ping);
		}
	}

	//Metodo de actualización de estado

	public void updateTimeReceivedMessage() {
		lastTimeReceivedMessage = System.currentTimeMillis();
	}
}
