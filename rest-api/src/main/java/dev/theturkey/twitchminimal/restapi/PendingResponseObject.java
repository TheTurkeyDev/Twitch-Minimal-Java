package dev.theturkey.twitchminimal.restapi;

public interface PendingResponseObject<T>
{
	void onResponse(ResponseObject<T> resp);
}

