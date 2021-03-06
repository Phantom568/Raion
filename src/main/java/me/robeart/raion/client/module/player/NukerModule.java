package me.robeart.raion.client.module.player;

import me.robeart.raion.client.module.Module;
import me.robeart.raion.client.value.BooleanValue;
import me.robeart.raion.client.value.DoubleValue;
import me.robeart.raion.client.value.ListValue;
import net.minecraft.block.Block;
import scala.collection.immutable.List;

import java.util.Arrays;

public class NukerModule extends Module {
	
	
	public BooleanValue allblocks = new BooleanValue("All Blocks", true);
	public BooleanValue flatten = new BooleanValue("Flatten", false);
	public BooleanValue noparticles = new BooleanValue("No Particles", false);
	public BooleanValue rotate = new BooleanValue("Rotate", false);
	public DoubleValue range = new DoubleValue("Range", 4.2, 1, 6, 0.1);
	public DoubleValue cooldown = new DoubleValue("Cooldown", 0, 0, 4, 0.1);
	public ListValue mode = new ListValue("Mode", "Normal", Arrays.asList("Normal", "Instant"));
	public ListValue sort = new ListValue("Sort", "Normal", Arrays.asList("Normal", "Hardness"));
	
	private List<Block> blockList;
	
	public NukerModule() {
		super("Nuker", "Breaks blocks around you", Category.PLAYER);
	}
	
	public void onEnable() {
		super.onEnable();
	}

        /*@Subscribe
        public void onTick(EventTick event) {
            double range = getSettings().get(1).toSlider().getValue();
            List<BlockPos> blocks = new ArrayList<>();

            for(int x = (int) range; x >= (int) -range; x--) {
                for(int y = (int) range; y >= (getSettings().get(4).toToggle().state ? 0 : (int) -range); y--) {
                    for(int z = (int) range; z >= (int) -range; z--) {
                        BlockPos pos = new BlockPos(mc.player.getPos().add(x, y + 0.1, z));
                        if(!canSeeBlock(pos) || mc.world.getBlockState(pos).getBlock() == Blocks.AIR || WorldUtils.isFluid(pos)) continue;
                        blocks.add(pos);
                    }
                }
            }

            if(!blocks.isEmpty() && getSettings().get(6).toToggle().state) FabricReflect.writeField(
                    mc.particleManager, Maps.newIdentityHashMap(), "field_3830", "particles");

            if(getSettings().get(7).toMode().mode == 1) blocks.sort((a, b) -> Float.compare(
                    mc.world.getBlockState(a).getHardness(null, a), mc.world.getBlockState(b).getHardness(null, b)));

            for(BlockPos pos: blocks) {
                if(!getSettings().get(3).toToggle().state) if(!blockList.contains(mc.world.getBlockState(pos).getBlock())) continue;

                Vec3d vec = new Vec3d(pos).add(0.5, 0.5, 0.5);

                if(mc.player.getPos().distanceTo(vec) > range + 0.5) continue;

                Direction dir = null;
                double dist = 6.9;
                for(Direction d: Direction.values()) {
                    double dist2 = mc.player.getPos().distanceTo(new Vec3d(pos.offset(d)).add(0.5, 0.5, 0.5));
                    if(dist2 > range || mc.world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR || dist2 > dist) continue;
                    dist = dist2;
                    dir = d;
                }

                if(dir == null) continue;

                if(getSettings().get(5).toToggle().state) {
                    float[] prevRot = new float[] {mc.player.yaw, mc.player.pitch};
                    EntityUtils.facePos(vec.x, vec.y, vec.z);
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
                            mc.player.yaw, mc.player.pitch, mc.player.onGround));
                    mc.player.yaw = prevRot[0];
                    mc.player.pitch = prevRot[1];
                }

                if(getSettings().get(0).toMode().mode == 1) mc.interactionManager.attackBlock(pos, dir);
                else mc.interactionManager.method_2902(pos, dir);

                mc.player.swingHand(Hand.MAIN_HAND);
                if(getSettings().get(0).toMode().mode != 1) return;
            }
        }

        public boolean canSeeBlock(BlockPos pos) {
            double diffX = pos.getX() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).x;
            double diffY = pos.getY() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).y;
            double diffZ = pos.getZ() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).z;

            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float yaw = mc.player.yaw + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90 - mc.player.yaw);
            float pitch = mc.player.pitch + MathHelper.wrapDegrees((float)-Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.pitch);

            Vec3d rotation = new Vec3d(
                    (double)(MathHelper.sin(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)),
                    (double)(-MathHelper.sin(pitch * 0.017453292F)),
                    (double)(MathHelper.cos(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)));

            Vec3d rayVec = mc.player.getCameraPosVec(mc.getTickDelta()).add(rotation.x * 6, rotation.y * 6, rotation.z * 6);
            return mc.world.rayTrace(new RayTraceContext(mc.player.getCameraPosVec(mc.getTickDelta()),
                    rayVec, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, mc.player))
                    .getBlockPos().equals(pos);
        }

    }*/
}
