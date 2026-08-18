// buggy function
        protected Object _deserializeFromEmptyString() throws IOException {
            // As per [databind#398], URI requires special handling
            if (_kind == STD_URI) {
                return URI.create("");
            }
            // As per [databind#1123], Locale too
            return super._deserializeFromEmptyString();
        }

// trigger testcase
// com/fasterxml/jackson/databind/deser/TestJdkTypes.java::testLocale
public void testLocale() throws IOException
    {
        assertEquals(new Locale("en"), MAPPER.readValue(quote("en"), Locale.class));
        assertEquals(new Locale("es", "ES"), MAPPER.readValue(quote("es_ES"), Locale.class));
        assertEquals(new Locale("FI", "fi", "savo"),
                MAPPER.readValue(quote("fi_FI_savo"), Locale.class));
        // [databind#1123]
        Locale loc = MAPPER.readValue(quote(""), Locale.class);
        assertSame(Locale.ROOT, loc);
    }
