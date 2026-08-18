// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testGenericSignature1195_Additional() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        Method m;
        JavaType t;

        m = Generic1195_Additional.class.getMethod("getAtomicReferenceOfList");
        t = tf.constructType(m.getGenericReturnType());
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/util/List<Ljava/lang/String;>;>;",
                t.getGenericSignature());
    }

    static class Generic1195_Additional {
        public java.util.concurrent.atomic.AtomicReference<java.util.List<String>> getAtomicReferenceOfList() {
            return null;
        }
    }
