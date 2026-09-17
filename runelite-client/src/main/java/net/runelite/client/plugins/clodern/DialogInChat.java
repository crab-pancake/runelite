package net.runelite.client.plugins.clodern;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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
