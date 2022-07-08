package net.runelite.client.plugins.lineofsight;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(name = "Line of Sight", description = "los")

@Slf4j
public class LineOfSightPlugin extends Plugin
{

	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private LineOfSightOverlay overlay;
	@Inject
	private LineOfSightConfig config;
	@Inject
	private KeyManager keyManager;
	private boolean toggled = false;

	private final HotkeyListener masterSwitch = new HotkeyListener(() -> config.getDCHotkey()){

		public void hotkeyPressed() {
			toggled = !toggled;
			client.setOculusOrbState(toggled ? 1 : 0);
			if (toggled) {
				client.setOculusOrbNormalSpeed(config.getDCSpeed());
			}
		}
	};

	@Provides
	LineOfSightConfig providesConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(LineOfSightConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		keyManager.registerKeyListener(this.masterSwitch);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		this.keyManager.unregisterKeyListener(this.masterSwitch);
		this.toggled = false;
		this.client.setOculusOrbState(0);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals("LineOfSight") && e.getKey().equals("dcSpeed") && this.toggled)
		{
			this.client.setOculusOrbNormalSpeed(this.config.getDCSpeed());
		}
	}
}