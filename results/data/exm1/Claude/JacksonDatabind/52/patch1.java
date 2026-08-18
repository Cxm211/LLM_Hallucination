public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            Integer existingIndex = _nameToPropertyIndex.get(property.getName());
            if (existingIndex != null) {
                return;
            }
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, typeDeser));
            _nameToPropertyIndex.put(property.getName(), index);
            _nameToPropertyIndex.put(typeDeser.getPropertyName(), index);
        }