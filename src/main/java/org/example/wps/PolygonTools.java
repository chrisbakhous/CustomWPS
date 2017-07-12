package org.example.wps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.factory.StaticMethodsProcessFactory;
import org.geotools.text.Text;

public class PolygonTools extends StaticMethodsProcessFactory<PolygonTools> {

  public PolygonTools() {
    super(Text.text("Polygon Tools"), "custom", PolygonTools.class);
  }
//
  static Geometry polygonize(Geometry geometry) {
      List lines = LineStringExtracter.getLines(geometry); //Extract all lineString elements from a geometry
      Polygonizer polygonizer = new Polygonizer(); //Polygonizer est une collection d'elements geometrique contenant des lignes representant les arretes d'un graphe planaire
      // En gros, c pour creer des polygones à partir des lignes d'intersection
      // Pour creer un polygonizer avec la même GeometryFactory que la geometrie d'entrée 
      polygonizer.add(lines);//Ajouter une collection d'element géometrique pour etre polygonizer 
      Collection polys = polygonizer.getPolygons();//envoie la liste des polygons former par la polygonization 
      Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);//converire la liste en array
      return geometry.getFactory().createGeometryCollection(polyArray);
  }

  @DescribeProcess(title = "splitPolygon", description = "Splits a polygon by a linestring")
  @DescribeResult(description = "Geometry collection created by splitting the input polygon")
  public static Geometry splitPolygon(
      @DescribeParameter(name = "polygon", description = "Polygon to be split") Geometry poly,
      @DescribeParameter(name = "line", description = "Line to split the polygon") Geometry line) {

      Geometry nodedLinework = poly.getBoundary().union(line);
      Geometry polys = polygonize(nodedLinework);

      // Only keep polygons which are inside the input
      List output = new ArrayList();
      for (int i = 0; i < polys.getNumGeometries(); i++) {
          Polygon candpoly = (Polygon) polys.getGeometryN(i);
          if (poly.contains(candpoly.getInteriorPoint())) {
              output.add(candpoly);
          }
      }
      return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
  }
}