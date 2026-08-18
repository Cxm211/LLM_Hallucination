public JavaType resolveSubType(JavaType baseType, String subClass)
        throws JsonMappingException
    {
        if (subClass.indexOf('<') > 0) {
            JavaType t = getTypeFactory().constructFromCanonical(subClass);
            if (baseType.isTypeOrSuperTypeOf(t.getRawClass())) {
                return t;
            }
        } else {
            Class<?> cls;
            try {
                cls =  getTypeFactory().findClass(subClass);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (Exception e) {
                throw invalidTypeIdException(baseType, subClass, String.format(
                        "problem: (%s) %s",
                        e.getClass().getName(),
                        e.getMessage()));
            }
            if (baseType.isTypeOrSuperTypeOf(cls)) {
                return getTypeFactory().constructSpecializedType(baseType, cls);
            }
        }
        throw invalidTypeIdException(baseType, subClass, "Not a subtype");
    }