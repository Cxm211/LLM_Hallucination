// buggy function
    public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }

// trigger testcase
// com/fasterxml/jackson/databind/type/TestTypeFactoryWithRecursiveTypes.java::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType
public void testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType() throws IOException {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(Base.class);
        tf.constructType(Sub.class);
        Sub sub = new Sub();
        String serialized = objectMapper().writeValueAsString(sub);
        assertEquals("{\"base\":1,\"sub\":2}", serialized);
    }
