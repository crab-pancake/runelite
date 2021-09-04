/*
 * Copyright (c) 2019, Hexagon <hexagon@fking.work>
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
package net.runelite.client.plugins.nightmareboss;

import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

class NightmareBossOverlay extends Overlay
{
	private final Client client;
	private final NightmareBossConfig config;
	private final NightmareBossPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private NightmareBossOverlay(Client client, NightmareBossConfig config, NightmareBossPlugin plugin,
		ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
//		Stroke stroke = new BasicStroke((float) config.width());
		List<GraphicsObject> hands = plugin.getHands();
		if (!hands.isEmpty())
		{
			hands.removeIf(GraphicsObject::finished);

			for (GraphicsObject hand : hands)
			{
				final Player localPlayer = client.getLocalPlayer();
				final LocalPoint handLocation = hand.getLocation();
				modelOutlineRenderer.drawOutline(hand, (int) config.width(),
						localPlayer.getLocalLocation().equals(handLocation) ? config.warningColour(): config.colour(),
						config.outlineFeather());
//				if (polygon != null)
//				{
//					OverlayUtil.renderPolygon(graphics, polygon, localPlayer.getLocalLocation().equals(graphicsObjectLocation) ? Color.RED : config.colour(), stroke);
//				}
			}
		}
		return null;
	}
}
