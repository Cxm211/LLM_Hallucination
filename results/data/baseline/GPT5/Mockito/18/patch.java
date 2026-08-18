Object returnValueFor(Class<?> type) {
        if (Primitives.isPrimitiveOrWrapper(type)) {
            return Primitives.defaultValueForPrimitiveOrWrapper(type);
        } else if (type == LinkedHashSet.class) {
            return new LinkedHashSet<Object>();
        } else if (type == TreeSet.class) {
            return new TreeSet<Object>();
        } else if (type == HashSet.class) {
            return new HashSet<Object>();
        } else if (type == LinkedList.class) {
            return new LinkedList<Object>();
        } else if (type == ArrayList.class) {
            return new ArrayList<Object>();
        } else if (type == TreeMap.class) {
            return new TreeMap<Object, Object>();
        } else if (type == LinkedHashMap.class) {
            return new LinkedHashMap<Object, Object>();
        } else if (type == HashMap.class) {
            return new HashMap<Object, Object>();
        } else if (SortedSet.class.isAssignableFrom(type)) {
            return new TreeSet<Object>();
        } else if (Set.class.isAssignableFrom(type)) {
            return new HashSet<Object>();
        } else if (List.class.isAssignableFrom(type)) {
            return new LinkedList<Object>();
        } else if (SortedMap.class.isAssignableFrom(type)) {
            return new TreeMap<Object, Object>();
        } else if (Map.class.isAssignableFrom(type)) {
            return new HashMap<Object, Object>();
        } else if (Collection.class.isAssignableFrom(type)) {
            return new LinkedList<Object>();
        }
        return null;
    }