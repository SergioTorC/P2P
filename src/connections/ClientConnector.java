package connections;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import controller.P2P;


public class ClientConnector {

	private final int serverPort;
	private final P2P p2p;
	private boolean runState;


	//Constructor
	public ClientConnector(P2P p2p, int serverPort) {
		this.p2p = p2p;
		this.serverPort = serverPort;
		runState = true;
		new Thread(this::run).start();
	}


	//Metodos de ejecucion y control

	private void run() {
		while(runState) {
			// Recibe la lista de los antiguos peers que se han desconectado
			List<Connection> connectionList = p2p.getPeersList().stream().filter((connection) -> !connection.isOk()).toList();
			// Intenta conectar a cada peer de la lista
			for(Connection connection: connectionList) {
				try {
					System.err.println("Intento de reconexión con " + connection.getIp());
					Socket socket = new Socket(connection.getIp(), serverPort);
					System.err.println("Éxito en la reconexión con " + connection.getIp());
					p2p.addConnection(socket);
				} catch (IOException e) {
					System.err.println("Falló la reconexión con " + connection.getIp());
				}
			}

			// Espera para la siguente conexion
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {}
		}
		System.err.println("Se ha parado el cliente");
	}

	public void stopClientConnection() {
		System.err.println("Parando " + this.getClass().getSimpleName());
		runState = false;
	}
	


}
