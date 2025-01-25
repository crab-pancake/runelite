package net.runelite.client.plugins.clodern;

import com.google.inject.Provides;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import static net.runelite.client.plugins.clodern.ClodernConfig.GROUP;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.WidgetOverlay;

@Slf4j
@PluginDescriptor(
	name = "Resizable Clodern"
)
public class Clodern extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClodernConfig config;

	@Inject
	private OverlayManager overlayManager;

	private boolean inventoryWasHidden;

	private Widget bottomBar;
	private Widget topBar;
	private Widget inventoryBox;

	private WidgetOverlay topBarOverlay;
	private WidgetOverlay inventoryBoxOverlay;

	private WidgetOverlay logoutDoor;

	private final int TAB_X_OFFSET_SIZE = 33;

	private static final Map<Integer, Integer> offsets = Map.ofEntries(
		Map.entry(38,0),Map.entry(44,0),
		Map.entry(39,1),Map.entry(45,1),
		Map.entry(40,2),Map.entry(46,2),
		Map.entry(41,4),Map.entry(47,4),
		Map.entry(42,5),Map.entry(48,5),
		Map.entry(43,6),Map.entry(49,6)
		);

	@Override
	protected void startUp() throws Exception
	{
		inventoryWasHidden = false;

		bottomBar = null;
		topBar = null;
		inventoryBox = null;

		topBarOverlay = null;
		inventoryBoxOverlay = null;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e){
		if (!GROUP.equals(e.getGroup()))
			return;

		if ("logoutDoor".equals(e.getKey())){
			clientThread.invoke(this::checkLogoutDoor);
		}
//		else if ("moveTopBar".equals(e.getKey())){
//			// if turned OFF: return to original positions? TODO
//		}
	}

	@Subscribe
	private void onScriptPostFired(ScriptPostFired e){
		if (e.getScriptId() != 907 && e.getScriptId() != 6010)
			return;

		clientThread.invoke(this::checkLogoutDoor);
	}

	private void checkLogoutDoor()
	{
		if (config.logoutDoor()){
			// move tabs, unhide the classic logout door and put it in the right spot (also set parent? (unset parent when changing interface style?))
			Widget bottomBarButtonContainer = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,37);
			if (bottomBarButtonContainer == null || bottomBarButtonContainer.isHidden())
				return;
			bottomBarButtonContainer.setRelativeX(0);
			bottomBarButtonContainer.setWidth(231);

			// shuffle all (?) the buttons and icons to the left
			offsets.forEach((id, offset) -> {
				Widget button = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,id);
				if (button == null)
					return;
				button.setRelativeX(offset * TAB_X_OFFSET_SIZE);
			});

			// unhide logout door
			// TODO: need to add a new overlay for this i think
		}
		else {
			Widget bottomBarButtonContainer = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,37);
			if (bottomBarButtonContainer == null)
				return;
			bottomBarButtonContainer.revalidate();

			offsets.forEach((id, offset) -> {
				Widget button = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,id);
				if (button == null)
					return;
				button.revalidate();
			});
		}
	}

	@Subscribe
	private void onVarClientIntChanged(VarClientIntChanged e){
		if (e.getIndex() != VarClientInt.INVENTORY_TAB || !config.moveTopBar())
			return;

		// make sure all the stuff exists
		if (bottomBar == null)
			bottomBar = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS1);
		if (topBar == null)
			topBar = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS1);
		if (inventoryBox == null)
			inventoryBox = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_PARENT);
		if (topBarOverlay == null)
		{
			topBarOverlay = overlayManager.get(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS2);
			if (topBarOverlay == null)
			{
//				log.warn("couldn't find top bar widgetoverlay");
				return;
			}
		}

		if (inventoryBoxOverlay == null)
		{
			inventoryBoxOverlay = overlayManager.get(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_PARENT);
			if (inventoryBoxOverlay == null)
			{
//				log.warn("couldn't find inventory box widgetoverlay");
				return;
			}
		}

		if (bottomBar == null || bottomBar.isHidden() || topBar == null || topBar.isHidden())
			return;

		// snap top bar to bottom bar when inventory box is hidden
		if (client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == -1){

			inventoryWasHidden = true;
//			log.debug("inventory is hidden, move top bar");

			topBarOverlay.setPreferredLocation(new Point(bottomBar.getRelativeX(), bottomBar.getRelativeY() - topBar.getHeight()));
		}

		// snap invy to bottom bar, top bar to invy when invy is unhidden
		else {
			if (!inventoryWasHidden)
			{
				if (config.logoutDoor())
					clientThread.invoke(this::checkLogoutDoor);
				return;
			}

			inventoryWasHidden = false;
//			log.debug("inventory un-hidden, move inventory box and top bar");

			// snap inventory to bottom bar
			if (bottomBar.isHidden() || inventoryBox == null)
				return;

			int snapToX;
			switch (config.inventoryPosition()){
				case LEFT:
					snapToX = bottomBar.getRelativeX();
					break;
				case CENTRE:
					snapToX = bottomBar.getRelativeX() + (bottomBar.getWidth() - inventoryBox.getWidth()) / 2;
					break;
				default:
					snapToX = bottomBar.getRelativeX() + bottomBar.getWidth() - inventoryBox.getWidth();
			}

			inventoryBoxOverlay.setPreferredLocation(new Point(snapToX, bottomBar.getRelativeY() - inventoryBox.getHeight()));

			// snap top bar to top of inventory

			topBarOverlay.setPreferredLocation(new Point(bottomBar.getRelativeX(), bottomBar.getRelativeY() - inventoryBox.getHeight() - topBar.getHeight()));
		}
	}

	@Provides
	ClodernConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClodernConfig.class);
	}
}
