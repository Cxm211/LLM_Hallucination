// com/fasterxml/jackson/databind/type/TestTypeFactory.java::testCanonicalNames
        // Additional raw Map canonical handling
        JavaType t2 = tf.constructFromCanonical("java.util.Map");
        assertEquals(java.util.Map.class, t2.getRawClass());
        assertEquals(MapType.class, t2.getClass());
        assertEquals(Object.class, t2.getKeyType().getRawClass());
        assertEquals(Object.class, t2.getContentType().getRawClass());
        String can2 = t2.toCanonical();
        assertEquals("java.util.Map<java.lang.Object,java.lang.Object>", can2);
        assertEquals(t2, tf.constructFromCanonical(can2));
