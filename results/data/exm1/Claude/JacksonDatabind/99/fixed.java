// ===== FIXED com.fasterxml.jackson.databind.type.ReferenceType :: buildCanonicalName() [lines 163-171] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-99-fixed/src/main/java/com/fasterxml/jackson/databind/type/ReferenceType.java =====
    protected String buildCanonicalName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_class.getName());
        sb.append('<');
        sb.append(_referencedType.toCanonical());
        sb.append('>');
        return sb.toString();
    }
