    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        if (index < this.keys.size()) {
        rebuildIndex();
        }
    }

    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
			return;
        }
        removeValue(index);
    }

    public void removeColumn(Comparable columnKey) {
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }

// trigger testcase
public void testBug1835955() {
    	DefaultCategoryDataset d = new DefaultCategoryDataset();
    	d.addValue(1.0, "R1", "C1");
    	d.addValue(2.0, "R2", "C2");
    	d.removeColumn("C2");
    	d.addValue(3.0, "R2", "C2");
    	assertEquals(3.0, d.getValue("R2", "C2").doubleValue(), EPSILON);
    }

public void testRemoveColumnByKey() {
    	DefaultKeyedValues2D d = new DefaultKeyedValues2D();
    	d.addValue(new Double(1.0), "R1", "C1");
    	d.addValue(new Double(2.0), "R2", "C2");
    	d.removeColumn("C2");
    	d.addValue(new Double(3.0), "R2", "C2");
    	assertEquals(3.0, d.getValue("R2", "C2").doubleValue(), EPSILON);
    	
    	// check for unknown column
    	boolean pass = false;
    	try {
    		d.removeColumn("XXX");
    	}
    	catch (UnknownKeyException e) {
    		pass = true;
    	}
    	assertTrue(pass);
    }

public void testGetIndex2() {
    	DefaultKeyedValues v = new DefaultKeyedValues();
    	assertEquals(-1, v.getIndex("K1"));
    	v.addValue("K1", 1.0);
    	assertEquals(0, v.getIndex("K1"));
    	v.removeValue("K1");
    	assertEquals(-1, v.getIndex("K1"));
    }

public void testRemoveValue() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", new Double(1.0));
        data.addValue("B", null);
        data.addValue("C", new Double(3.0));
        data.addValue("D", new Double(2.0));
        assertEquals(1, data.getIndex("B"));
        data.removeValue("B");
        assertEquals(-1, data.getIndex("B"));
        
        boolean pass = false;
        try {
            data.removeValue("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
    }
