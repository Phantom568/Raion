package me.robeart.raion.client.module.combat;

import me.robeart.raion.client.events.events.player.OnUpdateEvent;
import me.robeart.raion.client.module.Module;
import me.robeart.raion.client.util.minecraft.MinecraftUtils;
import me.robeart.raion.client.value.BooleanValue;
import me.robeart.raion.client.value.IntValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class AutoTotemModule extends Module {
	
	public BooleanValue gui = new BooleanValue("Gui", false);
	public BooleanValue conditions = new BooleanValue("Conditions", "Conditions to activating auto totem", true);
	public BooleanValue caCheck = new BooleanValue("CA Check", "Only enable if CA is disabled", false, conditions);
	public BooleanValue caTargetCheck = new BooleanValue("CA Target Check", "Enable if no CA targets in range", true, conditions);
	public IntValue health = new IntValue("Health", 20, 0, 36, 1, conditions);
	
	public AutoTotemModule() {
		super("AutoTotem", new String[]{"Totem"}, "Automatically equips a Totem of Undying in your offhand", Category.COMBAT);
	}
	
	private long noTargetFor = -1;
	
	public boolean areConditionsMet() {
		if (conditions.getValue()) {
			if (caCheck.getValue() && !CrystalAuraModule.INSTANCE.getState()) {
				return true;
			}
			if (caTargetCheck.getValue()) {
				EntityLivingBase target = CrystalAuraModule.INSTANCE.target;
				if (target == null) {
					if (noTargetFor == -1) {
						noTargetFor = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - noTargetFor >= 1000) {
						return true;
					}
				} else {
					noTargetFor = -1;
				}
			}
			if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue()) {
				return true;
			}
		}
		return true;
	}
	
	@Listener
	public void onUpdate(OnUpdateEvent event) {
		Item offhand = mc.player.getHeldItemOffhand().getItem();
		if (offhand == Items.TOTEM_OF_UNDYING || !areConditionsMet())
			return;
		int slot = MinecraftUtils.getSlotOfItem(Items.TOTEM_OF_UNDYING);
		if (slot != -1) {
			if (mc.currentScreen != null && gui.getValue()) return;
			mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
			mc.playerController.updateController();
		}
	}
	
	
	@Override
	public String getHudInfo() {
		return String.valueOf(MinecraftUtils.getItemCount(Items.TOTEM_OF_UNDYING));
	}
}
