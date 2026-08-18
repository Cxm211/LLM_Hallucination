public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref == null && ann instanceof AnnotatedMember) {
            Class<?> cls = ((AnnotatedMember) ann).getDeclaringClass();
            if (cls != null) {
                JsonIdentityReference clsRef = cls.getAnnotation(JsonIdentityReference.class);
                if (clsRef != null) {
                    ref = clsRef;
                }
            }
        }
        if (ref != null && objectIdInfo != null) {
            objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
        }
        return objectIdInfo;
    }