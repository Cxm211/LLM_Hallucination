// buggy code
    public JavaType resolveSubType(JavaType baseType, String subClass)
        throws JsonMappingException
    {
        // 30-Jan-2010, tatu: Most ids are basic class names; so let's first
        //    check if any generics info is added; and only then ask factory
        //    to do translation when necessary
        if (subClass.indexOf('<') > 0) {
            // note: may want to try combining with specialization (esp for EnumMap)?
            // 17-Aug-2017, tatu: As per [databind#1735] need to ensure assignment
            //    compatibility -- needed later anyway, and not doing so may open
            //    security issues.
            JavaType t = getTypeFactory().constructFromCanonical(subClass);
            if (t.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return t;
            }
        } else {
            Class<?> cls;
            try {
                cls =  getTypeFactory().findClass(subClass);
            } catch (ClassNotFoundException e) { // let caller handle this problem
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

    public Date parseDate(String dateStr) throws IllegalArgumentException
    {
        try {
            DateFormat df = getDateFormat();
            return df.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format(
                    "Failed to parse Date value '%s': %s", dateStr,
                    e.getMessage()));
        }
    }

    public JsonMappingException instantiationException(Class<?> instClass, Throwable cause) {
        // Most likely problem with Creator definition, right?
        final JavaType type = constructType(instClass);
        String excMsg;
        if (cause == null) {
            excMsg = "N/A";
        } else if ((excMsg = cause.getMessage()) == null) {
            excMsg = ClassUtil.nameOf(cause.getClass());
        }
        String msg = String.format("Cannot construct instance of %s, problem: %s",
                ClassUtil.nameOf(instClass), excMsg);
        InvalidDefinitionException e = InvalidDefinitionException.from(_parser, msg, type);
        e.initCause(cause);
        return e;
    }

    public static JsonMappingException fromUnexpectedIOE(IOException src) {
        return new JsonMappingException(null,
                String.format("Unexpected IOException (of type %s): %s",
                        src.getClass().getName(),
                        src.getMessage()));
    }

    public static JsonMappingException wrapWithPath(Throwable src, Reference ref)
    {
        JsonMappingException jme;
        if (src instanceof JsonMappingException) {
            jme = (JsonMappingException) src;
        } else {
            // [databind#2128]: try to avoid duplication
            String msg = src.getMessage();
            // Let's use a more meaningful placeholder if all we have is null
            if (msg == null || msg.length() == 0) {
                msg = "(was "+src.getClass().getName()+")";
            }
            // 17-Aug-2015, tatu: Let's also pass the processor (parser/generator) along
            Closeable proc = null;
            if (src instanceof JsonProcessingException) {
                Object proc0 = ((JsonProcessingException) src).getProcessor();
                if (proc0 instanceof Closeable) {
                    proc = (Closeable) proc0;
                }
            }
            jme = new JsonMappingException(proc, msg, src);
        }
        jme.prependPath(ref);
        return jme;
    }

    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(Class<?> rawType)
        throws JsonMappingException
    {
        JavaType fullType = _config.constructType(rawType);
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(fullType);
        } catch (IllegalArgumentException iae) {
            // We better only expose checked exceptions, since those
            // are what caller is expected to handle
            ser = null; // doesn't matter but compiler whines otherwise
            reportMappingProblem(iae, iae.getMessage());
        }

        if (ser != null) {
            // 21-Dec-2015, tatu: Best to cache for both raw and full-type key
            _serializerCache.addAndResolveNonTypedSerializer(rawType, fullType, ser, this);
        }
        return ser;
    }

    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type)
        throws JsonMappingException
    {        
        JsonSerializer<Object> ser;
        try {
            ser = _createUntypedSerializer(type);
        } catch (IllegalArgumentException iae) {
            // We better only expose checked exceptions, since those
            // are what caller is expected to handle
            ser = null;
            reportMappingProblem(iae, iae.getMessage());
        }
    
        if (ser != null) {
            // 21-Dec-2015, tatu: Should we also cache using raw key?
            _serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }

    public TypeDeserializer findTypeDeserializer(DeserializationConfig config,
            JavaType baseType)
        throws JsonMappingException
    {
        BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);

        // Ok: if there is no explicit type info handler, we may want to
        // use a default. If so, config object knows what to use.
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, ac);
        }
        // May need to figure out default implementation, if none found yet
        // (note: check for abstract type is not 100% mandatory, more of an optimization)
        if ((b.getDefaultImpl() == null) && baseType.isAbstract()) {
            JavaType defaultType = mapAbstractType(config, baseType);
            if ((defaultType != null) && !defaultType.hasRawClass(baseType.getRawClass())) {
                b = b.defaultImpl(defaultType.getRawClass());
            }
        }
        // 05-Apt-2018, tatu: Since we get non-mapping exception due to various limitations,
        //    map to better type here
        try {
            return b.buildTypeDeserializer(config, baseType, subtypes);
        } catch (IllegalArgumentException e0) {
            InvalidDefinitionException e = InvalidDefinitionException.from((JsonParser) null,
                    e0.getMessage(), baseType);
            e.initCause(e0);
            throw e;
        }
    }

    public JsonDeserializer<Object> buildBeanDeserializer(DeserializationContext ctxt,
            JavaType type, BeanDescription beanDesc)
        throws JsonMappingException
    {
        // First: check what creators we can use, if any
        ValueInstantiator valueInstantiator;
        /* 04-Jun-2015, tatu: To work around [databind#636], need to catch the
         *    issue, defer; this seems like a reasonable good place for now.
         *   Note, however, that for non-Bean types (Collections, Maps) this
         *   probably won't work and needs to be added elsewhere.
         */
        try {
            valueInstantiator = findValueInstantiator(ctxt, beanDesc);
        } catch (NoClassDefFoundError error) {
            return new ErrorThrowingDeserializer(error);
        } catch (IllegalArgumentException e) {
            // 05-Apr-2017, tatu: Although it might appear cleaner to require collector
            //   to throw proper exception, it doesn't actually have reference to this
            //   instance so...
            throw InvalidDefinitionException.from(ctxt.getParser(),
                    e.getMessage(),
                    beanDesc, null);
        }
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, beanDesc);
        builder.setValueInstantiator(valueInstantiator);
         // And then setters for deserializing from JSON Object
        addBeanProps(ctxt, beanDesc, builder);
        addObjectIdReader(ctxt, beanDesc, builder);

        // managed/back reference fields/setters need special handling... first part
        addBackReferenceProperties(ctxt, beanDesc, builder);
        addInjectables(ctxt, beanDesc, builder);
        
        final DeserializationConfig config = ctxt.getConfig();
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer;

        if (type.isAbstract() && !valueInstantiator.canInstantiate()) {
            deserializer = builder.buildAbstract();
        } else {
            deserializer = builder.build();
        }
        // may have modifier(s) that wants to modify or replace serializer we just built
        // (note that `resolve()` and `createContextual()` called later on)
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    protected JsonDeserializer<Object> buildBuilderBasedDeserializer(
    		DeserializationContext ctxt, JavaType valueType, BeanDescription builderDesc)
        throws JsonMappingException
    {
        // Creators, anyone? (to create builder itself)
        ValueInstantiator valueInstantiator;
        try {
            valueInstantiator = findValueInstantiator(ctxt, builderDesc);
        } catch (NoClassDefFoundError error) {
            return new ErrorThrowingDeserializer(error);
        } catch (IllegalArgumentException e) {
            // 05-Apr-2017, tatu: Although it might appear cleaner to require collector
            //   to throw proper exception, it doesn't actually have reference to this
            //   instance so...
            throw InvalidDefinitionException.from(ctxt.getParser(),
                    e.getMessage(),
                    builderDesc, null);
        }
        final DeserializationConfig config = ctxt.getConfig();
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, builderDesc);
        builder.setValueInstantiator(valueInstantiator);
         // And then "with methods" for deserializing from JSON Object
        addBeanProps(ctxt, builderDesc, builder);
        addObjectIdReader(ctxt, builderDesc, builder);
        
        // managed/back reference fields/setters need special handling... first part
        addBackReferenceProperties(ctxt, builderDesc, builder);
        addInjectables(ctxt, builderDesc, builder);

        JsonPOJOBuilder.Value builderConfig = builderDesc.findPOJOBuilderConfig();
        final String buildMethodName = (builderConfig == null) ?
                JsonPOJOBuilder.DEFAULT_BUILD_METHOD : builderConfig.buildMethodName;
        
        // and lastly, find build method to use:
        AnnotatedMethod buildMethod = builderDesc.findMethod(buildMethodName, null);
        if (buildMethod != null) { // note: can't yet throw error; may be given build method
            if (config.canOverrideAccessModifiers()) {
            	ClassUtil.checkAndFixAccess(buildMethod.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        builder.setPOJOBuilder(buildMethod, builderConfig);
        // this may give us more information...
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, builderDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.buildBuilderBased(
        		valueType, buildMethodName);

        // [JACKSON-440]: may have modifier(s) that wants to modify or replace serializer we just built:
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, builderDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    protected JsonDeserializer<Object> _createAndCache2(DeserializationContext ctxt,
            DeserializerFactory factory, JavaType type)
        throws JsonMappingException
    {
        JsonDeserializer<Object> deser;
        try {
            deser = _createDeserializer(ctxt, factory, type);
        } catch (IllegalArgumentException iae) {
            // We better only expose checked exceptions, since those
            // are what caller is expected to handle
            throw JsonMappingException.from(ctxt, iae.getMessage(), iae);
        }
        if (deser == null) {
            return null;
        }
        /* cache resulting deserializer? always true for "plain" BeanDeserializer
         * (but can be re-defined for sub-classes by using @JsonCachable!)
         */
        // 27-Mar-2015, tatu: As per [databind#735], avoid caching types with custom value desers
        boolean addToCache = !_hasCustomHandlers(type) && deser.isCachable();

        /* we will temporarily hold on to all created deserializers (to
         * handle cyclic references, and possibly reuse non-cached
         * deserializers (list, map))
         */
        /* 07-Jun-2010, tatu: Danger: [JACKSON-296] was caused by accidental
         *   resolution of a reference -- couple of ways to prevent this;
         *   either not add Lists or Maps, or clear references eagerly.
         *   Let's actually do both; since both seem reasonable.
         */
        /* Need to resolve? Mostly done for bean deserializers; required for
         * resolving cyclic references.
         */
        if (deser instanceof ResolvableDeserializer) {
            _incompleteDeserializers.put(type, deser);
            ((ResolvableDeserializer)deser).resolve(ctxt);
            _incompleteDeserializers.remove(type);
        }
        if (addToCache) {
            _cachedDeserializers.put(type, deser);
        }
        return deser;
    }

    protected void _throwAsIOE(Exception e, Object propName, Object value)
        throws IOException
    {
        if (e instanceof IllegalArgumentException) {
            String actType = ClassUtil.classNameOf(value);
            StringBuilder msg = new StringBuilder("Problem deserializing \"any\" property '").append(propName);
            msg.append("' of class "+getClassName()+" (expected type: ").append(_type);
            msg.append("; actual type: ").append(actType).append(")");
            String origMsg = e.getMessage();
            if (origMsg != null) {
                msg.append(", problem: ").append(origMsg);
            } else {
                msg.append(" (no error message provided)");
            }
            throw new JsonMappingException(null, msg.toString(), e);
        }
        ClassUtil.throwIfIOE(e);
        ClassUtil.throwIfRTE(e);
        // let's wrap the innermost problem
        Throwable t = ClassUtil.getRootCause(e);
        throw new JsonMappingException(null, t.getMessage(), t);
    }

    protected void _throwAsIOE(JsonParser p, Exception e, Object value) throws IOException
    {
        if (e instanceof IllegalArgumentException) {
            String actType = ClassUtil.classNameOf(value);
            StringBuilder msg = new StringBuilder("Problem deserializing property '")
                    .append(getName())
                    .append("' (expected type: ")
                    .append(getType())
                    .append("; actual type: ")
                    .append(actType).append(")");
            String origMsg = e.getMessage();
            if (origMsg != null) {
                msg.append(", problem: ")
                    .append(origMsg);
            } else {
                msg.append(" (no error message provided)");
            }
            throw JsonMappingException.from(p, msg.toString(), e);
        }
        _throwAsIOE(p, e);
    }

    protected IOException _throwAsIOE(JsonParser p, Exception e) throws IOException
    {
        ClassUtil.throwIfIOE(e);
        ClassUtil.throwIfRTE(e);
        // let's wrap the innermost problem
        Throwable th = ClassUtil.getRootCause(e);
        throw JsonMappingException.from(p, th.getMessage(), th);
    }

    protected java.util.Date _parseDate(String value, DeserializationContext ctxt)
        throws IOException
    {
        try {
            // Take empty Strings to mean 'empty' Value, usually 'null':
            if (_isEmptyOrTextualNull(value)) {
                return (java.util.Date) getNullValue(ctxt);
            }
            return ctxt.parseDate(value);
        } catch (IllegalArgumentException iae) {
            return (java.util.Date) ctxt.handleWeirdStringValue(_valueClass, value,
                    "not a valid representation (error: %s)",
                    iae.getMessage());
        }
    }

    public Object deserializeKey(String key, DeserializationContext ctxt)
        throws IOException
    {
        if (key == null) { // is this even legal call?
            return null;
        }
        try {
            Object result = _parse(key, ctxt);
            if (result != null) {
                return result;
            }
        } catch (Exception re) {
            return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation, problem: (%s) %s",
                    re.getClass().getName(),
                    re.getMessage());
        }
        if (_keyClass.isEnum() && ctxt.getConfig().isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            return null;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    }

    protected Object _weirdKey(DeserializationContext ctxt, String key, Exception e) throws IOException {
        return ctxt.handleWeirdKey(_keyClass, key, "problem: %s",
                e.getMessage());
    }

    protected JsonMappingException wrapException(Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+t.getMessage(), t);
    }

    public Object instantiateBean(boolean fixAccess) {
        AnnotatedConstructor ac = _classInfo.getDefaultConstructor();
        if (ac == null) {
            return null;
        }
        if (fixAccess) {
            ac.fixAccess(_config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
        }
        try {
            return ac.getAnnotated().newInstance();
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            ClassUtil.throwIfError(t);
            ClassUtil.throwIfRTE(t);
            throw new IllegalArgumentException("Failed to instantiate bean of type "
                    +_classInfo.getAnnotated().getName()+": ("+t.getClass().getName()+") "
                    +t.getMessage(), t);
        }
    }

    public boolean includeFilterSuppressNulls(Object filter) throws JsonMappingException
    {
        if (filter == null) {
            return true;
        }
        // should let filter decide what to do with nulls:
        // But just case, let's handle unexpected (from our perspective) problems explicitly
        try {
            return filter.equals(null);
        } catch (Throwable t) {
            String msg = String.format(
"Problem determining whether filter of type '%s' should filter out `null` values: (%s) %s",
filter.getClass().getName(), t.getClass().getName(), t.getMessage());
            reportBadDefinition(filter.getClass(), msg, t);
            return false; // never gets here
        }
    }

    private IOException _wrapAsIOE(JsonGenerator g, Exception e) {
        if (e instanceof IOException) {
            return (IOException) e;
        }
        String msg = e.getMessage();
        if (msg == null) {
            msg = "[no message for "+e.getClass().getName()+"]";
        }
        return new JsonMappingException(g, msg, e);
    }

    protected BeanPropertyWriter buildWriter(SerializerProvider prov,
            BeanPropertyDefinition propDef, JavaType declaredType, JsonSerializer<?> ser,
            TypeSerializer typeSer, TypeSerializer contentTypeSer,
            AnnotatedMember am, boolean defaultUseStaticTyping)
        throws JsonMappingException
    {
        // do we have annotation that forces type to use (to declared type or its super type)?
        JavaType serializationType;
        try {
            serializationType = findSerializationType(am, defaultUseStaticTyping, declaredType);
        } catch (JsonMappingException e) {
            if (propDef == null) {
                return prov.reportBadDefinition(declaredType, e.getMessage());
            }
            return prov.reportBadPropertyDefinition(_beanDesc, propDef, e.getMessage());
        }

        // Container types can have separate type serializers for content (value / element) type
        if (contentTypeSer != null) {
            // 04-Feb-2010, tatu: Let's force static typing for collection, if there is
            //    type information for contents. Should work well (for JAXB case); can be
            //    revisited if this causes problems.
            if (serializationType == null) {
//                serializationType = TypeFactory.type(am.getGenericType(), _beanDesc.getType());
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            // Not exactly sure why, but this used to occur; better check explicitly:
            if (ct == null) {
                prov.reportBadPropertyDefinition(_beanDesc, propDef,
                        "serialization type "+serializationType+" has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }

        Object valueToSuppress = null;
        boolean suppressNulls = false;

        // 12-Jul-2016, tatu: [databind#1256] Need to make sure we consider type refinement
        JavaType actualType = (serializationType == null) ? declaredType : serializationType;
        
        // 17-Mar-2017: [databind#1522] Allow config override per property type
        AnnotatedMember accessor = propDef.getAccessor();
        if (accessor == null) {
            // neither Setter nor ConstructorParameter are expected here
            return prov.reportBadPropertyDefinition(_beanDesc, propDef,
                    "could not determine property type");
        }
        Class<?> rawPropertyType = accessor.getRawType();

        // 17-Aug-2016, tatu: Default inclusion covers global default (for all types), as well
        //   as type-default for enclosing POJO. What we need, then, is per-type default (if any)
        //   for declared property type... and finally property annotation overrides
        JsonInclude.Value inclV = _config.getDefaultInclusion(actualType.getRawClass(),
                rawPropertyType, _defaultInclusion);

        // property annotation override
        
        inclV = inclV.withOverrides(propDef.findInclusion());

        JsonInclude.Include inclusion = inclV.getValueInclusion();
        if (inclusion == JsonInclude.Include.USE_DEFAULTS) { // should not occur but...
            inclusion = JsonInclude.Include.ALWAYS;
        }
        switch (inclusion) {
        case NON_DEFAULT:
            // 11-Nov-2015, tatu: This is tricky because semantics differ between cases,
            //    so that if enclosing class has this, we may need to access values of property,
            //    whereas for global defaults OR per-property overrides, we have more
            //    static definition. Sigh.
            // First: case of class/type specifying it; try to find POJO property defaults
            Object defaultBean;

            // 16-Oct-2016, tatu: Note: if we cannot for some reason create "default instance",
            //    revert logic to the case of general/per-property handling, so both
            //    type-default AND null are to be excluded.
            //    (as per [databind#1417]
            if (_useRealPropertyDefaults && (defaultBean = getDefaultBean()) != null) {
                // 07-Sep-2016, tatu: may also need to front-load access forcing now
                if (prov.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                    am.fixAccess(_config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                try {
                    valueToSuppress = am.getValue(defaultBean);
                } catch (Exception e) {
                    _throwWrapped(e, propDef.getName(), defaultBean);
                }
            } else {
                valueToSuppress = BeanUtil.getDefaultValue(actualType);
                suppressNulls = true;
            }
            if (valueToSuppress == null) {
                suppressNulls = true;
            } else {
                if (valueToSuppress.getClass().isArray()) {
                    valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                }
            }
            break;
        case NON_ABSENT: // new with 2.6, to support Guava/JDK8 Optionals
            // always suppress nulls
            suppressNulls = true;
            // and for referential types, also "empty", which in their case means "absent"
            if (actualType.isReferenceType()) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        case NON_EMPTY:
            // always suppress nulls
            suppressNulls = true;
            // but possibly also 'empty' values:
            valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            break;
        case CUSTOM: // new with 2.9
            valueToSuppress = prov.includeFilterInstance(propDef, inclV.getValueFilter());
            if (valueToSuppress == null) { // is this legal?
                suppressNulls = true;
            } else {
                suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
            }
            break;
        case NON_NULL:
            suppressNulls = true;
            // fall through
        case ALWAYS: // default
        default:
            // we may still want to suppress empty collections
            if (actualType.isContainerType()
                    && !_config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) {
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
            break;
        }
        Class<?>[] views = propDef.findViews();
        if (views == null) {
            views = _beanDesc.findDefaultViews();
        }
        BeanPropertyWriter bpw = new BeanPropertyWriter(propDef,
                am, _beanDesc.getClassAnnotations(), declaredType,
                ser, typeSer, serializationType, suppressNulls, valueToSuppress, views);

        // How about custom null serializer?
        Object serDef = _annotationIntrospector.findNullSerializer(am);
        if (serDef != null) {
            bpw.assignNullSerializer(prov.serializerInstance(am, serDef));
        }
        // And then, handling of unwrapping
        NameTransformer unwrapper = _annotationIntrospector.findUnwrappingNameTransformer(am);
        if (unwrapper != null) {
            bpw = bpw.unwrappingWriter(unwrapper);
        }
        return bpw;
    }

    public static String backticked(String text) {
        if (text == null) {
            return "[null]";
        }
        return new StringBuilder(text.length()+2).append('`').append(text).append('`').toString();
    }

// relevant test
// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNonFinalBean
    public void testNonFinalBean() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        StringBean bean = new StringBean("x");
        assertEquals("{\"name\":\"x\"}", m.writeValueAsString(bean));
        
        m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        assertEquals("[\""+StringBean.class.getName()+"\",{\"name\":\"x\"}]",
            m.writeValueAsString(bean));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNullValue
    public void testNullValue() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        BeanHolder h = new BeanHolder();
        String json = m.writeValueAsString(h);
        assertNotNull(json);
        BeanHolder result = m.readValue(json, BeanHolder.class);
        assertNotNull(result);
        assertNull(result.bean);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumAsObject
    public void testEnumAsObject() throws Exception
    {
        
        Object[] input = new Object[] { Choice.YES };
        Object[] input2 = new Object[] { ComplexChoice.MAYBE};
        
        assertEquals("[\"YES\"]", serializeAsString(input));
        assertEquals("[\"MAYBE\"]", serializeAsString(input2));

        
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        String json = m.writeValueAsString(input);
        assertEquals("[[\""+Choice.class.getName()+"\",\"YES\"]]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(Choice.YES, output[0]);

        
        json = m.writeValueAsString(input2);
        assertEquals("[[\""+ComplexChoice.class.getName()+"\",\"MAYBE\"]]", json);
        output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        assertEquals(ComplexChoice.MAYBE, output[0]);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumSet
    public void testEnumSet() throws Exception
    {
        EnumSet<Choice> set = EnumSet.of(Choice.NO);
        Object[] input = new Object[] { set };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumSet<?>);
        EnumSet<Choice> set2 = (EnumSet<Choice>) ob;
        assertEquals(1, set2.size());
        assertTrue(set2.contains(Choice.NO));
        assertFalse(set2.contains(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testEnumMap
    public void testEnumMap() throws Exception
    {
        EnumMap<Choice,String> map = new EnumMap<Choice,String>(Choice.class);
        map.put(Choice.NO, "maybe");
        Object[] input = new Object[] { map };
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(input);
        Object[] output = m.readValue(json, Object[].class);
        assertEquals(1, output.length);
        Object ob = output[0];
        assertTrue(ob instanceof EnumMap<?,?>);
        EnumMap<Choice,String> map2 = (EnumMap<Choice,String>) ob;
        assertEquals(1, map2.size());
        assertEquals("maybe", map2.get(Choice.NO));
        assertNull(map2.get(Choice.YES));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testJackson311
    public void testJackson311() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString(new PolymorphicType("hello", 2));
        PolymorphicType value = mapper.readValue(json, PolymorphicType.class);
        assertEquals("hello", value.foo);
        assertEquals(Integer.valueOf(2), value.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testTokenBuffer
    public void testTokenBuffer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        
        TokenBuffer buf = new TokenBuffer(mapper, false);
        buf.writeStartObject();
        buf.writeNumberField("num", 42);
        buf.writeEndObject();
        String json = mapper.writeValueAsString(new ObjectHolder(buf));
        ObjectHolder holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        JsonParser jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
        buf.writeStartArray();
        buf.writeBoolean(true);
        buf.writeEndArray();
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();

        
        buf = new TokenBuffer(mapper, false);
        buf.writeNumber(321);
        json = mapper.writeValueAsString(new ObjectHolder(buf));
        holder = mapper.readValue(json, ObjectHolder.class);
        assertNotNull(holder.value);
        assertSame(TokenBuffer.class, holder.value.getClass());
        jp = ((TokenBuffer) holder.value).asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(321, jp.getIntValue());
        assertNull(jp.nextToken());
        jp.close();
        buf.close();
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testIssue352
    public void testIssue352() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping (ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        DiscussBean d1 = new DiscussBean();
        d1.subject = "mouse";
        d1.weight=88;
        DomainBeanWrapper wrapper = new DomainBeanWrapper();
        wrapper.name = "mickey";
        wrapper.myBean = d1;
        String json = mapper.writeValueAsString(wrapper);
        DomainBeanWrapper result = mapper.readValue(json, DomainBeanWrapper.class);
        assertNotNull(result);
        assertNotNull(wrapper.myBean);
        assertSame(DiscussBean.class, wrapper.myBean.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testFeature432
    public void testFeature432() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "*CLASS*");
        String json = mapper.writeValueAsString(new BeanHolder(new StringBean("punny")));
        assertEquals("{\"bean\":{\"*CLASS*\":\"com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject$StringBean\",\"name\":\"punny\"}}", json);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForObject::testNoGoWithExternalProperty
    public void testNoGoWithExternalProperty() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                    JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Should not have passed");
        } catch (IllegalArgumentException e) {
            verifyException(e, "Cannot use includeAs of EXTERNAL_PROPERTY");
        }
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testNumericScalars
    public void testNumericScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[123]", m.writeValueAsString(new Object[] { Integer.valueOf(123) }));
        assertEquals("[[\"java.lang.Long\",37]]", m.writeValueAsString(new Object[] { Long.valueOf(37) }));
        assertEquals("[0.25]", m.writeValueAsString(new Object[] { Double.valueOf(0.25) }));
        assertEquals("[[\"java.lang.Float\",0.5]]", m.writeValueAsString(new Object[] { Float.valueOf(0.5f) }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDateScalars
    public void testDateScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        long ts = 12345678L;
        assertEquals("[[\"java.util.Date\","+ts+"]]",
                m.writeValueAsString(new Object[] { new Date(ts) }));

        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        String json = m.writeValueAsString(new Object[] { c });
        assertEquals("[[\""+c.getClass().getName()+"\","+ts+"]]", json);
        
        Object[] result = m.readValue(json, Object[].class);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof Calendar);
        assertEquals(ts, ((Calendar) result[0]).getTimeInMillis());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testMiscScalars
    public void testMiscScalars() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();

        
        assertEquals("[\"abc\"]", m.writeValueAsString(new Object[] { "abc" }));
        assertEquals("[true,null,false]", m.writeValueAsString(new Boolean[] { true, null, false }));
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testScalarArrays
    public void testScalarArrays() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        Object[] input = new Object[] {
                "abc", new Date(1234567), null, Integer.valueOf(456)
        };
        String json = m.writeValueAsString(input);
        assertEquals("[\"abc\",[\"java.util.Date\",1234567],null,456]", json);

        
        Object[] output = m.readValue(json, Object[].class);
        assertArrayEquals(input, output);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::test417
    public void test417() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        Jackson417Bean input = new Jackson417Bean();
        String json = m.writeValueAsString(input);
        Jackson417Bean result = m.readValue(json, Jackson417Bean.class);
        assertEquals(input.foo, result.foo);
        assertEquals(input.bar, result.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForScalars::testDefaultTypingWithLong
    public void testDefaultTypingWithLong() throws Exception
    {
        Data data = new Data();
        data.key = 1L;
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);

        
        ObjectMapper mapper = new ObjectMapper();
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        
        String json = mapper.writeValueAsString(mapData);

        
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForTreeNodes::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        String json = DEFAULT_MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultForTreeNodes::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception
    {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreators
    public void testWithCreators() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        UrlJob input = new UrlJob(123L, "http://foo", 3);
        String json = mapper.writeValueAsString(input);
        assertNotNull(json);
        Job output = mapper.readValue(json, Job.class);
        assertNotNull(output);
        assertSame(UrlJob.class, output.getClass());
        UrlJob o2 = (UrlJob) output;
        assertEquals(123L, o2.id);
        assertEquals("http://foo", o2.getUrl());
        assertEquals(3, o2.getCount());
    }

// com.fasterxml.jackson.databind.jsontype.deftyping.TestDefaultWithCreators::testWithCreatorAndJsonValue
    public void testWithCreatorAndJsonValue() throws Exception
    {
        final byte[] BYTES = new byte[] { 1, 2, 3, 4, 5 };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String json = mapper.writeValueAsString(new Bean1385Wrapper(
                new Bean1385(BYTES)
        ));
        Bean1385Wrapper result = mapper.readValue(json, Bean1385Wrapper.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(Bean1385.class, result.value.getClass());
        Bean1385 b = (Bean1385) result.value;
        Assert.assertArrayEquals(BYTES, b.raw);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testFails
    public void testFails() throws Exception {
        String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId198Test::testWorks
    public void testWorks() throws Exception {
        String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeId999Test::testExternalTypeId
    public void testExternalTypeId() throws Exception
    {
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() { };

        Message<?> msg = MAPPER.readValue(aposToQuotes("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);

        
        msg = MAPPER.readValue(aposToQuotes("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleSerialization
    public void testSimpleSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        
        
        
        
        
        
        assertEquals("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}",
                mapper.writeValueAsString(new ExternalBean(11)));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdSerialization
    public void testImproperExternalIdSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"extType\":\"funk\",\"i\":3}",
                mapper.writeValueAsString(new FunkyExternalBean()));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeIdWithNull
    public void testExternalTypeIdWithNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':null,'extType':'vbean'}"),
                ExternalBean.class);
        assertNotNull(b);
        b = mapper.readValue(aposToQuotes("{'extType':'vbean','bean':null}"),
                ExternalBean.class);
        assertNotNull(b);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testSimpleDeserialization
    public void testSimpleDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean result = mapper.readValue("{\"bean\":{\"value\":11},\"extType\":\"vbean\"}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        ValueBean vb = (ValueBean) result.bean;
        assertEquals(11, vb.value);

        
        result = mapper.readValue("{\"extType\":\"vbean\", \"bean\":{\"value\":13}}", ExternalBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        vb = (ValueBean) result.bean;
        assertEquals(13, vb.value);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testMultipleTypeIdsDeserialization
    public void testMultipleTypeIdsDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBean3(3));
        ExternalBean3 result = mapper.readValue(json, ExternalBean3.class);
        assertNotNull(result);
        assertNotNull(result.value1);
        assertNotNull(result.value2);
        assertNotNull(result.value3);
        assertEquals(3, ((ValueBean)result.value1).value);
        assertEquals(4, ((ValueBean)result.value2).value);
        assertEquals(5, ((ValueBean)result.value3).value);
        assertEquals(3, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithCreator
    public void testExternalTypeWithCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        String json = mapper.writeValueAsString(new ExternalBeanWithCreator(7));
        ExternalBeanWithCreator result = mapper.readValue(json, ExternalBeanWithCreator.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(7, ((ValueBean)result.value).value);
        assertEquals(7, result.foo);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testImproperExternalIdDeserialization
    public void testImproperExternalIdDeserialization() throws Exception
    {
        FunkyExternalBean result = MAPPER.readValue("{\"extType\":\"funk\",\"i\":3}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(3, result.i);

        result = MAPPER.readValue("{\"i\":4,\"extType\":\"funk\"}",
                FunkyExternalBean.class);
        assertNotNull(result);
        assertEquals(4, result.i);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue798
    public void testIssue798() throws Exception
    {
        Base base = new Derived1("derived1 prop val", "base prop val");
        BaseContainer baseContainer = new BaseContainer("bc prop val", base);
        String generatedJson = MAPPER.writeValueAsString(baseContainer);
        BaseContainer baseContainer2 = MAPPER.readValue(generatedJson,BaseContainer.class);
        assertEquals("bc prop val", baseContainer.getBaseContainerProperty());

        Base b = baseContainer2.getBase();
        assertNotNull(b);
        if (b.getClass() != Derived1.class) {
            fail("Should have type Derived1, was "+b.getClass().getName());
        }

        Derived1 derived1 = (Derived1) b;
        assertEquals("base prop val", derived1.getBaseProperty());
        assertEquals("derived1 prop val", derived1.getDerived1Property());
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testIssue831
    public void testIssue831() throws Exception
    {
        final String JSON = "{ \"petType\": \"dog\",\n"
                +"\"pet\": { \"name\": \"Pluto\" }\n}";
        House831 result = MAPPER.readValue(JSON, House831.class);
        assertNotNull(result);
        assertNotNull(result.pet);
        assertSame(Dog.class, result.pet.getClass());
        assertEquals("dog", result.petType);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithScalar118
    public void testWithScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new java.util.Date(123L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof java.util.Date);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithNaturalScalar118
    public void testWithNaturalScalar118() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(Integer.valueOf(13));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Integer);

        
        input = new ExternalTypeWithNonPOJO(Boolean.TRUE);
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof Boolean);

        input = new ExternalTypeWithNonPOJO("foobar");
        json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result.value);
        assertTrue(result.value instanceof String);
        assertEquals("foobar", result.value);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testWithAsValue
    public void testWithAsValue() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(12345L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":12345,\"type\":\"thingy\"}", json);

        
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(AsValueThingy.class, result.value.getClass());
        assertEquals(12345L, ((AsValueThingy) result.value).rawDate);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testExternalTypeWithProp222
    public void testExternalTypeWithProp222() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        Issue222Bean input = new Issue222Bean(13);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value\":{\"x\":13},\"type\":\"foo\"}", json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testInverseExternalId928
    public void testInverseExternalId928() throws Exception
    {
        final String CLASS = Payload928.class.getName();

        ObjectMapper mapper = new ObjectMapper();

        final String successCase = "{\"payload\":{\"something\":\"test\"},\"class\":\""+CLASS+"\"}";
        Envelope928 envelope1 = mapper.readValue(successCase, Envelope928.class);
        assertNotNull(envelope1);
        assertEquals(Payload928.class, envelope1._payload.getClass());

        
        final String failCase = "{\"class\":\""+CLASS+"\",\"payload\":{\"something\":\"test\"}}";
        Envelope928 envelope2 = mapper.readValue(failCase, Envelope928.class);
        assertNotNull(envelope2);
        assertEquals(Payload928.class, envelope2._payload.getClass());
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdTest::testBigDecimal965
    public void testBigDecimal965() throws Exception
    {

        Wrapper965 w = new Wrapper965();
        w.typeEnum = Type965.BIG_DECIMAL;
        final String NUM_STR = "-10000000000.0000000001";
        w.value = new BigDecimal(NUM_STR);

        String json = MAPPER.writeValueAsString(w);

        
        if (!json.contains(NUM_STR)) {
            fail("JSON content should contain value '"+NUM_STR+"', does not appear to: "+json);
        }
        
        Wrapper965 w2 = MAPPER.readerFor(Wrapper965.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(json);

        assertEquals(w.typeEnum, w2.typeEnum);
        assertTrue(String.format("Expected %s = %s; got back %s = %s",
            w.value.getClass().getSimpleName(), w.value.toString(), w2.value.getClass().getSimpleName(), w2.value.toString()),
            w.value.equals(w2.value));
    }

// com.fasterxml.jackson.databind.jsontype.ext.ExternalTypeIdWithEnum1328Test::testExample
    public void testExample() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(Arrays.asList(new AnimalAndType(AnimalType.Dog, new Dog())));
        List<AnimalAndType> list = mapper.readerFor(new TypeReference<List<AnimalAndType>>() { })
            .readValue(json);
        assertNotNull(list);
    }

// com.fasterxml.jackson.databind.jsontype.ext.JsonValueExtTypeIdTest::testDoubleMetadata
    public void testDoubleMetadata() throws IOException {
        DoubleMetadata doub = new DoubleMetadata();
        String expected = "{\"metadata\":[{\"key\":\"num\",\"value\":1234.25,\"@type\":\"doubleValue\"}]}";
        String json = MAPPER.writeValueAsString(doub);
        assertEquals("Serialized json not equivalent", expected, json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.JsonValueExtTypeIdTest::testDecimalMetadata
    public void testDecimalMetadata() throws IOException{
        DecimalMetadata dec = new DecimalMetadata();
        String expected = "{\"metadata\":[{\"key\":\"num\",\"value\":111.1,\"@type\":\"decimalValue\"}]}";
        String json = MAPPER.writeValueAsString(dec);
        assertEquals("Serialized json not equivalent", expected, json);
    }

// com.fasterxml.jackson.databind.jsontype.ext.MultipleExternalIds291Test::testMultipleValuesSingleExtId
    public void testMultipleValuesSingleExtId() throws Exception
    {
        
        _testMultipleValuesSingleExtId(
"{'type' : '1',\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);

        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'field2' : { 'c' : 'CCC' },\n"
+"'type' : '1'\n"
+"}"
);
        
        _testMultipleValuesSingleExtId(
"{\n"
+"'field1' : { 'a' : 'AAA' },\n"
+"'type' : '1',\n"
+"'field2' : { 'c' : 'CCC' }\n"
+"}"
);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationPresent
    public void testDeserializationPresent() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBox();
        checkAppleBox();
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationNull
    public void testDeserializationNull() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxNullJson);
        checkAppleBoxNull(appleBoxNullJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationEmpty
    public void testDeserializationEmpty() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxEmpty(orangeBoxEmptyJson);
        checkAppleBoxEmpty(appleBoxEmptyJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissing
    public void testDeserializationMissing() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkOrangeBoxNull(orangeBoxMissingJson);
        checkAppleBoxNull(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkBoxJsonMappingException(orangeBoxMissingJson);
        checkBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.jsontype.ext.TestSubtypesExternalPropertyMissingProperty::testDeserializationMissingRequired
    public void testDeserializationMissingRequired() throws Exception {
        MAPPER.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);

        MAPPER.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        checkReqBoxJsonMappingException(orangeBoxMissingJson);
        checkReqBoxJsonMappingException(appleBoxMissingJson);
    }

// com.fasterxml.jackson.databind.misc.AccessFixTest::testCauseOfThrowableIgnoral
    public void testCauseOfThrowableIgnoral() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            _testCauseOfThrowableIgnoral(mapper);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }

// com.fasterxml.jackson.databind.misc.BeanPropertyMapTest::testArrayOutOfBounds884
    public void testArrayOutOfBounds884() throws Exception
    {
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        PropertyMetadata md = PropertyMetadata.STD_REQUIRED;
        props.add(new ObjectIdValueProperty(new MyObjectIdReader("pk"), md));
        props.add(new ObjectIdValueProperty(new MyObjectIdReader("firstName"), md));
        BeanPropertyMap propMap = new BeanPropertyMap(false, props,
                new HashMap<String,List<PropertyName>>());
        propMap = propMap.withProperty(new ObjectIdValueProperty(new MyObjectIdReader("@id"), md));
        assertNotNull(propMap);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitive1854Test::testIssue1854
    public void testIssue1854() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        final String DOC = aposToQuotes("{'ID': 1, 'Items': [ { 'ChildID': 10 } ]}");
        Obj1854 result = mapper.readValue(DOC, Obj1854.class);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveDeserialization
    public void testCaseInsensitiveDeserialization() throws Exception
    {
        final String JSON = "{\"Value1\" : {\"nAme\" : \"fruit\", \"vALUe\" : \"apple\"}, \"valUE2\" : {\"NAME\" : \"color\", \"value\" : \"red\"}}";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        try {
            mapper.readValue(JSON, Issue476Bean.class);
            
            fail("Should not accept improper case properties by default");
        } catch (JsonProcessingException e) {
            verifyException(e, "Unrecognized field");
            assertValidLocation(e.getLocation());
        }

        
        ObjectReader r = INSENSITIVE_MAPPER.readerFor(Issue476Bean.class);
        Issue476Bean result = r.readValue(JSON);
        assertEquals(result.value1.name, "fruit");
        assertEquals(result.value1.value, "apple");
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitive1036
    public void testCaseInsensitive1036() throws Exception
    {
        final String json = "{\"ErrorCode\":2,\"DebugMessage\":\"Signature not valid!\"}";

        BaseResponse response = INSENSITIVE_MAPPER.readValue(json, BaseResponse.class);
        assertEquals(2, response.errorCode);
        assertEquals("Signature not valid!", response.debugMessage);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveWithFormat
    public void testCaseInsensitiveWithFormat() throws Exception {
        CaseInsensitiveRoleWrapper w = MAPPER.readValue
                (aposToQuotes("{'role':{'id':'12','name':'Foo'}}"),
                        CaseInsensitiveRoleWrapper.class);
        assertNotNull(w);
        assertEquals("12", w.role.ID);
        assertEquals("Foo", w.role.Name);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCreatorWithInsensitive
    public void testCreatorWithInsensitive() throws Exception
    {
        final String json = aposToQuotes("{'VALUE':3}");
        InsensitiveCreator bean = INSENSITIVE_MAPPER.readValue(json, InsensitiveCreator.class);
        assertEquals(3, bean.v);
    }

// com.fasterxml.jackson.databind.misc.CaseInsensitiveDeserTest::testCaseInsensitiveWithClassFormat
    public void testCaseInsensitiveWithClassFormat() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Role.class)
            .setFormat(JsonFormat.Value.empty()
                    .withFeature(JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        Role role = mapper.readValue
                (aposToQuotes("{'id':'12','name':'Foo'}"),
                        Role.class);
        assertNotNull(role);
        assertEquals("12", role.ID);
        assertEquals("Foo", role.Name);
    }

// com.fasterxml.jackson.databind.misc.RaceCondition738Test::testRepeatedly
    public void testRepeatedly() throws Exception {
        final int COUNT = 3000;
        for (int i = 0; i < COUNT; i++) {
            runOnce(i, COUNT);
        }
    }

// com.fasterxml.jackson.databind.misc.TestBlocking::testEagerAdvance
    public void testEagerAdvance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = createParserUsingReader("[ 1  ");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());

        
        Integer I = mapper.readValue(jp, Integer.class);
        assertEquals(Integer.valueOf(1), I);

        
        try {
            jp.nextToken();
        } catch (IOException ioe) {
            verifyException(ioe, "Unexpected end-of-input: expected close marker for ARRAY");
        }
        jp.close();
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testSimpleScalars
    public void testSimpleScalars() throws Exception
    {
        assertEquals("callback(\"abc\")",
                MAPPER.writeValueAsString(new JSONPObject("callback", "abc")));
        assertEquals("calc(123)",
                MAPPER.writeValueAsString(new JSONPObject("calc", Integer.valueOf(123))));
        assertEquals("dummy(null)",
                MAPPER.writeValueAsString(new JSONPObject("dummy", null)));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testSimpleBean
    public void testSimpleBean() throws Exception
    {
        assertEquals("xxx({\"a\":\"123\",\"b\":\"456\"})",
                MAPPER.writeValueAsString(new JSONPObject("xxx",
                        new Impl("123", "456"))));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testWithType
    public void testWithType() throws Exception
    {
        Object ob = new Impl("abc", "def");
        JavaType type = MAPPER.constructType(Base.class);
        assertEquals("do({\"a\":\"abc\"})",
                MAPPER.writeValueAsString(new JSONPObject("do", ob, type)));
    }

// com.fasterxml.jackson.databind.misc.TestJSONP::testGeneralWrapping
    public void testGeneralWrapping() throws Exception
    {
        JSONWrappedObject input = new JSONWrappedObject("", "\n// the end",
                Arrays.asList());
        assertEquals("[]\n// the end", MAPPER.writeValueAsString(input));
    }

// com.fasterxml.jackson.databind.misc.ThreadSafety1759Test::testCalendarForDeser
    public void testCalendarForDeser() throws Exception
    {
        final ObjectMapper mapper = newObjectMapper();

        final int numThreads = 4;
        final int COUNT = 3000;
        final AtomicInteger counter = new AtomicInteger();

        
        List<Callable<Throwable>> calls = new ArrayList<Callable<Throwable>>();
        for (int thread = 1; thread <= numThreads; ++thread) {
            final String json = quote(String.format("2017-01-%02dT16:30:49Z", thread));
            final long timestamp = mapper.readValue(json, Date.class).getTime();

            calls.add(createCallable(thread, mapper, json, timestamp, COUNT, counter));
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Throwable>> results = new ArrayList<>();
        for (Callable<Throwable> c : calls) {
            results.add(executor.submit(c));
        }
        executor.shutdown();
        for (Future<Throwable> f : results) {
            Throwable t = f.get(5, TimeUnit.SECONDS);
            if (t != null) {
                fail("Exception during processing: "+t.getMessage());
            }
        }
        assertEquals(numThreads * COUNT, counter.get());
    }

// com.fasterxml.jackson.databind.mixins.MapperMixinsCopy1998Test::testB_KO
    public void testB_KO() throws Exception
    {
        final ObjectMapper DEFAULT = defaultMapper();
        MyModelRoot myModelInstance = new MyModelRoot();
        myModelInstance.setChild(new MyChildB("testB"));

        ObjectMapper myObjectMapper = DEFAULT.copy();

        String postResult = getString(myModelInstance, myObjectMapper);
        assertEquals(FULLMODEL, postResult);

        myObjectMapper = DEFAULT.copy();

        myObjectMapper.addMixIn(MyModelRoot.class, MixinConfig.MyModelRoot.class)
                .addMixIn(MyModelChildBase.class, MixinConfig.MyModelChildBase.class)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .setConfig(myObjectMapper.getSerializationConfig().withView(MyModelView.class));

        String result = getString(myModelInstance, myObjectMapper);
        assertEquals(EXPECTED, result);

    }

// com.fasterxml.jackson.databind.mixins.MixinsWithBundlesTest::testMixinWithBundles
    public void testMixinWithBundles() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().addMixIn(Foo.class, FooMixin.class);
        String result = mapper.writeValueAsString(new Foo("result"));
        assertEquals("{\"bar\":\"result\"}", result);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsTopLevel
    public void testClassMixInsTopLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        
        LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("XXXvalue", result.a);

        
        
        m = new ObjectMapper();
        m.addMixIn(LeafClass.class, MixIn.class);
        result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
        assertEquals("value", result.a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsMidLevel
    public void testClassMixInsMidLevel() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        {
            BaseClass result = m.readValue("{\"a\":\"value\"}", BaseClass.class);
            assertEquals("value", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"value\"}", LeafClass.class);
            assertEquals("XXXvalue", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testClassMixInsForObjectClass
    public void testClassMixInsForObjectClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(Object.class, MixIn.class);
        
        {
            BaseClass result = m.readValue("{\"a\":\"\"}", BaseClass.class);
            assertEquals("", result.a);
        }

        
        {
            LeafClass result = m.readValue("{\"a\":\"\"}", LeafClass.class);
            assertEquals("XXX", result.a);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForClass::testHashCodeViaObject
    public void testHashCodeViaObject() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
                .addMixIn(Object.class, HashCodeMixIn.class);

        
        assertEquals( "{\"hashCode\":13}",
                mapper.writeValueAsString(new Bean1990WithHashCode()));

        
        String prefix = "{\"hashCode\":";
        String json = mapper.writeValueAsString(new Bean1990WithoutHashCode());
        if (!json.startsWith(prefix)) {
            fail("Should start with ["+prefix+"], does not: ["+json+"]");
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForConstructor
    public void testForConstructor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClassWithPrivateCtor.class, MixInForPrivate.class);
        BaseClassWithPrivateCtor result = m.readValue("\"?\"", BaseClassWithPrivateCtor.class);
        assertEquals("?...", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testForFactoryAndCtor
    public void testForFactoryAndCtor() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        BaseClass result;

        
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("string...", result._a);

        
        m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        result = m.readValue("\"string\"", BaseClass.class);
        assertEquals("stringX", result._a);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testFactoryDelegateMixIn
    public void testFactoryDelegateMixIn() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(StringWrapper.class, StringWrapperMixIn.class);
        StringWrapper result = m.readValue("\"a\"", StringWrapper.class);
        assertEquals("a", result._value);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForCreators::testFactoryPropertyMixin
    public void testFactoryPropertyMixin() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Pair2020.class, MyPairMixIn8.class);

        String doc = aposToQuotes( "{'value0' : 456, 'value1' : 789}");
        Pair2020 pair2 = objectMapper.readValue(doc, Pair2020.class);
        assertEquals(456, pair2.x);
        assertEquals(789, pair2.y);
    }

// com.fasterxml.jackson.databind.mixins.TestMixinDeserForMethods::testWithAnySetter
    public void testWithAnySetter() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        m.addMixIn(BaseClass.class, MixIn.class);
        BaseClass result = m.readValue("{ \"a\" : 3, \"b\" : true }", BaseClass.class);
        assertNotNull(result);
        assertEquals(2, result.values.size());
        assertEquals(Integer.valueOf(3), result.values.get("a"));
        assertEquals(Boolean.TRUE, result.values.get("b"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinInheritance::testMixinFieldInheritance
    public void testMixinFieldInheritance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Beano.class, BeanoMixinSub.class);
        Map<String,Object> result;
        result = writeAndMap(mapper, new Beano());
        assertEquals(2, result.size());
        if (!result.containsKey("id")
                || !result.containsKey("name")) {
            fail("Should have both 'id' and 'name', but content = "+result);
        }
    }

// com.fasterxml.jackson.databind.mixins.TestMixinInheritance::testMixinMethodInheritance
    public void testMixinMethodInheritance() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Beano2.class, BeanoMixinSub2.class);
        Map<String,Object> result;
        result = writeAndMap(mapper, new Beano2());
        assertEquals(2, result.size());
        assertTrue(result.containsKey("id"));
        assertTrue(result.containsKey("name"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinMerging::testDisappearingMixins515
    public void testDisappearingMixins515() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .disable(MapperFeature.AUTO_DETECT_FIELDS)
            .disable(MapperFeature.AUTO_DETECT_GETTERS)
            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
            .disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        SimpleModule module = new SimpleModule("Test");
        module.setMixInAnnotation(Person.class, PersonMixin.class);        
        mapper.registerModule(module);

        assertEquals("{\"city\":\"Seattle\"}", mapper.writeValueAsString(new PersonImpl()));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForClass::testClassMixInsTopLevel
    public void testClassMixInsTopLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;

        
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(1, result.size());
        assertEquals("abc", result.get("a"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(LeafClass.class, MixIn.class);
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(2, result.size());
        assertEquals("abc", result.get("a"));
        assertEquals("c", result.get("c"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, new LeafClass("abc"));
        assertEquals(1, result.size());
        assertEquals("abc", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForClass::testClassMixInsMidLevel
    public void testClassMixInsMidLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        LeafClass bean = new LeafClass("xyz");
        bean._c = "c2";

        
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("xyz", result.get("a"));
        assertEquals("c2", result.get("c"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixInAutoDetect.class);
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("c2", result.get("c"));

        
        ObjectMapper mapper2 = new ObjectMapper();
        result = writeAndMap(mapper2, bean);
        assertEquals(2, result.size());
        ObjectMapper mapper3 = mapper2.copy();
        mapper3.addMixIn(BaseClass.class, MixInAutoDetect.class);
        result = writeAndMap(mapper3, bean);
        assertEquals(1, result.size());
        assertEquals("c2", result.get("c"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForFields::testFieldMixInsTopLevel
    public void testFieldMixInsTopLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("1", "2");

        
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("1", result.get("a"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("banana"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForFields::testMultipleFieldMixIns
    public void testMultipleFieldMixIns() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        HashMap<Class<?>,Class<?>> mixins = new HashMap<Class<?>,Class<?>>();
        mixins.put(SubClass.class, MixIn.class);
        mixins.put(BaseClass.class, MixIn2.class);
        mapper.setMixIns(mixins);

        Map<String,Object> result;
        result = writeAndMap(mapper, new SubClass("1", "2"));
        assertEquals(1, result.size());
        
        assertEquals("2", result.get("banana"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testLeafMixin
    public void testLeafMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("a1", "b2");

        
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("b2", result.get("b"));

        
        mapper = new ObjectMapper();
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("b2", result.get("b2"));
        assertEquals("a1", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testIntermediateMixin
    public void testIntermediateMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        LeafClass bean = new LeafClass("XXX", "b2");

        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("XXX", result.get("a"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testIntermediateMixin2
    public void testIntermediateMixin2() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(EmptyBean.class, MixInForSimple.class);
        Map<String,Object> result = writeAndMap(mapper, new SimpleBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("x"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerForMethods::testCustomResolver
    public void testCustomResolver() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setMixInResolver(new ClassIntrospector.MixInResolver() {
            @Override
            public Class<?> findMixInClassFor(Class<?> target) {
                if (target == EmptyBean.class) {
                    return MixInForSimple.class;
                }
                return null;
            }

            @Override
            public MixInResolver copy() {
                return this;
            }
        });
        Map<String,Object> result = writeAndMap(mapper, new SimpleBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("x"));
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerWithViews::testDataBindingUsage
    public void testDataBindingUsage( ) throws Exception
    {
      ObjectMapper objectMapper = createObjectMapper();
      ObjectWriter objectWriter = objectMapper.writerWithView(Views.View.class).withDefaultPrettyPrinter();
      Object object = new ComplexTestData();
      String json = objectWriter.writeValueAsString(object);
      assertTrue( json.indexOf( "nameHidden" ) == -1 );
      assertTrue( json.indexOf( "\"name\" : \"shown\"" ) > 0 );
    }

// com.fasterxml.jackson.databind.mixins.TestMixinSerWithViews::testIssue560
    public void testIssue560() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        A a = new A("myname", 29, "mysurname");

        
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, Boolean.FALSE);
        mapper.addMixIn(A.class, AMixInAnnotation.class);
        String json = mapper.writerWithView(AView.class).writeValueAsString(a);

        assertTrue(json.indexOf("\"name\"") > 0);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testWithoutModule
    public void testWithoutModule()
    {
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            mapper.writeValueAsString(new CustomBean("foo", 3));
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "No serializer found");
        }

        
        try {
            mapper.readValue("{\"str\":\"ab\",\"num\":2}", CustomBean.class);
            fail("Should have caused an exception");
        } catch (IOException e) {
            verifyException(e, "Cannot construct");
            verifyException(e, "no creators");
        }
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleBeanSerializer
    public void testSimpleBeanSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new CustomBeanSerializer());
        mapper.registerModule(mod);
        assertEquals(quote("abcde|5"), mapper.writeValueAsString(new CustomBean("abcde", 5)));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleEnumSerializer
    public void testSimpleEnumSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new SimpleEnumSerializer());
        
        mapper.registerModules(mod);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleInterfaceSerializer
    public void testSimpleInterfaceSerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addSerializer(new BaseSerializer());
        
        List<SimpleModule> mods = Arrays.asList(mod);
        mapper.registerModules(mods);
        assertEquals(quote("Base:1"), mapper.writeValueAsString(new Impl1()));
        assertEquals(quote("Base:2"), mapper.writeValueAsString(new Impl2()));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleBeanDeserializer
    public void testSimpleBeanDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(CustomBean.class, new CustomBeanDeserializer());
        mapper.registerModule(mod);
        CustomBean bean = mapper.readValue(quote("xyz|3"), CustomBean.class);
        assertEquals("xyz", bean.str);
        assertEquals(3, bean.num);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testSimpleEnumDeserializer
    public void testSimpleEnumDeserializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addDeserializer(SimpleEnum.class, new SimpleEnumDeserializer());
        mapper.registerModule(mod);
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMultipleModules
    public void testMultipleModules() throws Exception
    {
        MySimpleModule mod1 = new MySimpleModule("test1", Version.unknownVersion());
        SimpleModule mod2 = new SimpleModule("test2", Version.unknownVersion());
        mod1.addSerializer(SimpleEnum.class, new SimpleEnumSerializer());
        mod1.addDeserializer(CustomBean.class, new CustomBeanDeserializer());

        Map<Class<?>,JsonDeserializer<?>> desers = new HashMap<>();
        desers.put(SimpleEnum.class, new SimpleEnumDeserializer());
        mod2.setDeserializers(new SimpleDeserializers(desers));
        mod2.addSerializer(CustomBean.class, new CustomBeanSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod1);
        mapper.registerModule(mod2);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        SimpleEnum result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);

        
        mapper = new ObjectMapper();
        mapper.registerModule(mod2);
        mapper.registerModule(mod1);
        assertEquals(quote("b"), mapper.writeValueAsString(SimpleEnum.B));
        result = mapper.readValue(quote("a"), SimpleEnum.class);
        assertSame(SimpleEnum.A, result);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testGetRegisteredModules
    public void testGetRegisteredModules()
    {
        MySimpleModule mod1 = new MySimpleModule("test1", Version.unknownVersion());
        AnotherSimpleModule mod2 = new AnotherSimpleModule("test2", Version.unknownVersion());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod1);
        mapper.registerModule(mod2);

        Set<Object> registeredModuleIds = mapper.getRegisteredModuleIds();
        assertEquals(2, registeredModuleIds.size());
        assertTrue(registeredModuleIds.contains(mod1.getTypeId()));
        assertTrue(registeredModuleIds.contains(mod2.getTypeId()));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMixIns
    public void testMixIns() throws Exception
    {
        SimpleModule module = new SimpleModule("test", Version.unknownVersion());
        module.setMixInAnnotation(MixableBean.class, MixInForOrder.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        Map<String,Object> props = this.writeAndMap(mapper, new MixableBean());
        assertEquals(3, props.size());
        assertEquals(Integer.valueOf(3), props.get("c"));
        assertEquals(Integer.valueOf(1), props.get("a"));
        assertEquals(Integer.valueOf(2), props.get("b"));
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testAccessToMapper
    public void testAccessToMapper() throws Exception
    {
        ContextVerifierModule module = new ContextVerifierModule();        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testMixIns626
    public void testMixIns626() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerModule(new TestModule626(Object.class, String.class));
        Class<?> found = mapper.findMixInClassFor(Object.class);
        assertEquals(String.class, found);
    }

// com.fasterxml.jackson.databind.module.SimpleModuleTest::testAutoDiscovery
    public void testAutoDiscovery() throws Exception
    {
        List<?> mods = ObjectMapper.findModules();
        assertEquals(0, mods.size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testCollectionDefaulting
    public void testCollectionDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Collection.class, List.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);
        mapper.registerModule(mod);
        Collection<?> result = mapper.readValue("[]", Collection.class);
        assertEquals(LinkedList.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testMapDefaultingBasic
    public void testMapDefaultingBasic() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mapper.registerModule(mod);
        Map<?,?> result = mapper.readValue("{}", Map.class);
        assertEquals(TreeMap.class, result.getClass());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testDefaultingRecursive
    public void testDefaultingRecursive() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());

        
        mod.addAbstractTypeMapping(Map.class, TreeMap.class);
        mod.addAbstractTypeMapping(List.class, LinkedList.class);

        mapper.registerModule(mod);
        Object result;

        result = mapper.readValue("[ {} ]", Object.class);
        assertEquals(LinkedList.class, result.getClass());
        Object v = ((List<?>) result).get(0);
        assertNotNull(v);
        assertEquals(TreeMap.class, v.getClass());

        result = mapper.readValue("{ \"x\": [ 3 ] }", Object.class);
        assertEquals(TreeMap.class, result.getClass());
        Map<?,?> map = (Map<?,?>) result;
        assertEquals(1, map.size());
        v = map.get("x");
        assertNotNull(v);
        assertEquals(LinkedList.class, v.getClass());
        assertEquals(1, ((List<?>) v).size());
    }

// com.fasterxml.jackson.databind.module.TestAbstractTypes::testInterfaceDefaulting
    public void testInterfaceDefaulting() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        
        mod.addAbstractTypeMapping(CharSequence.class, MyString.class);
        mapper.registerModule(mod);
        Object result = mapper.readValue(quote("abc"), CharSequence.class);
        assertEquals(MyString.class, result.getClass());
        assertEquals("abc", ((MyString) result).value);

        
        mod = new SimpleModule();
        mod.addAbstractTypeMapping(Abstract.class, AbstractImpl.class);
        mapper = new ObjectMapper()
                .registerModule(mod);
        Abstract a = mapper.readValue("{}", Abstract.class);
        assertNotNull(a);
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testWithEnumKeys
    public void testWithEnumKeys() throws Exception {
        ObjectMapper plainObjectMapper = new ObjectMapper();
        JsonNode tree = plainObjectMapper.readTree(aposToQuotes("{'red' : [ 'a', 'b']}"));

        ObjectMapper fancyObjectMapper = new ObjectMapper().registerModule(new TestEnumModule());

        
        Map<TestEnum, Set<String>> map = fancyObjectMapper.convertValue(tree,
                new TypeReference<Map<TestEnum, Set<String>>>() { } );
        assertNotNull(map);
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumKeySerializerWithPolymorphic
    public void testCustomEnumKeySerializerWithPolymorphic() throws IOException
    {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(SuperTypeEnum.class, new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext deserializationContext)
                    throws IOException
            {
                return SuperTypeEnum.valueOf(p.getText());
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(simpleModule);

        SuperType superType = mapper.readValue("{\"someMap\": {\"FOO\": \"bar\"}}",
                SuperType.class);
        assertEquals("Deserialized someMap.FOO should equal bar", "bar",
                superType.someMap.get(SuperTypeEnum.FOO));
    }

// com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumValueAndKeyViaModifier
    public void testCustomEnumValueAndKeyViaModifier() throws IOException
    {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {        
            @Override
            public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                    final JavaType type, BeanDescription beanDesc,
                    final JsonDeserializer<?> deserializer) {
                return new JsonDeserializer<Enum>() {
                    @Override
                    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        final String str = p.getValueAsString().toLowerCase();
                        return KeyEnum.valueOf(rawClass, str);
                    }
                };
            }

            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config,
                    final JavaType type, KeyDeserializer deserializer)
            {
                if (!type.isEnumType()) {
                    return deserializer;
                }
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt)
                            throws IOException
                    {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, key.toLowerCase());
                    }
                };
            }
        });
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(module);

        
        KeyEnum key = mapper.readValue(quote(KeyEnum.replacements.name().toUpperCase()),
                KeyEnum.class);
        assertSame(KeyEnum.replacements, key);

        
        EnumMap<KeyEnum,String> map = mapper.readValue(
                aposToQuotes("{'REPlaceMENTS':'foobar'}"),
                new TypeReference<EnumMap<KeyEnum,String>>() { });
        assertEquals(1, map.size());
        assertSame(KeyEnum.replacements, map.keySet().iterator().next());
    }

// com.fasterxml.jackson.databind.module.TestDuplicateRegistration::testDuplicateRegistration
    public void testDuplicateRegistration() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.isEnabled(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS));
        MyModule module = new MyModule();
        mapper.registerModule(module);
        mapper.registerModule(module);
        mapper.registerModule(module);
        assertEquals(1, module.regCount);

        
        mapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        mapper.registerModule(module);
        assertEquals(2, module.regCount);

        
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        MyModule module2 = new MyModule();
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        assertEquals(3, module2.regCount);
    }

// com.fasterxml.jackson.databind.module.TestKeyDeserializers::testKeyDeserializers
    public void testKeyDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", Version.unknownVersion());
        mod.addKeyDeserializer(Foo.class, new FooKeyDeserializer());
        mapper.registerModule(mod);
        Map<Foo,Integer> map = mapper.readValue("{\"a\":3}",
                new TypeReference<Map<Foo,Integer>>() {} );
        assertNotNull(map);
        assertEquals(1, map.size());
        Foo foo = map.keySet().iterator().next();
        assertEquals("a", foo.value);
    }

// com.fasterxml.jackson.databind.module.TestTypeModifierNameResolution::testTypeModiferNameResolution
	public void testTypeModiferNameResolution() throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new CustomTypeModifier()));
		mapper.addMixIn(MyType.class, Mixin.class);

		MyType obj = new MyTypeImpl();
		obj.setData("something");

		String s = mapper.writer().writeValueAsString(obj);
		assertTrue(s.startsWith("{\"TestTypeModifierNameResolution$MyType\":"));
	}

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeTypeConstruction
    public void testMapLikeTypeConstruction() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));

        JavaType type = mapper.constructType(MyMapLikeType.class);
        assertTrue(type.isMapLikeType());
        
        JavaType param = ((MapLikeType) type).getKeyType();
        assertNotNull(param);
        assertSame(String.class, param.getRawClass());
        param = ((MapLikeType) type).getContentType();
        assertNotNull(param);
        assertSame(Integer.class, param.getRawClass());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeTypeConstruction
    public void testCollectionLikeTypeConstruction() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));

        JavaType type = mapper.constructType(MyCollectionLikeType.class);
        assertTrue(type.isCollectionLikeType());
        JavaType param = ((CollectionLikeType) type).getContentType();
        assertNotNull(param);
        assertSame(Integer.class, param.getRawClass());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeSerialization
    public void testCollectionLikeSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        assertEquals("[19]", mapper.writeValueAsString(new MyCollectionLikeType(19)));
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeSerialization
    public void testMapLikeSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        assertEquals("{\"x\":\"xxx:3\"}", mapper.writeValueAsString(new MyMapLikeType("x", 3)));
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testCollectionLikeDeserialization
    public void testCollectionLikeDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        MyMapLikeType result = mapper.readValue("{\"a\":13}", MyMapLikeType.class);
        assertEquals("a", result.getKey());
        assertEquals(Integer.valueOf(13), result.getValue());
    }

// com.fasterxml.jackson.databind.module.TestTypeModifiers::testMapLikeDeserialization
    public void testMapLikeDeserialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(new MyTypeModifier()));
        mapper.registerModule(new ModifierModule());
        
        MyCollectionLikeType result = mapper.readValue("[-37]", MyCollectionLikeType.class);
        assertEquals(Integer.valueOf(-37), result.getValue());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testDirectCreation
    public void testDirectCreation() throws IOException
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        assertStandardEquals(n);
        assertFalse(n.elements().hasNext());
        assertFalse(n.fieldNames().hasNext());
        TextNode text = TextNode.valueOf("x");
        n.add(text);
        assertEquals(1, n.size());
        assertFalse(0 == n.hashCode());
        assertTrue(n.elements().hasNext());
        
        assertFalse(n.fieldNames().hasNext());
        assertNull(n.get("x")); 
        assertTrue(n.path("x").isMissingNode());
        assertSame(text, n.get(0));

        
        assertFalse(n.has("field"));
        assertFalse(n.hasNonNull("field"));
        assertTrue(n.has(0));
        assertTrue(n.hasNonNull(0));
        assertFalse(n.has(1));
        assertFalse(n.hasNonNull(1));
        
        
        n.add((JsonNode) null);
        assertEquals(2, n.size());
        assertTrue(n.get(1).isNull());
        assertTrue(n.has(1));
        assertFalse(n.hasNonNull(1));
        
        n.set(1, text);
        assertSame(text, n.get(1));
        n.set(0, null);
        assertTrue(n.get(0).isNull());

        
        ArrayNode n2 = new ArrayNode(JsonNodeFactory.instance);
        n2.add("foobar");
        assertFalse(n.equals(n2));
        n.addAll(n2);
        assertEquals(3, n.size());

        assertFalse(n.get(0).isTextual());
        assertNotNull(n.remove(0));
        assertEquals(2, n.size());
        assertTrue(n.get(0).isTextual());
        assertNull(n.remove(-1));
        assertNull(n.remove(100));
        assertEquals(2, n.size());

        ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();
        nodes.add(text);
        n.addAll(nodes);
        assertEquals(3, n.size());
        assertNull(n.get(10000));
        assertNull(n.remove(-4));

        TextNode text2 = TextNode.valueOf("b");
        n.insert(0, text2);
        assertEquals(4, n.size());
        assertSame(text2, n.get(0));

        assertNotNull(n.addArray());
        assertEquals(5, n.size());
        n.addPOJO("foo");
        assertEquals(6, n.size());

        n.removeAll();
        assertEquals(0, n.size());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testDirectCreation2
    public void testDirectCreation2() throws IOException
    {
        JsonNodeFactory f = objectMapper().getNodeFactory();
        ArrayList<JsonNode> list = new ArrayList<>();
        list.add(f.booleanNode(true));
        list.add(f.textNode("foo"));
        ArrayNode n = new ArrayNode(f, list);
        assertEquals(2, n.size());
        assertTrue(n.get(0).isBoolean());
        assertTrue(n.get(1).isTextual());

        
        try {
            n.set(2, f.nullNode());
            fail("Should not pass");
        } catch (IndexOutOfBoundsException e) {
            verifyException(e, "illegal index");
        }
        n.insert(1, (String) null);
        assertEquals(3, n.size());
        assertTrue(n.get(0).isBoolean());
        assertTrue(n.get(1).isNull());
        assertTrue(n.get(2).isTextual());

        n.removeAll();
        n.insert(0, (JsonNode) null);
        assertEquals(1, n.size());
        assertTrue(n.get(0).isNull());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testArrayViaMapper
    public void testArrayViaMapper() throws Exception
    {
        final String JSON = "[[[-0.027512,51.503221],[-0.008497,51.503221],[-0.008497,51.509744],[-0.027512,51.509744]]]";

        JsonNode n = objectMapper().readTree(JSON);
        assertNotNull(n);
        assertTrue(n.isArray());
        ArrayNode an = (ArrayNode) n;
        assertEquals(1, an.size());
        ArrayNode an2 = (ArrayNode) n.get(0);
        assertTrue(an2.isArray());
        assertEquals(4, an2.size());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testAdds
    public void testAdds()
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        assertNotNull(n.addArray());
        assertNotNull(n.addObject());
        n.addPOJO("foobar");
        n.add(1);
        n.add(1L);
        n.add(0.5);
        n.add(0.5f);
        n.add(new BigDecimal("0.2"));
        n.add(BigInteger.TEN);
        assertEquals(9, n.size());

        assertNotNull(n.insertArray(0));
        assertNotNull(n.insertObject(0));
        n.insertPOJO(2, "xxx");
        assertEquals(12, n.size());

        n.insert(0, BigInteger.ONE);
        n.insert(0, new BigDecimal("0.1"));
        assertEquals(14, n.size());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testNullAdds
    public void testNullAdds()
    {
        JsonNodeFactory f = objectMapper().getNodeFactory();
        ArrayNode array = f.arrayNode(14);

        array.add((BigDecimal) null);
        array.add((BigInteger) null);
        array.add((Boolean) null);
        array.add((byte[]) null);
        array.add((Double) null);
        array.add((Float) null);
        array.add((Integer) null);
        array.add((JsonNode) null);
        array.add((Long) null);
        array.add((String) null);

        assertEquals(10, array.size());
        
        for (JsonNode node : array) {
            assertTrue(node.isNull());
        }
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testNullInserts
    public void testNullInserts()
    {
        JsonNodeFactory f = objectMapper().getNodeFactory();
        ArrayNode array = f.arrayNode(3);

        array.insert(0, (BigDecimal) null);
        array.insert(0, (BigInteger) null);
        array.insert(0, (Boolean) null);
        
        
        array.insert(-56, (byte[]) null);
        array.insert(0, (Double) null);
        array.insert(200, (Float) null);
        array.insert(0, (Integer) null);
        array.insert(1, (JsonNode) null);
        array.insert(array.size(), (Long) null);
        array.insert(1, (String) null);

        assertEquals(10, array.size());
        
        for (JsonNode node : array) {
            assertTrue(node.isNull());
        }
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testNullChecking
    public void testNullChecking()
    {
        ArrayNode a1 = JsonNodeFactory.instance.arrayNode();
        ArrayNode a2 = JsonNodeFactory.instance.arrayNode();
        
        a1.addAll(a2);
        assertEquals(0, a1.size());
        assertEquals(0, a2.size());

        a2.addAll(a1);
        assertEquals(0, a1.size());
        assertEquals(0, a2.size());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testNullChecking2
    public void testNullChecking2()
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode src = mapper.createArrayNode();
        ArrayNode dest = mapper.createArrayNode();
        src.add("element");
        dest.addAll(src);
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testParser
    public void testParser() throws Exception
    {
        ArrayNode n = new ArrayNode(JsonNodeFactory.instance);
        n.add(123);
        TreeTraversingParser p = new TreeTraversingParser(n, null);
        p.setCodec(null);
        assertNull(p.getCodec());
        assertNotNull(p.getParsingContext());
        assertNotNull(p.getTokenLocation());
        assertNotNull(p.getCurrentLocation());
        assertNull(p.getEmbeddedObject());
        assertNull(p.currentNode());

        

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        p.skipChildren();
        assertToken(JsonToken.END_ARRAY, p.getCurrentToken());
        p.close();

        p = new TreeTraversingParser(n, null);
        p.nextToken();
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        p.close();
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testArrayNodeEquality
    public void testArrayNodeEquality()
    {
        ArrayNode n1 = new ArrayNode(null);
        ArrayNode n2 = new ArrayNode(null);

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));

        n1.add(TextNode.valueOf("Test"));

        assertFalse(n1.equals(n2));
        assertFalse(n2.equals(n1));

        n2.add(TextNode.valueOf("Test"));

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testSimpleArray
    public void testSimpleArray() throws Exception
    {
        ArrayNode result = objectMapper().createArrayNode();

        assertTrue(result.isArray());
        assertType(result, ArrayNode.class);

        assertFalse(result.isObject());
        assertFalse(result.isNumber());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());

        
        result.add(false);
        result.insertNull(0);

        
        assertEquals(result, result);
        assertFalse(result.equals(null)); 

        
        assertEquals(NullNode.instance, result.path(0));
        assertEquals(NullNode.instance, result.get(0));
        assertEquals(BooleanNode.FALSE, result.path(1));
        assertEquals(BooleanNode.FALSE, result.get(1));
        assertEquals(2, result.size());

        assertNull(result.get(-1));
        assertNull(result.get(2));
        JsonNode missing = result.path(2);
        assertTrue(missing.isMissingNode());
        assertTrue(result.path(-100).isMissingNode());

        
        ArrayNode array2 = objectMapper().createArrayNode();
        array2.addNull();
        array2.add(false);
        assertEquals(result, array2);

        
        JsonNode rm1 = array2.remove(0);
        assertEquals(NullNode.instance, rm1);
        assertEquals(1, array2.size());
        assertEquals(BooleanNode.FALSE, array2.get(0));
        assertFalse(result.equals(array2));

        JsonNode rm2 = array2.remove(0);
        assertEquals(BooleanNode.FALSE, rm2);
        assertEquals(0, array2.size());
    }

// com.fasterxml.jackson.databind.node.ArrayNodeTest::testSimpleMismatch
    public void testSimpleMismatch() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        try {
            mapper.readValue(" 123 ", ArrayNode.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of VALUE_NUMBER_INT token");
        }
    }

// com.fasterxml.jackson.databind.node.JsonNodeFactoryTest::testSimpleCreation
    public void testSimpleCreation()
    {
        JsonNodeFactory f = MAPPER.getNodeFactory();
        JsonNode n;

        n = f.numberNode((byte) 4);
        assertTrue(n.isInt());
        assertEquals(4, n.intValue());

        assertTrue(f.numberNode((Byte) null).isNull());

        assertTrue(f.numberNode((Short) null).isNull());

        assertTrue(f.numberNode((Integer) null).isNull());

        assertTrue(f.numberNode((Long) null).isNull());

        assertTrue(f.numberNode((Float) null).isNull());

        assertTrue(f.numberNode((Double) null).isNull());

        assertTrue(f.numberNode((BigDecimal) null).isNull());

        assertTrue(f.numberNode((BigInteger) null).isNull());
    }

// com.fasterxml.jackson.databind.node.NotANumberConversionTest::testBigDecimalWithNaN
    public void testBigDecimalWithNaN() throws Exception
    {
        JsonNode tree = m.valueToTree(new DoubleWrapper(Double.NaN));
        assertNotNull(tree);
        String json = m.writeValueAsString(tree);
        assertNotNull(json);

        tree = m.valueToTree(new DoubleWrapper(Double.NEGATIVE_INFINITY));
        assertNotNull(tree);
        json = m.writeValueAsString(tree);
        assertNotNull(json);

        tree = m.valueToTree(new DoubleWrapper(Double.POSITIVE_INFINITY));
        assertNotNull(tree);
        json = m.writeValueAsString(tree);
        assertNotNull(json);
    }

// com.fasterxml.jackson.databind.node.NotANumberConversionTest::testBigDecimalWithoutNaN
    public void testBigDecimalWithoutNaN() throws Exception
    {
        BigDecimal input = new BigDecimal(Double.MIN_VALUE).divide(new BigDecimal(10L));
        JsonNode tree = m.readTree(input.toString());
        assertTrue(tree.isBigDecimal());
        BigDecimal output = tree.decimalValue();
        assertEquals(input, output);
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testShort
    public void testShort()
    {
        ShortNode n = ShortNode.valueOf((short) 1);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.INT, n.numberType());	
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());

        assertNodeNumbers(n, 1, 1.0);

        assertTrue(ShortNode.valueOf((short) 0).canConvertToInt());
        assertTrue(ShortNode.valueOf(Short.MAX_VALUE).canConvertToInt());
        assertTrue(ShortNode.valueOf(Short.MIN_VALUE).canConvertToInt());

        assertTrue(ShortNode.valueOf((short) 0).canConvertToLong());
        assertTrue(ShortNode.valueOf(Short.MAX_VALUE).canConvertToLong());
        assertTrue(ShortNode.valueOf(Short.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testIntViaMapper
    public void testIntViaMapper() throws Exception
    {
        int value = -90184;
        JsonNode result = MAPPER.readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertTrue(result.isIntegralNumber());
        assertTrue(result.isInt());
        assertType(result, IntNode.class);
        assertFalse(result.isLong());
        assertFalse(result.isFloatingPointNumber());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue().intValue());
        assertEquals(value, result.intValue());
        assertEquals(String.valueOf(value), result.asText());
        assertEquals((double) value, result.doubleValue());
        assertEquals((long) value, result.longValue());

        
        assertEquals(result, IntNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testInt
    public void testInt()
    {
        IntNode n = IntNode.valueOf(1);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.INT, n.numberType());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());
        
        assertEquals("1", n.asText("foo"));
        
        assertNodeNumbers(n, 1, 1.0);

        assertTrue(IntNode.valueOf(0).canConvertToInt());
        assertTrue(IntNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(IntNode.valueOf(Integer.MIN_VALUE).canConvertToInt());

        assertTrue(IntNode.valueOf(0).canConvertToLong());
        assertTrue(IntNode.valueOf(Integer.MAX_VALUE).canConvertToLong());
        assertTrue(IntNode.valueOf(Integer.MIN_VALUE).canConvertToLong());

    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testLong
    public void testLong()
    {
        LongNode n = LongNode.valueOf(1L);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.LONG, n.numberType());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());

        assertNodeNumbers(n, 1, 1.0);

        
        assertTrue(LongNode.valueOf(0).canConvertToInt());
        assertTrue(LongNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(LongNode.valueOf(Integer.MIN_VALUE).canConvertToInt());
        
        assertFalse(LongNode.valueOf(1L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(LongNode.valueOf(-1L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(LongNode.valueOf(0L).canConvertToLong());
        assertTrue(LongNode.valueOf(Long.MAX_VALUE).canConvertToLong());
        assertTrue(LongNode.valueOf(Long.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testLongViaMapper
    public void testLongViaMapper() throws Exception
    {
        
        long value = 12345678L << 32;
        JsonNode result = MAPPER.readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertTrue(result.isIntegralNumber());
        assertTrue(result.isLong());
        assertType(result, LongNode.class);
        assertFalse(result.isInt());
        assertFalse(result.isFloatingPointNumber());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue().longValue());
        assertEquals(value, result.longValue());
        assertEquals(String.valueOf(value), result.asText());
        assertEquals((double) value, result.doubleValue());

        
        assertEquals(result, LongNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testDouble
    public void testDouble() throws Exception
    {
        DoubleNode n = DoubleNode.valueOf(0.25);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.DOUBLE, n.numberType());
        assertEquals(0, n.intValue());
        assertEquals(0.25, n.doubleValue());
        assertNotNull(n.decimalValue());
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());
        assertEquals("0.25", n.asText());

        assertNodeNumbers(DoubleNode.valueOf(4.5), 4, 4.5);

        assertTrue(DoubleNode.valueOf(0).canConvertToInt());
        assertTrue(DoubleNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(DoubleNode.valueOf(Integer.MIN_VALUE).canConvertToInt());
        assertFalse(DoubleNode.valueOf(1L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(DoubleNode.valueOf(-1L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(DoubleNode.valueOf(0L).canConvertToLong());
        assertTrue(DoubleNode.valueOf(Long.MAX_VALUE).canConvertToLong());
        assertTrue(DoubleNode.valueOf(Long.MIN_VALUE).canConvertToLong());

        JsonNode num = objectMapper().readTree(" -0.0");
        assertTrue(num.isDouble());
        n = (DoubleNode) num;
        assertEquals(-0.0, n.doubleValue());
        assertEquals("-0.0", String.valueOf(n.doubleValue()));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testDoubleViaMapper
    public void testDoubleViaMapper() throws Exception
    {
        double value = 3.04;
        JsonNode result = MAPPER.readTree(String.valueOf(value));
        assertTrue(result.isNumber());
        assertFalse(result.isNull());
        assertType(result, DoubleNode.class);
        assertTrue(result.isFloatingPointNumber());
        assertTrue(result.isDouble());
        assertFalse(result.isInt());
        assertFalse(result.isLong());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.doubleValue());
        assertEquals(value, result.numberValue().doubleValue());
        assertEquals((int) value, result.intValue());
        assertEquals((long) value, result.longValue());
        assertEquals(String.valueOf(value), result.asText());

        
        assertEquals(result, DoubleNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testFloat
    public void testFloat()
    {
        FloatNode n = FloatNode.valueOf(0.45f);
        assertStandardEquals(n);
        assertTrue(0 != n.hashCode());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.FLOAT, n.numberType());
        assertEquals(0, n.intValue());
        
        
        assertEquals(0.45f, n.floatValue());
        assertEquals("0.45", n.asText());

        
        
        assertEquals("0.45",  String.valueOf((float) n.doubleValue()));

        assertNotNull(n.decimalValue());
        
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());
        assertEquals("0.45", n.asText());

        
        assertNodeNumbers(FloatNode.valueOf(4.5f), 4, 4.5f);

        assertTrue(FloatNode.valueOf(0).canConvertToInt());
        assertTrue(FloatNode.valueOf(Integer.MAX_VALUE).canConvertToInt());
        assertTrue(FloatNode.valueOf(Integer.MIN_VALUE).canConvertToInt());

        
        assertFalse(FloatNode.valueOf(1000L + Integer.MAX_VALUE).canConvertToInt());
        assertFalse(FloatNode.valueOf(-1000L + Integer.MIN_VALUE).canConvertToInt());

        assertTrue(FloatNode.valueOf(0L).canConvertToLong());
        assertTrue(FloatNode.valueOf(Integer.MAX_VALUE).canConvertToLong());
        assertTrue(FloatNode.valueOf(Integer.MIN_VALUE).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testDecimalNode
    public void testDecimalNode() throws Exception
    {
        DecimalNode n = DecimalNode.valueOf(BigDecimal.ONE);
        assertStandardEquals(n);
        assertTrue(n.equals(new DecimalNode(BigDecimal.ONE)));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, n.asToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, n.numberType());
        assertTrue(n.isNumber());
        assertFalse(n.isIntegralNumber());
        assertFalse(n.isArray());
        assertTrue(n.isBigDecimal());
        assertEquals(BigDecimal.ONE, n.numberValue());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigDecimal.ONE, n.decimalValue());
        assertEquals("1", n.asText());

        assertNodeNumbers(n, 1, 1.0);

        assertTrue(DecimalNode.valueOf(BigDecimal.ZERO).canConvertToInt());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Integer.MAX_VALUE)).canConvertToInt());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Integer.MIN_VALUE)).canConvertToInt());
        assertFalse(DecimalNode.valueOf(BigDecimal.valueOf(1L + Integer.MAX_VALUE)).canConvertToInt());
        assertFalse(DecimalNode.valueOf(BigDecimal.valueOf(-1L + Integer.MIN_VALUE)).canConvertToInt());

        assertTrue(DecimalNode.valueOf(BigDecimal.ZERO).canConvertToLong());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Long.MAX_VALUE)).canConvertToLong());
        assertTrue(DecimalNode.valueOf(BigDecimal.valueOf(Long.MIN_VALUE)).canConvertToLong());

        
        BigDecimal value = new BigDecimal("0.1");
        JsonNode result = DecimalNode.valueOf(value);

        assertFalse(result.isObject());
        assertTrue(result.isNumber());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isLong());
        assertType(result, DecimalNode.class);
        assertFalse(result.isInt());
        assertTrue(result.isFloatingPointNumber());
        assertTrue(result.isBigDecimal());
        assertFalse(result.isDouble());
        assertFalse(result.isNull());
        assertFalse(result.isTextual());
        assertFalse(result.isMissingNode());

        assertEquals(value, result.numberValue());
        assertEquals(value.toString(), result.asText());

        
        assertEquals(result, DecimalNode.valueOf(value));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testDecimalNodeEqualsHashCode
    public void testDecimalNodeEqualsHashCode()
    {
        
        
        
        BigDecimal b1 = BigDecimal.ONE;
        BigDecimal b2 = new BigDecimal("1.0");
        BigDecimal b3 = new BigDecimal("0.01e2");
        BigDecimal b4 = new BigDecimal("1000e-3");

        DecimalNode node1 = new DecimalNode(b1);
        DecimalNode node2 = new DecimalNode(b2);
        DecimalNode node3 = new DecimalNode(b3);
        DecimalNode node4 = new DecimalNode(b4);

        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node2.hashCode(), node3.hashCode());
        assertEquals(node3.hashCode(), node4.hashCode());

        assertEquals(node1, node2);
        assertEquals(node2, node1);
        assertEquals(node2, node3);
        assertEquals(node3, node4);
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testBigIntegerNode
    public void testBigIntegerNode() throws Exception
    {
        BigIntegerNode n = BigIntegerNode.valueOf(BigInteger.ONE);
        assertStandardEquals(n);
        assertTrue(n.equals(new BigIntegerNode(BigInteger.ONE)));
        assertEquals(JsonToken.VALUE_NUMBER_INT, n.asToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, n.numberType());
        assertTrue(n.isNumber());
        assertTrue(n.isIntegralNumber());
        assertTrue(n.isBigInteger());
        assertEquals(BigInteger.ONE, n.numberValue());
        assertEquals(1, n.intValue());
        assertEquals(1L, n.longValue());
        assertEquals(BigInteger.ONE, n.bigIntegerValue());
        assertEquals("1", n.asText());
        assertNodeNumbers(n, 1, 1.0);

        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        
        n = BigIntegerNode.valueOf(maxLong);
        assertEquals(Long.MAX_VALUE, n.longValue());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode n2 = mapper.readTree(maxLong.toString());
        assertEquals(Long.MAX_VALUE, n2.longValue());

        
        BigInteger beyondLong = maxLong.shiftLeft(2); 
        n2 = mapper.readTree(beyondLong.toString());
        assertEquals(beyondLong, n2.bigIntegerValue());

        assertTrue(BigIntegerNode.valueOf(BigInteger.ZERO).canConvertToInt());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Integer.MAX_VALUE)).canConvertToInt());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Integer.MIN_VALUE)).canConvertToInt());
        assertFalse(BigIntegerNode.valueOf(BigInteger.valueOf(1L + Integer.MAX_VALUE)).canConvertToInt());
        assertFalse(BigIntegerNode.valueOf(BigInteger.valueOf(-1L + Integer.MIN_VALUE)).canConvertToInt());

        assertTrue(BigIntegerNode.valueOf(BigInteger.ZERO).canConvertToLong());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Long.MAX_VALUE)).canConvertToLong());
        assertTrue(BigIntegerNode.valueOf(BigInteger.valueOf(Long.MIN_VALUE)).canConvertToLong());
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testBigDecimalAsPlain
    public void testBigDecimalAsPlain() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        final String INPUT = "{\"x\":1e2}";
        final JsonNode node = mapper.readTree(INPUT);
        String result = mapper.writeValueAsString(node);
        assertEquals("{\"x\":100}", result);

        
        assertEquals("{\"x\":100}", mapper.writer().writeValueAsString(node));

        
        BigDecimal bigDecimal = new BigDecimal(100);
        JsonNode tree = mapper.valueToTree(bigDecimal);
        assertEquals("100", mapper.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.NumberNodesTest::testCanonicalNumbers
    public void testCanonicalNumbers() throws Exception
    {
        JsonNodeFactory f = new JsonNodeFactory();
        NumericNode n = f.numberNode(123);
        assertTrue(n.isInt());
        n = f.numberNode(1L + Integer.MAX_VALUE);
        assertFalse(n.isInt());
        assertTrue(n.isLong());

        
        
        
        n = f.numberNode(123L);
        assertTrue(n.isLong());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimpleObject
    public void testSimpleObject() throws Exception
    {
        String JSON = "{ \"key\" : 1, \"b\" : \"x\" }";
        JsonNode root = MAPPER.readTree(JSON);

        
        assertFalse(root.isValueNode());
        assertTrue(root.isContainerNode());
        assertFalse(root.isArray());
        assertTrue(root.isObject());
        assertEquals(2, root.size());

        Iterator<JsonNode> it = root.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        JsonNode n = it.next();
        assertNotNull(n);
        assertEquals(IntNode.valueOf(1), n);

        assertTrue(it.hasNext());
        n = it.next();
        assertNotNull(n);
        assertEquals(TextNode.valueOf("x"), n);

        assertFalse(it.hasNext());

        
        ObjectNode obNode = (ObjectNode) root;
        Iterator<Map.Entry<String,JsonNode>> fit = obNode.fields();
        
        assertTrue(fit.hasNext());
        Map.Entry<String,JsonNode> en = fit.next();
        assertEquals("key", en.getKey());
        assertEquals(IntNode.valueOf(1), en.getValue());

        assertTrue(fit.hasNext());
        en = fit.next();
        assertEquals("b", en.getKey());
        assertEquals(TextNode.valueOf("x"), en.getValue());

        
        fit.remove();
        assertEquals(1, obNode.size());
        assertEquals(IntNode.valueOf(1), root.get("key"));
        assertNull(root.get("b"));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testEmptyNodeAsValue
    public void testEmptyNodeAsValue() throws Exception
    {
        Data w = MAPPER.readValue("{}", Data.class);
        assertNotNull(w);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testBasics
    public void testBasics()
    {
        ObjectNode n = new ObjectNode(JsonNodeFactory.instance);
        assertStandardEquals(n);

        assertFalse(n.elements().hasNext());
        assertFalse(n.fields().hasNext());
        assertFalse(n.fieldNames().hasNext());
        assertNull(n.get("a"));
        assertTrue(n.path("a").isMissingNode());

        TextNode text = TextNode.valueOf("x");
        assertSame(n, n.set("a", text));
        
        assertEquals(1, n.size());
        assertTrue(n.elements().hasNext());
        assertTrue(n.fields().hasNext());
        assertTrue(n.fieldNames().hasNext());
        assertSame(text, n.get("a"));
        assertSame(text, n.path("a"));
        assertNull(n.get("b"));
        assertNull(n.get(0)); 

        assertFalse(n.has(0));
        assertFalse(n.hasNonNull(0));
        assertTrue(n.has("a"));
        assertTrue(n.hasNonNull("a"));
        assertFalse(n.has("b"));
        assertFalse(n.hasNonNull("b"));

        ObjectNode n2 = new ObjectNode(JsonNodeFactory.instance);
        n2.put("b", 13);
        assertFalse(n.equals(n2));
        n.setAll(n2);
        
        assertEquals(2, n.size());
        n.set("null", (JsonNode)null);
        assertEquals(3, n.size());
        
        assertTrue(n.has("null"));
        assertFalse(n.hasNonNull("null"));
        
        n.put("null", "notReallNull");
        assertEquals(3, n.size());
        assertNotNull(n.remove("null"));
        assertEquals(2, n.size());

        Map<String,JsonNode> nodes = new HashMap<String,JsonNode>();
        nodes.put("d", text);
        n.setAll(nodes);
        assertEquals(3, n.size());

        n.removeAll();
        assertEquals(0, n.size());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testBigNumbers
    public void testBigNumbers()
    {
        ObjectNode n = new ObjectNode(JsonNodeFactory.instance);
        assertStandardEquals(n);
        BigInteger I = BigInteger.valueOf(3);
        BigDecimal DEC = new BigDecimal("0.1");

        n.put("a", DEC);
        n.put("b", I);

        assertEquals(2, n.size());

        assertTrue(n.path("a").isBigDecimal());
        assertEquals(DEC, n.get("a").decimalValue());
        assertTrue(n.path("b").isBigInteger());
        assertEquals(I, n.get("b").bigIntegerValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNullChecking
    public void testNullChecking()
    {
        ObjectNode o1 = JsonNodeFactory.instance.objectNode();
        ObjectNode o2 = JsonNodeFactory.instance.objectNode();
        
        o1.setAll(o2);
        assertEquals(0, o1.size());
        assertEquals(0, o2.size());

        
        o1.set("x", null);
        JsonNode n = o1.get("x");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("str", (String) null);
        n = o1.get("str");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("d", (BigDecimal) null);
        n = o1.get("d");
        assertNotNull(n);
        assertSame(n, NullNode.instance);

        o1.put("3", (BigInteger) null);
        n = o1.get("3");
        assertNotNull(3);
        assertSame(n, NullNode.instance);

        assertEquals(4, o1.size());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNullChecking2
    public void testNullChecking2()
    {
        ObjectNode src = MAPPER.createObjectNode();
        ObjectNode dest = MAPPER.createObjectNode();
        src.put("a", "b");
        dest.setAll(src);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testRemove
    public void testRemove()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.without(Arrays.asList("a", "c")));
        assertEquals(1, ob.size());
        assertEquals("b", ob.get("b").textValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testRetain
    public void testRetain()
    {
        ObjectNode ob = MAPPER.createObjectNode();
        ob.put("a", "a");
        ob.put("b", "b");
        ob.put("c", "c");
        assertEquals(3, ob.size());
        assertSame(ob, ob.retain("a", "c"));
        assertEquals(2, ob.size());
        assertEquals("a", ob.get("a").textValue());
        assertNull(ob.get("b"));
        assertEquals("c", ob.get("c").textValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testValidWith
    public void testValidWith() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.with("prop");
        assertTrue(child instanceof ObjectNode);
        assertEquals("{\"prop\":{}}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testValidWithArray
    public void testValidWithArray() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals("{}", MAPPER.writeValueAsString(root));
        JsonNode child = root.withArray("arr");
        assertTrue(child instanceof ArrayNode);
        assertEquals("{\"arr\":[]}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testInvalidWith
    public void testInvalidWith() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.with("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testInvalidWithArray
    public void testInvalidWithArray() throws Exception
    {
        JsonNode root = MAPPER.createArrayNode();
        try { 
            root.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "not of type ObjectNode");
        }
        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("prop", 13);
        try { 
            root2.withArray("prop");
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            verifyException(e, "has value that is not");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSetAll
    public void testSetAll() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        assertEquals(0, root.size());
        HashMap<String,JsonNode> map = new HashMap<String,JsonNode>();
        map.put("a", root.numberNode(1));
        root.setAll(map);
        assertEquals(1, root.size());
        assertTrue(root.has("a"));
        assertFalse(root.has("b"));

        map.put("b", root.numberNode(2));
        root.setAll(map);
        assertEquals(2, root.size());
        assertTrue(root.has("a"));
        assertTrue(root.has("b"));
        assertEquals(2, root.path("b").intValue());

        
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        root2.setAll(root);
        assertEquals(2, root.size());
        assertEquals(2, root2.size());

        ObjectNode root3 = MAPPER.createObjectNode();
        root3.put("a", 2);
        root3.put("c", 3);
        assertEquals(2, root3.path("a").intValue());
        root3.setAll(root2);
        assertEquals(3, root3.size());
        assertEquals(1, root3.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testFailOnDupKeys
    public void testFailOnDupKeys() throws Exception
    {
        final String DUP_JSON = "{ \"a\":1, \"a\":2 }";
        
        
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY));
        ObjectNode root = (ObjectNode) mapper.readTree(DUP_JSON);
        assertEquals(2, root.path("a").asInt());
        
        
        try {
            mapper.reader(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).readTree(DUP_JSON);
            fail("Should have thrown exception!");
        } catch (JsonMappingException e) {
            verifyException(e, "duplicate field 'a'");
        }
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testEqualityWrtOrder
    public void testEqualityWrtOrder() throws Exception
    {
        ObjectNode ob1 = MAPPER.createObjectNode();
        ObjectNode ob2 = MAPPER.createObjectNode();

        
        
        ob1.put("a", 1);
        ob1.put("b", 2);
        ob1.put("c", 3);

        ob2.put("b", 2);
        ob2.put("c", 3);
        ob2.put("a", 1);

        assertTrue(ob1.equals(ob2));
        assertTrue(ob2.equals(ob1));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimplePath
    public void testSimplePath() throws Exception
    {
        JsonNode root = MAPPER.readTree("{ \"results\" : { \"a\" : 3 } }");
        assertTrue(root.isObject());
        JsonNode rnode = root.path("results");
        assertNotNull(rnode);
        assertTrue(rnode.isObject());
        assertEquals(3, rnode.path("a").intValue());
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testNonEmptySerialization
    public void testNonEmptySerialization() throws Exception
    {
        ObNodeWrapper w = new ObNodeWrapper(MAPPER.createObjectNode()
                .put("a", 3));
        assertEquals("{\"node\":{\"a\":3}}", MAPPER.writeValueAsString(w));
        w = new ObNodeWrapper(MAPPER.createObjectNode());
        assertEquals("{}", MAPPER.writeValueAsString(w));
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testIssue941
    public void testIssue941() throws Exception
    {
        ObjectNode object = MAPPER.createObjectNode();

        String json = MAPPER.writeValueAsString(object);

        ObjectNode de1 = MAPPER.readValue(json, ObjectNode.class);  

        assertNotNull(de1);

        MyValue de2 = MAPPER.readValue(json, MyValue.class);  

        assertNotNull(de2);
    }

// com.fasterxml.jackson.databind.node.ObjectNodeTest::testSimpleMismatch
    public void testSimpleMismatch() throws Exception
    {
        ObjectMapper mapper = objectMapper();
        try {
            mapper.readValue("[ 1, 2, 3 ]", ObjectNode.class);
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "out of START_ARRAY token");
        }
    }

// com.fasterxml.jackson.databind.node.POJONodeTest::testPOJONodeCustomSer
    public void testPOJONodeCustomSer() throws Exception
    {
      Data data = new Data();
      data.aStr = "Hello";

      Map<String, Object> mapTest = new HashMap<>();
      mapTest.put("data", data);

      ObjectNode treeTest = MAPPER.createObjectNode();
      treeTest.putPOJO("data", data);

      final String EXP = "{\"data\":{\"aStr\":\"The value is: Hello!\"}}";
      
      String mapOut = MAPPER.writer().withAttribute("myAttr", "Hello!").writeValueAsString(mapTest);
      assertEquals(EXP, mapOut);

      String treeOut = MAPPER.writer().withAttribute("myAttr", "Hello!").writeValueAsString(treeTest);
      assertEquals(EXP, treeOut);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsInt
    public void testAsInt() throws Exception
    {
        assertEquals(9, IntNode.valueOf(9).asInt());
        assertEquals(7, LongNode.valueOf(7L).asInt());
        assertEquals(13, new TextNode("13").asInt());
        assertEquals(0, new TextNode("foobar").asInt());
        assertEquals(27, new TextNode("foobar").asInt(27));
        assertEquals(1, BooleanNode.TRUE.asInt());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testAsBoolean
    public void testAsBoolean() throws Exception
    {
        assertEquals(false, BooleanNode.FALSE.asBoolean());
        assertEquals(true, BooleanNode.TRUE.asBoolean());
        assertEquals(false, IntNode.valueOf(0).asBoolean());
        assertEquals(true, IntNode.valueOf(1).asBoolean());
        assertEquals(false, LongNode.valueOf(0).asBoolean());
        assertEquals(true, LongNode.valueOf(-34L).asBoolean());
        assertEquals(true, new TextNode("true").asBoolean());
        assertEquals(false, new TextNode("false").asBoolean());
        assertEquals(false, new TextNode("barf").asBoolean());
        assertEquals(true, new TextNode("barf").asBoolean(true));

        assertEquals(true, new POJONode(Boolean.TRUE).asBoolean());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValue
    public void testTreeToValue() throws Exception
    {
        String JSON = "{\"leaf\":{\"value\":13}}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Leaf.class, LeafMixIn.class);
        JsonNode root = mapper.readTree(JSON);
        
        Root r1 = mapper.treeToValue(root, Root.class);
        assertNotNull(r1);
        assertEquals(13, r1.leaf.value);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testTreeToValueWithPOJO
    public void testTreeToValueWithPOJO() throws Exception
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(0));
        ValueNode pojoNode = MAPPER.getNodeFactory().pojoNode(c);        
        Calendar result = MAPPER.treeToValue(pojoNode, Calendar.class);
        assertNotNull(result);
        assertEquals(result.getTimeInMillis(), c.getTimeInMillis());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBase64Text
    public void testBase64Text() throws Exception
    {
        
        
        final int[] LENS = { 1, 2, 3, 4, 7, 9, 32, 33, 34, 35 };
        final Base64Variant[] VARIANTS = {
                Base64Variants.MIME,
                Base64Variants.MIME_NO_LINEFEEDS,
                Base64Variants.MODIFIED_FOR_URL,
                Base64Variants.PEM
        };

        for (int len : LENS) {
            byte[] input = new byte[len];
            for (int i = 0; i < input.length; ++i) {
                input[i] = (byte) i;
            }
            for (Base64Variant variant : VARIANTS) {
                TextNode n = new TextNode(variant.encode(input));
                byte[] data = null;
                try {
                    data = n.getBinaryValue(variant);
                } catch (Exception e) {
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);

                
                JsonParser p = new TreeTraversingParser(n);
                assertEquals(JsonToken.VALUE_STRING, p.nextToken());
                try {
                    data = p.getBinaryValue(variant);
                } catch (Exception e) {
                    fail("Failed (variant "+variant+", data length "+len+"): "+e.getMessage());
                }
                assertNotNull(data);
                assertArrayEquals(data, input);
                p.close();
            }
        }
    }

// com.fasterxml.jackson.databind.node.TestConversions::testIssue709
    public void testIssue709() throws Exception
    {
        byte[] inputData = new byte[] { 1, 2, 3 };
        ObjectNode node = MAPPER.createObjectNode();
        node.put("data", inputData);
        Issue709Bean result = MAPPER.treeToValue(node, Issue709Bean.class);
        String json = MAPPER.writeValueAsString(node);
        Issue709Bean resultFromString = MAPPER.readValue(json, Issue709Bean.class);
        Issue709Bean resultFromConvert = MAPPER.convertValue(node, Issue709Bean.class);
        
        
        Assert.assertArrayEquals(inputData, resultFromString.data);
        Assert.assertArrayEquals(inputData, resultFromConvert.data);
        Assert.assertArrayEquals(inputData, result.data);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testEmbeddedByteArray
    public void testEmbeddedByteArray() throws Exception
    {
        TokenBuffer buf = new TokenBuffer(MAPPER, false);
        buf.writeObject(new byte[3]);
        JsonNode node = MAPPER.readTree(buf.asParser());
        buf.close();
        assertTrue(node.isBinary());
        byte[] data = node.binaryValue();
        assertNotNull(data);
        assertEquals(3, data.length);
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBigDecimalAsPlainStringTreeConversion
    public void testBigDecimalAsPlainStringTreeConversion() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Map<String, Object> map = new HashMap<String, Object>();
        String PI_STR = "3.00000000";
        map.put("pi", new BigDecimal(PI_STR));
        JsonNode tree = mapper.valueToTree(map);
        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertTrue(tree.has("pi"));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBeanToTree
    public void testBeanToTree() throws Exception
    {
        final CustomSerializedPojo pojo = new CustomSerializedPojo();
        pojo.setFoo("bar");
        final JsonNode node = MAPPER.valueToTree(pojo);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfPojos
    public void testConversionOfPojos() throws Exception
    {
        final Issue467Bean input = new Issue467Bean(13);
        final String EXP = "{\"x\":13}";
        
        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testConversionOfTrees
    public void testConversionOfTrees() throws Exception
    {
        final Issue467Tree input = new Issue467Tree();
        final String EXP = "true";

        
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isBoolean());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }

// com.fasterxml.jackson.databind.node.TestConversions::testBufferedLongViaCoercion
    public void testBufferedLongViaCoercion() throws Exception {
        long EXP = 1519348261000L;
        JsonNode tree = MAPPER.readTree("{\"longObj\": "+EXP+".0, \"_class\": \""+LongContainer1940.class.getName()+"\"}");
        LongContainer1940 obj = MAPPER.treeToValue(tree, LongContainer1940.class);
        assertEquals(Long.valueOf(EXP), obj.longObj);
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithObjectSimple
    public void testWithObjectSimple()
    {
        ObjectNode root = mapper.createObjectNode();
        root.put("a", 3);
        assertEquals(1, root.size());
        
        ObjectNode copy = root.deepCopy();
        assertEquals(1, copy.size());

        
        root.put("b", 7);
        assertEquals(2, root.size());
        assertEquals(1, copy.size());

        
        copy.put("c", 3);
        assertEquals(2, root.size());
        assertEquals(2, copy.size());
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithArraySimple
    public void testWithArraySimple()
    {
        ArrayNode root = mapper.createArrayNode();
        root.add("a");
        assertEquals(1, root.size());
        
        ArrayNode copy = root.deepCopy();
        assertEquals(1, copy.size());

        
        root.add( 7);
        assertEquals(2, root.size());
        assertEquals(1, copy.size());

        
        copy.add(3);
        assertEquals(2, root.size());
        assertEquals(2, copy.size());
    }

// com.fasterxml.jackson.databind.node.TestDeepCopy::testWithNested
    public void testWithNested()
    {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode leafObject = root.putObject("ob");
        ArrayNode leafArray = root.putArray("arr");
        assertEquals(2, root.size());

        leafObject.put("a", 3);
        assertEquals(1, leafObject.size());
        leafArray.add(true);
        assertEquals(1, leafArray.size());
        
        ObjectNode copy = root.deepCopy();
        assertNotSame(copy, root);
        assertEquals(2, copy.size());

        

        leafObject.put("x", 9);
        assertEquals(2, leafObject.size());
        assertEquals(1, copy.get("ob").size());

        leafArray.add("foobar");
        assertEquals(2, leafArray.size());
        assertEquals(1, copy.get("arr").size());

        
        ((ObjectNode) copy.get("ob")).put("c", 3);
        assertEquals(2, leafObject.size());
        assertEquals(2, copy.get("ob").size());

        ((ArrayNode) copy.get("arr")).add(13);
        assertEquals(2, leafArray.size());
        assertEquals(2, copy.get("arr").size());
    }

// com.fasterxml.jackson.databind.node.TestEndOfInputHandling::testErrorHandling
  public void testErrorHandling() throws IOException {
      ObjectMapper mapper = new ObjectMapper();

      String json = "{\"A\":{\"B\":\n";
      JsonParser parser = mapper.getFactory().createParser(json);
      parser.setCodec(new ObjectMapper());
      try {
          parser.readValueAsTree();
      } catch(JsonParseException e) {
          verifyException(e, "Unexpected end-of-input");
      }
      parser.close();

      try {
          mapper.readTree(json);
      }
      catch(JsonParseException e) {
          verifyException(e, "Unexpected end-of-input");
      }
  }

// com.fasterxml.jackson.databind.node.TestFindMethods::testNonMatching
    public void testNonMatching() throws Exception
    {
        JsonNode root = _buildTree();

        assertNull(root.findValue("boogaboo"));
        assertNull(root.findParent("boogaboo"));
        JsonNode n = root.findPath("boogaboo");
        assertNotNull(n);
        assertTrue(n.isMissingNode());

        assertTrue(root.findValues("boogaboo").isEmpty());
        assertTrue(root.findParents("boogaboo").isEmpty());
    }

// com.fasterxml.jackson.databind.node.TestFindMethods::testMatchingSingle
    public void testMatchingSingle() throws Exception
    {
        JsonNode root = _buildTree();

        JsonNode node = root.findValue("b");
        assertNotNull(node);
        assertEquals(3, node.intValue());
        node = root.findParent("b");
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(1, ((ObjectNode) node).size());
        assertEquals(3, node.path("b").intValue());
    }

// com.fasterxml.jackson.databind.node.TestFindMethods::testMatchingMultiple
    public void testMatchingMultiple() throws Exception
    {
        JsonNode root = _buildTree();

        List<JsonNode> nodes = root.findValues("value");
        assertEquals(2, nodes.size());
        
        assertEquals(3, nodes.get(0).intValue());
        assertEquals(42, nodes.get(1).intValue());

        nodes = root.findParents("value");
        assertEquals(2, nodes.size());
        
        assertTrue(nodes.get(0).isObject());
        assertTrue(nodes.get(1).isObject());
        assertEquals(3, nodes.get(0).path("value").intValue());
        assertEquals(42, nodes.get(1).path("value").intValue());

        
        List<String> values = root.findValuesAsText("value");
        assertEquals(2, values.size());
        assertEquals("3", values.get(0));
        assertEquals("42", values.get(1));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testBoolean
    public void testBoolean() throws Exception
    {
        BooleanNode f = BooleanNode.getFalse();
        assertNotNull(f);
        assertTrue(f.isBoolean());
        assertSame(f, BooleanNode.valueOf(false));
        assertStandardEquals(f);
        assertFalse(f.booleanValue());
        assertFalse(f.asBoolean());
        assertEquals("false", f.asText());
        assertEquals(JsonToken.VALUE_FALSE, f.asToken());

        
        BooleanNode t = BooleanNode.getTrue();
        assertNotNull(t);
        assertTrue(t.isBoolean());
        assertSame(t, BooleanNode.valueOf(true));
        assertStandardEquals(t);
        assertTrue(t.booleanValue());
        assertTrue(t.asBoolean());
        assertEquals("true", t.asText());
        assertEquals(JsonToken.VALUE_TRUE, t.asToken());

        assertNodeNumbers(f, 0, 0.0);
        assertNodeNumbers(t, 1, 1.0);
    
        JsonNode result = objectMapper().readTree("true\n");
        assertFalse(result.isNull());
        assertFalse(result.isNumber());
        assertFalse(result.isTextual());
        assertTrue(result.isBoolean());
        assertType(result, BooleanNode.class);
        assertTrue(result.booleanValue());
        assertEquals("true", result.asText());
        assertFalse(result.isMissingNode());

        
        assertEquals(result, BooleanNode.valueOf(true));
        assertEquals(result, BooleanNode.getTrue());
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testBinary
    public void testBinary() throws Exception
    {
        assertNull(BinaryNode.valueOf(null));
        assertNull(BinaryNode.valueOf(null, 0, 0));

        BinaryNode empty = BinaryNode.valueOf(new byte[1], 0, 0);
        assertSame(BinaryNode.EMPTY_BINARY_NODE, empty);
        assertStandardEquals(empty);

        byte[] data = new byte[3];
        data[1] = (byte) 3;
        BinaryNode n = BinaryNode.valueOf(data, 1, 1);
        data[2] = (byte) 3;
        BinaryNode n2 = BinaryNode.valueOf(data, 2, 1);
        assertTrue(n.equals(n2));
        assertEquals("\"Aw==\"", n.toString());

        assertEquals("AAMD", new BinaryNode(data).asText());
        assertNodeNumbersForNonNumeric(n);
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testPOJO
    public void testPOJO()
    {
        POJONode n = new POJONode("x"); 
        assertStandardEquals(n);
        assertEquals(n, new POJONode("x"));
        assertEquals("x", n.asText());
        
        assertEquals("x", n.toString());

        assertEquals(new POJONode(null), new POJONode(null));

        
        assertNodeNumbersForNonNumeric(n);
        
        assertNodeNumbers(new POJONode(Integer.valueOf(123)), 123, 123.0);
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testRawValue
    public void testRawValue() throws Exception
    {
        ObjectNode root = MAPPER.createObjectNode();
        root.putRawValue("a", new RawValue(new SerializedString("[1, 2, 3]")));

        assertEquals("{\"a\":[1, 2, 3]}", MAPPER.writeValueAsString(root));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testCustomComparators
    public void testCustomComparators() throws Exception
    {
        ObjectNode nestedObject1 = MAPPER.createObjectNode();
        nestedObject1.put("value", 6);
        ArrayNode nestedArray1 = MAPPER.createArrayNode();
        nestedArray1.add(7);
        ObjectNode root1 = MAPPER.createObjectNode();
        root1.put("value", 5);
        root1.set("nested_object", nestedObject1);
        root1.set("nested_array", nestedArray1);

        ObjectNode nestedObject2 = MAPPER.createObjectNode();
        nestedObject2.put("value", 6.9);
        ArrayNode nestedArray2 = MAPPER.createArrayNode();
        nestedArray2.add(7.0);
        ObjectNode root2 = MAPPER.createObjectNode();
        root2.put("value", 5.0);
        root2.set("nested_object", nestedObject2);
        root2.set("nested_array", nestedArray2);

        
        assertFalse(root1.equals(root2));
        assertFalse(root2.equals(root1));
        assertTrue(root1.equals(root1));
        assertTrue(root2.equals(root2));

        assertTrue(nestedArray1.equals(nestedArray1));
        assertFalse(nestedArray1.equals(nestedArray2));
        assertFalse(nestedArray2.equals(nestedArray1));

        
        Comparator<JsonNode> cmp = new Comparator<JsonNode>() {

            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                if (o1 instanceof ContainerNode || o2 instanceof ContainerNode) {
                    fail("container nodes should be traversed, comparator should not be invoked");
                }
                if (o1.equals(o2)) {
                    return 0;
                }
                if ((o1 instanceof NumericNode) && (o2 instanceof NumericNode)) {
                    int d1 = ((NumericNode) o1).asInt();
                    int d2 = ((NumericNode) o2).asInt();
                    if (d1 == d2) { 
                        return 0;
                    }
                    if (d1 < d2) {
                        return -1;
                    }
                    return 1;
                }
                return 0;
            }
        };
        assertTrue(root1.equals(cmp, root2));
        assertTrue(root2.equals(cmp, root1));
        assertTrue(root1.equals(cmp, root1));
        assertTrue(root2.equals(cmp, root2));

        ArrayNode array3 = MAPPER.createArrayNode();
        array3.add(123);
        
        assertFalse(root2.equals(cmp, nestedArray1));
        assertTrue(nestedArray1.equals(cmp, nestedArray1));
        assertFalse(nestedArray1.equals(cmp, root2));
        assertFalse(nestedArray1.equals(cmp, array3));
    }

// com.fasterxml.jackson.databind.node.TestJsonNode::testArrayWithDefaultTyping
    public void testArrayWithDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .enableDefaultTyping();

        JsonNode array = mapper.readTree("[ 1, 2 ]");
        assertTrue(array.isArray());
        assertEquals(2, array.size());

        JsonNode obj = mapper.readTree("{ \"a\" : 2 }");
        assertTrue(obj.isObject());
        assertEquals(1, obj.size());
        assertEquals(2, obj.path("a").asInt());
    }

// com.fasterxml.jackson.databind.node.TestJsonPointer::testIt
    public void testIt() throws Exception
    {
        final JsonNode SAMPLE_ROOT = objectMapper().readTree(SAMPLE_DOC_JSON_SPEC);
        
        
        assertSame(SAMPLE_ROOT, SAMPLE_ROOT.at(JsonPointer.compile("")));

        
        assertTrue(SAMPLE_ROOT.at(JsonPointer.compile("/Image")).isObject());

        JsonNode n = SAMPLE_ROOT.at(JsonPointer.compile("/Image/Width"));
        assertTrue(n.isNumber());
        assertEquals(SAMPLE_SPEC_VALUE_WIDTH, n.asInt());

        
        assertEquals(SAMPLE_SPEC_VALUE_HEIGHT,
                SAMPLE_ROOT.at("/Image/Height").asInt());

        assertEquals(SAMPLE_SPEC_VALUE_TN_ID3,
                SAMPLE_ROOT.at(JsonPointer.compile("/Image/IDs/2")).asInt());

        
        assertTrue(SAMPLE_ROOT.at("/Image/Depth").isMissingNode());
        assertTrue(SAMPLE_ROOT.at("/Image/1").isMissingNode());
    }

// com.fasterxml.jackson.databind.node.TestJsonPointer::testLongNumbers
    public void testLongNumbers() throws Exception
    {
        
        
        JsonNode root = objectMapper().readTree("{\"123\" : 456}");
        JsonNode jn2 = root.at("/123"); 
        assertEquals(456, jn2.asInt());

        
        root = objectMapper().readTree("{\"35361706045\" : 1234}");
        jn2 = root.at("/35361706045"); 
        assertEquals(1234, jn2.asInt());
    }

// com.fasterxml.jackson.databind.node.TestMissingNode::testMissing
    public void testMissing()
    {
        MissingNode n = MissingNode.getInstance();
        assertTrue(n.isMissingNode());
        assertEquals(JsonToken.NOT_AVAILABLE, n.asToken());
        assertEquals("", n.asText());
        assertStandardEquals(n);
        assertEquals("", n.toString());

        assertNodeNumbersForNonNumeric(n);

        assertTrue(n.asBoolean(true));
        assertEquals(4, n.asInt(4));
        assertEquals(5L, n.asLong(5));
        assertEquals(0.25, n.asDouble(0.25));

        assertEquals("foo", n.asText("foo"));
    }

// com.fasterxml.jackson.databind.node.TestMissingNode::testMissingViaMapper
    public void testMissingViaMapper() throws Exception
    {
        String JSON = "[ { }, [ ] ]";
        JsonNode result = objectMapper().readTree(new StringReader(JSON));

        assertTrue(result.isContainerNode());
        assertTrue(result.isArray());
        assertEquals(2, result.size());

        int count = 0;
        for (JsonNode node : result) {
            ++count;
        }
        assertEquals(2, count);

        Iterator<JsonNode> it = result.iterator();

        JsonNode onode = it.next();
        assertTrue(onode.isContainerNode());
        assertTrue(onode.isObject());
        assertEquals(0, onode.size());
        assertFalse(onode.isMissingNode()); 
        assertNull(onode.textValue());

        
        assertNull(onode.get(0));
        JsonNode dummyNode = onode.path(0);
        assertNotNull(dummyNode);
        assertTrue(dummyNode.isMissingNode());
        assertNull(dummyNode.get(3));
        assertNull(dummyNode.get("whatever"));
        JsonNode dummyNode2 = dummyNode.path(98);
        assertNotNull(dummyNode2);
        assertTrue(dummyNode2.isMissingNode());
        JsonNode dummyNode3 = dummyNode.path("field");
        assertNotNull(dummyNode3);
        assertTrue(dummyNode3.isMissingNode());

        

        JsonNode anode = it.next();
        assertTrue(anode.isContainerNode());
        assertTrue(anode.isArray());
        assertFalse(anode.isMissingNode()); 
        assertEquals(0, anode.size());

        assertNull(anode.get(0));
        dummyNode = anode.path(0);
        assertNotNull(dummyNode);
        assertTrue(dummyNode.isMissingNode());
        assertNull(dummyNode.get(0));
        assertNull(dummyNode.get("myfield"));
        dummyNode2 = dummyNode.path(98);
        assertNotNull(dummyNode2);
        assertTrue(dummyNode2.isMissingNode());
        dummyNode3 = dummyNode.path("f");
        assertNotNull(dummyNode3);
        assertTrue(dummyNode3.isMissingNode());
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testBasicsWithNullNode
    public void testBasicsWithNullNode() throws Exception
    {
        
        NullNode n = NullNode.instance;

        
        assertFalse(n.isContainerNode());
        assertFalse(n.isBigDecimal());
        assertFalse(n.isBigInteger());
        assertFalse(n.isBinary());
        assertFalse(n.isBoolean());
        assertFalse(n.isPojo());
        assertFalse(n.isMissingNode());

        
        assertFalse(n.booleanValue());
        assertNull(n.numberValue());
        assertEquals(0, n.intValue());
        assertEquals(0L, n.longValue());
        assertEquals(BigDecimal.ZERO, n.decimalValue());
        assertEquals(BigInteger.ZERO, n.bigIntegerValue());

        assertEquals(0, n.size());
        assertFalse(n.elements().hasNext());
        assertFalse(n.fieldNames().hasNext());
        
        assertNotNull(n.path("xyz"));
        assertTrue(n.path("xyz").isMissingNode());

        assertFalse(n.has("field"));
        assertFalse(n.has(3));

        assertNodeNumbersForNonNumeric(n);

        
        assertEquals("foo", n.asText("foo"));
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullHandling
    public void testNullHandling() throws Exception
    {
        
        JsonNode n = objectReader().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());
        assertFalse(n.isNumber());
        assertFalse(n.isTextual());
        assertEquals("null", n.asText());
        assertEquals(n, NullNode.instance);

        n = objectMapper().readTree("null");
        assertNotNull(n);
        assertTrue(n.isNull());
        
        
        ObjectNode root = (ObjectNode) objectReader().readTree("{\"x\":null}");
        assertEquals(1, root.size());
        n = root.get("x");
        assertNotNull(n);
        assertTrue(n.isNull());
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullSerialization
    public void testNullSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, NullNode.instance);
        assertEquals("null", sw.toString());
    }

// com.fasterxml.jackson.databind.node.TestNullNode::testNullHandlingCovariance
    public void testNullHandlingCovariance() throws Exception
    {
        String JSON = "{\"object\" : null, \"array\" : null }";
        CovarianceBean bean = objectMapper().readValue(JSON, CovarianceBean.class);

        ObjectNode on = bean._object;
        assertNull(on);

        ArrayNode an = bean._array;
        assertNull(an);
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testObjectNodeEquality
    public void testObjectNodeEquality()
    {
        ObjectNode n1 = new ObjectNode(null);
        ObjectNode n2 = new ObjectNode(null);

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));

        n1.set("x", TextNode.valueOf("Test"));

        assertFalse(n1.equals(n2));
        assertFalse(n2.equals(n1));

        n2.set("x", TextNode.valueOf("Test"));

        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));
    }

// com.fasterxml.jackson.databind.node.TestTreeDeserialization::testReadFromString
    public void testReadFromString() throws Exception
    {
        String json = "{\"field\":\"{\\\"name\\\":\\\"John Smith\\\"}\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readValue(json, JsonNode.class);

        String generated = mapper.writeValueAsString( jNode);  
        JsonNode out = mapper.readValue( generated, JsonNode.class );   
        assertTrue(out.isObject());
        assertEquals(1, out.size());
        String value = out.path("field").asText();
        assertNotNull(value);
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testFromArray
    public void testFromArray() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();
        root.add(TEXT1);
        root.add(3);
        ObjectNode obj = root.addObject();
        obj.put(FIELD1, true);
        obj.putArray(FIELD2);
        root.add(false);

        
        for (int i = 0; i < 2; ++i) {
            StringWriter sw = new StringWriter();
            if (i == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            verifyFromArray(sw.toString());
        }
            
        
        verifyFromArray(root.toString());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testFromMap
    public void testFromMap()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put(FIELD4, TEXT2);
        root.put(FIELD3, -1);
        root.putArray(FIELD2);
        root.put(FIELD1, DOUBLE_VALUE);

        
        for (int i = 0; i < 2; ++i) {
            StringWriter sw = new StringWriter();
            if (i == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            verifyFromMap(sw.toString());
        }

        
        verifyFromMap(root.toString());
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testSmallNumbers
    public void testSmallNumbers()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();
        for (int i = -20; i <= 20; ++i) {
            JsonNode n = root.numberNode(i);
            root.add(n);
            
            assertEquals(String.valueOf(i), n.toString());
        }

        
        for (int type = 0; type < 2; ++type) {
            StringWriter sw = new StringWriter();
            if (type == 0) {
                JsonGenerator gen = new JsonFactory().createGenerator(sw);
                root.serialize(gen, null);
                gen.close();
            } else {
                mapper.writeValue(sw, root);
            }
            
            String doc = sw.toString();
            JsonParser p = new JsonFactory().createParser(new StringReader(doc));
            
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            for (int i = -20; i <= 20; ++i) {
                assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                assertEquals(i, p.getIntValue());
                assertEquals(""+i, p.getText());
            }
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
            p.close();
        }
    }

// com.fasterxml.jackson.databind.node.TestTreeMapperSerializer::testBinary
    public void testBinary() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        final int LENGTH = 13045;
        byte[] data = new byte[LENGTH];
        for (int i = 0; i < LENGTH; ++i) {
            data[i] = (byte) i;
        }
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, BinaryNode.valueOf(data));

        JsonParser p = new JsonFactory().createParser(sw.toString());
        
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertArrayEquals(data, p.getBinaryValue());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSimple
    public void testSimple() throws Exception
    {
        
        final String JSON =
            "{ \"a\" : 123, \"list\" : [ 12.25, null, true, { }, [ ] ] }";
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser p = tree.traverse();

        assertNull(p.getCurrentToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals("Expected START_OBJECT", JsonToken.START_OBJECT.asString(), p.getText());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals("a", p.getText());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(123, p.getIntValue());
        assertEquals("123", p.getText());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("list", p.getCurrentName());
        assertEquals("list", p.getText());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("list", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY.asString(), p.getText());

        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals(12.25, p.getDoubleValue(), 0);
        assertEquals("12.25", p.getText());

        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL.asString(), p.getText());

        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertNull(p.getCurrentName());
        assertTrue(p.getBooleanValue());
        assertEquals(JsonToken.VALUE_TRUE.asString(), p.getText());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());

        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertNull(p.nextToken());

        p.close();
        assertTrue(p.isClosed());
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testArray
    public void testArray() throws Exception
    {
        
        ObjectMapper m = new ObjectMapper();

        JsonParser p = m.readTree("[]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        p = m.readTree("[[]]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();

        p = m.readTree("[[ 12.1 ]]").traverse();
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testNested
    public void testNested() throws Exception
    {
        
        final String JSON =
            "{\"coordinates\":[[[-3,\n1],[179.859681,51.175092]]]}"
            ;
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(JSON);
        JsonParser p = tree.traverse();
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSpecDoc
    public void testSpecDoc() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree(SAMPLE_DOC_JSON_SPEC);
        JsonParser p = tree.traverse();
        verifyJsonSpecSampleDoc(p, true);
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryPojo
    public void testBinaryPojo() throws Exception
    {
        byte[] inputBinary = new byte[] { 1, 2, 100 };
        POJONode n = new POJONode(inputBinary);
        JsonParser p = n.traverse();

        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);
        Object pojo = p.getEmbeddedObject();
        assertSame(data, pojo);
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testBinaryNode
    public void testBinaryNode() throws Exception
    {
        byte[] inputBinary = new byte[] { 0, -5 };
        BinaryNode n = new BinaryNode(inputBinary);
        JsonParser p = n.traverse();

        assertNull(p.getCurrentToken());
        
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(inputBinary, data);

        
        assertEquals("APs=", p.getText());

        assertNull(p.nextToken());
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testTextAsBinary
    public void testTextAsBinary() throws Exception
    {
        TextNode n = new TextNode("   APs=\n");
        JsonParser p = n.traverse();
        assertNull(p.getCurrentToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        byte[] data = p.getBinaryValue();
        assertNotNull(data);
        assertArrayEquals(new byte[] { 0, -5 }, data);

        assertNull(p.nextToken());
        p.close();
        assertTrue(p.isClosed());

        
        n = new TextNode("?!??");
        p = n.traverse();
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        try {
            p.getBinaryValue();
        } catch (InvalidFormatException e) {
            verifyException(e, "Illegal character");
        }
        p.close();
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testDataBind
    public void testDataBind() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode tree = m.readTree
            ("{ \"name\" : \"Tatu\", \n"
             +"\"magicNumber\" : 42,"
             +"\"kids\" : [ \"Leo\", \"Lila\", \"Leia\" ] \n"
             +"}");
        Person tatu = m.treeToValue(tree, Person.class);
        assertNotNull(tatu);
        assertEquals(42, tatu.magicNumber);
        assertEquals("Tatu", tatu.name);
        assertNotNull(tatu.kids);
        assertEquals(3, tatu.kids.size());
        assertEquals("Leo", tatu.kids.get(0));
        assertEquals("Lila", tatu.kids.get(1));
        assertEquals("Leia", tatu.kids.get(2));
    }

// com.fasterxml.jackson.databind.node.TestTreeTraversingParser::testSkipChildrenWrt370
    public void testSkipChildrenWrt370() throws Exception
    {
        ObjectMapper o = new ObjectMapper();
        ObjectNode n = o.createObjectNode();
        n.putObject("inner").put("value", "test");
        n.putObject("unknown").putNull("inner");
        Jackson370Bean obj = o.readValue(n.traverse(), Jackson370Bean.class);
        assertNotNull(obj.inner);
        assertEquals("test", obj.inner.value);        
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueAsStringWithoutDefaultTyping
    public void testValueAsStringWithoutDefaultTyping() throws Exception {

        Foo foo = new Foo("baz");
        String json = MAPPER.writeValueAsString(foo);

        JsonNode jsonNode = MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueAsStringWithDefaultTyping
    public void testValueAsStringWithDefaultTyping() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Foo foo = new Foo("baz");
        String json = mapper.writeValueAsString(foo);

        JsonNode jsonNode = mapper.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testReadTreeWithDefaultTyping
    public void testReadTreeWithDefaultTyping() throws Exception
    {
        final String CLASS = Foo.class.getName();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        String json = "{\"@class\":\""+CLASS+"\",\"bar\":\"baz\"}";
        JsonNode jsonNode = mapper.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), "baz");
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueToTreeWithoutDefaultTyping
    public void testValueToTreeWithoutDefaultTyping() throws Exception {

        Foo foo = new Foo("baz");
        JsonNode jsonNode = MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testValueToTreeWithDefaultTyping
    public void testValueToTreeWithDefaultTyping() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Foo foo = new Foo("baz");
        JsonNode jsonNode = mapper.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }

// com.fasterxml.jackson.databind.node.TestTreeWithType::testIssue353
    public void testIssue353() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

         SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, "TEST", "TEST"));
         testModule.addDeserializer(SavedCookie.class, new SavedCookieDeserializer());
         mapper.registerModule(testModule);

         SavedCookie savedCookie = new SavedCookie("key", "v");
         String json = mapper.writeValueAsString(savedCookie);
         SavedCookie out = mapper.readerFor(SavedCookie.class).readValue(json);

         assertEquals("key", out.name);
         assertEquals("v", out.value);
    }

// com.fasterxml.jackson.databind.node.TextNodeTest::testText
    public void testText()
    {
        assertNull(TextNode.valueOf(null));
        TextNode empty = TextNode.valueOf("");
        assertStandardEquals(empty);
        assertSame(TextNode.EMPTY_STRING_NODE, empty);

        assertNodeNumbers(TextNode.valueOf("-3"), -3, -3.0);
        assertNodeNumbers(TextNode.valueOf("17.75"), 17, 17.75);
    
        long value = 127353264013893L;
        TextNode n = TextNode.valueOf(String.valueOf(value));
        assertEquals(value, n.asLong());
        
        
        n = TextNode.valueOf("foobar");
        assertNodeNumbersForNonNumeric(n);

        assertEquals("foobar", n.asText("barf"));
        assertEquals("", empty.asText("xyz"));

        assertTrue(TextNode.valueOf("true").asBoolean(true));
        assertTrue(TextNode.valueOf("true").asBoolean(false));
        assertFalse(TextNode.valueOf("false").asBoolean(true));
        assertFalse(TextNode.valueOf("false").asBoolean(false));
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testSimple
    public void testSimple() throws Exception
    {
        final String JSON = SAMPLE_DOC_JSON_SPEC;

        for (int type = 0; type < 2; ++type) {
            JsonNode result;

            if (type == 0) {
                result = objectMapper().readTree(new StringReader(JSON));
            } else {
                result = objectMapper().readTree(JSON);
            }

            assertType(result, ObjectNode.class);
            assertEquals(1, result.size());
            assertTrue(result.isObject());
            
            ObjectNode main = (ObjectNode) result;
            assertEquals("Image", main.fieldNames().next());
            JsonNode ob = main.elements().next();
            assertType(ob, ObjectNode.class);
            ObjectNode imageMap = (ObjectNode) ob;
            
            assertEquals(5, imageMap.size());
            ob = imageMap.get("Width");
            assertTrue(ob.isIntegralNumber());
            assertFalse(ob.isFloatingPointNumber());
            assertEquals(SAMPLE_SPEC_VALUE_WIDTH, ob.intValue());
            ob = imageMap.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_HEIGHT, ob.intValue());
            
            ob = imageMap.get("Title");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TITLE, ob.textValue());
            
            ob = imageMap.get("Thumbnail");
            assertType(ob, ObjectNode.class);
            ObjectNode tn = (ObjectNode) ob;
            ob = tn.get("Url");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_URL, ob.textValue());
            ob = tn.get("Height");
            assertTrue(ob.isIntegralNumber());
            assertEquals(SAMPLE_SPEC_VALUE_TN_HEIGHT, ob.intValue());
            ob = tn.get("Width");
            assertTrue(ob.isTextual());
            assertEquals(SAMPLE_SPEC_VALUE_TN_WIDTH, ob.textValue());
            
            ob = imageMap.get("IDs");
            assertTrue(ob.isArray());
            ArrayNode idList = (ArrayNode) ob;
            assertEquals(4, idList.size());
            assertEquals(4, calcLength(idList.elements()));
            assertEquals(4, calcLength(idList.iterator()));
            {
                int[] values = new int[] {
                    SAMPLE_SPEC_VALUE_TN_ID1,
                    SAMPLE_SPEC_VALUE_TN_ID2,
                    SAMPLE_SPEC_VALUE_TN_ID3,
                    SAMPLE_SPEC_VALUE_TN_ID4
                };
                for (int i = 0; i < values.length; ++i) {
                    assertEquals(values[i], idList.get(i).intValue());
                }
                int i = 0;
                for (JsonNode n : idList) {
                    assertEquals(values[i], n.intValue());
                    ++i;
                }
            }
        }
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testMixed
    public void testMixed() throws IOException
    {
        ObjectMapper om = new ObjectMapper();
        String JSON = "{\"node\" : { \"a\" : 3 }, \"x\" : 9 }";
        Bean bean = om.readValue(JSON, Bean.class);

        assertEquals(9, bean._x);
        JsonNode n = bean._node;
        assertNotNull(n);
        assertEquals(1, n.size());
        ObjectNode on = (ObjectNode) n;
        assertEquals(3, on.get("a").intValue());
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testEOF
    public void testEOF() throws Exception
    {
        String JSON =
            "{ \"key\": [ { \"a\" : { \"name\": \"foo\",  \"type\": 1\n"
            +"},  \"type\": 3, \"url\": \"http://www.google.com\" } ],\n"
            +"\"name\": \"xyz\", \"type\": 1, \"url\" : null }\n  "
            ;
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser(new StringReader(JSON));
        JsonNode result = objectMapper().readTree(p);

        assertTrue(result.isObject());
        assertEquals(4, result.size());

        assertNull(objectMapper().readTree(p));
        p.close();
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testMultiple
    public void testMultiple() throws Exception
    {
        String JSON = "12  \"string\" [ 1, 2, 3 ]";
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser(new StringReader(JSON));
        final ObjectMapper mapper = objectMapper();
        JsonNode result = mapper.readTree(p);

        assertTrue(result.isIntegralNumber());
        assertTrue(result.isInt());
        assertFalse(result.isTextual());
        assertEquals(12, result.intValue());

        result = mapper.readTree(p);
        assertTrue(result.isTextual());
        assertFalse(result.isIntegralNumber());
        assertFalse(result.isInt());
        assertEquals("string", result.textValue());

        result = mapper.readTree(p);
        assertTrue(result.isArray());
        assertEquals(3, result.size());

        assertNull(mapper.readTree(p));
        p.close();
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testNullFromEOFViaMapper
    public void testNullFromEOFViaMapper() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        assertNull(mapper.readTree(new StringReader("")));
        assertNull(mapper.readTree(new ByteArrayInputStream(new byte[0])));
    }

// com.fasterxml.jackson.databind.node.TreeReadViaMapperTest::testNullFromEOFViaObjectReader
    public void testNullFromEOFViaObjectReader() throws Exception
    {
        final ObjectMapper mapper = objectMapper();

        assertNull(mapper.readTree(new StringReader("")));
        assertNull(mapper.readTree(new ByteArrayInputStream(new byte[0])));
        assertNull(mapper.readerFor(JsonNode.class)
                .readTree(new StringReader("")));
        assertNull(mapper.readerFor(JsonNode.class)
                .readTree(new ByteArrayInputStream(new byte[0])));
    }

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1255
    public void testIssue1255() throws Exception
    {
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = MAPPER.writeValueAsString(mo);

        Foo result = MAPPER.readValue(json, Foo.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest::testIssue1607
    public void testIssue1607() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReallyAlwaysContainer());
        assertEquals(aposToQuotes("{'alwaysClass':1,'alwaysProp':2}"), json);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testStructJSOGRef
    public void testStructJSOGRef() throws Exception
    {
        IdentifiableExampleJSOG result = MAPPER.readValue(EXP_EXAMPLE_JSOG,
                IdentifiableExampleJSOG.class);
        assertEquals(66, result.foo);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testPolymorphicRoundTrip
    public void testPolymorphicRoundTrip() throws Exception
    {
        JSOGWrapper w = new JSOGWrapper(15);
        
        IdentifiableExampleJSOG ex = new IdentifiableExampleJSOG(123);
        ex.next = ex;
        w.jsog = ex;

        String json = MAPPER.writeValueAsString(w);

        JSOGWrapper out = MAPPER.readValue(json, JSOGWrapper.class);
        assertNotNull(out);
        assertEquals(15, out.value);
        assertTrue(out.jsog instanceof IdentifiableExampleJSOG);
        IdentifiableExampleJSOG jsog = (IdentifiableExampleJSOG) out.jsog;
        assertEquals(123, jsog.foo);
        assertSame(jsog, jsog.next);
    }

// com.fasterxml.jackson.databind.objectid.JSOGDeserialize622Test::testAlterativePolymorphicRoundTrip669
    public void testAlterativePolymorphicRoundTrip669() throws Exception
    {
        Outer outer = new Outer();
        outer.foo = "foo";
        outer.inner1 = outer.inner2 = new SubInner("bar", "extra");

        String jsog = MAPPER.writeValueAsString(outer);
        
        Outer back = MAPPER.readValue(jsog, Outer.class);

        assertSame(back.inner1, back.inner2);
    }

// com.fasterxml.jackson.databind.objectid.ObjectId687Test::testSerializeDeserializeWithCreator
    public void testSerializeDeserializeWithCreator() throws IOException {
        ReferredWithCreator base = new ReferredWithCreator("label1");
        ReferringToObjWithCreator r = new ReferringToObjWithCreator();
        r.addRef(base);
        EnclosingForRefsWithCreator e = new EnclosingForRefsWithCreator();
        e.baseRef = base;
        e.nextRef = r;

        String json = MAPPER.writeValueAsString(e);

        EnclosingForRefsWithCreator result = MAPPER.readValue(json,
                EnclosingForRefsWithCreator.class);
        assertNotNull(result);
        assertEquals(result.label, e.label);

        
        assertEquals(json, MAPPER.writeValueAsString(result));
    }

// com.fasterxml.jackson.databind.objectid.ObjectId687Test::testSerializeDeserializeNoCreator
    public void testSerializeDeserializeNoCreator() throws IOException {
        ReferredWithNoCreator base = new ReferredWithNoCreator();
        ReferringToObjWithNoCreator r = new ReferringToObjWithNoCreator();
        r.addRef(base);
        EnclosingForRefWithNoCreator e = new EnclosingForRefWithNoCreator();
        e.baseRef = base;
        e.nextRef = r;

        String json = MAPPER.writeValueAsString(e);

        EnclosingForRefWithNoCreator result = MAPPER.readValue(json,
                EnclosingForRefWithNoCreator.class);
        assertNotNull(result);
        assertEquals(result.label, e.label);

        
        assertEquals(json, MAPPER.writeValueAsString(result));
    }

// com.fasterxml.jackson.databind.objectid.ObjectId825BTest::testFull825
    public void testFull825() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String INPUT = aposToQuotes(
"{\n"+
"    '@class': '_PKG_CTC',\n"+
"     'var': [{\n"+
"      'ch': {\n"+
"        '@class': '_PKG_Ch',\n"+
"         'act': [{\n"+
"            '@class': '_PKG_CTD',\n"+
"            'oidString': 'oid1',\n"+
"            'dec': [{\n"+
"              '@class': '_PKG_Dec',\n"+
"                'oidString': 'oid2',\n"+
"                'outTr': [{\n"+
"                  '@class': '_PKG_Tr',\n"+
"                  'target': {\n"+
"                    '@class': '_PKG_Ti',\n"+
"                    'oidString': 'oid3',\n"+
"                    'timer': 'problemoid',\n"+
"                    'outTr': [{\n"+
"                      '@class': '_PKG_Tr',\n"+
"                      'target': {\n"+
"                        '@class': '_PKG_Ti',\n"+
"                        'oidString': 'oid4',\n"+
"                        'timer': {\n"+
"                          '@class': '_PKG_V',\n"+
"                          'oidString': 'problemoid'\n"+
"                        }\n"+
"                      }\n"+
"                    }]\n"+
"                  }\n"+
"                }]\n"+
"              }]\n"+
"         }],\n"+
"         'oidString': 'oid5'\n"+
"      },\n"+
"       '@class': '_PKG_CTV',\n"+
"       'oidString': 'oid6',\n"+
"       'locV': ['problemoid']\n"+
"    }],\n"+
"     'oidString': 'oid7'\n"+
"}\n"
                );

        
        final String newPkg = getClass().getName() + "\\$";
        INPUT = INPUT.replaceAll("_PKG_", newPkg);
        
        CTC result = mapper.readValue(INPUT, CTC.class);
        assertNotNull(result);
    }

// com.fasterxml.jackson.databind.objectid.ObjectId825Test::testDeserialize
    public void testDeserialize() throws Exception {
        TestA a = new TestA();
        a.oidString = "oidA";

        TestC c = new TestC();
        c.oidString = "oidC";

        a.testAbst = c;

        TestD d = new TestD();
        d.oidString = "oidD";

        c.d = d;
        a.d = d;

        String json = DEF_TYPING_MAPPER.writeValueAsString(a);

        TestA testADeserialized = DEF_TYPING_MAPPER.readValue(json, TestA.class);

        assertNotNull(testADeserialized);
        assertNotNull(testADeserialized.d);
        assertEquals("oidD", testADeserialized.d.oidString);
    }

// com.fasterxml.jackson.databind.objectid.ObjectIdReordering1388Test::testDeserializationFinalClassJSOG
    public void testDeserializationFinalClassJSOG() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        final UUID id = UUID.fromString("a59aa02c-fe3c-43f8-9b5a-5fe01878a818");
        final NamedThing thing = new NamedThing(id, "Hello");

        final TypeReference<?> namedThingListType = new TypeReference<List<NamedThing>>() { };

        {
            final String jsog = mapper.writeValueAsString(Arrays.asList(thing, thing, thing));
            final List<NamedThing> list = mapper.readValue(jsog, namedThingListType);
            _assertAllSame(list);
            
            assertTrue(jsog.equals("[{\"@id\":1,\"id\":\"a59aa02c-fe3c-43f8-9b5a-5fe01878a818\",\"name\":\"Hello\"},1,1]"));
        }

        
        
        {
            final String json = "[1,1,{\"@id\":1,\"id\":\"a59aa02c-fe3c-43f8-9b5a-5fe01878a818\",\"name\":\"Hello\"}]";
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }

        
        {
            final String json = aposToQuotes("[{'id':'a59aa02c-fe3c-43f8-9b5a-5fe01878a818','@id':1,'name':'Hello'}, 1, 1]");
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }

        
        {
            final String json = aposToQuotes("[{'id':'a59aa02c-fe3c-43f8-9b5a-5fe01878a818','name':'Hello','@id':1}, 1, 1]");
            final List<NamedThing> forward = mapper.readValue(json, namedThingListType);
            _assertAllSame(forward);
        }
    }

// com.fasterxml.jackson.databind.objectid.ObjectWithCreator1261Test::testObjectIds1261
    public void testObjectIds1261() throws Exception
    {
         ObjectMapper mapper = new ObjectMapper();
         mapper.enable(SerializationFeature.INDENT_OUTPUT);
         mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

         Answer initialAnswer = createInitialAnswer();
         String initialAnswerString = mapper.writeValueAsString(initialAnswer);

         JsonNode tree = mapper.readTree(initialAnswerString);
         Answer deserializedAnswer = mapper.readValue(initialAnswerString,
               Answer.class);
         String reserializedAnswerString = mapper
               .writeValueAsString(deserializedAnswer);
         JsonNode newTree = mapper.readTree(reserializedAnswerString);
         if (!tree.equals(newTree)) {
                  fail("Original and recovered Json are different. Recovered = \n"
                        + reserializedAnswerString + "\n");
         }
   }

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testWithAbstractUsingProp
    public void testWithAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerViaProp v1 = new VehicleOwnerViaProp();
        v1.ownedVehicle = c;
        VehicleOwnerViaProp v2 = new VehicleOwnerViaProp();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writer()
                .writeValueAsString(new VehicleOwnerViaProp[] { v1, v2 });
        

        VehicleOwnerViaProp[] deserialized = objectMapper.readValue(serialized, VehicleOwnerViaProp[].class);
        assertEquals(2, deserialized.length);
        assertSame(deserialized[0].ownedVehicle, deserialized[1].ownedVehicle);
    }

// com.fasterxml.jackson.databind.objectid.PolymorphicWithObjectId1551Test::testFailingAbstractUsingProp
    public void testFailingAbstractUsingProp() throws Exception
    {
        Car c = new Car();
        c.vehicleId = "123";
        c.numberOfDoors = 2;
        
        VehicleOwnerBroken v1 = new VehicleOwnerBroken();
        v1.ownedVehicle = c;
        VehicleOwnerBroken v2 = new VehicleOwnerBroken();
        v2.ownedVehicle = c;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writer()
                .writeValueAsString(new VehicleOwnerBroken[] { v1, v2 });
        } catch (InvalidDefinitionException e) {
            
            assertEquals(Car.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }

        
        final String JSON = aposToQuotes(
"[{'ownedVehicle':{'@class':'com.fasterxml.jackson.failing.PolymorphicWithObjectId1551Test$Car','vehicleId':'123',"
+"'numberOfDoors':2}},{'ownedVehicle':'123'}]"
                );
        try {
            objectMapper.readValue(JSON, VehicleOwnerBroken[].class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            assertEquals(Vehicle.class, e.getType().getRawClass());
            verifyException(e, "Invalid Object Id definition");
            verifyException(e, "cannot find property with name 'bogus'");
        }
    }

// com.fasterxml.jackson.databind.objectid.ReferentialWithObjectIdTest::testAtomicWithObjectId
    public void testAtomicWithObjectId() throws Exception
    {
        Employee first = new Employee();
        first.id = 1;
        first.name = "Alice";

        Employee second = new Employee();
        second.id = 2;
        second.name = "Bob";

        first.next(second);
        second.next(first);

        EmployeeList input = new EmployeeList();
        input.first = new AtomicReference<Employee>(first);

        String json = MAPPER.writeValueAsString(input);

        
 
        EmployeeList result = MAPPER.readValue(json, EmployeeList.class);
        Employee firstB = result.first.get();
        assertNotNull(firstB);
        assertEquals("Alice", firstB.name);
        Employee secondB = firstB.next.get();
        assertNotNull(secondB);
        assertEquals("Bob", secondB.name);
        assertNotNull(secondB.next.get());
        assertSame(firstB, secondB.next.get());
    }

// com.fasterxml.jackson.databind.objectid.TestAbstractWithObjectId::testIssue877
    public void testIssue877() throws Exception
    {
        
        BaseInterfaceImpl one = new BaseInterfaceImpl();
        BaseInterfaceImpl two = new BaseInterfaceImpl();

        
        one.addInstance(two);
        two.addInstance(one);

        
        ListWrapper<BaseInterfaceImpl> myList = new ListWrapper<BaseInterfaceImpl>();
        myList.add(one);
        myList.add(two);

        
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        
        String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(myList);
        ListWrapper<BaseInterfaceImpl> result;
        
        result = om.readValue(json, new TypeReference<ListWrapper<BaseInterfaceImpl>>() { });

        assertNotNull(result);
        
        assertEquals(2, result.size());
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testColumnMetadata
    public void testColumnMetadata() throws Exception
    {
        ColumnMetadata col = new ColumnMetadata("Billy", "employee", "comment");
        Wrapper w = new Wrapper();
        w.a = col;
        w.b = col;
        String json = MAPPER.writeValueAsString(w);
        
        Wrapper deserialized = MAPPER.readValue(json, Wrapper.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.a);
        assertNotNull(deserialized.b);
        
        assertEquals("Billy", deserialized.a.getName());
        assertEquals("employee", deserialized.a.getType());
        assertEquals("comment", deserialized.a.getComment());

        assertSame(deserialized.a, deserialized.b);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testMixedRefsIssue188
    public void testMixedRefsIssue188() throws Exception
    {
        Company comp = new Company();
        Employee e1 = new Employee(1, "First", null);
        Employee e2 = new Employee(2, "Second", e1);
        e1.addReport(e2);
        comp.add(e1);
        comp.add(e2);

        String json = MAPPER.writeValueAsString(comp);
        
        assertEquals("{\"employees\":["
                +"{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                +"{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                +"]}",
                json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testObjectAndTypeId
    public void testObjectAndTypeId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();

        Bar inputRoot = new Bar();
        Foo inputChild = new Foo();
        inputRoot.next = inputChild;
        inputChild.ref = inputRoot;

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(inputRoot);
        
        BaseEntity resultRoot = mapper.readValue(json, BaseEntity.class);
        assertNotNull(resultRoot);
        assertTrue(resultRoot instanceof Bar);
        Bar first = (Bar) resultRoot;

        assertNotNull(first.next);
        assertTrue(first.next instanceof Foo);
        Foo second = (Foo) first.next;
        assertNotNull(second.ref);
        assertSame(first, second.ref);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectId::testWithFieldsInBaseClass1083
    public void testWithFieldsInBaseClass1083() throws Exception {
          final String json = aposToQuotes("{'schemas': [{\n"
              + "  'name': 'FoodMart'\n"
              + "}]}\n");
          MAPPER.readValue(json, JsonRoot.class);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserializationClass
    public void testSimpleDeserializationClass() throws Exception
    {
        
        Identifiable result = MAPPER.readValue(EXP_SIMPLE_INT_CLASS, Identifiable.class);
        assertEquals(13, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testMissingObjectId
    public void testMissingObjectId() throws Exception
    {
        Identifiable result = MAPPER.readValue(aposToQuotes("{'value':28, 'next':{'value':29}}"),
                Identifiable.class);
        assertNotNull(result);
        assertEquals(28, result.value);
        assertNotNull(result.next);
        assertEquals(29, result.next.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleUUIDForClassRoundTrip
    public void testSimpleUUIDForClassRoundTrip() throws Exception
    {
        UUIDNode root = new UUIDNode(1);
        UUIDNode child1 = new UUIDNode(2);
        UUIDNode child2 = new UUIDNode(3);
        root.first = child1;
        root.second = child2;
        child1.parent = root;
        child2.parent = root;
        child1.first = child2;

        String json = MAPPER.writeValueAsString(root);

        
        UUIDNode result = MAPPER.readValue(json, UUIDNode.class);
        assertEquals(1, result.value);
        UUIDNode result2 = result.first;
        UUIDNode result3 = result.second;
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(2, result2.value);
        assertEquals(3, result3.value);

        assertSame(result, result2.parent);
        assertSame(result, result3.parent);
        assertSame(result3, result2.first);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserializationProperty
    public void testSimpleDeserializationProperty() throws Exception
    {
        IdWrapper result = MAPPER.readValue(EXP_SIMPLE_INT_PROP, IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testSimpleDeserWithForwardRefs
    public void testSimpleDeserWithForwardRefs() throws Exception
    {
        IdWrapper result = MAPPER.readValue("{\"node\":{\"value\":7,\"next\":{\"node\":1}, \"@id\":1}}"
                ,IdWrapper.class);
        assertEquals(7, result.node.value);
        assertSame(result.node, result.node.next.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReference
    public void testForwardReference()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":2,\"reports\":[]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":null,\"reports\":[1]}"
                      + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEquals(1, firstEmployee.id);
        assertEquals(2, secondEmployee.id);
        assertEquals(secondEmployee, firstEmployee.manager); 
        assertEquals(firstEmployee, secondEmployee.reports.get(0)); 
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceInCollection
    public void testForwardReferenceInCollection()
        throws Exception
    {
        String json = "{\"employees\":["
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(2, company.employees.size());
        Employee firstEmployee = company.employees.get(0);
        Employee secondEmployee = company.employees.get(1);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceInMap
    public void testForwardReferenceInMap()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"2\": 2,"
                      + "\"3\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = MAPPER.readValue(json, MappedCompany.class);
        assertEquals(3, company.employees.size());
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(3);
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testForwardReferenceAnySetterCombo
    public void testForwardReferenceAnySetterCombo() throws Exception {
        String json = "{\"@id\":1, \"foo\":2, \"bar\":{\"@id\":2, \"foo\":1}}";
        AnySetterObjectId value = MAPPER.readValue(json, AnySetterObjectId.class);
        assertSame(value.values.get("bar"), value.values.get("foo"));
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testUnresolvedForwardReference
    public void testUnresolvedForwardReference()
        throws Exception
    {
        String json = "{\"employees\":[" 
                      + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[3]},"
                      + "{\"id\":2,\"name\":\"Second\",\"manager\":3,\"reports\":[]}" 
                      + "]}";
        try {
            MAPPER.readValue(json, Company.class);
            fail("Should have thrown.");
        } catch (UnresolvedForwardReference exception) {
            
            List<UnresolvedId> unresolvedIds = exception.getUnresolvedIds();
            assertEquals(2, unresolvedIds.size());
            UnresolvedId firstUnresolvedId = unresolvedIds.get(0);
            assertEquals(3, firstUnresolvedId.getId());
            assertEquals(Employee.class, firstUnresolvedId.getType());
            UnresolvedId secondUnresolvedId = unresolvedIds.get(1);
            assertEquals(firstUnresolvedId.getId(), secondUnresolvedId.getId());
            assertEquals(Employee.class, secondUnresolvedId.getType());
        }
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testUnresolvableAsNull
    public void testUnresolvableAsNull() throws Exception
    {
        IdWrapper w = MAPPER.readerFor(IdWrapper.class)
                .without(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)
                .readValue(aposToQuotes("{'node':123}"));
        assertNotNull(w);
        assertNull(w.node);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testKeepCollectionOrdering
    public void testKeepCollectionOrdering() throws Exception
    {
        String json = "{\"employees\":[2,1,"
                + "{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                + "{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                + "]}";
        Company company = MAPPER.readValue(json, Company.class);
        assertEquals(4, company.employees.size());
        
        Employee firstEmployee = company.employees.get(1);
        Employee secondEmployee = company.employees.get(0);
        assertSame(firstEmployee, company.employees.get(2));
        assertSame(secondEmployee, company.employees.get(3));
        assertEmployees(firstEmployee, secondEmployee);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testKeepMapOrdering
    public void testKeepMapOrdering()
        throws Exception
    {
        String json = "{\"employees\":{"
                      + "\"1\":2, \"2\":1,"
                      + "\"3\":{\"id\":1,\"name\":\"First\",\"manager\":null,\"reports\":[2]},"
                      + "\"4\":{\"id\":2,\"name\":\"Second\",\"manager\":1,\"reports\":[]}"
                      + "}}";
        MappedCompany company = MAPPER.readValue(json, MappedCompany.class);
        assertEquals(4, company.employees.size());
        Employee firstEmployee = company.employees.get(2);
        Employee secondEmployee = company.employees.get(1);
        assertEmployees(firstEmployee, secondEmployee);
        
        
        Iterator<Entry<Integer,Employee>> iterator = company.employees.entrySet().iterator();
        assertSame(secondEmployee, iterator.next().getValue());
        assertSame(firstEmployee, iterator.next().getValue());
        assertSame(firstEmployee, iterator.next().getValue());
        assertSame(secondEmployee, iterator.next().getValue());
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomDeserializationClass
    public void testCustomDeserializationClass() throws Exception
    {
        
        IdentifiableCustom result = MAPPER.readValue(EXP_CUSTOM_VIA_CLASS, IdentifiableCustom.class);
        assertEquals(-900, result.value);
        assertSame(result, result.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomDeserializationProperty
    public void testCustomDeserializationProperty() throws Exception
    {
        
        IdWrapperExt result = MAPPER.readValue(EXP_CUSTOM_VIA_PROP, IdWrapperExt.class);
        assertEquals(99, result.node.value);
        assertSame(result.node, result.node.next.node);
        assertEquals(3, result.node.customId);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testCustomPoolResolver
    public void testCustomPoolResolver() throws Exception
    {
        Map<Object,WithCustomResolution> pool = new HashMap<Object,WithCustomResolution>();
        pool.put(1, new WithCustomResolution(1, 1));
        pool.put(2, new WithCustomResolution(2, 2));
        pool.put(3, new WithCustomResolution(3, 3));
        pool.put(4, new WithCustomResolution(4, 4));
        pool.put(5, new WithCustomResolution(5, 5));
        ContextAttributes attrs = MAPPER.getDeserializationConfig().getAttributes().withSharedAttribute(POOL_KEY, pool);
        String content = "{\"data\":[1,2,3,4,5]}";
        CustomResolutionWrapper wrapper = MAPPER.readerFor(CustomResolutionWrapper.class).with(attrs).readValue(content);
        assertFalse(wrapper.data.isEmpty());
        for (WithCustomResolution ob : wrapper.data) {
            assertSame(pool.get(ob.id), ob);
        }
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdDeserialization::testNullObjectId
    public void testNullObjectId() throws Exception
    {
        
        
        Identifiable value = MAPPER.readValue
                (aposToQuotes("{'value':3, 'next':null, 'id':null}"), Identifiable.class);
        assertNotNull(value);
        assertEquals(3, value.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSimpleSerializationClass
    public void testSimpleSerializationClass() throws Exception
    {
        Identifiable src = new Identifiable(13);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_CLASS, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSimpleSerializationProperty
    public void testSimpleSerializationProperty() throws Exception
    {
        IdWrapper src = new IdWrapper(7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_SIMPLE_INT_PROP, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testEmptyObjectWithId
    public void testEmptyObjectWithId() throws Exception
    {
        final ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new EmptyObject());
        assertEquals(aposToQuotes("{'@id':1}"), json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testSerializeWithOpaqueStringId
    public void testSerializeWithOpaqueStringId() throws Exception
    {
        StringIdentifiable ob1 = new StringIdentifiable(12);
        StringIdentifiable ob2 = new StringIdentifiable(34);
        ob1.next = ob2;
        ob2.next = ob1;

        
        String json = MAPPER.writeValueAsString(ob1);
        assertNotNull(json);

        
        StringIdentifiable output = MAPPER.readValue(json, StringIdentifiable.class);
        assertNotNull(output);
        assertEquals(12, output.value);
        assertNotNull(output.next);
        assertEquals(34, output.next.value);
        assertSame(output.next.next, output);

        String json2 = aposToQuotes("{'id':'foobar','value':3, 'next':{'id':'barf','value':5,'next':'foobar'}}");
        output = MAPPER.readValue(json2, StringIdentifiable.class);
        assertNotNull(output);
        assertEquals(3, output.value);
        assertNotNull(output.next);
        assertEquals(5, output.next.value);
        assertSame(output.next.next, output);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testCustomPropertyForClass
    public void testCustomPropertyForClass() throws Exception
    {
        IdentifiableWithProp src = new IdentifiableWithProp(123, -19);
        src.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);

        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testCustomPropertyViaProperty
    public void testCustomPropertyViaProperty() throws Exception
    {
        IdWrapperCustom src = new IdWrapperCustom(123, 7);
        src.node.next = src;
        
        
        String json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
        
        json = MAPPER.writeValueAsString(src);
        assertEquals(EXP_CUSTOM_PROP_VIA_REF, json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testAlwaysAsId
    public void testAlwaysAsId() throws Exception
    {
        String json = MAPPER.writeValueAsString(new AlwaysContainer());
        assertEquals("{\"a\":1,\"b\":2}", json);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testAlwaysIdForTree
    public void testAlwaysIdForTree() throws Exception
    {
        TreeNode root = new TreeNode(null, 1, "root");     
        TreeNode leaf = new TreeNode(root, 2, "leaf");
        root.child = leaf;
        String json = MAPPER.writeValueAsString(root);

        assertEquals("{\"id\":1,\"name\":\"root\",\"parent\":null,\"child\":"
                +"{\"id\":2,\"name\":\"leaf\",\"parent\":1,\"child\":null}}",
                json);
        		
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testNullStringPropertyId
    public void testNullStringPropertyId() throws Exception
    {
        IdentifiableStringId value = MAPPER.readValue
                (aposToQuotes("{'value':3, 'next':null, 'id':null}"), IdentifiableStringId.class);
        assertNotNull(value);
        assertEquals(3, value.value);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdSerialization::testInvalidProp
    public void testInvalidProp() throws Exception
    {
        try {
            MAPPER.writeValueAsString(new Broken());
            fail("Should have thrown an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "cannot find property with name 'id'");
        }
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testSimpleEquals
    public void testSimpleEquals() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        
        assertFalse(mapper.isEnabled(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID));
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        Foo foo = new Foo(1);

        Bar bar1 = new Bar(1);
        Bar bar2 = new Bar(2);
        
        
        
        Bar anotherBar1 = new Bar(1);

        foo.bars.add(bar1);
        foo.bars.add(bar2);
        
        foo.otherBars.add(anotherBar1);
        foo.otherBars.add(bar2);

        String json = mapper.writeValueAsString(foo);
        assertEquals("{\"id\":1,\"bars\":[{\"id\":1},{\"id\":2}],\"otherBars\":[1,2]}", json);

        Foo foo2 = mapper.readValue(json, Foo.class);       
        assertNotNull(foo2);
        assertEquals(foo.id, foo2.id);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithEquals::testEqualObjectIdsExternal
    public void testEqualObjectIdsExternal() throws Exception
    {
        Element element = new Element();
        element.uri = URI.create("URI");
        element.name = "Element1";

        Element element2 = new Element();
        element2.uri = URI.create("URI");
        element2.name = "Element2";

        
        

        List<Element> input = Arrays.asList(element, element2);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);

        String json = mapper.writerFor(new TypeReference<List<Element>>() { })
                .writeValueAsString(input);

        Element[] output = mapper.readValue(json, Element[].class);
        assertNotNull(output);
        assertEquals(2, output.length);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithInjectables538::testWithInjectables538
    public void testWithInjectables538() throws Exception
    {
        A a = new A("a");
        B b = new B("b");
        a.b = b;
        b.a = a;

        String json = MAPPER.writeValueAsString(a);

        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("i1", "e1");
        inject.addValue("i2", "e2");
        A output = null;

        try {
            output = MAPPER.reader(inject).forType(A.class).readValue(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize from JSON '"+json+"'", e);
        }
        assertNotNull(output);
        assertNotNull(output.b);
        assertSame(output, output.b.a);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testPolymorphicRoundtrip
    public void testPolymorphicRoundtrip() throws Exception
    {
        
        Impl in1 = new Impl(123, 456);
        in1.next = new Impl(111, 222);
        in1.next.next = in1;
        
        String json = mapper.writeValueAsString(in1);
        
        
        Base result0 = mapper.readValue(json, Base.class);
        assertNotNull(result0);
        assertSame(Impl.class, result0.getClass());
        Impl result = (Impl) result0;
        assertEquals(123, result.value);
        assertEquals(456, result.extra);
        Impl result2 = (Impl) result.next;
        assertEquals(111, result2.value);
        assertEquals(222, result2.extra);
        assertSame(result, result2.next);
    }

// com.fasterxml.jackson.databind.objectid.TestObjectIdWithPolymorphic::testIssue811
    public void testIssue811() throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@class");
    
        Process p = new Process();
        Scope s = new Scope(p, null);
        FaultHandler fh = new FaultHandler(p);
        Catch c = new Catch(p, s);
        fh.catchBlocks.add(c);
        s.faultHandlers.add(fh);
        
        String json = om.writeValueAsString(p);
        Process restored = om.readValue(json, Process.class);
        assertNotNull(restored);

        assertEquals(0, p.id);
        assertEquals(3, p.children.size());
        assertSame(p, p.children.get(0).owner);
        assertSame(p, p.children.get(1).owner);
        assertSame(p, p.children.get(2).owner);
    }

// com.fasterxml.jackson.databind.seq.PolyMapWriter827Test::testPolyCustomKeySerializer
    public void testPolyCustomKeySerializer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        mapper.registerModule(new SimpleModule("keySerializerModule")
            .addKeySerializer(CustomKey.class, new CustomKeySerializer()));

        Map<CustomKey, String> map = new HashMap<CustomKey, String>();
        CustomKey key = new CustomKey();
        key.a = "foo";
        key.b = 1;
        map.put(key, "bar");

        final ObjectWriter writer = mapper.writerFor(new TypeReference<Map<CustomKey,String>>() { });
        String json = writer.writeValueAsString(map);
        Assert.assertEquals("[\"java.util.HashMap\",{\"foo,1\":\"bar\"}]", json);
    }

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testRootBeans
    public void testRootBeans() throws Exception
    {
        final String JSON = aposToQuotes("{'a':3} {'x':5}");
        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        
        assertTrue(it.hasNextValue());
        Bean bean = it.nextValue();
        assertEquals(3, bean.a);
        
        try {
            bean = it.nextValue();
            fail("Should not have succeeded");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"x\"");
        }
        
        assertFalse(it.hasNextValue());

        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testSimpleRootRecovery
    public void testSimpleRootRecovery() throws Exception
    {
        final String JSON = aposToQuotes("{'a':3}{'a':27,'foo':[1,2],'b':{'x':3}}  {'a':1,'b':2} ");

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        Bean bean = it.nextValue();

        assertNotNull(bean);
        assertEquals(3, bean.a);

        
        try {
            it.nextValue();
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"foo\"");
        }

        
        bean = it.nextValue();
        assertNotNull(bean);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);

        assertFalse(it.hasNextValue());
        
        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadRecoveryTest::testSimpleArrayRecovery
    public void testSimpleArrayRecovery() throws Exception
    {
        final String JSON = aposToQuotes("[{'a':3},{'a':27,'foo':[1,2],'b':{'x':3}}  ,{'a':1,'b':2}  ]");

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        Bean bean = it.nextValue();

        assertNotNull(bean);
        assertEquals(3, bean.a);

        
        try {
            it.nextValue();
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field \"foo\"");
        }

        
        bean = it.nextValue();
        assertNotNull(bean);
        assertEquals(1, bean.a);
        assertEquals(2, bean.b);

        assertFalse(it.hasNextValue());
        
        it.close();
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeans
    public void testRootBeans() throws Exception
    {
        for (Source src : Source.values()) {
            _testRootBeans(src);
        }
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeansInArray
    public void testRootBeansInArray() throws Exception
    {
        final String JSON = "[{\"a\":6}, {\"a\":-7}]";

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);

        assertNotNull(it.getCurrentLocation());
        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(6, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(-7, b.a);
        assertFalse(it.hasNext());
        it.close();

        
        it = MAPPER.readerFor(Bean.class).readValues(JSON);
        List<Bean> all = it.readAll();
        assertEquals(2, all.size());
        it.close();

        it = MAPPER.readerFor(Bean.class).readValues("[{\"a\":4},{\"a\":4}]");
        Set<Bean> set = it.readAll(new HashSet<Bean>());
        assertEquals(HashSet.class, set.getClass());
        assertEquals(1, set.size());
        assertEquals(4, set.iterator().next().a);
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootMaps
    public void testRootMaps() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        Iterator<Map<?,?>> it = MAPPER.readerFor(Map.class).readValues(JSON);

        assertNotNull(((MappingIterator<?>) it).getCurrentLocation());
        assertTrue(it.hasNext());
        Map<?,?> map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        assertTrue(it.hasNext());
        assertNotNull(((MappingIterator<?>) it).getCurrentLocation());
        map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(27), map.get("a"));
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootBeansWithParser
    public void testRootBeansWithParser() throws Exception
    {
        final String JSON = "{\"a\":3}{\"a\":27}  ";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        
        Iterator<Bean> it = jp.readValuesAs(Bean.class);

        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testRootArraysWithParser
    public void testRootArraysWithParser() throws Exception
    {
        final String JSON = "[1][3]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        Iterator<int[]> it = MAPPER.readerFor(int[].class).readValues(jp);
        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testHasNextWithEndArray
    public void testHasNextWithEndArray() throws Exception {
        final String JSON = "[1,3]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);

        
        
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        jp.nextToken();
        
        Iterator<Integer> it = MAPPER.readerFor(Integer.class).readValues(jp);
        assertTrue(it.hasNext());
        int value = it.next();
        assertEquals(1, value);
        assertTrue(it.hasNext());
        value = it.next();
        assertEquals(3, value);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testHasNextWithEndArrayManagedParser
    public void testHasNextWithEndArrayManagedParser() throws Exception {
        final String JSON = "[1,3]";

        Iterator<Integer> it = MAPPER.readerFor(Integer.class).readValues(JSON);
        assertTrue(it.hasNext());
        int value = it.next();
        assertEquals(1, value);
        assertTrue(it.hasNext());
        value = it.next();
        assertEquals(3, value);
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootBeans
    public void testNonRootBeans() throws Exception
    {
        final String JSON = "{\"leaf\":[{\"a\":3},{\"a\":27}]}";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        
        
        
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        
        Iterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(jp);

        assertTrue(it.hasNext());
        Bean b = it.next();
        assertEquals(3, b.a);
        assertTrue(it.hasNext());
        b = it.next();
        assertEquals(27, b.a);
        assertFalse(it.hasNext());
        jp.close();
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootMapsWithParser
    public void testNonRootMapsWithParser() throws Exception
    {
        final String JSON = "[{\"a\":3},{\"a\":27}]";
        JsonParser jp = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());

        
        
        
        jp.clearCurrentToken();
        
        Iterator<Map<?,?>> it = MAPPER.readerFor(Map.class).readValues(jp);

        assertTrue(it.hasNext());
        Map<?,?> map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(3), map.get("a"));
        assertTrue(it.hasNext());
        map = it.next();
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(27), map.get("a"));
        assertFalse(it.hasNext());
        jp.close();
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootMapsWithObjectReader
    public void testNonRootMapsWithObjectReader() throws Exception
    {
        String JSON = "[{ \"hi\": \"ho\", \"neighbor\": \"Joe\" },\n"
            +"{\"boy\": \"howdy\", \"huh\": \"what\"}]";
        final MappingIterator<Map<String, Object>> iterator = MAPPER
                .reader()
                .forType(new TypeReference<Map<String, Object>>(){})
                .readValues(JSON);

        Map<String,Object> map;
        assertTrue(iterator.hasNext());
        map = iterator.nextValue();
        assertEquals(2, map.size());
        assertTrue(iterator.hasNext());
        map = iterator.nextValue();
        assertEquals(2, map.size());
        assertFalse(iterator.hasNext());
    }

// com.fasterxml.jackson.databind.seq.ReadValuesTest::testNonRootArraysUsingParser
    public void testNonRootArraysUsingParser() throws Exception
    {
        final String JSON = "[[1],[3]]";
        JsonParser p = MAPPER.getFactory().createParser(JSON);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        
        
        
        
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        
        Iterator<int[]> it = MAPPER.readValues(p, int[].class);

        assertTrue(it.hasNext());
        int[] array = it.next();
        assertEquals(1, array.length);
        assertEquals(1, array[0]);
        assertTrue(it.hasNext());
        array = it.next();
        assertEquals(1, array.length);
        assertEquals(3, array[0]);
        assertFalse(it.hasNext());
        p.close();
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleNonArray
    public void testSimpleNonArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .forType(Bean.class)
                .writeValues(strw);
        w.write(new Bean(13))
            .write(new Bean(-6))
            .writeAll(new Bean[] { new Bean(3), new Bean(1) })
            .writeAll(Arrays.asList(new Bean(5), new Bean(7)))
        ;
        w.close();
        assertEquals(aposToQuotes("{'a':13}\n{'a':-6}\n{'a':3}\n{'a':1}\n{'a':5}\n{'a':7}"),
                strw.toString());

        strw = new StringWriter();
        JsonGenerator gen = WRITER.getFactory().createGenerator(strw);
        w = WRITER
                .withRootValueSeparator(new SerializedString("/"))
                .writeValues(gen);
        w.write(new Bean(1))
            .write(new Bean(2));
        w.close();
        gen.close();
        assertEquals(aposToQuotes("{'a':1}/{'a':2}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleArray
    public void testSimpleArray() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER.writeValuesAsArray(strw);
        w.write(new Bean(1))
            .write(new Bean(2))
            .writeAll(new Bean[] { new Bean(-7), new Bean(2) });
        w.close();
        assertEquals(aposToQuotes("[{'a':1},{'a':2},{'a':-7},{'a':2}]"),
                strw.toString());

        strw = new StringWriter();
        JsonGenerator gen = WRITER.getFactory().createGenerator(strw);
        w = WRITER.writeValuesAsArray(gen);
        Collection<Bean> bean = Collections.singleton(new Bean(3));
        w.write(new Bean(1))
            .write(null)
            .writeAll((Iterable<Bean>) bean);
        w.close();
        gen.close();
        assertEquals(aposToQuotes("[{'a':1},null,{'a':3}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicNonArrayWithoutType
    public void testPolymorphicNonArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValues(strw);
        w.write(new ImplA(3))
            .write(new ImplA(4))
            .close();
        assertEquals(aposToQuotes("{'type':'A','value':3}\n{'type':'A','value':4}"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithoutType
    public void testPolymorphicArrayWithoutType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplA(6))
            .close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'A','value':6}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testPolymorphicArrayWithType
    public void testPolymorphicArrayWithType() throws Exception
    {
        StringWriter strw = new StringWriter();
        SequenceWriter w = WRITER
                .forType(PolyBase.class)
                .writeValuesAsArray(strw);
        w.write(new ImplA(-1))
            .write(new ImplB(3))
            .write(new ImplA(7));
        w.flush();
        w.close();
        assertEquals(aposToQuotes("[{'type':'A','value':-1},{'type':'B','b':3},{'type':'A','value':7}]"),
                strw.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testSimpleCloseable
    public void testSimpleCloseable() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                .with(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableValue input = new CloseableValue();
        assertFalse(input.closed);
        StringWriter out = new StringWriter();
        SequenceWriter seq = w.writeValues(out);
        input = new CloseableValue();
        assertFalse(input.closed);
        seq.write(input);
        assertTrue(input.closed);
        seq.close();
        input.close();
        assertEquals(aposToQuotes("{'x':0,'closed':false}"), out.toString());
    }

// com.fasterxml.jackson.databind.seq.SequenceWriterTest::testWithExplicitType
    public void testWithExplicitType() throws Exception
    {
        ObjectWriter w = MAPPER.writer()
                
                .without(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)
                .with(SerializationFeature.CLOSE_CLOSEABLE);
        StringWriter out = new StringWriter();
        SequenceWriter seq = w.writeValues(out);
        
        seq.write(new BareBaseExt());
        
        seq.write(new BareBaseExt(), MAPPER.constructType(BareBase.class));

        
        
        BareBaseCloseable cl = new BareBaseCloseable();
        seq.write(cl, MAPPER.constructType(BareBase.class));
        assertTrue(cl.closed);
        cl.close();

        seq.close();
        seq.flush();
        assertEquals(aposToQuotes("{'a':1,'b':2} {'a':1} {'a':1}"), out.toString());
    }

// com.fasterxml.jackson.databind.seq.TestInnerClassReaderFor::testReaderFor
    public void testReaderFor() throws IOException {

        X x = new X("dummy");
        objectMapper().readerForUpdating(x).readValue("{\"value\": \"updatedX\"}");
        assertEquals(x.getValue(), "updatedX");

        Y y = new Y("dummy");
        objectMapper().readerForUpdating(y).readValue("{\"value\": \"updatedY\"}");
        assertEquals(y.getValue(), "updatedY");

    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testSimpleAnyBean
    public void testSimpleAnyBean() throws Exception
    {
        String json = MAPPER.writeValueAsString(new Bean());
        Map<?,?> map = MAPPER.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(3), map.get("x"));
        assertEquals(Boolean.TRUE, map.get("a"));
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testAnyOnly
    public void testAnyOnly() throws Exception
    {
        ObjectMapper m;

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        String json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);

        
        m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json = serializeAsString(m, new AnyOnlyBean());
        assertEquals("{\"a\":3}", json);
    }

// com.fasterxml.jackson.databind.ser.AnyGetterTest::testAnyDisabling
    public void testAnyDisabling() throws Exception
    {
        String json = MAPPER.writeValueAsString(new NotEvenAnyBean());
        assertEquals(aposToQuotes("{'value':42}"), json);
    }
