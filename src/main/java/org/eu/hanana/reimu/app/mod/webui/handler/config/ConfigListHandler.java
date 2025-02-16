package org.eu.hanana.reimu.app.mod.webui.handler.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eu.hanana.reimu.app.mod.webui.Util;
import org.eu.hanana.reimu.app.mod.webui.config.HasName;
import org.eu.hanana.reimu.hnnapp.ModLoader;
import org.eu.hanana.reimu.hnnapp.mods.Mod;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class ConfigListHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/settings/wa/config_list.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            var ja = new JsonArray();
            for (Mod mod : ModLoader.getLoader().mods) {
                if (mod.cfgCore==null) continue;
                var jo = new JsonObject();
                jo.addProperty("id",mod.id);
                jo.addProperty("name",mod.name);
                var classes = new JsonArray();
                for (Class<?> cfgCl : mod.cfgCore.cfgCls) {
                    var item = new JsonObject();
                    item.addProperty("class",cfgCl.getName());
                    if (cfgCl.isAnnotationPresent(HasName.class)){
                        item.addProperty("name",cfgCl.getAnnotation(HasName.class).value());
                    }else {
                        item.addProperty("name",cfgCl.getName());
                    }
                    classes.add(item);
                }
                jo.add("classes",classes);
                ja.add(jo);
            }
            stringMonoSink.success(ja.toString());
        }));
    }
}
