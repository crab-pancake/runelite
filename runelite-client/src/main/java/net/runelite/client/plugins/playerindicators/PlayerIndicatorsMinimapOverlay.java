/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.playerindicators;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class PlayerIndicatorsMinimapOverlay extends Overlay
{
	private final PlayerIndicatorsService playerIndicatorsService;
	private final PlayerIndicatorsConfig config;
	private final PlayerIndicatorsPlugin plugin;

	@Inject
	private PlayerIndicatorsMinimapOverlay(PlayerIndicatorsConfig config, PlayerIndicatorsService playerIndicatorsService, PlayerIndicatorsPlugin plugin)
	{
		this.config = config;
		this.playerIndicatorsService = playerIndicatorsService;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		playerIndicatorsService.forEachPlayer((player, decorations) -> renderPlayerOverlay(graphics, player, decorations));
		return null;
	}

	private void renderPlayerOverlay(Graphics2D graphics, Player player, PlayerIndicatorsService.Decorations decorations)
	{
		if (config.drawMinimapNames())  // || (plugin.pvpZone && config.enableInPvP())
		{
			final net.runelite.api.Point minimapLocation = player.getMinimapLocation();

			final String name = player.getName().replace('\u00A0', ' ') + (plugin.pvpZone ? " ("+player.getCombatLevel()+")" : "");

			if (minimapLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, minimapLocation, name, decorations.getColor());
			}
		}
	}
}
