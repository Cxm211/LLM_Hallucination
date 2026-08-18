// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testRecursiveTypeVariable() throws Exception {
    TypeFactory tf = TypeFactory.defaultInstance();
    TypeVariable<?> typeVar = Enum.class.getTypeParameters()[0];
    JavaType stringType = tf.constructType(String.class);
    TypeBindings context = stringType.getBindings();
    JavaType result = tf.constructType(typeVar, context);
    assertNotNull(result);
}
