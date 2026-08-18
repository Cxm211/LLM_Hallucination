public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer0) throws IOException
    {
        Object value = null;
        try {
            value = _accessorMethod.getValue(bean);
            if (value == null) {
                provider.defaultSerializeNull(gen);
                return;
            }
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) {
                ser = provider.findValueSerializer(value.getClass(), _property);
            } else {
                if (_forceTypeInformation) {
                    typeSer0.writeTypePrefixForScalar(bean, gen);
                    ser.serialize(value, gen, provider);
                    typeSer0.writeTypeSuffixForScalar(bean, gen);
                    return;
                }
            }
            ser.serializeWithType(value, gen, provider, typeSer0);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Throwable t = e;
            while (t instanceof InvocationTargetException && t.getCause() != null) {
                t = t.getCause();
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw JsonMappingException.wrapWithPath(t, bean, _accessorMethod.getName() + "()");
        }
    }