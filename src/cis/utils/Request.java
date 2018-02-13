package cis.utils;

/**
 * Enumerated class that defines different request types
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public enum Request {

	READ("R"), WRITE("W"), ACK("A"), DATA("D"), ERROR("E"), INVALID("I");

	private String type;

	Request(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public byte[] getBytes() {
		if (this == READ) {
			return new byte[] { 0, 1 };
		} else if (this == WRITE) {
			return new byte[] { 0, 2 };
		}

		return new byte[] { 2, 1 };
	}
}
