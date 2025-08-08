package net.runelite.client.plugins.clodern;

import com.google.inject.Provides;
import java.awt.Point;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
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
	name = "Dialog in chat"
)
public class DialogInChat extends Plugin
{
	@Inject
	public Client client;
	@Inject
	public ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		if (client.getGameState() == GameState.LOGGED_IN){
			clientThread.invoke(() ->
				client.setVarbit(VarbitID.SHOW_DIALOGUE_IN_CHATBOX, 1));
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() ->
			client.setVarbit(VarbitID.SHOW_DIALOGUE_IN_CHATBOX, 0));
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN){
			clientThread.invoke(() ->
				client.setVarbit(VarbitID.SHOW_DIALOGUE_IN_CHATBOX, 1));
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage e){
		if (client.getVarbitValue(VarbitID.SHOW_DIALOGUE_IN_CHATBOX) == 0){
			clientThread.invoke(() ->
				client.setVarbit(VarbitID.SHOW_DIALOGUE_IN_CHATBOX, 1));
		}
	}
}
