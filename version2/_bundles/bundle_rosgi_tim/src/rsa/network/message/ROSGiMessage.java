package rsa.network.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * This class is based on RemoteOSGiMessage from R-OSGi project
 * Only REMOTE_CALL and REMOTE_CALL_RESULT messages are implemented
 */
public abstract class ROSGiMessage {
	
	public static final short REMOTE_CALL = 5;
	public static final short REMOTE_CALL_RESULT = 6;
	
	private short funcID;
	protected int xid;

	ROSGiMessage(final short funcID) {
		this.funcID = funcID;
	}

	public final int getXID() {
		return xid;
	}

	public void setXID(final int xid) {
		this.xid = xid;
	}

	public final short getFuncID() {
		return funcID;
	}

	/**
	 * reads in a network packet and constructs the corresponding subtype of
	 * R-OSGiMessage from it. The header is:
	 *   0                   1                   2                   3
	 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |    Version    |         Function-ID           |     XID       |
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |    XID cntd.  | 
	 *  +-+-+-+-+-+-+-+-+
	 *  
	 *  The body is added by the message subclasses
	 */
	public static ROSGiMessage parse(final ObjectInputStream input)
			throws IOException, ClassNotFoundException {
		input.readByte(); // version, currently unused
		final short funcID = input.readByte();
		final int xid = input.readInt();
		ROSGiMessage msg = null;
		switch (funcID) {
		case REMOTE_CALL:
			msg = new RemoteCallMessage(input);
			break;
		case REMOTE_CALL_RESULT:
			msg = new RemoteCallResultMessage(input);
			break;
		default:
			System.out.println("funcID " + funcID + " not supported."); 
		}
		msg.funcID = funcID;
		msg.xid = xid;
		return msg;
	}

	public final void send(final ObjectOutputStream out) throws IOException {
		synchronized (out) {
			out.write(1);
			out.write(funcID);
			out.writeInt(xid);
			writeBody(out);
			out.reset();
			out.flush();
		}
	}

	protected abstract void writeBody(final ObjectOutputStream output)
			throws IOException;

}
