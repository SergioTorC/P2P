package view;


import controller.P2P;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class View extends JFrame implements WindowListener {
	private final JTable connections;
	private final JTextArea areaChat;
	private final JTextField areaMessage;
	private JButton btnSend;
	private P2P controller;
	private boolean runState;

	public View() {
		setTitle("Chat P2P");
		setPreferredSize(new Dimension(800, 600));
		setLayout(new BorderLayout());
		addWindowListener(this);

		runState = true;
		connections = new JTable(new DefaultTableModel(new String[]{"IP", "STATUS"}, 0));
		areaChat = new JTextArea();
		areaMessage = new JTextField();

		initializeAreaChat();
		initializeAreaConnections();
		initializeAreaSendMessages();
		pack();
		setVisible(true);
	}

	public void addConnection(String ip) {
		DefaultTableModel model = (DefaultTableModel) connections.getModel();
		Object[] rowData = new Object[]{ip, ""};
		model.addRow(rowData);
	}

	public void setController(P2P controller) {
		this.controller = controller;
		new Thread(() -> {
			connectionsTable();
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ignored) {
			}
		}).start();
	}

	private void initializeAreaSendMessages() {
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = 2;
		areaMessage.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					btnSend.doClick();
				}

			}

			public void keyPressed(KeyEvent e) {
			}
		});
		area.add(areaMessage, c);
		btnSend = new JButton("Enviar");
		btnSend.addActionListener((ev) -> {
			String mensaje = areaMessage.getText();
			areaMessage.setText("");
			areaMessage.requestFocus();
			sendMessage(mensaje);
		});
		c.weightx = 0.0;
		area.add(btnSend, c);
		add(area, "South");
	}

	private void initializeAreaConnections() {
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		area.add(new JLabel("Peers"), c);
		++c.gridy;
		area.add(new JScrollPane(connections), c);
		this.add(area, "East");
	}

	private void initializeAreaChat() {
		areaChat.setLineWrap(true);
		JScrollPane scrollChat = new JScrollPane(areaChat);
		scrollChat.setHorizontalScrollBarPolicy(31);
		areaChat.setEditable(false);
		add(scrollChat, "Center");
	}

	private void connectionsTable() {
		DefaultTableModel tm = (DefaultTableModel) connections.getModel();

		while (this.runState) {
			boolean validConnector = false;

			for (int i = 0; i < tm.getRowCount(); ++i) {
				String ip = (String) tm.getValueAt(i, 0);
				if (controller.getConnectionStatus(ip)) {
					validConnector = true;
					tm.setValueAt("CONECTADO", i, 1);
				} else {
					tm.setValueAt("DESCONECTADO", i, 1);
				}

			}

			if (validConnector) {
				this.btnSend.setEnabled(true);
				this.areaMessage.setEnabled(true);
			} else {
				this.btnSend.setEnabled(false);
				this.areaMessage.setEnabled(false);
			}
		}

	}

	private void sendMessage(String message) {
		areaChat.append("SEND: " + message + "\n");
		controller.sendMessage("test", message);
	}

	public void pushMessage(String ip, String message) {
		areaChat.append(ip + ": " + message + "\n");
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		runState = false;
		setVisible(false);
		controller.stopAndQuit();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}