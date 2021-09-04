package net.runelite.client.plugins.nightmareboss;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("example")
public interface NightmareBossConfig extends Config
{
	@Alpha
	@ConfigItem(
			position = 0,
			keyName = "warningColour",
			name = "Warning colour",
			description = "Colour of the hand outline if underneath you"
	)
	default Color warningColour()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 1,
			keyName = "outlineColour",
			name = "Outline colour",
			description = "Colour of the hand outline otherwise"
	)
	default Color colour()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
			position = 1,
			keyName = "width",
			name = "Outline Width",
			description = "Width of the outline"
	)
	default double width()
	{
		return 1;
	}

	@ConfigItem(
			position = 3,
			keyName = "outlineFeather",
			name = "Outline feather",
			description = "Specify between 0-4 how much of the model outline should be faded"
	)
	@Range(
			min = 0,
			max = 4
	)
	default int outlineFeather()
	{
		return 0;
	}

}
