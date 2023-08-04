package dev.theturkey.twitchminimal.restapi.objects;

public enum BroadcasterType implements IDEnum
{
	PARTNER("partner"),
	AFFILIATE("affiliate"),
	NONE("");

	private String id;

	BroadcasterType(String id)
	{
		this.id = id;
	}

	@Override
	public String getSerialized()
	{
		return this.id;
	}

	public static BroadcasterType getFromId(String id)
	{
		for(BroadcasterType type : BroadcasterType.values())
			if(type.id.equals(id))
				return type;
		return BroadcasterType.NONE;
	}
}
