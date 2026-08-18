// ===== FIXED com.fasterxml.jackson.databind.ser.std.StdKeySerializers :: getFallbackKeySerializer(SerializationConfig, Class) [lines 69-88] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-55-fixed/src/main/java/com/fasterxml/jackson/databind/ser/std/StdKeySerializers.java =====
    public static JsonSerializer<Object> getFallbackKeySerializer(SerializationConfig config,
            Class<?> rawKeyType)
    {
        if (rawKeyType != null) {
            // 29-Sep-2015, tatu: Odd case here, of `Enum`, which we may get for `EnumMap`; not sure
            //   if that is a bug or feature. Regardless, it seems to require dynamic handling
            //   (compared to getting actual fully typed Enum).
            //  Note that this might even work from the earlier point, but let's play it safe for now
            // 11-Aug-2016, tatu: Turns out we get this if `EnumMap` is the root value because
            //    then there is no static type
            if (rawKeyType == Enum.class) {
                return new Dynamic();
            }
            if (rawKeyType.isEnum()) {
                return EnumKeySerializer.construct(rawKeyType,
                        EnumValues.constructFromName(config, (Class<Enum<?>>) rawKeyType));
            }
        }
        return DEFAULT_KEY_SERIALIZER;
    }
