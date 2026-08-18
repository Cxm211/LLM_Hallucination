public boolean useForType(JavaType t)
        {
            switch (_appliesFor) {
            case NON_CONCRETE_AND_ARRAYS:
                while (t.isArrayType()) {
                    t = t.getContentType();
                }
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
                return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass())
                        && !t.isPrimitive();
            default:
                return t.isJavaLangObject();
            }
        }