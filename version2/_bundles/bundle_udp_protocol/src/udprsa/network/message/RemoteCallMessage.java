package udprsa.network.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public final class RemoteCallMessage extends ROSGiMessage {

	private String serviceID;
	private String methodSignature;
	private Object[] arguments;

	public RemoteCallMessage(String serviceID, String methodSignature, Object[] args) {
		super(REMOTE_CALL);
		
		this.serviceID = serviceID;
		this.methodSignature = methodSignature;
		this.arguments = args;
	}
	
	/**
	 * creates a new InvokeMethodMessage from network packet:
	 *       0                   1                   2                   3
	 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |       R-OSGi header (function = InvokeMsg = 3)                |
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |   length of &lt;serviceID&gt;     |    &lt;serviceID&gt; String       \
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |    length of &lt;MethodSignature&gt;     |     &lt;MethodSignature&gt; String       \
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |   number of param blocks      |     Param blocks (if any)     \
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * 
	 */
	RemoteCallMessage(final ObjectInputStream input) throws IOException,
			ClassNotFoundException {
		super(REMOTE_CALL);

		serviceID = input.readUTF();
		methodSignature = input.readUTF();
		final short argLength = input.readShort();
		arguments = new Object[argLength];
		for (short i = 0; i < argLength; i++) {
			arguments[i] = input.readObject();
		}
	}

	public void writeBody(final ObjectOutputStream out) throws IOException {
		out.writeUTF(serviceID);
		out.writeUTF(methodSignature);
		if(arguments!=null){
			out.writeShort(arguments.length);
			for (short i = 0; i < arguments.length; i++) {
				out.writeObject(arguments[i]);
			}
		} else {
			out.writeShort(0);
		}
	}

	public String getServiceID() {
		return serviceID;
	}

	public Object[] getArgs() {
		return arguments;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[REMOTE_CALL] - XID: ");
		buffer.append(xid);
		buffer.append(", serviceID: ");
		buffer.append(serviceID);
		buffer.append(", methodName: ");
		buffer.append(methodSignature);
		buffer.append(", params: ");
		buffer.append(arguments == null ? "" : Arrays.asList(arguments)
				.toString());
		return buffer.toString();
	}
}
