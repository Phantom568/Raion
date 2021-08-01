package me.robeart.raion.client.module.render

import me.robeart.raion.client.events.events.player.OnUpdateEvent
import me.robeart.raion.client.events.events.player.UpdateWalkingPlayerEvent
import me.robeart.raion.client.events.events.render.Render2DEvent
import me.robeart.raion.client.gui.cui.RaionCui
import me.robeart.raion.client.module.Module
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener

/**
 * @author cookiedragon234 16/Jun/2020
 */
object CuiModule: Module("Cui", "Hud", Category.RENDER) {
	@Listener
	fun onRender2D(event: Render2DEvent) {
		if (mc.currentScreen == null) {
			RaionCui.onRender2D()
		}
	}
	
	@Listener
	fun onUpdate(event: UpdateWalkingPlayerEvent) {
		RaionCui.onUpdate()
	}
}
