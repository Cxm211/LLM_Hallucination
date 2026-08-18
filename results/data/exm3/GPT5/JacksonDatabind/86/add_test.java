// com/fasterxml/jackson/databind/type/RecursiveTypeTest.java::testSuperClassWithReferencedJavaType
public void testSuperClassWithReferencedJavaTypeWhenSubConstructedFirst() {
        TypeFactory tf = objectMapper().getTypeFactory();
        tf.constructType(Sub.class); // construct sub before base
        tf.constructType(Base.class);
        JavaType subType = tf.constructType(Sub.class);
        JavaType baseTypeFromSub = subType.getSuperClass();
        assertNotNull(baseTypeFromSub.getSuperClass());
    }