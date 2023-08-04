package dev.theturkey.twitchminimal.restapi;


import dev.theturkey.twitchminimal.restapi.objects.TwitchAPIError;

public class ResponseObject<T>
{
	public T data;
	public TwitchAPIError error;

	public ResponseObject(T data, TwitchAPIError error)
	{
		this.data = data;
		this.error = error;
	}
}
