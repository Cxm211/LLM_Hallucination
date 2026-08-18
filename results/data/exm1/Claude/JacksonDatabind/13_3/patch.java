public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> gen, ObjectIdResolver resolverType)
    {
        final ObjectIdGenerator.IdKey key = gen.key(id);

        if (_objectIds == null) {
            _objectIds = new LinkedHashMap<ObjectIdGenerator.IdKey,ReadableObjectId>();
        } else {
            ReadableObjectId entry = _objectIds.get(key);
            if (entry != null) {
                return entry;
            }
        }

        ObjectIdResolver resolver = null;

        if (_objectIdResolvers == null) {
            _objectIdResolvers = new ArrayList<ObjectIdResolver>(8);
        } else {
            for (ObjectIdResolver res : _objectIdResolvers) {
                if (res.canUseFor(resolverType)) {
                    resolver = res;
                    break;
                }
            }
        }

        if (resolver == null) {
            resolver = resolverType.newForDeserialization(this);
            _objectIdResolvers.add(resolver);
        }

        ReadableObjectId entry = new ReadableObjectId(key);
        entry.setResolver(resolver);
        _objectIds.put(key, entry);
        return entry;
    }