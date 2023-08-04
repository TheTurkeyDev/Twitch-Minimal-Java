package dev.theturkey.twitchminimal.websocketirc;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IRCWebsocket
{
	private final ScheduledExecutorService sss = Executors.newSingleThreadScheduledExecutor();

	private final URI uri;

	private WebSocketClient client;

	private boolean connected = false;

	private final ScheduledExecutorService messageQueueExecutor;
	private final Map<String, List<String>> messageQueue = new HashMap<>();

	private MessageHandler messageHandler;

	public IRCWebsocket() throws URISyntaxException
	{
		uri = new URI("wss://irc-ws.chat.twitch.tv:443");
		messageQueueExecutor = Executors.newSingleThreadScheduledExecutor();
		messageQueueExecutor.scheduleAtFixedRate(() ->
		{
			if(!isConnected())
				return;

			List<String> channelsToSend = new ArrayList<>(messageQueue.keySet());
			for(String chan : channelsToSend)
			{
				if(messageQueue.get(chan).size() > 0)
				{
					sendRaw("PRIVMSG " + getChannelClean(chan) + " :" + messageQueue.get(chan).remove(0));
					if(messageQueue.get(chan).size() == 0)
						messageQueue.remove(chan);
				}
			}

		}, 0, 500, TimeUnit.MILLISECONDS);

	}

	public void run()
	{
		if(messageQueueExecutor.isShutdown())
			return;

		try
		{
			connect();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void reconnect()
	{
		if(!messageQueueExecutor.isShutdown())
			sss.schedule(IRCWebsocket.this::run, 3, TimeUnit.SECONDS);
	}

	private void connect() throws Exception
	{
		client = createClient();
		client.connectBlocking();
	}

	private WebSocketClient createClient()
	{
		WebSocketClient client = new WebSocketClient(uri)
		{
			@Override
			public void onOpen(ServerHandshake handshake)
			{
				connected = true;
				sendRaw("PASS " + TwitchIRCWebSocketCore.botOAuth);
				sendRaw("NICK " + TwitchIRCWebSocketCore.botName);
			}

			@Override
			public void onMessage(String message)
			{
				int firstSpace = message.indexOf(" ");
				int secondSpace = message.indexOf(" ", firstSpace + 1);
				if(secondSpace >= 0)
				{
					String from = message.substring(0, firstSpace);
					String code = message.substring(firstSpace + 1, secondSpace);
					String rest = message.substring(secondSpace + 1);

					if("PRIVMSG".equals(code))
					{
						int space = rest.indexOf(" :");
						String channel = rest.substring(0, space);
						String sentMessage = rest.substring(space + 2).trim();
						String sender = from.substring(1, from.indexOf("!"));
						MessageEvent messageEvent = new MessageEvent(channel, sender, sentMessage);
						if(messageHandler != null)
							messageHandler.handleMessage(messageEvent);
					}
				}
			}

			@Override
			public void onError(Exception ex)
			{
				System.out.println("onError");
				ex.printStackTrace();
			}

			@Override
			public void onClose(int code, String reason, boolean remote)
			{
				IRCWebsocket.this.reconnect();
			}
		};
		// If not receive any message from server more than 10s, close the connection
		client.setConnectionLostTimeout(10);
		return client;
	}

	public IRCWebsocket registerMessageHandler(MessageHandler handler)
	{
		this.messageHandler = handler;
		return this;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public void sendRaw(String s)
	{
		if(connected)
			client.send(s);
	}

	public void sendMessage(String channel, String message)
	{
		messageQueue.computeIfAbsent(channel, c -> new ArrayList<>()).add(message);
	}

	public void joinChannel(String channel)
	{
		sendRaw("JOIN " + getChannelClean(channel));
	}

	public void leaveChannel(String channel)
	{
		sendRaw("PART " + getChannelClean(channel) + " :leaving");
	}

	public String getChannelClean(String channel)
	{
		return (channel.startsWith("#") ? "" : "#") + channel.toLowerCase();
	}

	public void shutdown()
	{
		messageQueueExecutor.shutdown();
		sss.shutdown();
		client.close();
	}
}
