package dev.theturkey.twitchminimal.websocketirc;

public interface MessageHandler
{
	void handleMessage(MessageEvent messageEvent);
}
