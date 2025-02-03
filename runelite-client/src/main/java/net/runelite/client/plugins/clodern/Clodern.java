package net.runelite.client.plugins.clodern;

import com.google.inject.Provides;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
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
import net.runelite.client.plugins.clodern.Stuff.FakeDoor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.WidgetOverlay;

@Slf4j
@PluginDescriptor(
	name = "Resizable Clodern"
)
public class Clodern extends Plugin
{
	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	private ClodernConfig config;

	@Inject
	private OverlayManager overlayManager;

	private boolean inventoryWasHidden;

	private FakeDoor fakeDoor;

	@Getter
	private Widget bottomBar;
	private Widget topBar;
	private Widget inventoryBox;

	private WidgetOverlay topBarOverlay;
	private WidgetOverlay inventoryBoxOverlay;

	private int lastClickedATab;

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
		inventoryWasHidden = true;

		bottomBar = null;
		topBar = null;
		inventoryBox = null;

		topBarOverlay = null;
		inventoryBoxOverlay = null;

		lastClickedATab = -1;
		clientThread.invoke(this::moveComponents);
	}

	@Override
	protected void shutDown() throws Exception
	{
		resetPositions();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e){
		if (!GROUP.equals(e.getGroup()))
			return;

		if ("logoutDoor".equals(e.getKey())){
			clientThread.invoke(this::shuffleButtons);
		}
		else if ("moveTopBar".equals(e.getKey())){
			if (!config.moveTopBar()){
				resetPositions();
			}
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked e){
		if (config.collapseTimeout() == -1 || !"".equals(e.getMenuTarget()))
			return;

		// check that this widget is one of the inventory tabs!
//		if (e.getWidget().getParent()){
//
//		}

		List<String> currentTabOptionName;

		switch (client.getVarcIntValue(VarClientInt.INVENTORY_TAB)){
			case 0:
				currentTabOptionName = List.of("Combat Options");
				break;
			case 1:
				currentTabOptionName = List.of("Skills");
				break;
			case 2:
				currentTabOptionName = List.of("Character Summary","Quest List","Achievement Diaries");
				break;
			case 3:
				currentTabOptionName = List.of("Inventory");
				break;
			case 4:
				currentTabOptionName = List.of("Worn Equipment");
				break;
			case 5:
				currentTabOptionName = List.of("Prayer");
				break;
			case 6:
				currentTabOptionName = List.of("Magic");
				break;
			case 7:
				currentTabOptionName = List.of("Grouping","Chat-channel","Your Clan","View another clan");
				break;
			case 8:
				currentTabOptionName = List.of("Account Management");
				break;
			case 9:
				currentTabOptionName = List.of("Friends List","Ignore list");
				break;
			case 10:
				currentTabOptionName = List.of("Logout");
				break;
			case 11:
				currentTabOptionName = List.of("Settings");
				break;
			case 12:
				currentTabOptionName = List.of("Emotes");
				break;
			case 13:
				currentTabOptionName = List.of("Music Player");
				break;
			default:
				// includes -1 (inventory hidden)
				return;
		}

		if (currentTabOptionName.stream().noneMatch(str -> str.equalsIgnoreCase(e.getMenuOption())))
			return;

		// block varcint change if enabled, inventory not currently collapsed AND we clicked after the collapse window
		if (client.getGameCycle() > lastClickedATab + config.collapseTimeout())
		{
			e.consume();
			log.debug("blocked invy collapse!");
			lastClickedATab = client.getGameCycle();
			return;
		}

		lastClickedATab = -1;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e){
		if (e.getGameState() == GameState.LOGGING_IN || e.getGameState() == GameState.HOPPING){
			inventoryWasHidden = true;
			// don't block the change on logging in & default tab plugin on hopping
			lastClickedATab = client.getTickCount();
		}
	}

	@Subscribe
	private void onScriptPostFired(ScriptPostFired e){
		if (e.getScriptId() != 907 && e.getScriptId() != 6010)
			return;

		clientThread.invoke(this::shuffleButtons);
	}

	private void shuffleButtons()
	{
		if (config.logoutDoor()){
			// shuffle buttons and icons to the left
			Widget bottomBarButtonContainer = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,37);
			if (bottomBarButtonContainer == null || bottomBarButtonContainer.isHidden())
				return;
			bottomBarButtonContainer.setRelativeX(0);
			bottomBarButtonContainer.setWidth(231);
			offsets.forEach((id, offset) -> {
				Widget button = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,id);
				if (button != null)
					button.setRelativeX(offset * TAB_X_OFFSET_SIZE);
			});

			addFakeDoor();
		}
		else {
			removeFakeDoor();
		}
	}

	@Subscribe
	private void onVarClientIntChanged(VarClientIntChanged e){
		if (e.getIndex() != VarClientInt.INVENTORY_TAB || !config.moveTopBar())
			return;

		moveComponents();
	}

	@Provides
	ClodernConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClodernConfig.class);
	}

	private void moveComponents(){
		// make sure all the stuff exists
		if (bottomBar == null)
			bottomBar = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS1);
		if (topBar == null)
			topBar = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS2);
		if (inventoryBox == null)
			inventoryBox = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_PARENT);
		if (topBarOverlay == null)
		{
			topBarOverlay = overlayManager.get(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS2);
			if (topBarOverlay == null)
			{
				log.info("couldn't find top bar widgetoverlay");
				return;
			}
		}

		if (inventoryBoxOverlay == null)
		{
			inventoryBoxOverlay = overlayManager.get(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_PARENT);
			if (inventoryBoxOverlay == null)
			{
				log.info("couldn't find inventory box widgetoverlay");
				return;
			}
		}

		if (bottomBar == null || bottomBar.isHidden() || topBar == null || topBar.isHidden())
			return;

		// snap top bar to bottom bar when inventory box is hidden
		if (client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == -1){
			inventoryWasHidden = true;
			log.debug("inventory is hidden, snap top bar");

			topBarOverlay.setPreferredLocation(new Point(bottomBar.getRelativeX(), bottomBar.getRelativeY() - topBar.getHeight()));
			topBarOverlay.revalidate();
		}

		// snap invy to bottom bar, top bar to invy when invy is unhidden
		else {
			if (!inventoryWasHidden)
			{
				if (config.logoutDoor())
					clientThread.invoke(this::shuffleButtons);
				return;
			}

			inventoryWasHidden = false;
			log.debug("inventory un-hidden, move inventory box and top bar");

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
			inventoryBoxOverlay.revalidate();

			// snap top bar to top of inventory

			topBarOverlay.setPreferredLocation(new Point(bottomBar.getRelativeX(), bottomBar.getRelativeY() - inventoryBox.getHeight() - topBar.getHeight()));
			topBarOverlay.revalidate();
		}
	}

	private void addFakeDoor(){
//		log.debug("adding door");
//		// unhide the classic logout door and put it in the right spot (also set parent? (unset parent when changing interface style?))
//		fakeDoor = new FakeDoor(this);
//		fakeDoor.create();
//		System.out.println("fake door widget exists?: "+ (fakeDoor.logoutButton == null));
//		fakeDoor.info();
	}

	private void removeFakeDoor(){
//		log.debug("removing door");
//		fakeDoor.destroy();
//		fakeDoor = null;
//
//		offsets.forEach((id, offset) -> {
//			Widget button = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,id);
//			if (button == null)
//				return;
//			button.revalidate();
//		});
	}

	private void resetPositions(){
		log.debug("resetting widgets to default position");

		inventoryBoxOverlay.setPreferredLocation(null);
		inventoryBoxOverlay.revalidate();
		topBarOverlay.setPreferredLocation(null);
		topBarOverlay.revalidate();

		Widget bottomBarButtonContainer = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE,37);
		if (bottomBarButtonContainer != null)
			clientThread.invoke(() -> {
				bottomBarButtonContainer.revalidate();

				offsets.forEach((id, offset) -> {
					Widget button = client.getWidget(InterfaceID.RESIZABLE_VIEWPORT_BOTTOM_LINE, id);
					if (button != null)
						button.revalidate();
				});
			});

		WidgetOverlay bottomBarOverlay = overlayManager.get(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_TABS1);
		if (bottomBarOverlay != null)
		{
			bottomBarOverlay.setPreferredLocation(null);
			bottomBarOverlay.revalidate();
		}
	}
}
