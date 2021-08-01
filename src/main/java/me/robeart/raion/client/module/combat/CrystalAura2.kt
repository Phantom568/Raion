package me.robeart.raion.client.module.combat

import me.robeart.raion.client.Raion
import me.robeart.raion.client.events.EventStageable
import me.robeart.raion.client.events.events.player.UpdateWalkingPlayerEvent
import me.robeart.raion.client.events.events.render.Render3DEvent
import me.robeart.raion.client.module.Module
import me.robeart.raion.client.util.immutable
import me.robeart.raion.client.util.minecraft.MinecraftUtils
import me.robeart.raion.client.util.minecraft.RenderUtils
import me.robeart.raion.client.util.mutableBlockPos
import me.robeart.raion.client.value.*
import me.robeart.raion.client.value.kotlin.ValueDelegate
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener
import java.awt.Color
import java.util.*

/**
 * @author Robeart 8/06/2020
 */
object CrystalAura2: Module("CrystalAura2", "Crystal aura but version2", Category.COMBAT) {
	
	//General
	val priority = ListValue("Priority", "Health", Arrays.asList("Health", "Distance", "Damage"))
	val minDamage by ValueDelegate(IntValue("Min Damage", 10, 1, 20, 1))
	val norotate by ValueDelegate(BooleanValue("NoRotate", true))
	val walls by ValueDelegate(BooleanValue("Walls", true))
	
	//Lethal
	val lethal = BooleanValue("Lethal Mode", true)
	val lethalDamage by ValueDelegate(IntValue("Lethal Health", 10, 1, 20, 1, lethal))
	val lethalMinDamage by ValueDelegate(IntValue("Lethal Min Damage", 2, 1, 20, 1, lethal))
	val lethalPlace by ValueDelegate(IntValue("Place Delay", 0, 0, 20, 1, lethal))
	val lethalHit by ValueDelegate(IntValue("Place Delay", 0, 0, 20, 1, lethal))
	
	//Autoswitch
	val autoswitch = BooleanValue("Autoswitch", true)
	val offhand by ValueDelegate(BooleanValue("Offhand", false, autoswitch))
	val offhandHp by ValueDelegate(IntValue("Offhand HP", 15, 1, 36, 1, autoswitch))
	
	//Hit
	val hit = BooleanValue("Hit", true)
	val hitRange by ValueDelegate(DoubleValue("Hit Range", 4.25, 1.0, 8.0, 0.05, hit))
	val hitDelay by ValueDelegate(IntValue("Hit Delay", 5, 0, 20, 1, hit))
	val hitSwitch by ValueDelegate(IntValue("Hit to switch", 20, 1, 50, 1, hit))
	
	//Place
	val place = BooleanValue("Place", true)
	val checkLast by ValueDelegate(BooleanValue("Check Last", true, place))
	val placeRange by ValueDelegate(DoubleValue("Place Range", 4.25, 1.0, 8.0, 0.05, place))
	val placeDelay by ValueDelegate(IntValue("Place Delay", 5, 0, 20, 1, place))
	
	//Wich entity's to hit
	val players by ValueDelegate(BooleanValue("Players", true))
	val monsters by ValueDelegate(BooleanValue("Monsters", true))
	val animals by ValueDelegate(BooleanValue("Animals", true))
	val friends by ValueDelegate(BooleanValue("Friends", true))
	
	//Rendering
	val render = BooleanValue("Render", true)
	val up by ValueDelegate(BooleanValue("Up", false, render))
	val height by ValueDelegate(DoubleValue("Height", 1.0, 0.1, 1.0, 0.1, render))
	val color by ValueDelegate(ColorValue("Color", Color.RED, render))
	
	//Multithread
	val multithread = BooleanValue("MultiThread", true)
	val threadDelay by ValueDelegate(IntValue("Delay", 0, 0, 20, 1, multithread))
	
	private var target: EntityLivingBase? = null
	private var toPlace: PlaceInfo? = null
	private var placed: PlaceInfo? = null
	private var renderPos: PlaceInfo? = null
	private var lastHit: EntityEnderCrystal? = null
	private var toHit: EntityEnderCrystal? = null
	private var hitTimes = 0
	private var placeNew = true
	
	private var threadTicks = 20
	private var hitTicks = 20
	private var placeTicks = 20
	
	override fun moduleLogic() {
		if (multithread.value) {
			if (threadTicks >= threadDelay) {
				getTargetandCrystal()
				//Do not calculate place if there is no target
				if (target == null) toPlace = null
				//Calculate best place for crystal
				else toPlace = bestToPlace(target!!)
				threadTicks = 0
			} else threadTicks++
		}
	}
	
	@Listener
	fun onWalkingPlayerUpdate(event: UpdateWalkingPlayerEvent) {
		if (event.stage != EventStageable.EventStage.PRE) return
		
		//Handle hit part
		if (hit.value && hitTicks >= getHitDelay(target)) {
			if (!multithread.value) {
				getTargetandCrystal()
			}
			if (toHit != null) {
				breakCrystal(toHit!!)
				if (lastHit == toHit) hitTimes++
				else {
					lastHit = toHit
					hitTimes = 1
				}
				toHit = null
			}
		}
		
		//Dont place if target is null
		if (target == null) {
			renderPos = null
			placed = null
			hitTimes = 0
			placeTicks = 0
			return
		}
		
		//handle place part
		if (place.value && placeTicks >= getPlaceDelay(target!!)) {
			if (!multithread.value) {
				//Do not calculate place if there is no target
				if (target == null) toPlace = null
				//Calculate best place for crystal
				else toPlace = bestToPlace(target!!)
			}
			if (toPlace != null) {
				//Check if last crystal has been broken
				if (checkLast && placed != null) {
					val crystal = getCrystal(placed!!.pos)
					if (crystal != null && !placeNew) {
						if (mc.player.getDistance(crystal) >= hitRange) placeNew = true
						renderPos = null
						return
					}
				}
				
				//placeCrystal
				renderPos = toPlace
				if (placeCrystal(toPlace!!)) {
					placed = toPlace
					hitTimes = 0
					placeTicks = 0
				} //else renderPos = null
			}
		} else placeTicks++
	}
	
	@Listener
	fun onRender3D(event: Render3DEvent) {
		if (render.value && renderPos != null) {
			val pos = renderPos!!.pos
			RenderUtils.blockEspHeight(if (up) pos.up() else pos, color.rgb, height)
		}
	}
	
	/**
	 * Prepare crystal aura get Target, EndCrystal and Place position
	 */
	private fun getTargetandCrystal() {
		//List of targets
		val targetList = ArrayList<EntityLivingBase>()
		//Check if the world is loaded
		if (mc.world == null || mc.player == null) return
		//Get all possible targets
		
		for (e in mc.world.loadedEntityList.toTypedArray()) { // Need to copy into an array for concurrency issues
			if (e is EntityLivingBase) {
				if (shouldAttack(e)) targetList.add(e)
			}
			if (e is EntityEnderCrystal) {
				if (mc.player.getDistance(e) > hitRange) continue
				if (!walls && !mc.player.canEntityBeSeen(e) && !MinecraftUtils.canEntityFeetBeSeen(e)) continue
				if (hitTimes >= hitSwitch && lastHit == e) {
					placeNew = true
					continue
				}
				toHit = e
				break
				
			}
		}
		//Get the best target
		target = getTarget(targetList)
	}
	
	/**
	 * Places a endcrystal at given place
	 * @param place the PlaceInfo of where to place
	 * @return if place was succesfull
	 */
	private fun placeCrystal(place: PlaceInfo): Boolean {
		val pos = place.pos
		val hand = autoSwitch() ?: return false
		val result = mc.world.rayTraceBlocks(
			Vec3d(
				mc.player.posX, mc.player.posY + mc.player
					.getEyeHeight(), mc.player.posZ
			), Vec3d(pos.x + .5, pos.y + .5, pos.z + .5)
		)
		val facing = if (result == null || result.sideHit == null) EnumFacing.UP else result.sideHit
		
		val hitVec = Vec3d(pos).add(0.5, 0.5, 0.5)
		
		lookAtPacket(pos.x + .5, pos.y - .5, pos.z + .5)
		if (mc.playerController.processRightClickBlock(
				mc.player,
				mc.world,
				pos,
				facing.opposite,
				hitVec,
				hand
			) != EnumActionResult.FAIL
		) {
			mc.player.swingArm(hand)
			return true
		}
		return false
	}
	
	/**
	 * breaks a crystal for you, made to keep the code a bit neater for this
	 * @param crystal the crystal input to break
	 */
	private fun breakCrystal(crystal: EntityEnderCrystal) {
		lookAtPacket(crystal.posX, crystal.posY, crystal.posX)
		mc.playerController.attackEntity(mc.player, crystal)
		mc.player.swingArm(EnumHand.MAIN_HAND)
	}
	
	private fun lookAtPacket(posX: Double, posY: Double, posZ: Double) {
		if (!norotate) MinecraftUtils.lookAt(posX, posY, posZ)
	}
	
	/**
	 * Switches to a endcrystal
	 */
	private fun autoSwitch(): EnumHand? {
		val mainhand = mc.player.heldItemMainhand.item
		val offhand = mc.player.heldItemOffhand.item
		if (mainhand === Items.END_CRYSTAL) return EnumHand.MAIN_HAND
		if (offhand === Items.END_CRYSTAL) return EnumHand.OFF_HAND
		return if (autoswitch.value) {
			if (this.offhand) {
				val hpCheck: Boolean = mc.player.health + mc.player.absorptionAmount > offhandHp
				if (hpCheck) {
					val slot = MinecraftUtils.getSlotOfItem(Items.END_CRYSTAL)
					mc.playerController.windowClick(
						mc.player.inventoryContainer.windowId,
						slot,
						0,
						ClickType.PICKUP,
						mc.player
					)
					mc.playerController.windowClick(
						mc.player.inventoryContainer.windowId,
						45,
						0,
						ClickType.PICKUP,
						mc.player
					)
					mc.playerController.windowClick(
						mc.player.inventoryContainer.windowId,
						slot,
						0,
						ClickType.PICKUP,
						mc.player
					)
					mc.playerController.updateController()
					EnumHand.OFF_HAND
				} else null
			} else {
				if (MinecraftUtils.holdItem(Items.END_CRYSTAL)) EnumHand.MAIN_HAND else null
			}
		} else null
	}
	
	/**
	 * Gets the preferred target from a list of possible targets
	 * @param targetList the list of targets to take from
	 * @return the preferred target
	 */
	private fun getTarget(targetList: ArrayList<EntityLivingBase>): EntityLivingBase? {
		var target: EntityLivingBase? = null
		var place: PlaceInfo? = null
		var damage: Float? = null
		var distance: Float? = null
		var health: Float? = null
		for (e in targetList) {
			when (priority.value) {
				"Damage" -> {
					//TODO add damage check mode
				}
				"Distance" -> {
					if (distance == null || mc.player.getDistance(e) < distance) {
						distance = mc.player.getDistance(e)
						target = e
					}
				}
				"Health" -> {
					if (health == null || e.health < health) {
						health = e.health
						target = e
					}
				}
			}
		}
		return target
	}
	
	/**
	 * checks if it should attack a entity
	 * @param e the entity to check
	 * @return if it should attack the entity
	 */
	private fun shouldAttack(e: Entity): Boolean {
		if (!e.isEntityAlive || e == mc.player || mc.player.getDistance(e) > 15) return false
		return (players && e is EntityPlayer && (!friends || !Raion.INSTANCE.friendManager.isFriend(e))) || (monsters && MinecraftUtils.isMobAggressive(
			e
		))
		|| (animals && (MinecraftUtils.isPassive(e) || MinecraftUtils.isNeutralMob(e)))
	}

	/**
	 * checks if it is possible to place a crystal at the specified spot
	 * @param pos the spot
	 * @return if you can
	 */
	private fun canPlaceCrystal(pos: BlockPos.PooledMutableBlockPos): Boolean {
		if(mc.player.getDistanceSq(pos.x + .5, pos.y + .5, pos.z + .5) > (placeRange * placeRange)) return false
		val state = mc.world.getBlockState(pos)
		if (state.block == Blocks.OBSIDIAN || state.block == Blocks.BEDROCK) {
			pos.setPos(pos.x, pos.y + 1, pos.z)
			val bool1 = mc.world.checkNoEntityCollision(AxisAlignedBB(pos)) //TODO Make this more accurate
			val bool2 = mc.world.getBlockState(pos).block == Blocks.AIR
			pos.setPos(pos.x, pos.y + 1, pos.z)
			val bool3 = mc.world.getBlockState(pos).block == Blocks.AIR
			return bool1 && bool2 && bool3
		}
		return false
	}

	/**
	 * Gets the best position to place a block
	 * @return the best block to place a crystal
	 */
	private fun bestToPlace(target: EntityLivingBase): PlaceInfo? {
		if(target == null) return null
		var bestPlace: BlockPos? = null
		var damage: Double = getMinDamage(target).toDouble()

		val negDistance = -8
		val posDistance = 8
		val negYDistance = -8
		val posYDistance = 8

		mutableBlockPos { currentPos ->
			for (x in negDistance..posDistance) {
				for (y in negYDistance..posYDistance) {
					for (z in negDistance..posDistance) {
						currentPos.setPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z)
						if (!canPlaceCrystal(currentPos)) continue
						currentPos.setPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z)
						//13026.5 6.0 -12393.5
						val d = calculateDamage(
								currentPos.x + .5,
								currentPos.y + 1.0,
								currentPos.z + .5,
							target
						)
						if (d > damage) {
							damage = d.toDouble()
							bestPlace = currentPos.immutable()
						}
					}
				}
			}
		}
		return bestPlace?.let { PlaceInfo(it, damage) }
	}
	
	/**
	 * calculates the damage done by an end crystal explosion at this spot
	 * @param posX the x coordinate of the spot
	 * @param posY the y coordinate of the spot
	 * @param posZ the z coordinate of the spot
	 * @param entity the entity being damaged
	 * @return the damage result
	 */
	open fun calculateDamage(posX: Double, posY: Double, posZ: Double, entity: Entity): Float {
		val doubleExplosionSize: Float = 6.0f * 2.0f
		val distancedsize: Double = entity.getDistance(posX, posY, posZ) / doubleExplosionSize
		val vec3d = Vec3d(posX, posY, posZ)
		val blockDensity: Double = entity.world.getBlockDensity(vec3d, entity.entityBoundingBox).toDouble()
		val v: Double = (1.0 - distancedsize) * blockDensity
		val damage: Float = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
		return if (entity is EntityLivingBase) {
			getBlastReduction(entity, getDamageMultiplied(damage), Explosion(mc.world, null, posX, posY, posZ, 6f, false, true))
		} else damage
	}
	
	/**
	 * gets the damage done by an EnderCrystal after blast reduction
	 * @param entity the entity being damaged
	 * @param damage the damage done to the entity
	 * @param explosion the explosion dealing the damage
	 * @return the damage after blast reduction
	 */
	open fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion?): Float {
		var damage = damage
		if (entity is EntityPlayer) {
			val ds = DamageSource.causeExplosionDamage(explosion)
			damage = CombatRules.getDamageAfterAbsorb(damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS)
					.attributeValue.toFloat())
			val k = EnchantmentHelper.getEnchantmentModifierDamage(entity.armorInventoryList, ds)
			val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
			damage *= (1.0f - f / 25.0f)
			if (entity.isPotionActive(Potion.getPotionById(11))) {
				damage -= damage / 4
			}
			return damage
		}
		damage = CombatRules.getDamageAfterAbsorb(damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS)
				.attributeValue.toFloat())
		return damage
	}
	
	/**
	 * Gets the damage with the difficulty multiplier
	 * @param damage the damage to calculate with the multiplier
	 * @return the multiplied damage
	 */
	private fun getDamageMultiplied(damage: Float): Float {
		val diff = mc.world.difficulty.id
		return damage * if (diff == 0) 0f else if (diff == 2) 1f else if (diff == 1) 0.5f else 1.5f
	}

	
	/**
	 * gets the minimum dealt damage to a specified target
	 * @param target the target to check
	 * @return the minimum damage to said target
	 */
	private fun getMinDamage(target: EntityLivingBase): Int {
		if (lethal.value && target.health <= lethalDamage) return lethalMinDamage
		return minDamage
	}
	
	/**
	 * gets the place delay taking into account the lethal setting
	 * @param target the target to check
	 * @return the place delay for specified target
	 */
	private fun getPlaceDelay(target: EntityLivingBase): Int {
		if (lethal.value && target.health <= lethalDamage) return lethalPlace
		return placeDelay
	}
	
	/**
	 * gets the hit delay for a specified target taking the lethal setting into account
	 * @param target the target to check for hit delay
	 * @return the hit delay for the target
	 */
	private fun getHitDelay(target: EntityLivingBase?): Int {
		if (target == null) return hitDelay
		if (lethal.value && target.health <= lethalDamage) return lethalHit
		return hitDelay
	}
	
	/**
	 * gets a crystal at a given BlockPos
	 * @param pos the BlockPos to check for crystals
	 * @return the crystal at the given spot
	 */
	private fun getCrystal(pos: BlockPos?): EntityEnderCrystal? {
		if (pos == null) return null
		val entitys = mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(pos.add(0, 1, 0)))
		for (entity in entitys) {
			if (entity is EntityEnderCrystal) return entity
		}
		return null
	}
	
	//Class that stores info about crystals that will be placed
	internal class PlaceInfo(var pos: BlockPos, var damage: Double)
	
}
