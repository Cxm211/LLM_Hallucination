        public void addExternal(SettableBeanProperty property, TypeDeserializer typeDeser)
        {
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, typeDeser));
            _nameToPropertyIndex.put(property.getName(), index);
            _nameToPropertyIndex.put(typeDeser.getPropertyName(), index);
            _nameToPropertyIndex.put(typeDeser.getPropertyName() + "#" + _properties.size(), index);
        }