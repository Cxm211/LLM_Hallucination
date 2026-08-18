// buggy function
    protected String _asString(BigDecimal value) throws IOException {
            // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
        return value.toString();
    }

    public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            String raw = Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features) ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }

    public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            String raw = isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN) ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }

// trigger testcase
// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java::testTooBigBigDecimal
public void testTooBigBigDecimal() throws Exception
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        // 24-Aug-2016, tatu: Initial check limits scale to [-9999,+9999]
        BigDecimal BIG = new BigDecimal("1E+9999");
        BigDecimal TOO_BIG = new BigDecimal("1E+10000");
        BigDecimal SMALL = new BigDecimal("1E-9999");
        BigDecimal TOO_SMALL = new BigDecimal("1E-10000");

        for (boolean useBytes : new boolean[] { false, true } ) {
            for (boolean asString : new boolean[] { false, true } ) {
                JsonGenerator g;
                
                if (useBytes) {
                    g = f.createGenerator(new ByteArrayOutputStream());
                } else {
                    g = f.createGenerator(new StringWriter());
                }
                if (asString) {
                    g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
                }

                // first, ok cases:
                g.writeStartArray();
                g.writeNumber(BIG);
                g.writeNumber(SMALL);
                g.writeEndArray();
                g.close();

                // then invalid
                for (BigDecimal input : new BigDecimal[] { TOO_BIG, TOO_SMALL }) {
                    if (useBytes) {
                        g = f.createGenerator(new ByteArrayOutputStream());
                    } else {
                        g = f.createGenerator(new StringWriter());
                    }
                    if (asString) {
                        g.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
                    }
                    try {
                        g.writeNumber(input);
                        fail("Should not have written without exception: "+input);
                    } catch (JsonGenerationException e) {
                        verifyException(e, "Attempt to write plain `java.math.BigDecimal`");
                        verifyException(e, "illegal scale");
                    }
                    g.close();
                }
            }
        }
    }
