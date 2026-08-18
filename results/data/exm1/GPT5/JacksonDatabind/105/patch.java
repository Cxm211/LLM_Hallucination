public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
    {
        // Try generic from-string deserializers first
        JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
        if (d != null) {
            return d;
        }
        // Then specific known JDK types
        if (rawType == java.util.UUID.class) {
            return new UUIDDeserializer();
        }
        if (rawType == java.lang.StackTraceElement.class) {
            return new StackTraceElementDeserializer();
        }
        if (rawType == java.util.concurrent.atomic.AtomicBoolean.class) {
            // (note: AtomicInteger/Long work due to single-arg constructor. For now?)
            return new AtomicBooleanDeserializer();
        }
        if (rawType == java.nio.ByteBuffer.class) {
            return new ByteBufferDeserializer();
        }
        return null;
    }