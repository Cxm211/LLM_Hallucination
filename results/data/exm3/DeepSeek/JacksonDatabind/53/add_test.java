// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMap1215Test.java
public void testConstructSpecializedTypeForConcurrentHashMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructParametricType(Map.class, String.class, Integer.class);
        JavaType specialized = tf.constructSpecializedType(mapType, ConcurrentHashMap.class);
        assertEquals(ConcurrentHashMap.class, specialized.getRawClass());
        assertEquals(2, specialized.containedTypeCount());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
        assertEquals(Integer.class, specialized.containedType(1).getRawClass());
    }
