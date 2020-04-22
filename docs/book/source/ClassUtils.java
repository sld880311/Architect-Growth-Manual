package com.sunld.manager_core.util.rtti;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sunld.manager_core.exception.ExceptionUtil;
import com.sunld.manager_core.util.CommonUtilInterface;


/**
 * <p>处理class的工具类</p>
 * @author 孙辽东
 * <p>createDate:2014年3月4日 下午2:03:25 </p>
 * @version V1.0
 */
public final class ClassUtils implements CommonUtilInterface{
	public static final String ARRAY_SUFFIX = "[]";
	/**
	 * <p>void=class java.lang.Void</p>
	 * <p>double=class java.lang.Double</p>
	 * <p>long=class java.lang.Long</p>
	 * <p>float=class java.lang.Float</p>
	 * <p>int=class java.lang.Integer</p>
	 * <p>byte=class java.lang.Byte</p>
	 * <p>char=class java.lang.Character</p>
	 * <p>short=class java.lang.Short</p>
	 * <p>boolean=class java.lang.Boolean</p>
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE = new HashMap<Class<?>, Class<?>>(16);
	/**
	 * 
	 */
	private static final Map<String, Class<?>> PRIMITIVE_TYPE_NAME = new HashMap<String, Class<?>>(16);
	/**
	 * <p>class java.lang.Double=double</p>
	 * <p>class java.lang.Long=long</p>
	 * <p>class java.lang.Integer=int</p>
	 * <p>class java.lang.Short=short</p>
	 * <p>class java.lang.Boolean=boolean</p>
	 * <p>class java.lang.Float=float</p>
	 * <p>class java.lang.Character=char</p>
	 * <p>class java.lang.Void=void</p>
	 * <p>class java.lang.Byte=byte</p>
	 */
	private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE = new HashMap<Class<?>, Class<?>>(16);
	
	private static final ConcurrentMap<Class<?>, Map<String, Field>> FIELDS_MAP = new ConcurrentHashMap<Class<?>, Map<String, Field>>();
    private static final ConcurrentMap<Class<?>, Map<String, Method>> METHODS_MAP = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
    private static final ConcurrentMap<Class<?>, Map<String, Method>> GETTERS_MAP = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
    private static final ConcurrentMap<Class<?>, Map<String, Method>> SETTERS_MAP = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
    private static final ConcurrentMap<String, Class<?>> CLASS_MAP = new ConcurrentHashMap<String, Class<?>>();
	private ClassUtils() {}
	/**
	 * <p>完成PRIMITIVE_TO_WRAPPER_TYPE到WRAPPER_TO_PRIMITIVE_TYPE的转换</p>
	 */
	static {
		add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, boolean.class, Boolean.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, byte.class, Byte.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, char.class, Character.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, double.class, Double.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, float.class, Float.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, int.class, Integer.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, long.class, Long.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, short.class, Short.class);
	    add(PRIMITIVE_TO_WRAPPER_TYPE, WRAPPER_TO_PRIMITIVE_TYPE, void.class, Void.class);
	    Iterator<Class<?>> it = WRAPPER_TO_PRIMITIVE_TYPE.values().iterator();
	    while (it.hasNext()) {
	      Class<?> primitiveClass = it.next();
	      PRIMITIVE_TYPE_NAME.put(primitiveClass.getName(), primitiveClass);
	    }
	}
	private static void add(Map<Class<?>, Class<?>> forward,Map<Class<?>, 
			Class<?>> backward, Class<?> key, Class<?> value) {
		    forward.put(key, value);
		    backward.put(value, key);
	}
	/**
	 * <p>Returns true if this type is a primitive.</p>
	 * @param type type 原始类型 Type
	 * @return boolean
	 * <p>createDate:2014年8月15日 下午2:36:02</p>
	 */
	public static boolean isPrimitive(Type type) {
		return PRIMITIVE_TO_WRAPPER_TYPE.containsKey(type);
	}
	/**
	 * Returns {@code true} if {@code type} is one of the nine
	 * primitive-wrapper types, such as {@link Integer}.
	 * @see Class#isPrimitive
	 * @param type 对象类型Type
	 * @return boolean
	 * <p>createDate:2014年8月15日 下午2:36:27</p>
	 */
	public static boolean isWrapperType(Type type) {
	    return WRAPPER_TO_PRIMITIVE_TYPE.containsKey(type);
	}
	/**
	 * Returns the corresponding wrapper type of {@code type} if it is a primitive
	 * type; otherwise returns {@code type} itself. Idempotent.
	 * <pre>
	 *     wrap(int.class) == Integer.class
	 *     wrap(Integer.class) == Integer.class
	 *     wrap(String.class) == String.class
	 * </pre>
	 * @param type Type类型
	 * @param <T> 泛型
	 * @return Class&lt;T&gt; 
	 * <p>createDate:2014年8月15日 下午2:36:56</p>
	 */
	public static <T> Class<T> wrap(Class<T> type) {
		@SuppressWarnings("unchecked")
		Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER_TYPE.get(type);
		return (wrapped == null) ? type : wrapped;
	}
	/**
	 * Returns the corresponding primitive type of {@code type} if it is a
	 * wrapper type; otherwise returns {@code type} itself. Idempotent.
	 * <pre>
	 *     unwrap(Integer.class) == int.class
	 *     unwrap(int.class) == int.class
	 *     unwrap(String.class) == String.class
	 * </pre>
	 * @param <T> 泛型
	 * @param type Type
	 * @return Class&lt;T&gt; 
	 * <p>createDate:2014年8月15日 下午2:37:39</p>
	 */
	public static <T> Class<T> unwrap(Class<T> type) {
		@SuppressWarnings("unchecked")
		Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE_TYPE.get(type);
		return (unwrapped == null) ? type : unwrapped;
	}
	/**
	 * <p>返回默认的类加载器</p>
	 * @return ClassLoader
	 * <p>creteDate: 2014年3月4日 下午2:04:00</p>
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = ClassUtils.class.getClassLoader();
		}
		return cl;
	}
	/**
	 * 
	 * @param name 类的全路径信息
	 * @throws ClassNotFoundException 抛出类存在异常 
	 * @return Class&lt;?&gt;
	 * <p>creteDate: 2014年3月4日 下午2:04:15</p>
	 */
	public static Class<?> forName(String name) throws ClassNotFoundException {
		return forName(name, getDefaultClassLoader());
	}
	/**
	 * 
	 * @param name 类的全路径信息
	 * @param classLoader 类加载器
	 * @throws ClassNotFoundException  抛出类存在异常 
	 * @return Class&lt;?&gt;
	 * <p>creteDate: 2014年3月4日 下午2:04:29</p>
	 */
	public static Class<?> forName(String name, ClassLoader classLoader)throws ClassNotFoundException {
		Class<?> clazz = CLASS_MAP.get(name);
		if(clazz==null){
			clazz = resolvePrimitiveClassName(name);
			if (clazz == null){
				if (name.endsWith("[]")) {
					String elementClassName = name.substring(0,name.length() - "[]".length());
					Class<?> elementClass = forName(elementClassName, classLoader);
					clazz =  Array.newInstance(elementClass, 0).getClass();
				}
				if(clazz==null){
					clazz = Class.forName(name, true, classLoader);
				}
			}
			CLASS_MAP.putIfAbsent(name, clazz);
		}
		return clazz;
	}
	/**
	 * <p>解释原始类型的Class，如name输入int则返回int.class</p>
	 * @param name 原始类型名称
	 * @return Class&lt;?&gt;
	 * <p>creteDate: 2014年3月4日 下午2:04:42</p>
	 */
	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> result = null;
		if ((name != null) && (name.length() <= 8)) {
			result = (Class<?>) PRIMITIVE_TYPE_NAME.get(name);
		}
		return result;
	}
	/**
	 * <p>获取某个累的所有接口，保留父类中实现的接口</p>
	 * @param cls Class对象
	 * @return Class&lt;?&gt;[]
	 * <p>creteDate: 2014年3月4日 下午2:04:58</p>
	 */
	public static Class<?>[] getAllInterfaces(Class<?> cls) {
		if (cls == null)
			return null;
		List<Class<?>> list = new ArrayList<Class<?>>();
		while (cls != null) {
			Class<?>[] interfaces = cls.getInterfaces();
			for (int i = 0; i < interfaces.length; ++i) {
				if (!(list.contains(interfaces[i])))
					list.add(interfaces[i]);
				Class<?>[] superInterfaces = getAllInterfaces(interfaces[i]);
				for (int j = 0; j < superInterfaces.length; ++j) {
					Class<?> intface = superInterfaces[j];
					if (!(list.contains(intface)))
						list.add(intface);
				}

			}
			cls = cls.getSuperclass();
		}
		return ((Class[]) list.toArray(new Class[0]));
	}
	/**
	 * <p>获取某个类中正在执行的方法</p>
	 * @return String
	 * <p>creteDate: 2014年3月4日 下午2:05:16</p>
	 */
	public static String getClassCurrentMethod() {
		Throwable t = new Throwable();
		StackTraceElement[] stes = t.getStackTrace();
		return stes[stes.length - 1].getClassName() + "."+ stes[stes.length - 1].getMethodName();
	}
	/**
	 * <p>获取当前类的文件信息</p>
	 * @param clazz Class对象
	 * @return File
	 * <p>creteDate: 2014年3月4日 下午2:05:28</p>
	 */
	public static File getClassFile(Class<?> clazz) {
		String className = clazz.getName();
		String classSimpleName = className.substring(className.lastIndexOf(".") + 1) + ".classs";
		URL path = clazz.getResource(classSimpleName);
		if (path == null) {
			String name = className.replaceAll("[.]", "/");
			path = clazz.getResource("/" + name + ".class");
		}
		String filePath = "";
		try {
			// 防止中文乱码
			filePath = path.toURI().getPath();
		} catch (URISyntaxException e) {
			LOGGER.error("Get class file path error:"+ ExceptionUtil.formatException(e));
		}
		return new File(filePath);
	}
	/**
	 * <p>获取类的全路径信息</p>
	 * <p>如：F:\Workspaces\自主研发\webmodules\WebContent\WEB-INF
	 * \classes\com\sunld\test\log4j\TestLogger.class</p>
	 * @param clazz Class对象
	 * @return String
	 * <p>creteDate: 2014年3月4日 下午2:06:21</p>
	 */
	public static String getClassFilePath(Class<?> clazz) {
		return getClassFile(clazz).getAbsolutePath();
	}
	/**
	 * <p>取得当前类所在的ClassPath目录，比如tomcat下的classes路径</p>
	 * @param clazz Class对象
	 * @return File
	 * <p>creteDate: 2014年3月4日 下午2:06:48</p>
	 */
	public static File getClassPathFile(Class<?> clazz) {
		File file = getClassFile(clazz);
		for (int i = 0, count = clazz.getName().split("[.]").length; i < count; i++)
			file = file.getParentFile();
		if (file.getName().toUpperCase().endsWith(".JAR!")) {
			file = file.getParentFile();
		}
		return file;
	}
	/**
	 * <p>取得当前类所在的ClassPath路径</p>
	 * <p>如：F:\Workspaces\自主研发\webmodules\WebContent\WEB-INF\classes</p>
	 * @param clazz Class对象
	 * @return String
	 * <p>creteDate: 2014年3月4日 下午2:07:07</p>
	 */
	public static String getClassPath(Class<?> clazz) {
		return getClassPathFile(clazz).getAbsolutePath();
	}
	
	/**
     * <p>获取class对象中的属性</p>
     * @param clazz Class对象
     * @return Map&lt;String,Field&gt;
     * <p>createDate:2014年5月21日 上午10:29:38</p>
     */
    public static Map<String,Field> getFields(Class<?> clazz){
    	Map<String,Field> fields = FIELDS_MAP.get(clazz);
    	if(fields == null){
    		fields = new HashMap<String,Field>();
    		Field[] fieldArray = clazz.getDeclaredFields();
    		for(Field field:fieldArray){
    			field.setAccessible(true);
    			fields.put(field.getName(), field);
    		}
    		FIELDS_MAP.putIfAbsent(clazz, fields);
    	}
    	return fields;
    }
    /**
     * <p>获取class对象中的属性,并且排除掉指定的参数</p>
     * @param clazz Class对象
     * @param excludsFields 需要排除的属性
     * @return Map&lt;String,Field&gt;
     * <p>createDate:2014年5月21日 上午10:35:52</p>
     */
    public static Map<String,Field> getFields(Class<?> clazz, List<String> excludsFields){
    	Map<String,Field> fields = getFields(clazz);
    	for(Map.Entry<String, Field> field:fields.entrySet()){
    		String fieldName = field.getKey();
    		if(excludsFields.contains(fieldName)){
    			fields.remove(fieldName);
    		}
    	}
    	return fields;
    }
    /**
     * <p>获取class中的getter方法</p>
     * @param clazz Class对象
     * @return Map&lt;String,Method&gt;
     * <p>createDate:2014年5月21日 上午10:52:52</p>
     */
    public static Map<String,Method> getGetters(Class<?> clazz){
	   Map<String,Method> getters = GETTERS_MAP.get(clazz);
	   if(getters==null){
		   getters = new HashMap<String,Method>();
		   Method[] methodArray = clazz.getMethods();
		   for(Method method:methodArray){
			   String methodName = method.getName();
			   String propertyName = "";
			   if(Modifier.isStatic(method.getModifiers())) break;
			   if(Void.TYPE.equals(method.getReturnType())) break;
			   if(method.getParameterTypes().length != 0) break;
			   if(methodName.startsWith("get")){
				   if (methodName.length() < 4) break;
				   if (methodName.equals("getClass")) break;
				   if (!(Character.isUpperCase(methodName.charAt(3)))) break;
				   propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
			   }   
			   if(methodName.startsWith("is")){
				   if (methodName.length() < 3) break;
				   if (!(Character.isUpperCase(methodName.charAt(2)))) break;
				   propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
			   }
			   getters.put(propertyName, method);
		   }
		   GETTERS_MAP.putIfAbsent(clazz, getters);
	   }
	   return getters;
    }
    /**
     * <p>获取class中的setter方法</p>
     * @param clazz Class对象
     * @return Map&lt;String,Method&gt;
     * <p>createDate:2014年5月21日 上午10:58:15</p>
     */
    public static Map<String, Method> getSetters(Class<?> clazz) {
      Map<String, Method> setters = SETTERS_MAP.get(clazz);
      if (setters == null) {
    	  setters = new HashMap<String, Method>();
    	  Method[] methodArray = clazz.getMethods();
    	  for(Method method:methodArray){
    		  String methodName = method.getName();
    		  if (Modifier.isStatic(method.getModifiers())) break;
    		  if (!(method.getReturnType().equals(Void.TYPE))) break;
    		  if (method.getParameterTypes().length != 1) break;
    		  if (methodName.startsWith("set")) {
    			  if (methodName.length() < 4) break;
    			  if (!(Character.isUpperCase(methodName.charAt(3)))) break;
    			  String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    			  setters.put(propertyName, method);
    		  }
    	  }
    	  SETTERS_MAP.putIfAbsent(clazz, setters);
      }
      return setters;
    }
    /**
     * <p>获取所有的method</p>
     * @param clasz Class对象
     * @return Map&lt;String,Method&gt;
     * <p>createDate:2014年5月21日 上午11:12:46</p>
     */
    public static Map<String,Method> getMethods(Class<?> clasz){
    	Map<String,Method> methods = METHODS_MAP.get(clasz);
    	if(methods==null){
    		methods = new HashMap<String,Method>();
    		Method[] methodArray = clasz.getMethods();
    		for(Method method:methodArray){
    			String methodName = method.getName();
    			methods.put(methodName, method);
    		}
    		METHODS_MAP.putIfAbsent(clasz, methods);
    	}
    	return methods;
    }
    /**
     * <p>获取指定名字的方法</p>
     * @param clasz Class对象
     * @param methodName 方法名称
     * @return Method
     * <p>createDate:2014年5月21日 上午11:18:22</p>
     */
    public static Method getMethods(Class<?> clasz,String methodName){
    	Map<String,Method> methods = getMethods(clasz);
    	return methods.get(methodName);
    }
    /**
     * 初始化信息
     * <p>createDate:2014年5月21日 上午11:09:34</p>
     */
    public static void destroy() {
        FIELDS_MAP.clear();
        METHODS_MAP.clear();
        CLASS_MAP.clear();
        GETTERS_MAP.clear();
    }
}