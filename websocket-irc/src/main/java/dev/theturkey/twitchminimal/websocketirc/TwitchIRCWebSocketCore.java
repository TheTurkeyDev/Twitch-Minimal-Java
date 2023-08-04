package dev.theturkey.twitchminimal.websocketirc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchIRCWebSocketCore
{
	private static final Map<IRCWebsocket, List<String>> ircWSChannels = new HashMap<>();

	public static String botOAuth;
	public static String botName;

	private static MessageHandler messageHandler;

	public static void init(String botOAuth, String botName)
	{
		TwitchIRCWebSocketCore.botOAuth = botOAuth;
		TwitchIRCWebSocketCore.botName = botName;
	}

	public static void sendMessage(String channel, String message)
	{
		for(Map.Entry<IRCWebsocket, List<String>> entry : ircWSChannels.entrySet())
		{
			for(String c : entry.getValue())
			{
				if(c.equalsIgnoreCase(channel))
				{
					entry.getKey().sendMessage(channel, message);
					return;
				}
			}
		}
	}

	public static void joinChannel(String channel)
	{
		for(Map.Entry<IRCWebsocket, List<String>> entry : ircWSChannels.entrySet())
		{
			if(entry.getValue().size() < 5)
			{
				entry.getValue().add(channel);
				entry.getKey().joinChannel(channel);
				return;
			}
		}

		try
		{
			IRCWebsocket irc = new IRCWebsocket();
			List<String> channels = new ArrayList<>();
			channels.add(channel);
			irc.registerMessageHandler(messageHandler);
			irc.run();
			irc.joinChannel(channel);
			ircWSChannels.put(irc, channels);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void leaveChannel(String channel)
	{
		IRCWebsocket toRemove = null;
		for(Map.Entry<IRCWebsocket, List<String>> entry : ircWSChannels.entrySet())
		{
			if(entry.getValue().contains(channel))
			{
				entry.getValue().remove(channel);
				entry.getKey().leaveChannel(channel);
				if(entry.getValue().size() == 0)
					toRemove = entry.getKey();
			}
		}

		if(toRemove != null)
		{
			toRemove.shutdown();
			ircWSChannels.remove(toRemove);
		}
	}

	public static void registerMessageHandler(MessageHandler handler)
	{
		messageHandler = handler;
	}

	public static Map<IRCWebsocket, List<String>> getIrcWSChannels()
	{
		return ircWSChannels;
	}
}
