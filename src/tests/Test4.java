/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.geotools.geometry.jts.JTSFactoryFinder;
import queryBuilder.RasterProcessorBoundRect;
import queryBuilder.SpatialProcessor;

/**
 *
 * @author zurriyot
 */
public class Test4 {
    private Polygon strabonPolygon;
    private List<BoundingRectangle> rectangles = new ArrayList<>();
    private final double offsetposX = 200.066234752;
    private final double offsetposY = -199.949587407;
    private final double lowCornerX = 475332.180694;
    private final double lowCornerY = 4628772.23596;
    
    public void checkPoints() {
        createGeometryFromStrabon();
    }
    
    public void createGeometryFromStrabon() {
        //Sending a query to Strabon and getting multipolygon coordinates in string type
        SpatialProcessor qbs = new SpatialProcessor();
        qbs.queryBuilderStrabon();
        String shape = qbs.getWkt();
        System.out.println("This is the MultiPolygon that is coming from Strabon endpoint: \n"+shape);
        //Removing all letters and commas from string shape, only double numbers and spaces are left
        String pointsStrabon = shape.replaceAll("[^\\d. ]", "");
        //Splitting shape string into several parts where each contains string points
        String[] arrayPoints = pointsStrabon.split(" ");
        //Parsing each string point into double value and 
        //adding those values into array of x and y coordinates
        Double[] xArray = new Double[(arrayPoints.length-1)/2];
        int index = 0;
        Double[] yArray = new Double[(arrayPoints.length-1)/2];
        for (int i = 1; i <= arrayPoints.length-1; i++) {
            if (i%2==1) {
                xArray[index]=Double.parseDouble(arrayPoints[i]);
            } else {
                yArray[index]=Double.parseDouble(arrayPoints[i]);
                index++;
            }
        }
        //Creating coordinate points depending on x and y arrays
        Coordinate[] coordinates = new Coordinate[xArray.length];
        for (int i=0; i < xArray.length; i++) {
            coordinates[i] = new Coordinate(xArray[i], yArray[i]);
        }
        //Create a geometry polygon depending on coordinate points
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        LinearRing lr = geometryFactory.createLinearRing(coordinates);
        strabonPolygon = geometryFactory.createPolygon(lr, null);
        System.out.println("\nThis is created Polygon depending on Strabon MultiPolygon coordinates: ");
        System.out.println(strabonPolygon);
        createRasdamanRectangle(qbs.getEnv());
    }
    
        
    public void createRasdamanRectangle(String boundRect) {
        //We have to find starting position coordinates, the size of the boxes (offsets),
        //number of boxes vertically and horizontally
        System.out.println(boundRect);
        String cornerPoints[] = boundRect.replaceAll("[^\\d., ]", "").split(",");
        BoundingRectangle strabonShapeBoundingRect = new BoundingRectangle();
        strabonShapeBoundingRect.setLowerCornerX(Double
                .parseDouble(cornerPoints[0].replaceFirst(" ", "").split(" ")[0]));
        strabonShapeBoundingRect.setLowerCornerY(Double
                .parseDouble(cornerPoints[0].replaceFirst(" ", "").split(" ")[1]));
        
        strabonShapeBoundingRect.setUpperCornerX(Double
                .parseDouble(cornerPoints[2].replaceFirst(" ", "").split(" ")[0]));
        strabonShapeBoundingRect.setUpperCornerY(Double
                .parseDouble(cornerPoints[2].replaceFirst(" ", "").split(" ")[1]));
        BoundingRectangle rasdamanRect = getRasdamanBoundingRectForShape(strabonShapeBoundingRect);
        divideIntoFour(rasdamanRect);
        getBoxValues();
    }
    
    public void divideIntoFour(BoundingRectangle rasdamanRect) {
        double numberOfRectanglesX = Math.round((rasdamanRect.getUpperCornerX()-
                rasdamanRect.getLowerCornerX())/offsetposX);
        double numberOfRectanglesY = Math.round((rasdamanRect.getUpperCornerY()-
                rasdamanRect.getLowerCornerY())/Math.abs(offsetposY));
        double startPosX = rasdamanRect.getLowerCornerX();
        double startPosY = rasdamanRect.getLowerCornerY();
        System.out.println(numberOfRectanglesX+" "+numberOfRectanglesY);
        if (numberOfRectanglesX==1&&numberOfRectanglesY==1) {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
            Point p = geometryFactory.createPoint(new Coordinate(startPosX+(offsetposX/2), startPosY-Math.abs(offsetposY/2)));
            if (strabonPolygon.contains(p)) {
                rectangles.add(rasdamanRect);
            }
            return;
        } else if (numberOfRectanglesX==1&&numberOfRectanglesY>1) {
            
        } else if (numberOfRectanglesX>1&&numberOfRectanglesY==1) {
            
        }

//        if (numberOfRectanglesX==1&&numberOfRectanglesY==1) {
//            
//        }
        BoundingRectangle rec = new BoundingRectangle();
        rec.setLowerCornerX(startPosX);
        rec.setLowerCornerY(startPosY);
        rec.setUpperCornerX(startPosX+Math.ceil(numberOfRectanglesX/2)*offsetposX);
        rec.setUpperCornerY(startPosY+Math.ceil(numberOfRectanglesY/2)*Math.abs(offsetposY));
        System.out.println(rec.getLowerCornerX()+" "+rec.getLowerCornerY());
        System.out.println(rec.getUpperCornerX()+" "+rec.getUpperCornerY());
        Polygon p1 = createPolygon(rec);
        System.out.println("1 ---"+strabonPolygon.contains(p1));
        if (strabonPolygon.contains(p1)) {
            rectangles.add(rec);
        } 
        else if (strabonPolygon.overlaps(p1)){
            divideIntoFour(rec);
        }
        
        
        rec.setLowerCornerX(startPosX);
        rec.setLowerCornerY(startPosY+Math.ceil(numberOfRectanglesY/2)*Math.abs(offsetposY));
        rec.setUpperCornerX(startPosX+Math.ceil(numberOfRectanglesX/2)*offsetposX);
        rec.setUpperCornerY(startPosY+numberOfRectanglesY*Math.abs(offsetposY));
        Polygon p2 = createPolygon(rec);
        System.out.println("2 ---"+strabonPolygon.covers(p2));
        if (strabonPolygon.covers(p2)) {
            rectangles.add(rec);
        } else if (strabonPolygon.overlaps(p2)) {
            divideIntoFour(rec);
        }
        
        
        rec.setLowerCornerX(startPosX+Math.ceil(numberOfRectanglesX/2)*offsetposX);
        rec.setLowerCornerY(startPosY);
        rec.setUpperCornerX(startPosX+numberOfRectanglesX*offsetposX);
        rec.setUpperCornerY(startPosY+Math.ceil(numberOfRectanglesY/2)*Math.abs(offsetposY));
        Polygon p3 = createPolygon(rec);
        System.out.println("3 ---"+strabonPolygon.covers(p3));
        if (strabonPolygon.covers(p3)) {
            rectangles.add(rec);
        } else if (strabonPolygon.overlaps(p3)) {
            divideIntoFour(rec);
        }
        
        
        rec.setUpperCornerX(startPosX+(numberOfRectanglesX*offsetposX));
        rec.setUpperCornerY(startPosY+(numberOfRectanglesY*Math.abs(offsetposY)));
        rec.setLowerCornerX(startPosX+Math.ceil(numberOfRectanglesX/2)*offsetposX);
        rec.setLowerCornerY(startPosY+Math.ceil(numberOfRectanglesY/2)*Math.abs(offsetposY));
        Polygon p4 = createPolygon(rec);
        System.out.println("4 --- "+strabonPolygon.covers(p4));
        if (strabonPolygon.covers(p4)) {
            rectangles.add(rec);
        } else if (strabonPolygon.overlaps(p4)) {
            divideIntoFour(rec);
        }
        System.out.println("");
    }
    
    public void findCentrals(BoundingRectangle rec) {
        
    }
    
    public void getBoxValues() {
        RasterProcessorBoundRect qbr = new RasterProcessorBoundRect();
        double average = 0;
        System.out.println("");
        for (int i = 0; i < rectangles.size(); i++) {
            qbr.queryBuilderRasdaman(rectangles.get(i));
            Double value = qbr.getBoxValues()[0];
            average+=value;
        }
        System.out.println("Average elevation value for the region: "+ (average/rectangles.size()));
    }
    
    public Polygon createPolygon(BoundingRectangle r) {
        
        //Creating coordinate points depending on x and y arrays
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(r.getLowerCornerX(), r.getLowerCornerY());
        coordinates[1] = new Coordinate(r.getLowerCornerX()+(r.getUpperCornerX()-r.getLowerCornerX()), r.getLowerCornerY());
        coordinates[2] = new Coordinate(r.getUpperCornerX(), r.getUpperCornerY());
        coordinates[3] = new Coordinate(r.getLowerCornerX(), r.getLowerCornerY()+(r.getUpperCornerY()-r.getLowerCornerY()));
        coordinates[4] = new Coordinate(r.getLowerCornerX(), r.getLowerCornerY());
        //Create a geometry polygon depending on coordinate points
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        LinearRing lr = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(lr, null);
        return polygon;
    }
    
    public BoundingRectangle getRasdamanBoundingRectForShape(BoundingRectangle strabonRect) {
        BoundingRectangle rasdamanRect = new BoundingRectangle();
        double result = lowCornerX;
        boolean done = true;
        while (result<strabonRect.getUpperCornerX()) {
            result += offsetposX;
            if (result>strabonRect.getLowerCornerX()&&done) {
//                rasdamanRect.setLowerCornerX(result-offsetposX+0.0000000076);
                rasdamanRect.setLowerCornerX(result-offsetposX);
                done=false;
            }
        }
//        rasdamanRect.setUpperCornerX(result+0.0000000078);
        rasdamanRect.setUpperCornerX(result);
        
        result = lowCornerY;
        done = true;
        while (result<strabonRect.getUpperCornerY()) {
            result += Math.abs(offsetposY);
            if (result>strabonRect.getLowerCornerY()&&done) {
//                rasdamanRect.setLowerCornerY(result-Math.abs(offsetposY)+0.00000306684);
                rasdamanRect.setLowerCornerY(result-Math.abs(offsetposY));
                done=false;
            }
        }
//        rasdamanRect.setUpperCornerY(result+0.00000306963);
        rasdamanRect.setUpperCornerY(result);
        return rasdamanRect;
    }
    

}
