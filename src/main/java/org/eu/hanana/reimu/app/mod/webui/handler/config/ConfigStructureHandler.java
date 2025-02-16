package org.eu.hanana.reimu.app.mod.webui.handler.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eu.hanana.reimu.app.mod.webui.Util;
import org.eu.hanana.reimu.hnnapp.Datas;
import org.eu.hanana.reimu.hnnapp.ModLoader;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ConfigStructureHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/settings/wa/config_structure.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            var result = new JsonObject();
            var gson = new Gson();
            try {
                var mod = ModLoader.getLoader().getMod(Util.getQueryParam(httpServerRequest.uri(),"id"));
                var clazz = Class.forName(Util.getQueryParam(httpServerRequest.uri(),"class"));
                if (mod==null) throw new IllegalArgumentException("找不到名为"+Util.getQueryParam(httpServerRequest.uri(),"id")+"的插件");
                if (!mod.cfgCore.cfgCls.contains(clazz)) throw new IllegalArgumentException("在插件"+Util.getQueryParam(httpServerRequest.uri(),"id")+"找不到该配置类");

                result.add("status",new JsonPrimitive("success"));
                result.add("data",getConfigStructure(clazz,null));

            }catch (Throwable throwable){
                result.addProperty("status","error");
                result.addProperty("msg",throwable.toString());
                throwable.printStackTrace();
            }
            stringMonoSink.success(result.toString());
        }));
    }
    protected JsonArray getConfigStructure(Class<?> clazz,Object internal) throws IllegalAccessException {
        var fields = clazz.getFields();
        var list = new JsonArray();
        for (Field field : fields) {
            if (internal==null&&!Modifier.isStatic(field.getModifiers())) continue;
            Object value = field.get(internal);
            var jo = new JsonObject();
            String name = value.getClass().getName();
            jo.add(field.getName(), !Datas.classList.contains(value.getClass()) ?new JsonPrimitive(name):getConfigStructure(value.getClass(),value));
            list.add(jo);
        }
        return list;
    }
}
