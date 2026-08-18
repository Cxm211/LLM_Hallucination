public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref != null) {
            if (objectIdInfo != null) {
                objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
            } else {
                return null;
            }
        }
        return objectIdInfo;
    }