// org/jfree/chart/junit/ScatterPlotTests.java
public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultXYDataset dataset = new DefaultXYDataset();
            double[] x = {1.0, 2.0, 3.0};
            double[] y = {4.0, 5.0, 6.0};
            double[][] data = {x, y};
            dataset.addSeries("Series 1", data);
            JFreeChart chart = ChartFactory.createScatterPlot(
                "Test Chart", "X", "Y", dataset, 
                PlotOrientation.VERTICAL, true, false, false);
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