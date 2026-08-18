// ===== FIXED com.fasterxml.jackson.databind.deser.std.JdkDeserializers :: find(Class, String) [lines 29-54] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-105-fixed/src/main/java/com/fasterxml/jackson/databind/deser/std/JdkDeserializers.java =====
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
                // (note: AtomicInteger/Long work due to single-arg constructor. For now?
                return new AtomicBooleanDeserializer();
            }
            if (rawType == ByteBuffer.class) {
                return new ByteBufferDeserializer();
            }
            if (rawType == Void.class) {
                return NullifyingDeserializer.instance;
            }
        }
        return null;
    }
