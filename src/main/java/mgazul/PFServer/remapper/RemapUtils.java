package mgazul.PFServer.remapper;

import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import mgazul.PFServer.CatServer;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.NodeType;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class RemapUtils {
    public static final String NMS_PREFIX = "net/minecraft/server/";
    public static final String NMS_VERSION = CatServer.getNativeVersion();
    // Classes
    public static String reverseMapExternal(Class<?> name) {
        return reverseMap(name).replace('$', '.').replace('/', '.');
    }

    public static String reverseMap(Class<?> name) {
        return reverseMap(Type.getInternalName(name));
    }

    public static String reverseMap(String check) {
        return (String)ReflectionTransformer.classDeMapping.getOrDefault(check, check);
    }

    // Methods
    public static String mapMethod(Class<?> inst, String name, Class<?>... parameterTypes) {
        String result = mapMethodInternal(inst, name, parameterTypes);
        if (result != null) {
            return result;
        }
        return name;
    }

    /**
     * Recursive method for finding a method from superclasses/interfaces
     */
    public static String mapMethodInternal(Class<?> inst, String name, Class<?>... parameterTypes) {
        String match = reverseMap(inst) + "/" + name + " ";

        for (Entry<String, String> entry : ReflectionTransformer.jarMapping.methods.entrySet()) {
            if (entry.getKey().startsWith(match)) {
                // Check type to see if it matches
                String[] str = entry.getKey().split("\\s+");
                int i = 0;
                for (Type type : Type.getArgumentTypes(str[1])) {
                    String typename = type.getSort() == 9 ? type.getInternalName() : type.getClassName();
                    if (i >= parameterTypes.length || !typename.equals(reverseMapExternal(parameterTypes[i]))) {
                        i=-1;
                        break;
                    }
                    i++;
                }

                if (i >= parameterTypes.length)
                    return entry.getValue();
            }
        }

        // Search interfaces
        ArrayList<Class<?>> parents = new ArrayList<Class<?>>();
        parents.add(inst.getSuperclass());
        parents.addAll(Arrays.asList(inst.getInterfaces()));

        for (Class<?> superClass : parents) {
            if (superClass == null) continue;
            mapMethodInternal(superClass, name, parameterTypes);
        }

        return null;
    }

    public static String mapClass(String pBukkitClass) {
        String tRemapped = JarRemapper.mapTypeName(pBukkitClass, ReflectionTransformer.jarMapping.packages, ReflectionTransformer.jarMapping.classes, pBukkitClass);
        if (tRemapped.equals(pBukkitClass) && pBukkitClass.startsWith("net/minecraft/server/") && !pBukkitClass.contains(NMS_VERSION)) {
            String tNewClassStr = "net/minecraft/server/" + NMS_VERSION + "/" + pBukkitClass.substring("net/minecraft/server/".length());
            return JarRemapper.mapTypeName(tNewClassStr, ReflectionTransformer.jarMapping.packages, ReflectionTransformer.jarMapping.classes, pBukkitClass);
        } else {
            return tRemapped;
        }
    }

    public static String getTypeDesc(Type pType) {
        try {
            return pType.getInternalName();
        } catch (NullPointerException var2) {
            return pType.toString();
        }
    }

    public static String demapFieldName(Field field) {
        String name = field.getName();
        String match = reverseMap(field.getDeclaringClass());
        Collection colls = ReflectionTransformer.methodDeMapping.get(name);
        Iterator var4 = colls.iterator();

        String value;
        do {
            if (!var4.hasNext()) {
                return name;
            }

            value = (String)var4.next();
        } while(!value.startsWith(match));

        String[] matched = value.split("\\/");
        String rtr = matched[matched.length - 1];
        return rtr;
    }

    public static String demapMethodName(Method method) {
        String name = method.getName();
        String match = reverseMap(method.getDeclaringClass());
        Collection colls = ReflectionTransformer.methodDeMapping.get(name);
        Iterator var4 = colls.iterator();

        String value;
        do {
            if (!var4.hasNext()) {
                return name;
            }

            value = (String)var4.next();
        } while(!value.startsWith(match));

        String[] matched = value.split("\\s+")[0].split("\\/");
        String rtr = matched[matched.length - 1];
        return rtr;
    }
}
