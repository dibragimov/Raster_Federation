/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package queryBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.data.oracle.sdo.Coordinates;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.geometry.BoundingBox;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author zurriyot
 */
public class SpatialProcessor {
    private final String endpoint = "http://localhost:8080/strabonendpoint/Query?query=";
    private Envelope envelope;
    private Geometry polygon;
    
    public SpatialProcessor(String boxId) {
        try {
            //Sending a query to strabon endpoint and getting xml file of RDFs
            String strabonQuery = "PREFIX lgd:<http://linkedgeodata.org/triplify/>"
                    + "PREFIX lgdgeo:<http://www.w3.org/2003/01/geo/wgs84_pos#>"
                    + "PREFIX lgdont:<http://linkedgeodata.org/ontology/>"
                    + "PREFIX geonames:<http://www.geonames.org/ontology#>"
                    + "PREFIX clc: <http://geo.linkedopendata.gr/corine/ontology#>"
                    + "PREFIX gag: <http://geo.linkedopendata.gr/greekadministrativeregion/ontology#>"
                    + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>"
                    + "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>"
                    + "PREFIX geor: <http://www.opengis.net/def/rule/geosparql/>"
                    + "PREFIX strdf: <http://strdf.di.uoa.gr/ontology#>"
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                    + "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>"
                    + "PREFIX gdl: <http://data.linkedeodata.eu/galicia/ontology#>"
                    + "SELECT *WHERE {  ?what geo:hasGeometry ?geometry .  ?geometry geo:asWKT ?wkt .  "
                    + "BIND(geof:envelope(?wkt) as ?env) .  FILTER(?what = "
                    + "<http://data.linkedeodata.eu/galicia/GaliciaCorine/id/"
                    + boxId +">)}";
            String query = URLEncoder.encode(strabonQuery, "UTF-8");
            String result = Helper.httpGet(endpoint+query);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(result));
            Document doc = builder.parse(src);
            //Getting coordinates of multipolygon which is 'wkt' variable in xml file
            String wkt = doc.getElementsByTagName("literal").item(0).getTextContent();
            try {
                polygon = new WKTReader().read(wkt);
            } catch (ParseException ex) {
                Logger.getLogger(SpatialProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Here we can get bounding rectangle of multipolygon, which is 'env' variable in xml file
            envelope = polygon.getEnvelopeInternal();
            
        } catch (IOException | ParserConfigurationException | DOMException | SAXException ex) {
            Logger.getLogger(SpatialProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public Geometry getPolygon() {
        return polygon;
    }
    

    public Envelope getEnvelope() {
        return envelope;
    }
}