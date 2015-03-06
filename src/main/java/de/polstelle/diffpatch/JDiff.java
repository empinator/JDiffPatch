package de.polstelle.diffpatch;

import de.polstelle.diffpatch.model.DiffDetail;
import de.polstelle.diffpatch.model.DiffIgnore;
import de.polstelle.diffpatch.model.DiffRecord;
import de.polstelle.diffpatch.model.DiffType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class JDiff {

    public static final String DATE_PATTERN = "dd.MM.yyyy HH:mm";

    private String prefix = "";

	private DiffRecord audit;

	private Object oldObject;

	private Object newObject;

    private Class<?> comparedClassType;

	private Map<String, Method> methodMap = new HashMap<String, Method>();

	public JDiff() {
	}

	JDiff(String prefix) {
		this.prefix = prefix;
	}

	public DiffRecord compare(Object oldEntity, Object newEntity) {
        newObject = newEntity;
        oldObject = oldEntity;

		// Beide Paramter null, nichts tun
		if (oldEntity == null && newEntity == null) {
			return null;
		}

		audit = new DiffRecord();

		// Das Neue Entity ist null, altes Entity hatte einen Werte, also
		// Löschung
		if (newEntity == null) {
			audit.setType(DiffType.DELETED);
            this.comparedClassType = oldEntity.getClass();
            compare();

        } else if (oldEntity == null) {
			audit.setType(DiffType.CREATED);
            this.comparedClassType = newEntity.getClass();
            compare();

        } else {

            this.comparedClassType = newEntity.getClass();
		    compare();
            if (audit.isNotEmpty()) {
				audit.setType(DiffType.UPDATED);
			} else {
				audit.setType(DiffType.UNCHANGED);
	         }
        }


		return audit;
	}

	private void compare() {
		fillMethodMap(methodMap, comparedClassType);
		getFieldValues();
	}

	/**
	 * Aufruf der vorher in einer Map zusammengefassten Methoden, zum Vergleich
	 * der Rückgabewerte
	 */
	private void getFieldValues() {

		for (String lName : methodMap.keySet()) {
            Method methodNAme = methodMap.get(lName);
            Object oo = oldObject;
            Object no = newObject;

            if (isCollectionObject(methodNAme)) {
                List<DiffDetail> listDetail = compareTwoLists(methodNAme, oo, no);
				audit.getDetails().addAll(listDetail);

			} else if (isPrimitiveObject(methodNAme)) {
                FieldCompare lComp = compareFields(methodNAme, oo, no);
                if (!lComp.isEqual()) {
                    audit.getDetails().add(lComp.getDetail());
                }
			} else  {
				DiffRecord diffRecord = compareReferencedObjects(methodNAme, oo, no);
				if(diffRecord != null) {
					if(CollectionUtils.isNotEmpty(diffRecord.getDetails())) {
						audit.getDetails().addAll(diffRecord.getDetails());
					}
				}
			}
		}

	}

	/**
	 * Die Methode vergleicht embedded Objekte, bspw. Adresse
	 * 
	 * @param method
	 *            Die Methode
	 * @param oldObject
	 *            Das Alte Objekt
	 * @param newObject
	 *            Das Neue Objekt
	 * @return Eine Liste von AuditDetails, die die Unterschiede innerhalb des
	 *         Embedded Objekts repräsentieren
	 */
	private DiffRecord compareReferencedObjects(Method method, Object oldObject, Object newObject) {
		if(isNotIgnore(method)) {
			try {
				Object newRetObj = (newObject == null) ? null : method.invoke(newObject);
				Object oldRetObj = (oldObject == null) ? null : method.invoke(oldObject);
				String formattedMethodName = getPropertyNameFromMethod(method);
				return new JDiff(formattedMethodName).compare(oldRetObj, newRetObj);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

    private List<DiffDetail> compareTwoLists(Method method, Object oldObject, Object newObject) {
		List<DiffDetail> listDetails = new ArrayList<>();

        try {

            Type newRetObj = method.getGenericReturnType();

            ParameterizedType pType = (ParameterizedType)newRetObj;
            Type[] arr = pType.getActualTypeArguments();

            Set<Class<?>> clazzes = new HashSet<>();
            for (Type tp: arr) {
                clazzes.add( (Class<?>) tp );
            }

            if (newRetObj instanceof ParameterizedType && isNotIgnore(method)) {


                Object[] oldObjArr = (oldObject == null) ? new Object[]{} : ((Collection<?>) method.invoke(oldObject)).toArray();
                Object[] newObjArr = (newObject == null) ? new Object[]{} : ((Collection<?>) method.invoke(newObject)).toArray();

                for(int i = 0; i < oldObjArr.length; i++) {
                    Object newRetObjOrNull = i < newObjArr.length ? newObjArr[i] : null;
                    Object oldRetObjOrNull = i < oldObjArr.length ? oldObjArr[i] : null;

					List<DiffDetail> diffDetails = compareListEntry(i, method, clazzes, newRetObjOrNull, oldRetObjOrNull);
                    if(!diffDetails.isEmpty()) {
                        listDetails.addAll(diffDetails);
                    }
                }
                if(newObjArr.length > oldObjArr.length) {
                    for(int i = oldObjArr.length; i < newObjArr.length; i++) {
                        Object newRetObjOrNull = i < newObjArr.length ? newObjArr[i] : null;

						List<DiffDetail> diffDetails = compareListEntry(i, method, clazzes, newRetObjOrNull, null);
						if(!diffDetails.isEmpty()) {
							listDetails.addAll(diffDetails);
						}

					}
                }


            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return listDetails;
    }

    private List<DiffDetail> compareListEntry(int index, Method method, Set<Class<?>> clazzes, Object newRetObjOrNull, Object oldRetObjOrNull) {

        String formattedMethodName = getPropertyNameFromMethod(method);

        boolean primitive = isPrmititiveClass(clazzes);

        List<DiffDetail> details = new ArrayList<>();
		if(primitive) {
            Class<?> firstParam = new ArrayList<>(clazzes).get(0);
            FieldCompare lComp = compareValues(formattedMethodName, firstParam, newRetObjOrNull, oldRetObjOrNull);
            if (!lComp.isEqual()) {
                DiffDetail detail = lComp.getDetail();
				detail.setListEntry(true);
				detail.setListIndex(index);
				details.add(detail);
            }
        } else {
			String property = String.format("%s[%d]", formattedMethodName, index);
			DiffRecord rec = new JDiff(property).compare(oldRetObjOrNull, newRetObjOrNull);
			if(CollectionUtils.isNotEmpty(rec.getDetails())) {
				details.addAll(rec.getDetails());
			}
		}
        return details;
    }

    /**
	 * Die Methode vergleicht den Rückgabewert einer Methoden an neuem und altem
	 * Objekt
	 * 
	 * @param method
	 *            Die Methode
	 * @param pOld
	 *            Altes Objekt
	 * @param pNew
	 *            Neues Objekt
	 * @return Comparator
	 */
	private FieldCompare compareFields(Method method, Object pOld, Object pNew) {
        if(isNotIgnore(method)) {
            try {
                String propertyName = getPropertyNameFromMethod(method);

                Object newRetObj = pNew == null ? null : method.invoke(pNew);
                Object oldRetObj = pOld == null ? null : method.invoke(pOld);

                return compareValues(propertyName, method.getReturnType(), newRetObj, oldRetObj);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

		return new FieldCompare();
	}

	private boolean isNotIgnore(Method method) {
		return !method.isAnnotationPresent(DiffIgnore.class);
	}

	private FieldCompare compareValues(String propertyName, Class<?> returnType, Object newRetObj, Object oldRetObj) {
		FieldCompare cmp = new FieldCompare();
		cmp.setName(propertyName);
        cmp.setPropertyType(returnType);
		cmp.setClazzName(comparedClassType.getSimpleName());

		String newString = null;
		String oldString = null;

		if (newRetObj != null) {
			newString = getValue(newRetObj);
		}
		if (oldRetObj != null) {
			oldString = getValue(oldRetObj);
		}

		cmp.setOldValue(oldString);
		cmp.setNewValue(newString);

		return cmp;
	}

    private String getPropertyNameFromMethod(Method method) {
        String pureName = method.getName().replaceAll("^(get|set|is)", "");
        pureName = pureName.substring(0, 1).toLowerCase() + pureName.substring(1);
        return ( StringUtils.equals("", prefix) ? pureName : String.format("%s.%s", prefix, pureName) );
    }

    /**
	 * Erzeugen eine Map von Methodennamen und Methoden, Rekursiv nach oben, bis
	 * zu Object
	 * 
	 * @param pMmap
	 *            Die Map
	 * @param clazz
	 *            Die Class
	 */
	private void fillMethodMap(Map<String, Method> pMmap, Class<?> clazz) {
		if (clazz.equals(Object.class)) {
			return;
		}
		// Rekursiver Aufruf
		fillMethodMap(pMmap, clazz.getSuperclass());

		Map<String, Method> allMethods = new HashMap<>();

		for (Method method : clazz.getDeclaredMethods()) {
			allMethods.put(method.getName(), method);
		}

		for (String methodName : allMethods.keySet()) {
			if (methodName.startsWith("get") || methodName.startsWith("is")) {
				// Nur wenn auch setter existiert
				if (allMethods.containsKey(methodName.replaceAll("^(get|is)", "set"))) {
					pMmap.put(methodName, allMethods.get(methodName));
				}
			}

		}
	}

	private String getValue(Object pObj) {
		if (pObj != null) {
			if (pObj instanceof Date) {
				SimpleDateFormat lSdf = new SimpleDateFormat(DATE_PATTERN);
				return lSdf.format(pObj);
            } else if(pObj instanceof Boolean) {
                return ( (Boolean) pObj) ? "true" : "false";
			} else {
				return pObj.toString();
			}
		} else {
			return null;
		}
	}

	private class FieldCompare {
		private String mOldValue;

		private String mNewValue;

		private String mFieldName;

        private String clazzName;

        private Class<?> propertyType;

        public String getOldValue() {
			return mOldValue;
		}

		public void setOldValue(String oldValue) {
			if (oldValue == null) {
				oldValue = "";
			}
			this.mOldValue = oldValue.trim();
		}

		public String getNewValue() {
			return mNewValue;
		}

		public void setNewValue(String newValue) {
			if (newValue == null) {
				newValue = "";
			}
			this.mNewValue = newValue.trim();
		}

		public void setName(String pMethod) {
			mFieldName = pMethod.trim();
		}

		public String getProperty() {
			return mFieldName;
		}

		public boolean isEqual() {
			return StringUtils.equals(mOldValue, mNewValue);
		}

        public DiffDetail getDetail() {
            DiffDetail lDetail = new DiffDetail();
            lDetail.setProperty(getProperty());
            lDetail.setParentClazzType(getClazzName());
            lDetail.setPropertyClazzType(getPropertyType().getName());
            lDetail.setOldValue(getOldValue());
            lDetail.setNewValue(getNewValue());
            return lDetail;
        }

		public void setClazzName(String clazzName) {
			this.clazzName = clazzName;
		}

		public String getClazzName() {
			return clazzName;
		}

        public Class<?> getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(Class<?> propertyType) {
            this.propertyType = propertyType;
        }
    }


    private boolean isPrimitiveObject(Method method) {
        try {
            Set<Class<?>> classes = getGeneralizations(method.getReturnType());
            return isPrmititiveClass(classes);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isPrmititiveClass(Set<Class<?>> classes) {
        return classes.contains(String.class)
                || classes.contains(Integer.class)
                || classes.contains(int.class)
                || classes.contains(Long.class)
                || classes.contains(long.class)
                || classes.contains(Boolean.class)
                || classes.contains(boolean.class)
                || classes.contains(Date.class)
                || classes.contains(Timestamp.class);
    }

    /**
     * Prüft, ob der Getter eine Collection (Collection, AbstractCollection,
     * Map, AbstractMap, DvVerteilerBag) liefert
     *
     * @param method
     *            Die Methode
     * @return True, wenn es sich um eine Collection handelt, ansonsten false
     */
    private boolean isCollectionObject(Method method) {
        try {
            Set<Class<?>> classes = getGeneralizations(method.getReturnType());
            return classes.contains(ArrayList.class) || classes.contains(Collection.class) || classes.contains(AbstractCollection.class) || classes.contains(Map.class)
                    || classes.contains(AbstractMap.class);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Builds an <b>unordered</b> set of all interface and object classes that
     * are generalizations of the provided class.
     *
     * @param classObject
     *            the class to find generalization of.
     * @return a Set of class objects.
     */
    private Set<Class<?>> getGeneralizations(Class classObject) {
        Set<Class<?>> generalizations = new HashSet<Class<?>>();

        generalizations.add(classObject);

        Class superClass = classObject.getSuperclass();
        if (superClass != null) {
            generalizations.addAll(getGeneralizations(superClass));
        }

        Class[] superInterfaces = classObject.getInterfaces();
        for (Class superInterface : superInterfaces) {
            generalizations.addAll(getGeneralizations(superInterface));
        }

        return generalizations;
    }
}
