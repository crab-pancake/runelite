/*
 * Copyright (c) 2021, Hydrox6 <ikada@protonmail.ch>
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

import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.clodern.Clodern;

public class FakeDoor
{
	private final Clodern plugin;
	public CustomWidgetToggleButton logoutButton = null;

	@Inject
	public FakeDoor(Clodern plugin)
	{
		this.plugin = plugin;
	}

	public void create(){
		final Widget parent = plugin.getBottomBar();
		if (parent == null)
			return;

		logoutButton = new CustomWidgetToggleButton(
			plugin.client,
			parent,
			"",
			SpriteID.RESIZEABLE_MODE_TAB_STONE_MIDDLE_SELECTED,
			SpriteID.RESIZEABLE_MODE_TAB_STONE_MIDDLE_SELECTED,
			SpriteID.RS2_TAB_LOGOUT,
			() ->
			{
				System.out.println("logout button clicked!");
				plugin.clientThread.invoke(() -> plugin.client.runScript(915, 10));
			});
//		logoutButton.setAction("Logout");
		logoutButton.create();
//		logoutButton.setSize(33, 36);
//		logoutButton.setIconSize(33, 36);
//		logoutButton.layout(4 * 33, 0);
	}

	public void info(){
		System.out.println("pos: "+logoutButton.base.getBounds().getX()+", "+logoutButton.base.getBounds().getY());
		System.out.println("actions: "+ Arrays.toString(logoutButton.base.getActions()));
	}

	public void destroy(){

	}
}