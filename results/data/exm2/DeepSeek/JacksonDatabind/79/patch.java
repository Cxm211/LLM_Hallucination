    public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref != null) {
            if (objectIdInfo == null) {
                objectIdInfo = new ObjectIdInfo(new PropertyName(null), null, null, ref.alwaysAsId(), null);
            } else {
                objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
            }
        }
        return objectIdInfo;
    }