package me.robeart.raion.mixin.common.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {
	
	@Shadow
	public abstract boolean renderModelSmooth(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, boolean checkSides, long rand);


    /*@Inject(method = "renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z", at = @At("HEAD"), cancellable = true)
    public void renderModel(IBlockAccess blockAccessIn, IBakedModel modelIn, IBlockState blockStateIn, BlockPos blockPosIn, BufferBuilder buffer, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> ci) {
        if(Raion.INSTANCE.getModuleManager().getModule(XrayModule.class).getState()) {
            boolean xray = !((XrayModule) Raion.INSTANCE.getModuleManager().getModule(XrayModule.class)).shouldXray(blockStateIn.getBlock());
            boolean returnvalue = renderModelSmooth(blockAccessIn, modelIn, blockStateIn, blockPosIn, buffer, xray, rand);
            ci.setReturnValue(returnvalue);
        }
    }*/
}
