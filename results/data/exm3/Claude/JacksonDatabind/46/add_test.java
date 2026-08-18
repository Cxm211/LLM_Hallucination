// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testGenericSignatureNestedGenerics() throws Exception
{
    TypeFactory tf = TypeFactory.defaultInstance();
    Method m;
    JavaType t;

    m = Generic1195.class.getMethod("getNestedList");
    t = tf.constructType(m.getGenericReturnType());
    assertEquals("Ljava/util/List<Ljava/util/List<Ljava/lang/String;>;>;", t.getGenericSignature());
}