package net.cb.cb.library.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/***
 * 自动填充bean,用于模拟数据
 */
public class PullUtil {
	private static List<Object> beanObj=new ArrayList<>();
	

	private static void setValue(String type, String name, Object object, Field field) throws Exception {
		Method m;
		switch (type) {
		case "class java.lang.String":
			m = object.getClass().getMethod("set" + name, String.class);
			if (m == null)
				return;
			m.invoke(object, UUID.randomUUID().toString().substring(0,5));
			break;
			
		case "int":
			m = object.getClass().getMethod("set" + name, int.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextInt(100));
			break;
		case "class java.lang.Integer":
			m = object.getClass().getMethod("set" + name, Integer.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextInt(100));
			break;
			
		case "long":
			m = object.getClass().getMethod("set" + name, long.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextLong());
			break;
		case "class java.lang.Long":
			m = object.getClass().getMethod("set" + name, Long.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextLong());
			break;
			
		case "boolean":
			m = object.getClass().getMethod("set" + name, boolean.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextBoolean());
			break;
		case "class java.lang.Boolean":
			m = object.getClass().getMethod("set" + name, Boolean.class);
			if (m == null)
				return;
			m.invoke(object, new Random().nextBoolean());
			break;
			
		case "float":
			m = object.getClass().getMethod("set" + name, float.class);
			if (m == null)
				return;

			m.invoke(object, ((float)Math.round((new Random().nextInt(100)+new Random().nextFloat())*100)/100));
			break;
		case "class java.lang.Float":
			m = object.getClass().getMethod("set" + name, Float.class);
			if (m == null)
				return;
			m.invoke(object, ((float)Math.round((new Random().nextInt(100)+new Random().nextFloat())*100)/100));
			break;
			
		case "double":
			m = object.getClass().getMethod("set" + name, double.class);
			if (m == null)
				return;
			m.invoke(object, ((double)Math.round((new Random().nextInt(100)+new Random().nextDouble())*100)/100));
			break;
		case "class java.lang.Double":
			m = object.getClass().getMethod("set" + name, Double.class);
			if (m == null)
				return;
			m.invoke(object, ((double)Math.round((new Random().nextInt(100)+new Random().nextDouble())*100)/100));
			break;

		case "class java.util.Date":
			m = object.getClass().getMethod("set" + name, Date.class);
			if (m == null)
				return;
			m.invoke(object, new Date());
			break;

		default:
			if(name.startsWith("This"))
				return;
			
			Class<?> cls = field.getType();
			m = object.getClass().getMethod("set" + name, cls);

			if (m == null)
				return;

			switch (field.getType().getName()) {
			case "java.util.List":
				Type gt = field.getGenericType();
				if (gt instanceof ParameterizedType) {//获取E类
					ParameterizedType pt = (ParameterizedType) gt;

					Class<?> lbean = (Class) pt.getActualTypeArguments()[0];

					Object objChild = 		createObj(lbean);
			
					load(objChild);

					LogUtil.getLog().d("a=", "list>>" + name + "->" + lbean);
					List list = new ArrayList<>();
					for (int i = 0; i < new Random().nextInt(5); i++) {

						list.add(objChild);
					}

					m.invoke(object, list);

				}

				break;

			default:
				LogUtil.getLog().d("a=", "obj>>" + name + "->" + field.getType());

				
				
				Object objChild =createObj(cls);

				load(objChild);
				m.invoke(object, objChild);
				break;
			}

			break;
		}

	}
	
	private static Object createObj(Class<?> cls){
		Object ret=null;
		
		
		for(int i=0;i<beanObj.size()&&ret==null;i++){
			
			try {
				ret=	cls.getDeclaredConstructor(beanObj.get(i).getClass()).newInstance(beanObj.get(i));
			} catch (Exception e) {
				// TODO Auto-generated catch block
		//		e.printStackTrace();
			}
		}

		if(ret==null)
			System.err.println("e>>"+cls.getName());
		
		
		return ret;

		
	}

	public static void load(Object object) {
		beanObj.add(object);
		Field[] field = object.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
		try {
			for (int j = 0; j < field.length; j++) { // 遍历所有属性
				String name = field[j].getName(); // 获取属性的名字
				name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
				String type = field[j].getGenericType().toString(); // 获取属性的类型

				setValue(type, name, object, field[j]);

				// 如果有需要,可以仿照上面继续进行扩充,再增加对其它类型的判断
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
