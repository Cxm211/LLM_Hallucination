    public static Collection removeAll(Collection collection, Collection remove) {
        if (collection == null || remove == null) {
            throw new NullPointerException();
        }
        List result = new ArrayList();
        Set removeSet = new HashSet(remove);
        for (Object obj : collection) {
            if (!removeSet.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }