package dev.theturkey.twitchminimal.websocketirc;

public record MessageEvent(String channel, String sender, String message)
{
	public String getSender()
	{
		return sender;
	}

	public String getMessage()
	{
		return message;
	}

	public String getChannel()
	{
		return channel;
	}

	public void respond(String message)
	{
		TwitchIRCWebSocketCore.sendMessage(channel, "@" + sender + " " + message);
	}

	public void respondWith(String message)
	{
		TwitchIRCWebSocketCore.sendMessage(channel, message);
	}
}
