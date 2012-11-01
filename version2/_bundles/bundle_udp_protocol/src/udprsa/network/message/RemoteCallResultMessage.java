package udprsa.network.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class RemoteCallResultMessage extends ROSGiMessage {

	private byte errorFlag;
	private Object result;
	private Throwable exception;

	public RemoteCallResultMessage(final Object result) {
		super(REMOTE_CALL_RESULT);
		
		this.result = result;
		errorFlag = 0;
	}

	public RemoteCallResultMessage(final Throwable t) {
		super(REMOTE_CALL_RESULT);
		
		this.exception = t;
		this.errorFlag = 1;
	}
	
	/**
	 * creates a new MethodResultMessage from network packet:
	 * 
	 *       0                   1                   2                   3
	 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |       R-OSGi header (function = Service = 2)                  |
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |  error flag   | result or Exception                           \
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	RemoteCallResultMessage(final ObjectInputStream input) throws IOException,
			ClassNotFoundException {
		super(REMOTE_CALL_RESULT);
		errorFlag = input.readByte();
		if (errorFlag == 0) {
			result = input.readObject();
			exception = null;
		} else {
			exception = (Throwable) input.readObject();
			result = null;
		}
	}

	public void writeBody(final ObjectOutputStream out) throws IOException {
		if (exception == null) {
			out.writeByte(0);
			out.writeObject(result);
		} else {
			out.writeByte(1);
			out.writeObject(exception);
		}
	}

	public boolean causedException() {
		return (errorFlag == 1);
	}

	public Object getResult() {
		return result;
	}

	public Throwable getException() {
		return exception;
	}

	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[REMOTE_CALL_RESULT] - XID: "); //$NON-NLS-1$
		buffer.append(xid);
		buffer.append(", errorFlag: "); //$NON-NLS-1$
		buffer.append(errorFlag);
		if (causedException()) {
			buffer.append(", exception: "); //$NON-NLS-1$
			buffer.append(exception.getMessage());
		} else {
			buffer.append(", result: "); //$NON-NLS-1$
			buffer.append(result);
		}
		return buffer.toString();
	}
}
