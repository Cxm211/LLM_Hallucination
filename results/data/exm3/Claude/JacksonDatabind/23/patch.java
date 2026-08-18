public static void addAll(Map<String, JsonSerializer<?>> allDeserializers)
{
    final JsonSerializer<?> intS = IntegerSerializer.instance;
    allDeserializers.put(Integer.class.getName(), intS);
    allDeserializers.put(Integer.TYPE.getName(), intS);
    allDeserializers.put(Long.class.getName(), LongSerializer.instance);
    allDeserializers.put(Long.TYPE.getName(), LongSerializer.instance);
    allDeserializers.put(Byte.class.getName(), IntLikeSerializer.instance);
    allDeserializers.put(Byte.TYPE.getName(), IntLikeSerializer.instance);
    allDeserializers.put(Short.class.getName(), ShortSerializer.instance);
    allDeserializers.put(Short.TYPE.getName(), ShortSerializer.instance);

    // Numbers, limited length floating point
    allDeserializers.put(Float.class.getName(), FloatSerializer.instance);
    allDeserializers.put(Float.TYPE.getName(), FloatSerializer.instance);
    allDeserializers.put(Double.class.getName(), DoubleSerializer.instance);
    allDeserializers.put(Double.TYPE.getName(), DoubleSerializer.instance);
}