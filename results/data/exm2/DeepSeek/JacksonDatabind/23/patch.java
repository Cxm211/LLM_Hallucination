    public static void addAll(Map<String, JsonSerializer<?>> allDeserializers)
    {
        final JsonSerializer<?> intS = new IntegerSerializer();
        allDeserializers.put(Integer.class.getName(), intS);
        allDeserializers.put(Integer.TYPE.getName(), intS);
        allDeserializers.put(Long.class.getName(), new LongSerializer());
        allDeserializers.put(Long.TYPE.getName(), new LongSerializer());
        allDeserializers.put(Byte.class.getName(), new IntLikeSerializer());
        allDeserializers.put(Byte.TYPE.getName(), new IntLikeSerializer());
        allDeserializers.put(Short.class.getName(), new ShortSerializer());
        allDeserializers.put(Short.TYPE.getName(), new ShortSerializer());

        // Numbers, limited length floating point
        allDeserializers.put(Float.class.getName(), new FloatSerializer());
        allDeserializers.put(Float.TYPE.getName(), new FloatSerializer());
        allDeserializers.put(Double.class.getName(), new DoubleSerializer());
        allDeserializers.put(Double.TYPE.getName(), new DoubleSerializer());
    }