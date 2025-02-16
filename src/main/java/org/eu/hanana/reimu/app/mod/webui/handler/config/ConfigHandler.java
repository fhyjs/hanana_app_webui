package org.eu.hanana.reimu.app.mod.webui.handler.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.impl.util.log.Log;
import org.eu.hanana.reimu.app.mod.webui.ModFabric;
import org.eu.hanana.reimu.app.mod.webui.Util;
import org.eu.hanana.reimu.hnnapp.Datas;
import org.eu.hanana.reimu.hnnapp.ModLoader;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceFieldVisitor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/settings/wa/config.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            var result = new JsonObject();
            var gson = new Gson();
            var acton = Util.getQueryParam(httpServerRequest.uri(),"action");
            try {
                var mod = ModLoader.getLoader().getMod(Util.getQueryParam(httpServerRequest.uri(),"id"));
                var clazz = Class.forName(Util.getQueryParam(httpServerRequest.uri(),"class"));
                if (mod==null) throw new IllegalArgumentException("找不到名为"+Util.getQueryParam(httpServerRequest.uri(),"id")+"的插件");
                if (!mod.cfgCore.cfgCls.contains(clazz)) throw new IllegalArgumentException("在插件"+Util.getQueryParam(httpServerRequest.uri(),"id")+"找不到该配置类");
                result.add("status",new JsonPrimitive("success"));

                if (acton.equals("default")){
                    Field[] fields = clazz.getFields();
                    var vals = getClInitDefault(clazz);
                    for (Field field : fields) {
                        if (!Modifier.isStatic(field.getModifiers())||!vals.containsKey(field.getName())) continue;
                        var o = vals.get(field.getName());
                        field.set(null,o);
                    }
                    mod.cfgCore.saveCfg();
                }else if (acton.equals("get")){
                    String s = Files.readString(new File(mod.cfgCore.cfgDir, clazz.getName() + ".cfg").toPath(), StandardCharsets.UTF_8);
                    result.addProperty("data",s);
                }else if (acton.equals("save")){
                    Util.getAllPostDataString(httpServerRequest).doOnError(stringMonoSink::error).doOnSuccess(s -> {
                        Path path = new File(mod.cfgCore.cfgDir, clazz.getName() + ".cfg").toPath();
                        String tmp;
                        try {
                            tmp = Files.readString(path, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            stringMonoSink.error(e);
                            return;
                        }
                        try {
                            Files.writeString(path,s,StandardCharsets.UTF_8);
                            mod.cfgCore.readCfgCls();
                            mod.cfgCore.saveCfg();
                        }catch (Throwable throwable){
                            try {
                                Files.writeString(path,tmp,StandardCharsets.UTF_8);
                                mod.cfgCore.readCfgCls();
                            } catch (Exception e) {
                                stringMonoSink.error(e);
                            }
                            result.addProperty("status","error");
                            result.addProperty("msg",throwable.toString());
                            throwable.printStackTrace();
                        }
                        stringMonoSink.success(result.toString());
                    }).subscribe();
                    return;
                }

            }catch (Throwable throwable){
                result.addProperty("status","error");
                result.addProperty("msg",throwable.toString());
                throwable.printStackTrace();
            }
            stringMonoSink.success(result.toString());
        }));
    }
    public static class ByteArrayClassLoader extends ClassLoader {
        public Class<?> loadClassFromByteArray(String className, byte[] classData) {
            return defineClass(className, classData, 0, classData.length);
        }
        public ByteArrayClassLoader(ClassLoader parent) {
            super(parent);
        }
    }
    public static Map<String,Object> getClInitDefault(Class<?> clazz) throws IllegalAccessException, IOException {
        List<String> fields = new ArrayList<>();
        var ba = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace(".","/")+".class");
        byte[] bytes = ba.readAllBytes();
        ba.close();
        ClassReader classReader = new ClassReader(bytes);
        // 使用 ASM 解析类字节码
        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                System.out.println(name);
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                // 仅处理静态初始化方法 <clinit>
                if ("<clinit>".equals(name)) {
                    Log.info(ModFabric.logCategory,"Found clinit method.");
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                            // 处理对字段的赋值操作
                            if (opcode == Opcodes.PUTSTATIC) {
                                fields.add(name);
                            }
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            // 处理其他操作，例如对常量的加载或方法调用等
                            super.visitInsn(opcode);
                        }
                    };
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        var cl = new ByteArrayClassLoader(clazz.getClassLoader());
        Class<?> vClass = cl.loadClassFromByteArray(clazz.getName(), bytes);
        Map<String,Object> result = new HashMap<>();
        for (String field : fields) {
            Field declaredField;
            try {
                declaredField = vClass.getDeclaredField(field);
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (!Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers()) || !Modifier.isPublic(declaredField.getModifiers()))
                continue;
            Object o = declaredField.get(null);
            result.put(field,o);
            Log.debug(ModFabric.logCategory, "Found field %s:%s", field, o);
        }
        return result;
    }
}
