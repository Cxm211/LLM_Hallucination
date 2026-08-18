// org/jfree/chart/renderer/category/junit/MinMaxCategoryRendererTests.java
public void testSerialization() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(false);
        r1.setGroupPaint(Color.blue);
        r1.setGroupStroke(new BasicStroke(1.5f));
        MinMaxCategoryRenderer r2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (MinMaxCategoryRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(r1, r2);
    }