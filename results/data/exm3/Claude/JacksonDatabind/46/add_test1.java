// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testGenericSignatureComplexNesting() throws Exception
{
    TypeFactory tf = TypeFactory.defaultInstance();
    Method m;
    JavaType t;

    m = Generic1195.class.getMethod("getMapOfLists");
    t = tf.constructType(m.getGenericReturnType());
    assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Ljava/lang/Integer;>;>;", t.getGenericSignature());
}