/*
 * Copyright (c) 2020, Hydrox6 <ikada@protonmail.ch>
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
package net.runelite.client.plugins.clodern.Stuff;

import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;

public class CustomWidgetToggleButton extends CustomWidgetWithIcon implements InteractibleWidget
{
	private final Client client;
	private final int selectedBackgroundSprite;
	private final int backgroundSpriteID;

	private String action;

	private final WidgetBooleanCallback callback;

	public CustomWidgetToggleButton(final Client client, final Widget parent, final String name, int backgroundSprite, int selectedBackgroundSprite, int iconSprite, final WidgetBooleanCallback callback)
	{
		super(parent, name, iconSprite);
		this.client = client;
		this.backgroundSpriteID = backgroundSprite;
		this.selectedBackgroundSprite = selectedBackgroundSprite;
		this.callback = callback;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	@Override
	public void layout(int x, int y)
	{
		layoutWidget(base, x, y);

		super.layout(x, y);
	}

	@Override
	public void create()
	{
		base = createSpriteWidget(width, height);
		base.setSpriteId(backgroundSpriteID);

		icon = createSpriteWidget(iconWidth, iconHeight);
		icon.setSpriteId(iconSpriteID);

		base.setOnOpListener((JavaScriptCallback) this::onButtonClicked);
		base.setHasListener(true);
		base.setAction(1, action);
	}

	@Override
	public void onButtonClicked(ScriptEvent scriptEvent)
	{
		callback.run();
		// this probably won't work: varcint hasnt changed yet
		if (client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == 10)
		{
			base.setSpriteId(selectedBackgroundSprite);
		}
		else {
			base.setSpriteId(backgroundSpriteID);
		}
	}
}