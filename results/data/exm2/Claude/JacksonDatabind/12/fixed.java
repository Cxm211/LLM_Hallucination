// ===== FIXED com.fasterxml.jackson.databind.deser.std.MapDeserializer :: isCachable() [lines 299-307] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-12-fixed/src/main/java/com/fasterxml/jackson/databind/deser/std/MapDeserializer.java =====
    public boolean isCachable() {
        /* As per [databind#735], existence of value or key deserializer (only passed
         * if annotated to use non-standard one) should also prevent caching.
         */
        return (_valueDeserializer == null)
                && (_keyDeserializer == null)
                && (_valueTypeDeserializer == null)
                && (_ignorableProperties == null);
    }
