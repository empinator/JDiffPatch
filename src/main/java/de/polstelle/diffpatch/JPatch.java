package de.polstelle.diffpatch;

import de.polstelle.diffpatch.model.DiffDetail;
import de.polstelle.diffpatch.model.DiffRecord;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

public class JPatch {

	private final Object obj;

	public JPatch(Object obj) {
		this.obj = obj;
	}

	public Object patch(DiffRecord diff) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, ParseException {
		for(DiffDetail d : diff.getDetails()) {
			String[] split = d.getProperty().split("\\.");
			Stack<String> s = new Stack<>();
			List<String> list = Arrays.asList(split);
			Collections.reverse(list);
			s.addAll(list);
			recursivelySetValue(s, obj, d);

			System.out.println();
		}
		return obj;
	}

	private void recursivelySetValue(Stack<String> s, Object obRec, DiffDetail detail) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, ParseException {
		String poppedProp = s.pop();

        Class propertyClazzType = ClassUtils.getClass(detail.getPropertyClazzType());
        Class<?> computerClass = obRec.getClass();
        Object nv = getValueAsObject(propertyClazzType, detail.getNewValue());
        if(poppedProp.endsWith("]")) {
            int startOfArrayIndex = poppedProp.indexOf("[");
            String extractPropName = poppedProp.substring(0, startOfArrayIndex);
            String extractedIndex = poppedProp.substring(startOfArrayIndex).replaceAll("\\[", "").replaceAll("\\]", "");

            Integer index = Integer.valueOf(extractedIndex);
            List result = getListObject(obRec, computerClass, extractPropName);
            if(!s.isEmpty()) {
                recursivelySetValue(s, result.get(index), detail);
            } else {
               // gibts net
            }
        } else if(detail.isListEntry()) {
            Integer index = detail.getListIndex();
            List result = getListObject(obRec, computerClass, poppedProp);
            if(StringUtils.equals(detail.getNewValue(), "")) {
                result.remove(index);
            } else {
                result.set(index, nv);
            }
        } else {

            if(!s.isEmpty()) {
                Method method = getFieldMethod("get", poppedProp, computerClass);
                Object result = method.invoke(obRec);
                recursivelySetValue(s, result, detail);
            } else {
                String methodName = getMethod("set", poppedProp);
                Method method = computerClass.getDeclaredMethod(methodName, propertyClazzType);
                method.invoke(obRec, nv);
            }
        }

	}

    private List getListObject(Object obRec, Class<?> computerClass, String extractPropName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = getFieldMethod("get", extractPropName, computerClass);
        return (List) method.invoke(obRec);
    }

    private Method getFieldMethod(String get, String poppedProp, Class<?> computerClass) throws NoSuchMethodException {
        String methodName = getMethod(get, poppedProp);
        return computerClass.getDeclaredMethod(methodName);
    }

    private Object getValueAsObject(Class propertyClazzType, String newValue) throws ParseException {
        Object nv = null;
        if(propertyClazzType.equals(Boolean.class) || propertyClazzType.equals(boolean.class) ) {
            nv = Boolean.valueOf(newValue);
        } else if(propertyClazzType.equals(Integer.class) || propertyClazzType.equals(int.class) ) {
            nv = Integer.parseInt(newValue);
        } else if(propertyClazzType.equals(Long.class) || propertyClazzType.equals(long.class) ) {
            nv = Long.parseLong(newValue);
        } else if(propertyClazzType.equals(Date.class) || propertyClazzType.equals(Timestamp.class) ) {
            nv = DateUtils.parseDate(newValue, new String[]{JDiff.DATE_PATTERN});
        } else {
            nv = newValue;
        }
        return nv;
    }

    private String getMethod(String pre, String poppedProp) {
		return pre + poppedProp.substring(0, 1).toUpperCase() + poppedProp.substring(1);
	}
}
