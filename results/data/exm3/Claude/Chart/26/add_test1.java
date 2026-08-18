// org/jfree/chart/junit/PieChartTests.java
public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("Category 1", 50.0);
            dataset.setValue("Category 2", 30.0);
            JFreeChart chart = ChartFactory.createPieChart(
                "Test Pie Chart", dataset, true, false, false);
            BufferedImage image = new BufferedImage(200, 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }