public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Class<?> rawBase = baseType.getRawClass();

        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String,NamedType> byName = new LinkedHashMap<String,NamedType>();

        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                rawBase);
        _collectAndResolveByTypeId(ac, rootType, config, typesHandled, byName);
        
        Collection<NamedType> st = ai.findSubtypes(property);
        if (st != null) {
            for (NamedType nt : st) {
                ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, nt.getType());
                _collectAndResolveByTypeId(ac, nt, config, typesHandled, byName);
            }
        }

        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                if (rawBase.isAssignableFrom(subtype.getType())) {
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolveByTypeId(curr, subtype, config, typesHandled, byName);
                }
            }
        }
        return _combineNamedAndUnnamed(rawBase, typesHandled, byName);
    }