package dev.theturkey.twitchminimal.restapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.theturkey.twitchminimal.restapi.objects.BroadcasterType;
import dev.theturkey.twitchminimal.restapi.objects.EnumDeserializer;
import dev.theturkey.twitchminimal.restapi.objects.EnumSerializer;
import dev.theturkey.twitchminimal.restapi.objects.IDEnum;
import dev.theturkey.twitchminimal.restapi.objects.TwitchAPIError;
import dev.theturkey.twitchminimal.restapi.objects.TwitchGame;
import dev.theturkey.twitchminimal.restapi.objects.TwitchUserInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TwitchAPI
{
	public static Gson GSON;
	public static HttpClient CLIENT = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.build();
	public static String URL_BASE = "https://api.twitch.tv/helix/";
	private static String appToken;
	private static int bucketPoints;
	private static int bucketResetTime;
	private static final ConcurrentLinkedQueue<TwitchAPIRequestData> REQUEST_QUEUE = new ConcurrentLinkedQueue<>();
	private static final Thread REQUEST_THREAD;

	private static String clientID;
	private static String clientSecret;

	static
	{
		GsonBuilder builder = new GsonBuilder();
		registerEnumTypeAdapters(builder, BroadcasterType.class, BroadcasterType::getFromId);
//		registerEnumTypeAdapters(builder, DiscordApplicationCommandOptionType.class, DiscordApplicationCommandOptionType::getFromId);
//		registerEnumTypeAdapters(builder, DiscordComponentType.class, DiscordComponentType::getFromId);
//		registerEnumTypeAdapters(builder, DiscordInteractionType.class, DiscordInteractionType::getFromId);
//		registerEnumTypeAdapters(builder, DiscordInteractionCallbackType.class, DiscordInteractionCallbackType::getFromId);
		GSON = builder.create();

		REQUEST_THREAD = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(true)
				{
					TwitchAPIRequestData reqData = REQUEST_QUEUE.poll();

					if(reqData != null)
					{
						long now = System.currentTimeMillis();
						//TODO: Inefficient
						if(reqData.holdTime > now)
						{
							REQUEST_QUEUE.add(reqData);
							continue;
						}
						if(bucketPoints <= 0 && now <= bucketResetTime)
						{
							reqData.holdTime = bucketResetTime;
							REQUEST_QUEUE.add(reqData);
							continue;
						}

						HttpRequest request = getDefaultReq(URL_BASE + reqData.url, reqData.reqType, reqData.body, reqData.authToken);
						HttpResponse<String> r;
						try
						{
							//TODO: Don't get right away
							r = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
						} catch(Exception e)
						{
							e.printStackTrace();
							continue;
						}

						int limit = Integer.parseInt(r.headers().firstValue("Ratelimit-Limit").orElse("0"));
						bucketPoints = Integer.parseInt(r.headers().firstValue("Ratelimit-Remaining").orElse("0"));
						bucketResetTime = Integer.parseInt(r.headers().firstValue("Ratelimit-Reset").orElse("0"));


						System.out.println("Your bucket limit is: " + limit);
						System.out.println("Your have : " + bucketPoints + " points left");
						System.out.println("Your bucket resets in: " + ((bucketResetTime - now) / 1000D) + " seconds");

						reqData.onComplete(r.statusCode(), r.body());
					}
					else
					{
						synchronized(REQUEST_THREAD)
						{
							try
							{
								REQUEST_THREAD.wait();
							} catch(InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		REQUEST_THREAD.start();
	}

	private static <T extends IDEnum> void registerEnumTypeAdapters(GsonBuilder gsonBuilder, Class<T> t, EnumDeserializer<IDEnum> deserializer)
	{
		gsonBuilder.registerTypeAdapter(t, new EnumSerializer<T>());
		gsonBuilder.registerTypeAdapter(t, deserializer);
	}

	public static void setClientInfo(String clientID, String clientSecret)
	{
		TwitchAPI.clientID = clientID;
		TwitchAPI.clientSecret = clientSecret;
	}

	private static HttpResponseWrapper sendRestCall(String url, String reqType)
	{
		return sendRestCall(url, reqType, null);
	}

	private static HttpResponseWrapper sendRestCall(String url, String reqType, String body)
	{
		HttpResponseWrapper response = new HttpResponseWrapper();
		REQUEST_QUEUE.add(new TwitchAPIRequestData(url, reqType, body, response));
		if(REQUEST_QUEUE.size() == 1)
		{
			synchronized(REQUEST_THREAD)
			{
				REQUEST_THREAD.notifyAll();
			}
		}
		return response;
	}

	public static HttpRequest getDefaultReq(String url, String reqType, String body, String authToken)
	{
		return HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofMinutes(1))
				.header("Authorization", "Bearer " + (authToken == null ? getAppToken() : authToken))
				.header("Client-Id", clientID)
				.header("Content-Type", "application/json")
				.header("User-Agent", "Twitch-Minimal-Java")
				.method(reqType, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
				.build();
	}


	private static <T> ResponseObject<T> getResponseObj(int code, String body, Class<T> clazz)
	{
		if(code != 200)
		{
			return new ResponseObject<>(null, GSON.fromJson(body, TwitchAPIError.class));
		}
		else
		{
			JsonElement json = JsonParser.parseString(body);
			if(!json.isJsonObject() || !json.getAsJsonObject().has("data"))
				return new ResponseObject<>(null, new TwitchAPIError());
			return new ResponseObject<>(GSON.fromJson(json, clazz), null);
		}
	}

	private static <T> ResponseObject<List<T>> getResponseObjList(int code, String body, Class<T> clazz)
	{
		if(code != 200)
		{
			return new ResponseObject<>(null, GSON.fromJson(body, TwitchAPIError.class));
		}
		else
		{
			//TODO Add pagination support
			JsonElement json = JsonParser.parseString(body);
			if(!json.isJsonObject() || !json.getAsJsonObject().has("data"))
				return new ResponseObject<>(null, new TwitchAPIError());
			JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray("data");
			List<T> commands = new ArrayList<>();
			for(JsonElement e : jsonArray)
				commands.add(GSON.fromJson(e, clazz));
			return new ResponseObject<>(commands, null);
		}
	}

	public static String getAppToken()
	{
		if(isAppTokenExpired())
		{
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://id.twitch.tv/oauth2/token?client_id=" + clientID + "&client_secret=" + clientSecret + "&grant_type=client_credentials"))
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();
			try
			{
				HttpResponse<String> r = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
				JsonElement json = JsonParser.parseString(r.body());
				if(json.isJsonObject())
					appToken = json.getAsJsonObject().get("access_token").getAsString();
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return appToken;
	}

	private static boolean isAppTokenExpired()
	{
		return appToken == null;
	}


	public static TwitchAPIResponse<List<TwitchUserInfo>> getUsers(long... ids)
	{
		long[] slicedID = Arrays.copyOfRange(ids, 0, Math.min(100, ids.length));
		TwitchAPIResponse<List<TwitchUserInfo>> apiResp = new TwitchAPIResponse<>();
		StringBuilder url = new StringBuilder("users?");
		for(long userId : slicedID)
			url.append("id=").append(userId).append("&");
		sendRestCall(url.toString(), "GET").onResponse((code, body) ->
				apiResp.resolveCall(getResponseObjList(code, body, TwitchUserInfo.class)));

		return apiResp;
	}

	public static TwitchAPIResponse<List<TwitchUserInfo>> getUsers(String... logins)
	{
		String[] slicedLogins = Arrays.copyOfRange(logins, 0, Math.min(100, logins.length));
		TwitchAPIResponse<List<TwitchUserInfo>> apiResp = new TwitchAPIResponse<>();
		StringBuilder url = new StringBuilder("users?");
		for(String login : slicedLogins)
			url.append("login=").append(login).append("&");
		sendRestCall(url.toString(), "GET").onResponse((code, body) ->
				apiResp.resolveCall(getResponseObjList(code, body, TwitchUserInfo.class)));

		return apiResp;
	}

	public static TwitchAPIResponse<List<TwitchGame>> getGames(String... ids)
	{
		String[] slicedIds = Arrays.copyOfRange(ids, 0, Math.min(100, ids.length));
		TwitchAPIResponse<List<TwitchGame>> apiResp = new TwitchAPIResponse<>();
		StringBuilder url = new StringBuilder("games?");
		for(String id : slicedIds)
			url.append("id=").append(id).append("&");
		sendRestCall(url.toString(), "GET").onResponse((code, body) ->
				apiResp.resolveCall(getResponseObjList(code, body, TwitchGame.class)));

		return apiResp;
	}
}
