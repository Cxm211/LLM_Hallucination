protected SettableBeanProperty constructSettableProperty(DeserializationContext ctxt,
            BeanDescription beanDesc, BeanPropertyDefinition propDef,
            JavaType propType0)
        throws JsonMappingException
    {
        AnnotatedMember mutator = propDef.getNonConstructorMutator();

        if (ctxt.canOverrideAccessModifiers() && mutator != null) {
            // [databind#877]: explicitly prevent forced access to `cause` of `Throwable`;
            // never needed and attempts may cause problems on some platforms.
            final Class<?> declClass = mutator.getDeclaringClass();
            final String propName = propDef.getName();
            if (!(declClass == Throwable.class && "cause".equals(propName))) {
                mutator.fixAccess(ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        BeanProperty.Std property = new BeanProperty.Std(propDef.getFullName(),
                propType0, propDef.getWrapperName(),
                beanDesc.getClassAnnotations(), mutator, propDef.getMetadata());
        JavaType type = resolveType(ctxt, beanDesc, propType0, mutator);
        if (type != propType0) {
            property = property.withType(type);
        }

        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(ctxt, mutator);
        type = modifyTypeByAnnotation(ctxt, mutator, type);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop;
        if (mutator instanceof AnnotatedMethod) {
            prop = new MethodProperty(propDef, type, typeDeser,
                    beanDesc.getClassAnnotations(), (AnnotatedMethod) mutator);
        } else {
            prop = new FieldProperty(propDef, type, typeDeser,
                    beanDesc.getClassAnnotations(), (AnnotatedField) mutator);
        }
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        AnnotationIntrospector.ReferenceProperty ref = propDef.findReferenceType();
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        ObjectIdInfo objectIdInfo = propDef.findObjectIdInfo();
        if(objectIdInfo != null){
            prop.setObjectIdInfo(objectIdInfo);
        }
        return prop;
    }