import dev.theturkey.twitchminimal.websocketirc.TwitchIRCWebSocketCore;

public class Test
{
	public static void main(String[] args)
	{
		TwitchIRCWebSocketCore.registerMessageHandler(event ->
		{
			System.out.println("HERE");
		});

		TwitchIRCWebSocketCore.init("", "");
		TwitchIRCWebSocketCore.joinChannel("");
	}
}
