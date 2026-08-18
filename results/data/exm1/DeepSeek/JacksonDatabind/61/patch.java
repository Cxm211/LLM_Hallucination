public boolean useForType(JavaType t) {
    if (t.isPrimitive()) {
        return false;
    }
    switch (_appliesFor) {
    case NON_CONCRETE_AND_ARRAYS:
        while (t.isArrayType()) {
            t = t.getContentType();
        }
        // fall through
    case OBJECT_AND_NON_CONCRETE:
        // 19-Apr-2016, tatu: ReferenceType like Optional also requires similar handling:
        while (t.isReferenceType()) {
            t = t.getReferencedType();
        }
        return t.isJavaLangObject()
                || (!t.isConcrete()
                        && !TreeNode.class.isAssignableFrom(t.getRawClass()));

    case NON_FINAL:
        while (t.isArrayType()) {
            t = t.getContentType();
        }
        while (t.isReferenceType()) {
            t = t.getReferencedType();
        }
        return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
    default:
    //case JAVA_LANG_OBJECT:
        return t.isJavaLangObject();
    }
}