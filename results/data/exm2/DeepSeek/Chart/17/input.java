    public Object clone() throws CloneNotSupportedException {
        Object clone = createCopy(0, getItemCount() - 1);
        return clone;
    }

// trigger testcase
public void testBug1832432() {
        TimeSeries s1 = new TimeSeries("Series");
        TimeSeries s2 = null;
        try {
            s2 = (TimeSeries) s1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(s1 != s2);
        assertTrue(s1.getClass() == s2.getClass());
        assertTrue(s1.equals(s2));

        // test independence
        s1.add(new Day(1, 1, 2007), 100.0);
        assertFalse(s1.equals(s2));    	
    }
