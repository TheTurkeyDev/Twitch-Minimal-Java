package dev.theturkey.twitchminimal.restapi;

public class HttpResponseWrapper
{
	private HttpResponseHandler pendingResponse;

	public void resolveCall(int code, String body)
	{
		if(pendingResponse != null)
			pendingResponse.onResponse(code, body);
	}

	public HttpResponseWrapper onResponse(HttpResponseHandler pendingResponse)
	{
		this.pendingResponse = pendingResponse;
		return this;
	}
}
