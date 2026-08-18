// com/fasterxml/jackson/databind/type/TestTypeFactoryWithRecursiveTypes.java
public void testSetReferenceSameReference() {
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType stringType = tf.constructType(String.class);
    ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(null, null);
    recursiveType.setReference(stringType);
    // This should not throw an exception
    recursiveType.setReference(stringType);
}
