package net.runelite.client.plugins.nightmareboss;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

// 1. turn on when: nightmare hp overlay is detected
//		turn off when: above conditions are not fulfilled
// 2. get list of hands (example from zalcano)
// 3. for now: highlight the tile that graphicsObjects corresponding to nm hand id are on. example code comes from zalcano
// 3. later: outline them like object indicators do. test this on other stuff first

@Slf4j
@PluginDescriptor(
	name = "Nightmare boss",
	description = "Makes Nightmare boss hands easier to see"
)
public class NightmareBossPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareBossConfig config;

	@Inject
	private NightmareBossOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Getter
	private final List<GraphicsObject> hands = new ArrayList<>();

	@Getter
	private boolean dreaming;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		log.info("Example started!");
		hands.clear();

		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				dreaming = isHealthbarActive();
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated)
	{
		if (dreaming)
		{
			GraphicsObject graphicsObject = graphicsObjectCreated.getGraphicsObject();
			hands.add(graphicsObject);
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Detected a hand: id " + graphicsObject.getId(), null);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOADING)
		{
			hands.clear();
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Cleared list of hands", null);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		dreaming = isHealthbarActive();
	}

	@Provides
	NightmareBossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareBossConfig.class);
	}

	private boolean isHealthbarActive()
	{
		int npcId = client.getVar(VarPlayer.HP_HUD_NPC_ID);
		return (npcId >= 9416 && npcId <= 9424);
	}
}
