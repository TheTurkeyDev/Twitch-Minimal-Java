package dev.theturkey.twitchminimal.restapi.objects;

import com.google.gson.JsonElement;

public class TwitchAPIError
{
	public int code;
	public String message;
	public JsonElement errors;
}
