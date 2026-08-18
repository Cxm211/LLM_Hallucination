protected void _removeUnwantedAccessor(Map<String, POJOPropertyBuilder> props)
    {
        final boolean inferMutators = _config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS);
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = props.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();
            // 26-Jan-2017, tatu: [databind#935]: need to denote removal of
            prop.removeNonVisible(inferMutators);
            if (prop.isEmpty()) {
                it.remove();
            }
        }
    }