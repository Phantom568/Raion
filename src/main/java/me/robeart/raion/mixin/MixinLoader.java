package me.robeart.raion.mixin;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class MixinLoader implements IFMLLoadingPlugin {
	
	private static boolean isObfuscatedEnvironment = false;
	
	public MixinLoader() {
		Objects.requireNonNull(Launch.blackboard, "Launch.blackboard illegally null");
		Object launchArgs = Launch.blackboard.get("launchArgs");
		if (launchArgs == null) {
			launchArgs = Launch.blackboard.get("forgeLaunchArgs");
		}
		Objects.requireNonNull(launchArgs, "Couldnt retrieve launch args");
		Map<String, String> castedArgs = Objects.requireNonNull((Map<String, String>) launchArgs, "Couldnt cast launch args");
		
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.raion.json");
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}
	
	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		isObfuscatedEnvironment = (boolean) (Boolean) data.get("runtimeDeobfuscationEnabled");
	}
	
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}

