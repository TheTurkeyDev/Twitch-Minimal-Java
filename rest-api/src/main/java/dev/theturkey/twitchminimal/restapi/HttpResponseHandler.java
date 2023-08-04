package dev.theturkey.twitchminimal.restapi;

public interface HttpResponseHandler
{
	void onResponse(int code, String body);
}
