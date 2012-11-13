package rsaother;

import org.restlet.resource.Get;

public interface IRemoteRestCall {
	@Get
	public Object doCall(Object[] args);
}
