package net.runelite.client.plugins.clodern;

import com.google.inject.Provides;
import java.awt.Point;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import static net.runelite.client.plugins.clodern.ClodernConfig.GROUP;
import net.runelite.client.plugins.clodern.FakeDoor.FakeDoor;
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

	private FakeDoor fakeDoor;

	@Getter
	private Widget bottomBar;
	private Widget topBar;
	private Widget inventoryBox;

	private WidgetOverlay topBarOverlay;
	private WidgetOverlay inventoryBoxOverlay;

	private int lastClickedATab;

	private Point inventoryPreferredLocation = null;
	private Point topBarPreferredLocation = null;

	private final int TAB_X_OFFSET_SIZE = 33;

	private static final Map<Integer, Integer> offsets = Map.ofEntries(
		Map.entry(38, 0), Map.entry(44, 0),
		Map.entry(39, 1), Map.entry(45, 1),
		Map.entry(40, 2), Map.entry(46, 2),
		Map.entry(41, 4), Map.entry(47, 4),
		Map.entry(42, 5), Map.entry(48, 5),
		Map.entry(43, 6), Map.entry(49, 6)
		);

	@Override
	protected void startUp() throws Exception
	{

		bottomBar = null;
		topBar = null;
		inventoryBox = null;

		topBarOverlay = null;
		inventoryBoxOverlay = null;

		lastClickedATab = -1;
//		clientThread.invoke(this::moveComponents);
	}

	@Override
	protected void shutDown() throws Exception
	{
		resetPositions();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!GROUP.equals(e.getGroup()))
			return;

		if ("logoutDoor".equals(e.getKey())){
			clientThread.invoke(this::shuffleStones);
		}
		else if ("moveTopBar".equals(e.getKey())){
			if (!config.moveTopBar()){
				resetPositions();
			}
		}
	}

//	@Subscribe
//	private void onWidgetClosed(WidgetClosed e)
//	{
//		if (e.getGroupId() == InterfaceID.INVENTORY){
//			clientThread.invoke(this::moveComponents);
//		}
//	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGING_IN || e.getGameState() == GameState.HOPPING){
			bottomBar = null;
			topBar = null;
			inventoryBox = null;
			topBarOverlay = null;

			// don't block the change on logging in & default tab plugin on hopping
			lastClickedATab = client.getTickCount();
		}
		if (e.getGameState() == GameState.LOGGED_IN){
			clientThread.invokeLater(this::moveComponents);
			clientThread.invokeLater(this::shuffleStones);
		}
	}

	@Subscribe
	private void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == 907 || e.getScriptId() == 6010)
		{
			clientThread.invoke(this::shuffleStones);
			clientThread.invokeLater(this::shuffleStones);
		}
		if (e.getScriptId() == 903){
//		if (e.getScriptId() == ScriptID.BANKMAIN_INIT || e.getScriptId() == 1075){
			// 1075: load shop items? 149 or 1074 (load name)?
			clientThread.invoke(this::moveComponents);
			clientThread.invokeLater(this::moveComponents);
		}
	}

	private void shuffleStones()
	{
		if (config.logoutDoor()){
			// shuffle buttons and icons to the left
			Widget bottomBarButtonContainer = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC,37);
			if (bottomBarButtonContainer == null || bottomBarButtonContainer.isHidden())
				return;
			bottomBarButtonContainer.setRelativeX(0);
			bottomBarButtonContainer.setWidth(231);
			offsets.forEach((id, offset) -> {
				Widget button = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC,id);
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
		if (!config.moveTopBar() || e.getIndex() != VarClientID.TOPLEVEL_PANEL)
			return;

		clientThread.invokeLater(this::moveComponents);
	}

	@Provides
	ClodernConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClodernConfig.class);
	}

	private void moveComponents(){
		if (!validateComponents() || config.tempDisable())
		{
			return;
		}

		if (bottomBar == null || bottomBar.isHidden() || topBar == null || topBar.isHidden())
			return;

		// snap top bar to bottom bar when inventory box is hidden
		if (inventoryBox.isHidden())  //(client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == -1)
		{
//			log.debug("inventory is hidden, snap top bar");

			topBarOverlay.setPreferredLocation(new Point(bottomBar.getRelativeX(), bottomBar.getRelativeY() - topBar.getHeight()));
			topBarOverlay.revalidate();
		}

		// snap invy to bottom bar, top bar to invy when invy is unhidden
		else
		{
			// always move: in case i manually changed the position
//			if (!inventoryWasHidden)
//			{
//				// can i hide these? should be covered by the script firing stuff
////				if (config.logoutDoor())
////					clientThread.invoke(this::shuffleStones);
//				return;
//			}

//			log.debug("inventory un-hidden, move inventory box and top bar");

			// snap inventory to bottom bar
			if (bottomBar.isHidden() || inventoryBox == null)
				return;

			int snapToX;
			switch (config.inventoryPosition())
			{
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

	private boolean validateComponents()
	{
		if (bottomBar == null)
			bottomBar = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
		if (topBar == null)
			topBar = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_MOVABLE_LAYER);
		if (inventoryBox == null)
			inventoryBox = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_CONTAINER);
		if (topBarOverlay == null)
		{
			topBarOverlay = overlayManager.get(InterfaceID.ToplevelPreEoc.SIDE_MOVABLE_LAYER);
			if (topBarOverlay == null)
			{
				log.warn("couldn't find top bar widgetoverlay");
				return false;
			}
			else {
				topBarPreferredLocation = topBarOverlay.getPreferredLocation();
			}
		}

		if (inventoryBoxOverlay == null)
		{
			inventoryBoxOverlay = overlayManager.get(InterfaceID.ToplevelPreEoc.SIDE_CONTAINER);
			if (inventoryBoxOverlay == null)
			{
				log.info("couldn't find inventory box widgetoverlay");
				return false;
			}
			else {
				inventoryPreferredLocation = inventoryBoxOverlay.getPreferredLocation();
			}
		}
		return true;
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

		inventoryBoxOverlay.setPreferredLocation(inventoryPreferredLocation);
		inventoryBoxOverlay.revalidate();
		topBarOverlay.setPreferredLocation(topBarPreferredLocation);
		topBarOverlay.revalidate();

		Widget bottomBarButtonContainer = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC);
		if (bottomBarButtonContainer != null)
			clientThread.invoke(() -> {
				bottomBarButtonContainer.revalidate();

				offsets.forEach((id, offset) -> {
					Widget button = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC, id);
					if (button != null)
						button.revalidate();
				});
			});

//		WidgetOverlay bottomBarOverlay = overlayManager.get(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
//		if (bottomBarOverlay != null)
//		{
//			bottomBarOverlay.setPreferredLocation(null);
//			bottomBarOverlay.revalidate();
//		}
	}
}
