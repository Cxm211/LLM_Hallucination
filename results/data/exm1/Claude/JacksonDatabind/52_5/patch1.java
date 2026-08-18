public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, typeDeser));
            _nameToPropertyIndex.put(property.getName(), index);
            String typePropertyName = typeDeser.getPropertyName();
            if (!property.getName().equals(typePropertyName)) {
                _nameToPropertyIndex.put(typePropertyName, index);
            }
        }