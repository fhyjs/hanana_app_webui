package org.eu.hanana.reimu.app.mod.webui;

import lombok.Getter;
import lombok.SneakyThrows;
import org.eu.hanana.reimu.app.mod.webui.event.WebUiClosingEvent;
import org.eu.hanana.reimu.app.mod.webui.event.WebUiCreatedEvent;
import org.eu.hanana.reimu.app.mod.webui.config.WebuiCfgCore;
import org.eu.hanana.reimu.app.mod.webui.handler.AppJsMainHandler;
import org.eu.hanana.reimu.app.mod.webui.handler.config.ConfigHandler;
import org.eu.hanana.reimu.app.mod.webui.handler.config.ConfigStructureHandler;
import org.eu.hanana.reimu.hnnapp.ModLoader;
import org.eu.hanana.reimu.hnnapp.mods.Event;
import org.eu.hanana.reimu.hnnapp.mods.ModEntry;
import org.eu.hanana.reimu.hnnapp.mods.events.AfterInfoEvent;
import org.eu.hanana.reimu.hnnapp.mods.events.ExitEvent;
import org.eu.hanana.reimu.hnnapp.mods.events.PostInitModsEvent;
import org.eu.hanana.reimu.webui.WebUi;

import static org.eu.hanana.reimu.app.mod.webui.ModFabric.MOD_ID_Legacy;

@SuppressWarnings("ThrowableNotThrown")
@ModEntry(id = MOD_ID_Legacy,name = ModFabric.MOD_ID)
public class ModMain {
    @Getter
    protected static ModMain WebUiMod;
    @Getter
    private WebUi  webUi;
    @Event
    public void onPostInitModsEvent(PostInitModsEvent event){
        ModLoader.getLoader().regCfgCore(MOD_ID_Legacy,new WebuiCfgCore());
    }
    @Event
    public void onPostInitModsEvent(AfterInfoEvent event){
        webUi = new WebUi("0.0.0.0",WebuiCfgCore.Config.port);
        ModLoader.postEvent(new WebUiCreatedEvent(webUi,WebuiCfgCore.Config.port));
        webUi.handlers.add(new AppJsMainHandler());
        webUi.handlers.add(new ConfigStructureHandler());
        webUi.handlers.add(new ConfigHandler());
        webUi.addPermissionRule("^/static/cp/webui_app/pages/settings.*",10);
        webUi.open(false);
    }
    @SneakyThrows
    @Event
    public void onExitEvent(ExitEvent event){
        ModLoader.postEvent(new WebUiClosingEvent(webUi));
        webUi.close();
    }
    public ModMain(){
        WebUiMod=this;
        ModLoader.getLoader().regEventBuses(this);
    }
}
