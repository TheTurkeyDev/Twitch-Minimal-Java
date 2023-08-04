package dev.theturkey.twitchminimal.restapi;

public class TwitchAPIResponse<T>
{
	private PendingResponseObject<T> pendingResponse;

	public void resolveCall(ResponseObject<T> resp)
	{
		if(pendingResponse != null)
			pendingResponse.onResponse(resp);
	}

	public TwitchAPIResponse<T> onResponse(PendingResponseObject<T> pendingResponse)
	{
		this.pendingResponse = pendingResponse;
		return this;
	}
}
