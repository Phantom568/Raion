package me.robeart.raion;

import me.robeart.raion.client.Raion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * @author Robeart
 */
@Mod(name = "Raion Loader", modid = "raionloader", version = "0.1", clientSideOnly = true)
public class LoadClient {
	static {
		System.out.println("Load Client clinit");
	}
	
	public LoadClient() {
		System.out.println("Load Client initialiseation");
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		Raion.INSTANCE.initClient();
	}
	
}
