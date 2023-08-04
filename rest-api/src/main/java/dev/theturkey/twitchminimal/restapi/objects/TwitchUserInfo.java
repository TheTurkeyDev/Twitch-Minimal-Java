package dev.theturkey.twitchminimal.restapi.objects;

import com.google.gson.annotations.SerializedName;

public class TwitchUserInfo
{
	/**
	 * User’s broadcaster type: "partner", "affiliate", or "".
	 */
	@SerializedName("broadcaster_type")
	public BroadcasterType broadcasterType;

	/**
	 * User’s channel description.
	 */
	public String description;

	/**
	 * User’s display name.
	 */
	@SerializedName("display_name")
	public String displayName;

	/**
	 * User’s ID.
	 */
	public String id;

	/**
	 * User’s login name.
	 */
	public String login;

	/**
	 * URL of the user’s offline image.
	 */
	@SerializedName("offline_image_url")
	public String offlineImageURL;

	/**
	 * URL of the user’s profile image.
	 */
	@SerializedName("profile_image_url")
	public String profileImageURL;

	/**
	 * User’s type: "staff", "admin", "global_mod", or "".
	 */
	public String type;

	/**
	 * Total number of views of the user’s channel.
	 */
	@SerializedName("view_count")
	public String viewCount;

	/**
	 * User’s verified email address. Returned if the request includes the user:read:email scope.
	 */
	public String email;

	/**
	 * Date when the user was created.
	 */
	@SerializedName("created_at")
	public String createdAt;
}
