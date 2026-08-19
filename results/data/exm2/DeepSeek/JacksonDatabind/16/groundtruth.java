// ===== FIXED com.fasterxml.jackson.databind.introspect.AnnotationMap :: _add(Annotation) [lines 107-113] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-16-fixed/src/main/java/com/fasterxml/jackson/databind/introspect/AnnotationMap.java =====
    protected final boolean _add(Annotation ann) {
        if (_annotations == null) {
            _annotations = new HashMap<Class<? extends Annotation>,Annotation>();
        }
        Annotation previous = _annotations.put(ann.annotationType(), ann);
        return (previous == null) || !previous.equals(ann);
    }
