package org.eu.hanana.reimu.app.mod.webui.mixin;

import org.eu.hanana.reimu.app.mod.webui.ModFabric;
import org.eu.hanana.reimu.app.mod.webui.ModMain;
import org.eu.hanana.reimu.hnnapp.Main;
import org.eu.hanana.reimu.hnnapp.ModLoader;
import org.eu.hanana.reimu.hnnapp.mods.Mod;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModLoader.class)
public class MixinModLoader {
    @Shadow @Final public List<Mod> mods;

    //@Inject(method = {"<init>"},at = @At(value = "INVOKE", target = "Lorg/eu/hanana/reimu/hnnapp/asm/AsmManager;<init>()V"),remap = false)
    public void onSetModList(CallbackInfo ci){
        var webui = new Mod();
        webui.INSTANCE=new ModMain();
        webui.id= ModFabric.MOD_ID_Legacy;
        webui.name= ModFabric.MOD_ID;
        webui.clazz= ModMain.class;
        this.mods.add(webui);
    }
}
