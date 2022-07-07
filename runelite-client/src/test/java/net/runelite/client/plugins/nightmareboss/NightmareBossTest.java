package net.runelite.client.plugins.nightmareboss;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NightmareBossTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NightmareBossPlugin.class);
		RuneLite.main(args);
	}
}