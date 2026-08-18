    public ValueMarker(double value, Paint paint, Stroke stroke, 
                       Paint outlinePaint, Stroke outlineStroke, float alpha) {
        super(paint, stroke, paint, stroke, alpha);
        this.value = value;
    }

// trigger testcase
public void test1808376() {
        Stroke stroke = new BasicStroke(1.0f);
        Stroke outlineStroke = new BasicStroke(2.0f);
        ValueMarker m = new ValueMarker(1.0, Color.red, stroke, Color.blue, 
                outlineStroke, 0.5f);
        assertEquals(1.0, m.getValue(), EPSILON);
        assertEquals(Color.red, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertEquals(Color.blue, m.getOutlinePaint());
        assertEquals(outlineStroke, m.getOutlineStroke());
        assertEquals(0.5f, m.getAlpha(), EPSILON);
    }
