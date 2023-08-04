package dev.theturkey.twitchminimal.restapi.objects;

import com.google.gson.annotations.SerializedName;

public class TwitchGame
{
	/**
	 * Template URL for the game’s box art.
	 */
	@SerializedName("box_art_url")
	public String boxArtUrl;

	/**
	 * Game ID.
	 */
	public String id;

	/**
	 * Game name.
	 */
	public String name;
}
