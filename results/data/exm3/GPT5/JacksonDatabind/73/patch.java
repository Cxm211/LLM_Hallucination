protected void _removeUnwantedAccessor(Map<String, POJOPropertyBuilder> props)
    {
        final boolean inferMutators = _config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS);
        Iterator<POJOPropertyBuilder> it = props.values().iterator();

        while (it.hasNext()) {
            POJOPropertyBuilder prop = it.next();
            // 26-Jan-2017, tatu: [databind#935]: need to denote removal of
            prop.removeNonVisible(inferMutators);
            // After trimming non-visible accessors, drop properties that are not
            // usable for current direction (serialize vs deserialize)
            if (_forSerialization) {
                if (!prop.couldSerialize()) {
                    it.remove();
                }
            } else {
                if (!prop.couldDeserialize()) {
                    it.remove();
                }
            }
        }
    }