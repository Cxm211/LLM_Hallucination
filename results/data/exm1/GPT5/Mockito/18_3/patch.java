Object returnValueFor(Class<?> type) {
        if (Primitives.isPrimitiveOrWrapper(type)) {
            return Primitives.defaultValueForPrimitiveOrWrapper(type);
        } else if (SortedSet.class.isAssignableFrom(type) || type == TreeSet.class) {
            return new TreeSet<Object>();
        } else if (LinkedHashSet.class.isAssignableFrom(type)) {
            return new LinkedHashSet<Object>();
        } else if (Set.class.isAssignableFrom(type) || type == HashSet.class) {
            return new HashSet<Object>();
        } else if (List.class.isAssignableFrom(type) || type == LinkedList.class) {
            return new LinkedList<Object>();
        } else if (type == ArrayList.class) {
            return new ArrayList<Object>();
        } else if (SortedMap.class.isAssignableFrom(type) || type == TreeMap.class) {
            return new TreeMap<Object, Object>();
        } else if (LinkedHashMap.class.isAssignableFrom(type)) {
            return new LinkedHashMap<Object, Object>();
        } else if (Map.class.isAssignableFrom(type) || type == HashMap.class) {
            return new HashMap<Object, Object>();
        } else if (Collection.class.isAssignableFrom(type) || type == Collection.class) {
            return new LinkedList<Object>();
        }
        //Let's not care about the rest of collections.
        return null;
    }