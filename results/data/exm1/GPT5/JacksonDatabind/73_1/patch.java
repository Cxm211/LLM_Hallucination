protected void _removeUnwantedAccessor(Map<String, POJOPropertyBuilder> props)
    {
        final boolean inferMutators = _config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS);
        Iterator<POJOPropertyBuilder> it = props.values().iterator();

        while (it.hasNext()) {
            POJOPropertyBuilder prop = it.next();
            // 26-Jan-2017, tatu: [databind#935]: need to denote removal of
            prop.removeNonVisible(inferMutators);
            // If nothing remains that's usable for current mode, drop the property
            if (_forSerialization) {
                if (!prop.hasGetter() && !prop.hasField()) {
                    it.remove();
                }
            } else {
                if (!prop.hasSetter() && !prop.hasField() && !prop.hasConstructorParameter()) {
                    it.remove();
                }
            }
        }
    }