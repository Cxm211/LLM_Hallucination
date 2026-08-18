// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMap1215Test.java
public void testConstructSpecializedTypeForCopyOnWriteArrayList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructParametricType(List.class, String.class);
        JavaType specialized = tf.constructSpecializedType(listType, CopyOnWriteArrayList.class);
        assertEquals(CopyOnWriteArrayList.class, specialized.getRawClass());
        assertEquals(1, specialized.containedTypeCount());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
    }
