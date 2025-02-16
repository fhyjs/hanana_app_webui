package org.eu.hanana.reimu.app.mod.webui;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;


public class ModFabric implements ModInitializer {
    public static final String MOD_ID = "webui";
    public static final String MOD_ID_Legacy = ModFabric.MOD_ID+"_legacy";
    public static final LogCategory logCategory = LogCategory.createCustom(MOD_ID);
    @Override
    public void onInitialize() {
        Log.info(logCategory,"webui is loaded!");
    }
}
