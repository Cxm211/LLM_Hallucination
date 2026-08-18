    public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref != null) {
            if (objectIdInfo == null) {
                objectIdInfo = new ObjectIdInfo(NAME_FOR_OBJECT_REF, null, null, false, null);
            }
            objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
        }
        return objectIdInfo;
    }