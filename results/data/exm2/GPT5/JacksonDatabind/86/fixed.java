// ===== FIXED com.fasterxml.jackson.databind.type.ResolvedRecursiveType :: setReference(JavaType) [lines 20-27] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-86-fixed/src/main/java/com/fasterxml/jackson/databind/type/ResolvedRecursiveType.java =====
    public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }
