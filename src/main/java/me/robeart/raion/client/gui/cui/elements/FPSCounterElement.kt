package me.robeart.raion.client.gui.cui.elements

import com.mojang.realmsclient.gui.ChatFormatting
import me.robeart.raion.client.Raion
import me.robeart.raion.client.gui.cui.element.CuiElement
import me.robeart.raion.client.util.Utils
import me.robeart.raion.client.util.font.Fonts
import me.robeart.raion.client.util.font.MinecraftFontRenderer
import net.minecraft.client.Minecraft
import net.minecraft.util.math.Vec2f

/**
 * @author cookiedragon234 17/Jun/2020
 */
class FPSCounterElement: CuiElement() {
	override fun render(mousePos: Vec2f) {
		super.render(mousePos)
		
		val fps = Minecraft.getDebugFPS()
		val text = "$fps${ChatFormatting.GRAY} FPS"
		if (shouldRender()) {
			font.drawString(text, this.position.posX + 1, this.position.posY + 1, Utils.getRgb(255,255,255,255))
		}
		val width = font.getStringWidth(text)
		val height = font.getStringHeight(text)
		position.sizeX = width
		position.sizeY = height
	}
}
