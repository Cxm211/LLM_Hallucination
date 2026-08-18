// ===== FIXED com.fasterxml.jackson.databind.type.ReferenceType :: getGenericSignature(StringBuilder) [lines 151-158] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-46-fixed/src/main/java/com/fasterxml/jackson/databind/type/ReferenceType.java =====
    public StringBuilder getGenericSignature(StringBuilder sb)
    {
        _classSignature(_class, sb, false);
        sb.append('<');
        sb = _referencedType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }
