// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testRemoveCaseInsensitiveProperty() throws Exception {
    com.fasterxml.jackson.databind.PropertyName propName = new com.fasterxml.jackson.databind.PropertyName("FullName");
    com.fasterxml.jackson.databind.deser.SettableBeanProperty prop = 
        new com.fasterxml.jackson.databind.deser.SettableBeanProperty.Nop(propName);
    java.util.ArrayList<com.fasterxml.jackson.databind.deser.SettableBeanProperty> props = 
        new java.util.ArrayList<>();
    props.add(prop);
    com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap map = 
        new com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap(props, true);
    assertNotNull(map.find("fullname"));
    map.remove(prop);
    assertNull(map.find("fullname"));
}

public void testRemoveNonExistentProperty() {
    com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap map = 
        new com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap(java.util.Collections.emptyList(), true);
    com.fasterxml.jackson.databind.PropertyName propName = new com.fasterxml.jackson.databind.PropertyName("Dummy");
    com.fasterxml.jackson.databind.deser.SettableBeanProperty prop = 
        new com.fasterxml.jackson.databind.deser.SettableBeanProperty.Nop(propName);
    try {
        map.remove(prop);
        fail("Should have thrown NoSuchElementException");
    } catch (java.util.NoSuchElementException e) {
        // expected
    }
}
