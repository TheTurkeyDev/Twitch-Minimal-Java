package dev.theturkey.twitchminimal.restapi.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class EnumSerializer<T extends IDEnum> implements JsonSerializer<T>
{
	@Override
	public JsonElement serialize(T type, Type typeOfSrc, JsonSerializationContext context)
	{
		return new JsonPrimitive(type.getSerialized());
	}
}
