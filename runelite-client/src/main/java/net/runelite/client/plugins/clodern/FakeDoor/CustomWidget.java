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
package net.runelite.client.plugins.clodern.FakeDoor;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

public abstract class CustomWidget
{
	protected final Widget parent;
	private final String name;

	protected int x;
	protected int y;
	protected int width;
	protected int height;

	protected Widget base;

	public static final String ORANGE_COLOUR_WIDGET_NAME = "<col=ff981f>";

	public CustomWidget(final Widget parent, final String name)
	{
		this.parent = parent;
		this.name = name;
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void layout(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public abstract void create();

	protected Widget createSpriteWidget(int width, int height)
	{
		final Widget w = parent.createChild(-1, WidgetType.GRAPHIC);
		w.setOriginalWidth(width);
		w.setOriginalHeight(height);
		w.setName(ORANGE_COLOUR_WIDGET_NAME + this.name);
		return w;
	}

	protected static void layoutWidget(Widget w, int x, int y)
	{
		w.setOriginalX(x);
		w.setOriginalY(y);
		w.revalidate();
	}
}