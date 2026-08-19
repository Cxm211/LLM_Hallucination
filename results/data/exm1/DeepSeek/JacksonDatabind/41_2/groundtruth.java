// ===== FIXED com.fasterxml.jackson.databind.type.TypeFactory :: constructType(Type, Class) [lines 601-605] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-41-fixed/src/main/java/com/fasterxml/jackson/databind/type/TypeFactory.java =====
    public JavaType constructType(Type type, Class<?> contextClass) {
        TypeBindings bindings = (contextClass == null)
                ? TypeBindings.emptyBindings() : constructType(contextClass).getBindings();
        return _fromAny(null, type, bindings);
    }

// ===== FIXED com.fasterxml.jackson.databind.type.TypeFactory :: constructType(Type, JavaType) [lines 611-615] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-41-fixed/src/main/java/com/fasterxml/jackson/databind/type/TypeFactory.java =====
    public JavaType constructType(Type type, JavaType contextType) {
        TypeBindings bindings = (contextType == null)
                ? TypeBindings.emptyBindings() : contextType.getBindings();
        return _fromAny(null, type, bindings);
    }
