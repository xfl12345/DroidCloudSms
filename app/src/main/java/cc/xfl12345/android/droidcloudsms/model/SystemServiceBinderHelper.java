package cc.xfl12345.android.droidcloudsms.model;

import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class SystemServiceBinderHelper {
    protected String serviceName;

    protected ShizukuBinderWrapper shizukuBinderWrapper;

    protected Class<?> serviceClass = null;

    protected Class<?> serviceStubClass = null;

    protected Method asInterfaceMethod = null;

    // protected Object serviceInstance = null;

    protected Map<String, Map<MethodParamArray, Method>> serviceClassDeclaredMethods;

    protected Map<String, Map<MethodParamArray, Method>> serviceStubClassDeclaredMethods;

    protected Map<String, Map<MethodParamArray, Constructor<?>>> serviceClassConstructors;

    protected Map<String, Map<MethodParamArray, Constructor<?>>> serviceStubClassConstructors;

    public String getServiceName() {
        return serviceName;
    }

    public ShizukuBinderWrapper getShizukuBinderWrapper() {
        return shizukuBinderWrapper;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public Class<?> getServiceStubClass() {
        return serviceStubClass;
    }

    public Method getAsInterfaceMethod() {
        return asInterfaceMethod;
    }

    public Object getServiceInstance() {
        try {
            return asInterfaceMethod.invoke(null, shizukuBinderWrapper);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public SystemServiceBinderHelper(String serviceName) throws ReflectiveOperationException, RemoteException {
        this.serviceName = serviceName;

        shizukuBinderWrapper = new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName));
        String clazzName = shizukuBinderWrapper.getInterfaceDescriptor();
        serviceClass = Class.forName(Objects.requireNonNull(clazzName));
        serviceStubClass = Class.forName(clazzName + "$Stub");

        serviceStubClassConstructors = new ConcurrentHashMap<>();
        serviceStubClassDeclaredMethods = new ConcurrentHashMap<>();
        addMethods2Map(serviceStubClass, serviceStubClassDeclaredMethods, serviceStubClassConstructors);

        serviceStubClassConstructors = new ConcurrentHashMap<>();
        serviceClassDeclaredMethods = new ConcurrentHashMap<>();
        addMethods2Map(serviceClass, serviceClassDeclaredMethods, serviceClassConstructors);

        asInterfaceMethod = getServiceStubDeclaredMethod("asInterface", IBinder.class);
        // serviceInstance = asInterfaceMethod.invoke(null, shizukuBinderWrapper);
    }

    protected void addMethods2Map(
            Class<?> clazz,
            Map<String, Map<MethodParamArray, Method>> methodMap,
            Map<String, Map<MethodParamArray, Constructor<?>>> constructorMap) {
        Stream<Executable> stream = methodMap instanceof ConcurrentMap ?
                HiddenApiBypassProxy.getDeclaredMethods(clazz).parallelStream() :
                HiddenApiBypassProxy.getDeclaredMethods(clazz).stream();

        stream.forEach(item -> {
            if (item instanceof Constructor) {
                Constructor<?> constructor = (Constructor<?>) item;
                Map<MethodParamArray, Constructor<?>> methods = constructorMap.putIfAbsent(constructor.getName(), new ConcurrentHashMap<>());
                if (methods == null) {
                    methods = constructorMap.get(constructor.getName());
                }

                Objects.requireNonNull(methods).put(new MethodParamArray(constructor.getParameterTypes()), (Constructor<?>) item);
            } else if (item instanceof Method) {
                Method method = (Method) item;
                Map<MethodParamArray, Method> methods = methodMap.putIfAbsent(method.getName(), new ConcurrentHashMap<>());
                if (methods == null) {
                    methods = methodMap.get(method.getName());
                }

                Objects.requireNonNull(methods).put(new MethodParamArray(method.getParameterTypes()), method);
            } else {
                throw new RuntimeException(new ClassCastException(String.format("Can not cast [%s] to neither Constructor or Method.", item.toString())));
            }
        });
    }

    public Method getServiceDeclaredMethod(String name, Class<?>... parameterTypes) {
        Map<MethodParamArray, Method> methods = serviceClassDeclaredMethods.get(name);
        return methods == null ? null : methods.get(new MethodParamArray(parameterTypes));
    }

    public Method getServiceStubDeclaredMethod(String name, Class<?>... parameterTypes) {
        Map<MethodParamArray, Method> methods = serviceStubClassDeclaredMethods.get(name);
        return methods == null ? null : methods.get(new MethodParamArray(parameterTypes));
    }

    public Object executeServiceDeclaredMethod(String name, Object... args) throws ReflectiveOperationException {
        Class<?>[] parameterTypes;
        boolean hasNullValue = false;
        if (args.length == 0) {
            parameterTypes = new Class<?>[0];
        } else {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    hasNullValue = true;
                    break;
                }
                parameterTypes[i] = arg.getClass();
            }
        }

        if (hasNullValue) {
            Map<MethodParamArray, Method> methodMap = Objects.requireNonNull(serviceClassDeclaredMethods.get(name));
            for (MethodParamArray methodParamArray : methodMap.keySet()) {
                if (methodParamArray.isMatch(args)) {
                    Method method = methodMap.get(methodParamArray);
                    if (method == null) {
                        throw new NoSuchMethodException(name + " is not found.");
                    }
                    return method.invoke(getServiceInstance(), args);
                }
            }

            throw new NoSuchMethodException(name + " is not found.");

        } else {
            Method method = getServiceDeclaredMethod(name, parameterTypes);
            if (method == null) {
                throw new NoSuchMethodException(name + " is not found.");
            }
            return method.invoke(getServiceInstance(), args);
        }
    }

    public static class MethodParamArray implements Comparable<MethodParamArray> {
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

        protected Class<?>[] params;

        protected int myHashCode = 0;

        public MethodParamArray(Class<?>[] params) {
            this.params = new Class[params.length];
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                Class<?> param = params[i];
                Class<?> param2 = typeDic.getOrDefault(param.getName(), param);
                assert param2 != null;
                this.params[i] = param2;
                stringBuilder.append(param2.getName());
            }
            myHashCode = stringBuilder.toString().hashCode();
        }

        public Class<?>[] getParams() {
            return params;
        }

        public boolean isMatch(Object... args) {
            boolean result = false;
            if (params.length == args.length) {
                result = true;
                for (int i = 0; i < params.length; i++) {
                    Object arg = args[i];
                    if (arg != null) {
                        Class<?> argClass = arg.getClass();
                        argClass = typeDic.getOrDefault(argClass.getName(), argClass);
                        if (!params[i].equals(argClass)) {
                            result = false;
                            break;
                        }
                    }
                }
            }

            return result;
        }

        @Override
        public int hashCode() {
            return myHashCode;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean result = super.equals(obj);
            if (!result) {
                if (obj instanceof MethodParamArray) {
                    Class<?>[] tmp = ((MethodParamArray) obj).getParams();
                    if (tmp.length == params.length) {
                        boolean flag = true;
                        for (int i = 0; i < params.length; i++) {
                            if (!tmp[i].equals(params[i])) {
                                flag = false;
                                break;
                            }
                        }
                        result = flag;
                    }
                }
            }

            return result;
        }

        @Override
        public int compareTo(MethodParamArray o) {
            return o.hashCode() == hashCode() ? 0 : -1;
        }
    }
}
