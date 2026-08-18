public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
    {
        JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
        if (d != null) {
            return d;
        }
        if (_classNames.contains(clsName)) {
            if (rawType == UUID.class) {
                return new UUIDDeserializer();
            }
            if (rawType == StackTraceElement.class) {
                return new StackTraceElementDeserializer();
            }
            if (rawType == AtomicBoolean.class) {
                return new AtomicBooleanDeserializer();
            }
            if (rawType == ByteBuffer.class) {
                return new ByteBufferDeserializer();
            }
        }
        return null;
    }