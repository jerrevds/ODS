package udprsa.network.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RemoteCallUDPRCVMessage extends ROSGiMessage {
	
	private int id;
	private int volgnr;
	

	public RemoteCallUDPRCVMessage(short funcID) {
		super(funcID);
		
	}

	public RemoteCallUDPRCVMessage(ObjectInputStream input) {
		super(UDP_RECEIVED);
		try {
			id = input.readInt();
			volgnr = input.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void writeBody(ObjectOutputStream output) throws IOException {
		output.writeInt(id);
		output.writeInt(volgnr);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVolgnr() {
		return volgnr;
	}

	public void setVolgnr(int volgnr) {
		this.volgnr = volgnr;
	}
	
	

}
