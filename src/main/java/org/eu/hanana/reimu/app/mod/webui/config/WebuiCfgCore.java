package org.eu.hanana.reimu.app.mod.webui.config;

import org.eu.hanana.reimu.hnnapp.mods.CfgCoreBase;

public class WebuiCfgCore extends CfgCoreBase {
    @Override
    public void init() {
        addCfgClass(WebuiCfgCore.Config.class);
    }

    public static class Config{
        public static int port = 5160;
    }
}