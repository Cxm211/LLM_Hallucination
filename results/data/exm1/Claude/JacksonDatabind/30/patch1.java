protected Object _convert(Object fromValue, JavaType toValueType)
        throws IllegalArgumentException
    {        
        Class<?> targetType = toValueType.getRawClass();
        if (targetType != Object.class
                && !toValueType.hasGenericTypes()
                && targetType.isAssignableFrom(fromValue.getClass())) {
            return fromValue;
        }
        
        TokenBuffer buf = new TokenBuffer(this, false);
        try {
            SerializationConfig config = getSerializationConfig().without(SerializationFeature.WRAP_ROOT_VALUE);
            _serializerProvider(config).serializeValue(buf, fromValue);

            final JsonParser jp = buf.asParser();
            Object result;
            final DeserializationConfig deserConfig = getDeserializationConfig();
            JsonToken t = _initForReading(jp);
            if (t == JsonToken.VALUE_NULL) {
                DeserializationContext ctxt = createDeserializationContext(jp, deserConfig);
                result = _findRootDeserializer(ctxt, toValueType).getNullValue(ctxt);
            } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = null;
            } else { 
                DeserializationContext ctxt = createDeserializationContext(jp, deserConfig);
                JsonDeserializer<Object> deser = _findRootDeserializer(ctxt, toValueType);
                result = deser.deserialize(jp, ctxt);
            }
            jp.close();
            return result;
        } catch (IOException e) { 
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }