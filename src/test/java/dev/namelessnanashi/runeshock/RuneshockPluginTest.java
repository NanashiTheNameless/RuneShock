package dev.namelessnanashi.runeshock;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneshockPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuneshockPlugin.class);
		RuneLite.main(args);
	}
}
