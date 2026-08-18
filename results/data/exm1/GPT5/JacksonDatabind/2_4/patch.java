public void writeObject(Object value) throws IOException
    {
            if (value == null) {
                writeNull();
                return;
            }
            // handle common scalars without codec
            if (value instanceof String) {
                writeString((String) value);
                return;
            }
            if (value instanceof Number) {
                if (value instanceof Integer) { writeNumber(((Integer) value).intValue()); return; }
                if (value instanceof Long) { writeNumber(((Long) value).longValue()); return; }
                if (value instanceof Double) { writeNumber(((Double) value).doubleValue()); return; }
                if (value instanceof Float) { writeNumber(((Float) value).floatValue()); return; }
                if (value instanceof Short) { writeNumber(((Short) value).shortValue()); return; }
                if (value instanceof Byte) { writeNumber(((Byte) value).byteValue()); return; }
                if (value instanceof java.math.BigInteger) { writeNumber((java.math.BigInteger) value); return; }
                if (value instanceof java.math.BigDecimal) { writeNumber((java.math.BigDecimal) value); return; }
            }
            if (value instanceof Boolean) {
                writeBoolean(((Boolean) value).booleanValue());
                return;
            }
            if (value instanceof Character) {
                writeString(value.toString());
                return;
            }
            if (value instanceof byte[]) {
                writeBinary((byte[]) value);
                return;
            }
            // If we have a codec, let it handle complex values
            if (_objectCodec != null) {
                _objectCodec.writeValue(this, value);
                return;
            }
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
    }