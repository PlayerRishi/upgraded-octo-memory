package rishi.player.picraftmod.mixin.client;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandMixin {
    
    @Inject(method = "getClientModName", at = @At("RETURN"), cancellable = true)
    private static void modifyClientBrand(CallbackInfoReturnable<String> cir) {
        String originalBrand = cir.getReturnValue();
        cir.setReturnValue(originalBrand + "+picraft");
    }
}