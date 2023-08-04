package dev.theturkey.twitchminimal.restapi;

public class TwitchAPIRequestData
{
	public String url;
	public String reqType;
	public String body;
	public String authToken;
	public HttpResponseWrapper response;

	// Time until we stop ignoring this request
	public long holdTime;

	public TwitchAPIRequestData(String url, String reqType, String body, HttpResponseWrapper response)
	{
		this(url, reqType, body, null, response);
	}

	public TwitchAPIRequestData(String url, String reqType, String body, String authToken, HttpResponseWrapper response)
	{
		this.url = url;
		this.reqType = reqType;
		this.body = body;
		this.authToken = authToken;
		this.response = response;
	}

	public void onComplete(int code, String body)
	{
		if(response != null)
			response.resolveCall(code, body);
	}
}
