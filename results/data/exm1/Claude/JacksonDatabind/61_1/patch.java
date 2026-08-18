public boolean useForType(JavaType t)
{
    switch (_appliesFor) {
    case NON_CONCRETE_AND_ARRAYS:
        while (t.isArrayType()) {
            t = t.getContentType();
        }
        // fall through
    case OBJECT_AND_NON_CONCRETE:
        while (t.isReferenceType()) {
            t = t.getReferencedType();
        }
        if (t.isPrimitive()) {
            return false;
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
        if (t.isPrimitive()) {
            return false;
        }
        return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
    default:
        return t.isJavaLangObject();
    }
}