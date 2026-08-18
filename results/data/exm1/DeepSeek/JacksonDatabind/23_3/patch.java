    public static void addAll(Map<String, JsonSerializer<?>> allSerializers)
    {
        final JsonSerializer<?> intS = new IntegerSerializer();
        allSerializers.put(Integer.class.getName(), intS);
        allSerializers.put(Integer.TYPE.getName(), intS);
        allSerializers.put(Long.class.getName(), LongSerializer.instance);
        allSerializers.put(Long.TYPE.getName(), LongSerializer.instance);
        allSerializers.put(Byte.class.getName(), IntLikeSerializer.instance);
        allSerializers.put(Byte.TYPE.getName(), IntLikeSerializer.instance);
        allSerializers.put(Short.class.getName(), ShortSerializer.instance);
        allSerializers.put(Short.TYPE.getName(), ShortSerializer.instance);

        // Numbers, limited length floating point
        allSerializers.put(Float.class.getName(), FloatSerializer.instance);
        allSerializers.put(Float.TYPE.getName(), FloatSerializer.instance);
        allSerializers.put(Double.class.getName(), DoubleSerializer.instance);
        allSerializers.put(Double.TYPE.getName(), DoubleSerializer.instance);
    }