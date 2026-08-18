    public VisibilityChecker<?> getDefaultVisibilityChecker()
    {
        VisibilityChecker<?> vchecker = super.getDefaultVisibilityChecker();
        if (!isEnabled(MapperFeature.AUTO_DETECT_SETTERS)) {
            vchecker = vchecker.withSetterVisibility(Visibility.NONE);
        }
        if (!isEnabled(MapperFeature.AUTO_DETECT_CREATORS)) {
            vchecker = vchecker.withCreatorVisibility(Visibility.NONE);
        }
        if (!isEnabled(MapperFeature.AUTO_DETECT_FIELDS)) {
            vchecker = vchecker.withFieldVisibility(Visibility.NONE);
        }
        return vchecker;
    }

    public VisibilityChecker<?> getDefaultVisibilityChecker()
    {
        VisibilityChecker<?> vchecker = super.getDefaultVisibilityChecker();
        // then global overrides (disabling)
        if (!isEnabled(MapperFeature.AUTO_DETECT_GETTERS)) {
            vchecker = vchecker.withGetterVisibility(Visibility.NONE);
        }
        if (!isEnabled(MapperFeature.AUTO_DETECT_IS_GETTERS)) {
            vchecker = vchecker.withIsGetterVisibility(Visibility.NONE);
        }
        if (!isEnabled(MapperFeature.AUTO_DETECT_FIELDS)) {
            vchecker = vchecker.withFieldVisibility(Visibility.NONE);
        }
        return vchecker;
    }

// trigger testcase
public void testVisibilityFeatures() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        // Only use explicitly specified values to be serialized/deserialized (i.e., JSONProperty).
        om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        om.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        om.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        om.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        om.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
        om.configure(MapperFeature.USE_ANNOTATIONS, true);

        JavaType javaType = om.getTypeFactory().constructType(TCls.class);        
        BeanDescription desc = (BeanDescription) om.getSerializationConfig().introspect(javaType);
        List<BeanPropertyDefinition> props = desc.findProperties();
        if (props.size() != 1) {
            fail("Should find 1 property, not "+props.size()+"; properties = "+props);
        }
    }
