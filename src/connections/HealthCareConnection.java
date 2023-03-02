package connections;

public class HealthCareConnection {
	
	private final Connection connection;
	private ConnectionStatus status;
	private final long healthTimeOut;
	public boolean runStateHCC;
	
	public HealthCareConnection(Connection connection, long healthTimeOut) {
		this.connection = connection;
		this.healthTimeOut = healthTimeOut;
		status = ConnectionStatus.OK;
		runStateHCC = true;
		new Thread(this::run).start();
	}

	public void stopHCC() {
		System.err.println("Parando HCC");
		runStateHCC = false;
	}
	private void run() {
		System.err.println("Activando HCC");
		while(runStateHCC) {
			if(connection.isOk()) {
				try {
					Thread.sleep(healthTimeOut/3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long lastTimeConnection = connection.getTimeReceivedMessage();
				long currentTime = System.currentTimeMillis();
				long diffTime = currentTime - lastTimeConnection;
				if(diffTime > healthTimeOut) {
					if(status == ConnectionStatus.OK) {
						status = ConnectionStatus.AWAITING;
						System.out.println("Ping");
						connection.doPing();
					} else {
						System.err.println("TIMEOUT");
						connection.killSocket();
						status = ConnectionStatus.OK;
					}
				} else {
					status = ConnectionStatus.OK;
				}
			}
		}
		System.err.println("STOP HCC");
	}

}
