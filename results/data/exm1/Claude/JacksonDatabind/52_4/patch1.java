public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            _properties.add(new ExtTypedProperty(property, typeDeser));
            Integer index = _properties.size() - 1;
            _nameToPropertyIndex.put(property.getName(), index);
            _nameToPropertyIndex.put(typeDeser.getPropertyName(), index);
        }