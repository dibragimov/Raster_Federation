/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.HashMap;
import queryBuilder.RasterProcessorQueryBoundRect;
import queryBuilder.SpatialProcessor;

/**
 *
 * @author zurriyot
 */
/**
 * We have lots of Polygons for Galicia. We retrieve one of the Polygons from Strabon endpoint,
 * sending a query.
 * We find the bounding rectangle of the Polygon and retrieve that rectangle part from Galicia
 * on Rasdaman endpoint.
 * The result of this Rasdaman query is a MultiPolygon that has equal sized boxes each containing specific
 * information. Here, we get central coordinates of each box.
 * The first algorithm/test checks whether central coordinates of each box are inside 
 * the Strabon Polygon or not. If central points are inside the Polygon, then we assume that
 * most part of the box is within the polygon shape and take that box. If central points are outside,
 * then we omit those boxes.
 * As a result, we get a list of boxes that make up that Strabon Polygon.
 */
public class Test1 {
    
    public void checkRasdamanPoints() {
        //Querying Strabon to get Polygon
        SpatialProcessor sProcessor = new SpatialProcessor("7432");
        Geometry multiPolygon = sProcessor.getPolygon();
        System.out.println("This is the Polygon (spatial data) coming from Strabon endpoint: ");
        System.out.println(multiPolygon);
        
        //Take all points and box values from RasDaMan
        RasterProcessorQueryBoundRect rProcessor = new RasterProcessorQueryBoundRect(sProcessor.getEnvelope());
        rProcessor.Process();
        Point[] pointsRasdaman = rProcessor.createCentralPointsForBoxes();
        Double[] boxValues = rProcessor.getBoxValues();
        System.out.println("\nThese are the points coming from RasDaMan: ");
        for (int i=0; i<pointsRasdaman.length; i++) {
            System.out.println((i+1)+". "+pointsRasdaman[i] +" "+boxValues[i]);
        }
        
        //Check if rasdaman point is inside the Strabon Polygon, 
        //if not, then do not add it to our list of Points;
        HashMap listOfInsidePoints = new HashMap();
        double averOfBoxValues = 0;
        System.out.println("\nThese are the points that are inside the Polygon: ");
        for (int i=0; i<pointsRasdaman.length; i++) {
            if (multiPolygon.covers(pointsRasdaman[i])) {
                System.out.print(i+". "+pointsRasdaman[i]+" "+boxValues[i]+"\n");
                listOfInsidePoints.put(i,pointsRasdaman[i]);
                averOfBoxValues+= boxValues[i];
            }
        }
        
        System.out.println("\nOverall number of points that are inside the Polygon: "+listOfInsidePoints.size());
        System.out.println("\nAverage elevation value for the region: "+(averOfBoxValues/listOfInsidePoints.size()));
        System.out.println("\nMap: ");
        ShapeDrawer.draw(rProcessor.getNumberOfBoxesX(), rProcessor.getNumberOfBoxesY(), listOfInsidePoints);
    }
}
