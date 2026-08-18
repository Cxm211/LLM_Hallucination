public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
        throws JsonMappingException
    {
        /* 03-Sep-2012, tatu: Not sure if this can be made to really work
         *    properly... but for now, try this:
         */
        // 02-Apr-2015, tatu: For dynamic case, very little we can do
        JsonSerializer<Object> ser = (JsonSerializer<Object>) _delegateSerializer;
        if (ser == null) {
            SerializerProvider prov = (visitor == null) ? null : visitor.getProvider();
            if (prov != null) {
                JavaType t = _delegateType;
                if (t == null) {
                    t = _converter.getOutputType(prov.getTypeFactory());
                }
                ser = (JsonSerializer<Object>) prov.findValueSerializer(t);
            }
        }
        if (ser != null) {
            ser.acceptJsonFormatVisitor(visitor, typeHint);
        }
    }