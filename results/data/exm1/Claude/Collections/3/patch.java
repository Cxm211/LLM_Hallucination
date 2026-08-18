public static Collection removeAll(Collection collection, Collection remove) {
        return ListUtils.subtract(collection, remove);
    }