package net.runelite.client.plugins.clodern;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import static net.runelite.client.plugins.clodern.ClodernConfig.GROUP;

@ConfigGroup(GROUP)
public interface ClodernConfig extends Config
{
	@ConfigItem(
		keyName = "moveTopBar",
		name = "Move top bar when inventory is open",
		description = "Whether to move the top bar to the top of the inventory container (like resizable classic)",
		position = 1
	)
	default boolean moveTopBar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "inventoryPosition",
		name = "Inventory Position",
		description = "How the inventory should snap to the bottom bar",
		position = 2
	)
	default Position inventoryPosition()
	{
		return Position.RIGHT;
	}

	@ConfigItem(
		keyName = "logoutDoor",
		name = "Replace log-out door",
		description = "Add the log-out door back to the bottom tab",
		position = 3
	)
	default boolean logoutDoor()
	{
		return true;
	}

	enum Position{
		LEFT,
		CENTRE,
		RIGHT
	}

	final String GROUP = "clodern";
}
