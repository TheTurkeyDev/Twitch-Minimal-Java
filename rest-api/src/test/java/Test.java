import dev.theturkey.twitchminimal.restapi.TwitchAPI;
import dev.theturkey.twitchminimal.restapi.objects.TwitchUserInfo;

import java.util.Arrays;
import java.util.List;

public class Test
{
	public static void main(String[] args)
	{
		Secret.init();
		List<String> users = Arrays.asList("TurkeyDev", "Superfraggle", "SirAlexFrost", "MJRBot", "Aarimous");
		for(String user : users)
		{
			TwitchAPI.getUsers(user).onResponse((resp ->
			{
				if(resp.data != null)
				{
					for(TwitchUserInfo userInfo : resp.data)
						System.out.println(user + "'s ID is: " + userInfo.id + " created at " + userInfo.createdAt);
				}
				else
				{
					System.out.println("There was ane error! Data is null!");
				}
			}));
		}
	}
}
