package view;

import java.io.Serializable;

public class Frame implements Serializable {

	// Enum que define los tipos de frames posibles
	public enum FrameType {
		PING,
		PONG,
		MESSAGE
	}

	protected FrameType frameType;
	private Integer timeToLive;
	private String sourceIp;
	private String targetIp;
	private String payload;

	// Establece los valores de tiempo de vida, IP de origen y IP de destino del frame
	public final void setHeader(Integer timeToLive, String sourceIp, String targetIp) {
		this.timeToLive = timeToLive;
		this.sourceIp = sourceIp;
		this.targetIp = targetIp;
	}

	public final void setPayload(String payload) {
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}

	public final boolean decrementTTL() {
		if(timeToLive > 0)
			return true;
		else
			--timeToLive;
		return false;
	}

	public final FrameType getFrameType() {
		return frameType;
	}

	// Obtiene la dirección IP de origen
	public final String getSourceIP() {
		return sourceIp;
	}

	// Obtiene la dirección IP de destino
	public final String getTargetIP() {
		return targetIp;
	}

	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
	}

}


