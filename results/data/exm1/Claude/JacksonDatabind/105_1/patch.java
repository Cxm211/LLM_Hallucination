public static JsonDeserializer<?> find(Class<?> rawType, String clsName)
{
    if (_classNames.contains(clsName)) {
        JsonDeserializer<?> d = FromStringDeserializer.findDeserializer(rawType);
        if (d != null) {
            return d;
        }
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
    
    if (rawType == AtomicBoolean.class) {
        return new AtomicBooleanDeserializer();
    }
    if (rawType == StackTraceElement.class) {
        return new StackTraceElementDeserializer();
    }
    if (rawType == ByteBuffer.class) {
        return new ByteBufferDeserializer();
    }
    if (rawType == UUID.class) {
        return new UUIDDeserializer();
    }
    
    return null;
}