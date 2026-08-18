    public String setValue(String val) {
        String oldVal = parent.get(this.key);
        if (parent != null) {
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound)
                parent.vals[i] = val;
        }
        this.val = val;
        return Attributes.checkNotNull(oldVal);
    }

// trigger testcase
@Test public void settersOnOrphanAttribute() {
        Attribute attr = new Attribute("one", "two");
        attr.setKey("three");
        String oldVal = attr.setValue("four");
        assertEquals("two", oldVal);
        assertEquals("three", attr.getKey());
        assertEquals("four", attr.getValue());
        assertEquals(null, attr.parent);
    }
