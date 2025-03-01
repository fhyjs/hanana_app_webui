package org.eu.hanana.reimu.app.mod.webui.config;

import org.eu.hanana.reimu.hnnapp.mods.CfgCoreBase;

public class WebuiCfgCore extends CfgCoreBase {
    @Override
    public void init() {
        addCfgClass(WebuiCfgCore.Config.class);
    }
    @HasName("控制台设置")
    @SaveProcessor(Config.Saver.class)
    public static class Config{
        public static int port = 5160;
        public static long session_expire_second = 10800;
        public static class Saver implements IConfigSaver{

            @Override
            public String save() {
                return "端口修改需要重启";
            }
        }
    }
}
