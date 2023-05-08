package cc.xfl12345.android.droidcloudsms;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// @SuppressWarnings("unchecked")
public class HiddenApiBypassProxy {

    public static final HashMap<String, Class<?>> typeDic = new HashMap<>();

    static {
        typeDic.put(int.class.getName(), Integer.class);
        typeDic.put(double.class.getName(), Double.class);
        typeDic.put(float.class.getName(), Float.class);
        typeDic.put(long.class.getName(), Long.class);
        typeDic.put(short.class.getName(), Short.class);
        typeDic.put(byte.class.getName(), Byte.class);
        typeDic.put(boolean.class.getName(), Boolean.class);
        typeDic.put(char.class.getName(), Character.class);
    }

    public static <T> T[] splicing(T[] first,T[] second){
        Class<?> theType = Objects.requireNonNull(first.getClass().getComponentType());
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(theType, first.length + second.length);
        if (result.length == 0) {
            return result;
        } else {
            System.arraycopy(first, 0, result, 0, first.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }
    }

    public static <T> Object[] cast2ObjectArray(T[] args) {
        // T[] result = (T[]) Array.newInstance(type, args.length);
        return Arrays.stream(args).map(item -> (Object) item).toArray();
    }

    public static Class<?>[] getParamTypeArray(Object... args) {
        // T[] result = (T[]) Array.newInstance(type, args.length);
        return Arrays.stream(args).map(item -> item == null ? null : item.getClass()).toArray(Class<?>[]::new);
    }

    public static boolean isMatch(Class<?>[] paramTypes, Object[] args) {
        boolean result = false;
        if (paramTypes.length == args.length) {
            result = true;
            for (int i = 0; i < paramTypes.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    Class<?> argClass = arg.getClass();
                    argClass = typeDic.getOrDefault(argClass.getName(), argClass);

                    Class<?> paramType = paramTypes[i];
                    paramType = typeDic.getOrDefault(paramType.getName(), paramType);

                    if (!Objects.equals(paramType, argClass)) {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public static Method getDeclaredMethod(@NonNull Class<?> clazz, @NonNull String methodName, Object... args) throws NoSuchMethodException {
        List<Executable> methods = getDeclaredMethods(clazz);
        for (Executable executable : methods) {
            if (executable instanceof Method) {
                Method tmpMethod = (Method) executable;
                if (methodName.equals(tmpMethod.getName()) && isMatch(tmpMethod.getParameterTypes(), args)) {
                    return tmpMethod;
                }
            }
        }

        throw new NoSuchMethodException(String.format("method [%s] is not found in class [%s].", methodName, clazz.getCanonicalName()));
    }


    public static Object newInstance(@NonNull Class<?> clazz, Class<?>... initargs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return HiddenApiBypass.newInstance(clazz,  cast2ObjectArray(initargs));
        } else {
            try {
                return clazz.getDeclaredConstructor(splicing(new Class<?>[] {clazz}, initargs));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Object invoke(@NonNull Class<?> clazz, @Nullable Object thiz, @NonNull String methodName, Object... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return HiddenApiBypass.invoke(clazz, thiz, methodName, args);
        } else {
            try {
                Class<?>[] theClassArgs = getParamTypeArray(Class.class, args);
                Method method = null;
                try {

                    method = getDeclaredMethod(clazz, methodName, args);
                } catch (NoSuchMethodException e) {
                    // ignore
                }
                if (method == null) {
                    method = clazz.getMethod(methodName, theClassArgs);
                }

                method.setAccessible(true);
                return method.invoke(thiz, args);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Executable> getDeclaredMethods(@NonNull Class<?> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (List<Executable>) HiddenApiBypass.getDeclaredMethods(clazz);
        } else {
            return List.of(clazz.getDeclaredMethods());
        }
    }

    @NonNull
    public static Method getDeclaredMethod(@NonNull Class<?> clazz, @NonNull String methodName, @NonNull Class<?>... parameterTypes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return HiddenApiBypass.getDeclaredMethod(clazz, methodName, parameterTypes);
        } else {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getDeclaredConstructor(@NonNull Class<T> clazz, @NonNull Class<?>... parameterTypes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (Constructor<T>) HiddenApiBypass.getDeclaredConstructor(clazz, parameterTypes);
        } else {
            try {
                return clazz.getDeclaredConstructor(parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static List<Field> getInstanceFields(@NonNull Class<?> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (List<Field>) HiddenApiBypass.getInstanceFields(clazz);
        } else {
            return List.of(clazz.getDeclaredFields())
                    .parallelStream()
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .collect(Collectors.toList());
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static List<Field> getStaticFields(@NonNull Class<?> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (List<Field>) HiddenApiBypass.getStaticFields(clazz);
        } else {
            return List.of(clazz.getDeclaredFields())
                    .parallelStream()
                    .filter(field -> Modifier.isStatic(field.getModifiers()))
                    .collect(Collectors.toList());
        }
    }

    @SuppressLint("NewApi")
    public static boolean setHiddenApiExemptions(@NonNull String... signaturePrefixes) {
        return HiddenApiBypass.setHiddenApiExemptions(signaturePrefixes);
    }

    @SuppressLint("NewApi")
    public static boolean addHiddenApiExemptions(String... signaturePrefixes) {
        return HiddenApiBypass.addHiddenApiExemptions(signaturePrefixes);
    }

    @SuppressLint("NewApi")
    public static boolean clearHiddenApiExemptions() {
        return HiddenApiBypass.clearHiddenApiExemptions();
    }
}
