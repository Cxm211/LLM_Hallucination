public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
    {
        // First, try any of the standard from-String deserializers regardless of class name lookup
        JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
        if (d != null) {
            return d;
        }
        // Then, handle a few special JDK types we support directly
        if (_classNames.contains(clsName)) {
            if (rawType == UUID.class) {
                return new UUIDDeserializer();
            }
            if (rawType == StackTraceElement.class) {
                return new StackTraceElementDeserializer();
            }
            if (rawType == AtomicBoolean.class) {
                // (note: AtomicInteger/Long work due to single-arg constructor. For now?
                return new AtomicBooleanDeserializer();
            }
            if (rawType == ByteBuffer.class) {
                return new ByteBufferDeserializer();
            }
        }
        return null;
    }