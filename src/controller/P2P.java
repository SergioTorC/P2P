package controller;

import java.io.FileInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import connections.ClientConnector;
import connections.Connection;
import connections.ServerConnector;
import view.Frame;
import view.View;


public class P2P {

    private final ArrayList<Connection> connectionList;
    private ServerConnector serverConnector;
    private ClientConnector clientConnector;
    private View view;

    public P2P() {
        Properties properties = new Properties();
        connectionList = new ArrayList<>();

        try {
            // Cargamos el archivo de propiedades donde tenemos nuestras IPs y el puerto del servidor
            properties.load(new FileInputStream("configuration.properties"));
            // Obtenemos el puerto
            int serverPort = Integer.parseInt(properties.getProperty("server_port"));
            String[] ipsArray;
            // Creamos un array donde guardaremos nuestras posibles IPs, actualmente configurado para recibir hasta 8 entidades.
            ipsArray = new String[]{"ip1", "ip2", "ip3", "ip4", "ip5", "ip6l", "ip7", "ip8"};

            // Itera sobre el arreglo de direcciones IP y crea una conexión para cada una que esté disponible
            for (String peer : ipsArray) {
                String ip = properties.getProperty(peer);
                if (ip != null) {
                    this.connectionList.add(new Connection(this, ip));
                }
            }
            serverConnector = new ServerConnector(this, serverPort);
            clientConnector = new ClientConnector(this, serverPort);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }


    // Establece la vista y agrega las conexiones a la vista
    public void setView(View view) {
        this.view = view;
        for (Connection c : connectionList) {
            view.addConnection(c.getIp());
        }
    }

    // Envia un mensaje a través de la conexión indicada por la dirección IP
    public void pushMessage(String ip, String message) {
        if (view != null) {
            view.pushMessage(ip, message);
        }
    }

    //Metodos de Gestion de Conexiones


    // Agrega una conexión vacía con la dirección IP especificada a la lista de conexiones
    private int addEmptyConnection(String ip) {
        int index = getConnectionIndex(ip);
        if (index == -1) {
            Connection connection = new Connection(this, ip);
            connectionList.add(connection);
            index = connectionList.size() - 1;
            if (view != null) {
                view.addConnection(ip);
            }
        }
        return index;
    }

    // Agrega una conexión utilizando el socket especificado
    public void addConnection(Socket socket) {
        int index = addEmptyConnection(socket.getInetAddress().getHostAddress());
        connectionList.get(index).setSocket(socket);
    }

    // Obtenemos una lista de pares conectados
    public List<Connection> getPeersList() {
        return connectionList;
    }

    // Obtenemos el estado de la conexión especificada por la dirección IP
    private int getConnectionIndex(String ip) {
        int index = -1;
        for (int i = 0; i < connectionList.size(); ++i) {
            Connection connection = connectionList.get(i);
            if (connection.getIp() != null && connection.getIp().equals(ip)) {
                index = i;
                break;
            }
        }
        return index;
    }

    // Obtenemos el status de la conexión especificada por la dirección IP
    public boolean getConnectionStatus(String ip) {
        boolean status = false;
        int index = getConnectionIndex(ip);
        if (index >= 0) {
            status = connectionList.get(index).isOk();
        }
        return status;
    }

    //Metodos de envio y reenvio de mensajes


    public void resend(String bannedIp, Frame frame) {
        // Obtiene la dirección IP destino
        String destinatioIp = frame.getPayload();
        // Obtiene el índice de la conexión que coincide con la dirección IP de destino
        int index = getConnectionIndex(destinatioIp);
        // Si se encuentra una conexión que coincide con la dirección IP de destino, envía el frame a esa conexión
        if (index != -1) {
            connectionList.get(index).sendFrame(frame);
        }
        // De lo contrario, envía el frame a todas las conexiones excepto la conexión prohibida
        else {
            for (Connection connection : connectionList) {
                if (!connection.getIp().equals(bannedIp)) {
                    connection.sendFrame(frame);
                }
            }
        }
    }

    public void sendMessage(String ip, String message) {
        if (ip == null) {
            for (Connection conn : connectionList) {
                conn.send(message);
            }
        } else {
            int index = getConnectionIndex(ip);
            if (index != -1) {
                connectionList.get(index).send(message);
            } else {
                for (Connection conn : connectionList) {
                    conn.send(message);
                }
            }
        }
    }

    //Metodos de parada y salida

    public void stopAndQuit() {
        // Detiene el servidor
        serverConnector.stopServerConnection();
        // Detiene el reconectar
        clientConnector.stopClientConnection();
        for (Connection connection : connectionList) {
            connection.stopConnection();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public static boolean isValidIp(String ip) {
        String IPV4_PATTERN =
                "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
        Pattern pattern = Pattern.compile(IPV4_PATTERN);
        return pattern.matcher(ip).matches();
    }

}
