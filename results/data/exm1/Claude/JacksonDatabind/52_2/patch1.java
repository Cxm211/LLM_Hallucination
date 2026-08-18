public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, typeDeser));
            _nameToPropertyIndex.put(property.getName(), index);
            String typePropName = typeDeser.getPropertyName();
            if (!property.getName().equals(typePropName)) {
                _nameToPropertyIndex.put(typePropName, index);
            }
        }