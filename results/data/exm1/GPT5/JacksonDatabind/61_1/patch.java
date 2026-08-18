public boolean useForType(JavaType t)
        {
            switch (_appliesFor) {
            case NON_CONCRETE_AND_ARRAYS: {
                boolean wasArray = t.isArrayType();
                while (t.isArrayType()) {
                    t = t.getContentType();
                }
                // 19-Apr-2016, tatu: ReferenceType like Optional also requires similar handling:
                while (t.isReferenceType()) {
                    t = t.getReferencedType();
                }
                boolean res = t.isJavaLangObject()
                        || (!t.isConcrete()
                                && !TreeNode.class.isAssignableFrom(t.getRawClass()));
                return res || wasArray;
            }
            case OBJECT_AND_NON_CONCRETE:
                // 19-Apr-2016, tatu: ReferenceType like Optional also requires similar handling:
                while (t.isReferenceType()) {
                    t = t.getReferencedType();
                }
                return t.isJavaLangObject()
                        || (!t.isConcrete()
                                // [databind#88] Should not apply to JSON tree models:
                                && !TreeNode.class.isAssignableFrom(t.getRawClass()));

            case NON_FINAL:
                while (t.isArrayType()) {
                    t = t.getContentType();
                }
                // 19-Apr-2016, tatu: ReferenceType like Optional also requires similar handling:
                while (t.isReferenceType()) {
                    t = t.getReferencedType();
                }
                // [databind#88] Should not apply to JSON tree models:
                return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
            default:
            //case JAVA_LANG_OBJECT:
                return t.isJavaLangObject();
            }
        }