package playground.gregor.shapeFileToMATSim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.vecmath.Vector2d;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.matsim.utils.collections.QuadTree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;


public class PolygonGeneratorII {

	private Envelope envelope;
	private QuadTree<Polygon> polygonTree;
	private QuadTree<LineString> lineStringTree;
	private QuadTree<Feature> lineTree;
	private HashSet<Polygon> polygons;
	private HashMap<Integer, LineString> lineStrings;
	private FeatureSource featureSourcePolygon;
	private FeatureSource featureSourceLineString;
	private FeatureCollection collectionLineString;
	private List<Feature> featureList;
	private Collection<Feature> retPolygons;
	private GeometryFactory geofac;
	static final double CATCH_RADIUS = 0.2;
	static final double DEFAULT_DISTANCE = 10;
	private static final Logger log = Logger.getLogger(PolygonGeneratorII.class);
	private boolean graph = false; 
	
	
	private HashMap<Integer, Point> interPoints = new HashMap<Integer, Point>();
	private FeatureType ftPolygon;
	private FeatureType ftPoint;
	private FeatureType ftLineString;
	private int optid = 0;
	
	public PolygonGeneratorII(FeatureSource ls, FeatureSource po){
		this.featureSourcePolygon = po;
		this.featureSourceLineString = ls;
		this.geofac = new GeometryFactory();
		this.retPolygons = new ArrayList<Feature>();
//		log.setLevel(Level.ERROR);
		initFeatureGenerator();
	}
		
	public PolygonGeneratorII(Collection<Feature> graph, FeatureSource po) {
		this.featureSourcePolygon = po;
		this.featureList = (List<Feature>) graph;
		this.geofac = new GeometryFactory();
		this.graph = true;
		this.retPolygons = new ArrayList<Feature>();
//		log.setLevel(Level.ERROR);
		initFeatureGenerator();
	}
	
	private void initFeatureGenerator(){
		
		AttributeType polygon = DefaultAttributeTypeFactory.newAttributeType("MultiPolygon",MultiPolygon.class, true, null, null, this.featureSourcePolygon.getSchema().getDefaultGeometry().getCoordinateSystem());
		AttributeType point = DefaultAttributeTypeFactory.newAttributeType("Point",Point.class, true, null, null, this.featureSourcePolygon.getSchema().getDefaultGeometry().getCoordinateSystem());
		AttributeType linestring = DefaultAttributeTypeFactory.newAttributeType("LineString",LineString.class, true, null, null, this.featureSourcePolygon.getSchema().getDefaultGeometry().getCoordinateSystem());
		AttributeType id = AttributeTypeFactory.newAttributeType("ID", Integer.class);
		AttributeType width = AttributeTypeFactory.newAttributeType("width", Double.class);
		AttributeType area = AttributeTypeFactory.newAttributeType("area", Double.class);
		AttributeType info = AttributeTypeFactory.newAttributeType("info", String.class);
		try {
			this.ftPolygon = FeatureTypeFactory.newFeatureType(new AttributeType[] {polygon, id, width, area, info }, "linkShape");
			this.ftPoint = FeatureTypeFactory.newFeatureType(new AttributeType[] {point, id, info }, "pointShape");
			this.ftLineString = FeatureTypeFactory.newFeatureType(new AttributeType[] {linestring, id, info }, "linString");			
		} catch (FactoryRegistryException e) {
			e.printStackTrace();
		} catch (SchemaException e) {
			e.printStackTrace();
		}
		
	}

	public Collection<Feature> generatePolygons() throws Exception{
		log.info("entering polygonGenerator");
		try {
			parsePolygons();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("leaving polygonGenerator");		
//		return(genPolygonFeatureCollection(mergePolygons()));	
		HashMap<Integer, Polygon> tmpPolygons = mergePolygons(); 
		QuadTree<Polygon> polygonNodes = getPolygonNodes(tmpPolygons);
//		cutPolygons(this.lineStrings,);
//		createPolygonFeatures(cutPolygons(this.lineStrings,mergePolygons()));
//		createPolygonFeatures(mergePolygons());
		
		
		
		return this.retPolygons;
		
	
//		cutPolygons(lineStrings,mergePolygons());
//		return(genPointFeatureCollection(interPoints));
	}


	private QuadTree<Polygon> getPolygonNodes(HashMap<Integer, Polygon> mergedPolygons) {
		
		log.info("generatin polygon nodes ...");
		QuadTree<Polygon> polygonNodes = new QuadTree<Polygon>(this.envelope.getMinX(), this.envelope.getMinY(), this.envelope.getMaxX() + (this.envelope.getMaxX() - this.envelope.getMinX()), this.envelope.getMaxY() + (this.envelope.getMaxY()-this.envelope.getMinY()));
	 	for(Iterator<Entry<Integer, LineString>> lsIter = lineStrings.entrySet().iterator() ; lsIter.hasNext() ; ){
			
			Entry<Integer, LineString> lsEntry  = lsIter.next();
			int lsId = lsEntry.getKey();
			LineString currLs = lsEntry.getValue();
			List<Point> po = new ArrayList<Point>();
			po.add(currLs.getStartPoint());
			po.add(currLs.getEndPoint());
			
			for (Point currPoint : po) {

				if (polygonNodes.get(currPoint.getX(),currPoint.getY(),CATCH_RADIUS).size() > 0) {
					continue;
				}
				
				Collection<LineString> tmpLs = this.lineStringTree.get(currPoint.getX(), currPoint.getY(), CATCH_RADIUS);
				
				if (tmpLs.size() <= 2) {
					continue;
				}
				
				Polygon tmpPoly; 
				if(mergedPolygons.containsKey(lsId)){
					tmpPoly = mergedPolygons.get(lsId);
				}else  {
					log.warn("No corresponding Polygon for LineString: " + lsId + " found!");
					continue;
				}
				
				
				SortedMap<Double, LineString> sortedLs = sortLines(tmpLs,currPoint);
				org.matsim.utils.collections.gnuclasspath.TreeMap<Double,LineString> sortedLsTree = new org.matsim.utils.collections.gnuclasspath.TreeMap<Double,LineString>(sortedLs);
				if (sortedLsTree.values().size() < 3) {
					log.warn("intersection with only: " + sortedLsTree.values().size() + " LineString found, this should not happen!" );
					continue;
				}
//				Coordinate [] nodeC = new Coordinate[sortedLsTree.values().size()+1];
				ArrayList<Coordinate> nodeC = new ArrayList<Coordinate>();
				int t = 0;
				for (double angle : sortedLs.keySet()) {
					Polygon p = getControlPolygon(currPoint, angle,sortedLsTree);
	
					if (p == null) {
						break;
					}
					createPolygonFeature(p, 2, lsId);
					QuadTree<Coordinate> tmpPolyQ = getCoordinateQuatTree(tmpPoly);	
					boolean found = false;
					Coordinate c = null;
					while (!found && tmpPolyQ.values().size() > 0) {
						c = tmpPolyQ.get(currPoint.getX(), currPoint.getY());
						LineString tmptmpl  = this.geofac.createLineString(new Coordinate [] {currPoint.getCoordinate(), c});
						if (p.contains(tmptmpl) || tmptmpl.crosses(p)) {
							found = true;
						} else {
							tmpPolyQ.remove(c.x, c.y, c);
						}

					}
					if (found) {
						nodeC.add(c);
					} 
				}
				if (nodeC.size() < 3) {
					log.warn("could not craete polygon node for ls:" + lsId);
					continue;
				}
				nodeC.add(nodeC.get(0));
				Coordinate [] nodeCa = new Coordinate [nodeC.size()];
				nodeC.toArray(nodeCa);
				LinearRing lr = this.geofac.createLinearRing(nodeCa);
				
				Polygon p = this.geofac.createPolygon(lr, null);
				polygonNodes.put(currPoint.getX(), currPoint.getY(), p);
				createPolygonFeature(p, 1, lsId);
				
				
			}
			
			
	 	}
		
		
		
		return null;
	}



	private QuadTree<Coordinate> getCoordinateQuatTree(Polygon p) {
		QuadTree<Coordinate> q = new QuadTree<Coordinate>(this.envelope.getMinX(), this.envelope.getMinY(), this.envelope.getMaxX() + (this.envelope.getMaxX() - this.envelope.getMinX()), this.envelope.getMaxY() + (this.envelope.getMaxY()-this.envelope.getMinY()));
		for (int i = 0; i < p.getExteriorRing().getNumPoints(); i++) {
			Coordinate c = p.getExteriorRing().getCoordinateN(i);
			q.put(c.x, c.y, c);
		}
		
		return q;
	}

	@Deprecated
	private HashMap<Integer, Polygon> cutPolygons(HashMap<Integer, LineString> lineStrings, HashMap<Integer, Polygon> returnPolys){
		
		int countlines = 0;
		int cCP = 0;
		int countInter = 0;
		
		HashMap<Integer, Polygon> controlPolys = new HashMap<Integer, Polygon>();
		HashMap<Integer, Polygon> debugPolys = new HashMap<Integer, Polygon>();
		

		CoordinateArraySequence s1 = new CoordinateArraySequence(new Coordinate[] {new Coordinate(651048.912,9893489.699)});
		Point p1 = new Point(s1,this.geofac);
		CoordinateArraySequence s2 = new CoordinateArraySequence(new Coordinate[] {new Coordinate(650854.77,9893215.73)});
		Point p2 = new Point(s2,this.geofac);
		
		
		log.info("cutting polygons ...");
		HashMap<Integer, Polygon> fPoly = new HashMap<Integer, Polygon>();
			
		 	for(Iterator<Entry<Integer, LineString>> lsIter = lineStrings.entrySet().iterator() ; lsIter.hasNext() ; ){
		
				Entry<Integer, LineString> lsEntry  = lsIter.next();
				int lsId = lsEntry.getKey();
				LineString currLs = lsEntry.getValue();
 				List<Point> po = new ArrayList<Point>();
				po.add(currLs.getStartPoint());
				po.add(currLs.getEndPoint());
				
		
				for(Point currPoint : po){
					
					countlines++;
//					log.info(countlines+": " + " lsId: "+lsId + " " +  currLs.getStartPoint().getCoordinate().toString() + " "+ currLs.getEndPoint().getCoordinate().toString());
					
				
					Coordinate currPointCoor = currPoint.getCoordinate();
					Collection<LineString> lines = this.lineStringTree.get(currPoint.getX(), currPoint.getY(), CATCH_RADIUS);
					
					if (lines.size() == 2) {
//						throw new RuntimeException("fragmented LineString! This should not happen!! LineString:" + currLs);
						continue;
					}
					
					//dead end
					if(lines.size()== 1){
						continue;
					}  
					
					Polygon poly; 
					if(fPoly.containsKey(lsId)){
						poly = fPoly.get(lsId);
					}else if (returnPolys.containsKey(lsId)) {
						poly  = returnPolys.get(lsId);
					
					
//						///////
//						Coordinate [] c = currLs.getCoordinates();
//						
//						for(int i = 0 ; i < c.length-1 ; i++){
//							Coordinate [] c1 = new Coordinate[]{c[i],c[i+1]};
//							CoordinateSequence seq4 = new CoordinateArraySequence(c1);
//							LineString li = new LineString(seq4, geofac);	
//						
//							if(poly.intersects(li)){
//								
//								
//								Geometry g = poly.intersection(li);
//								Coordinate co [] = g.getCoordinates();
//								
//								for(int ii = 0 ; ii < co.length ; ii++){
//									
//									Coordinate [] c3 = new Coordinate[]{co[ii]};
//									CoordinateSequence seq5 = new CoordinateArraySequence(c3);
//									Point p = new Point(seq5, geofac);
//									
//									
//									
//									if(!p.within(poly)){
////										log.warn(countlines+": LineString intersects polygon "+c.length+" times");
//										interPoints.put((countInter), p);
//										countInter++;
//									}
//								}
//								
////								continue;
//							}
//							
//							
//						}
//						/////////
										
					}else {
//						log.warn(countlines+": LineString has no poly");
//						createPolygonFeature(returnPolys.get(lsId), 1);
						continue;
					}
						
					if (!currPoint.within(poly)){
						log.warn(countlines+": Point is not covered by its polygon");
						
//						polygon = setAdditionalIntersects(polygon,ls.getEndPoint());
						
//						createPolygonFeature(poly, 2, lsId);
						if (fPoly.containsKey(lsId)){
							fPoly.remove(lsId);
						} else {
							returnPolys.remove(lsId);
						}
						
						break;
					}
					
					
					
					Coordinate [] coor = poly.getCoordinates();	
					
					List<Point> points = new ArrayList<Point>();				
											
					SortedMap<Double, LineString> sortedLines = sortLines(lines,currPoint);
					
					LineString [] l = new LineString[0];
					LineString [] lineArr = sortedLines.values().toArray(l);
					Double [] d = new Double[0];
					Double [] angleArr = sortedLines.keySet().toArray(d);
					
					for (int i = 0 ; i < lineArr.length ; i++ ){
												
						Polygon controlPoly = getControlPoly(currPointCoor, lineArr, angleArr, i);
//						if (!controlPoly.isValid()) {
//							continue;
//						}
						
						controlPolys.put(cCP, controlPoly);
						cCP++;
						
						boolean found = false;
						Envelope o = poly.getEnvelopeInternal();
						QuadTree<Coordinate> polyCoor = new QuadTree<Coordinate>(o.getMinX(), o.getMinY(), o.getMaxX()+o.getMaxX()-o.getMinX(), 
								o.getMaxY()+o.getMaxY()-o.getMinY());
						for(int ii = 0 ; ii < coor.length ; ii++ ){
							polyCoor.put(coor[ii].x, coor[ii].y, coor[ii]);
						}
						while(!found){
							
							Coordinate pp = polyCoor.get(currPointCoor.x, currPointCoor.y);
							
							Coordinate [] cpp = new Coordinate[]{pp};
							CoordinateSequence seq10 = new CoordinateArraySequence(cpp);
							Point cppPoint = new Point(seq10, geofac);
							
							
							if(pp == null || (currPoint.distance(cppPoint) > 10)){
								
								
//								log.warn(countlines+": polyCoor is empty: no point found. ControlPoly: " + controlPoly.toString());
								
								
								
								///////////////////////////////
								LineString lineI = lineArr[i];
								double angleI = angleArr[i];
								LineString lineII;
								double angleII;
								double deltaAngle;
								if (i == ( lineArr.length -1) ) {	
									lineII = lineArr[0];
									angleII = angleArr[0];
									
									if ( (angleII - angleI) <= -180 ){
										deltaAngle = 60;
									}else{
										deltaAngle = 270;
									}

								}else {
									lineII = lineArr[i+1];
									angleII = angleArr[i+1];
									deltaAngle = angleII - angleI;
								}
								
								Coordinate bisecCoor = getBisectingLine(lineII, lineI);
								
								if (deltaAngle > 180 ){
									Coordinate co = subCoord(currPointCoor, bisecCoor); 
									bisecCoor = addCoord(currPointCoor, co);
								}
								///
								
								Coordinate cb = subCoord(currPointCoor, bisecCoor); 
								Coordinate bisecCoorII = addCoord(currPointCoor, cb);
								
								
								Coordinate [] ccc = new Coordinate[]{bisecCoorII, bisecCoor};
								CoordinateSequence seq6 = new CoordinateArraySequence(ccc);
								LineString interLine = new LineString(seq6, geofac);
								
								SortedMap<Double, Coordinate> lengths = new TreeMap<Double, Coordinate>();
								
								Coordinate [] inter = null;
								
//								if(poly.intersects(interLine)){
									
									inter = poly.intersection(interLine).getCoordinates();
									
									
									for(int iii = 0 ; iii < inter.length ; iii++ ){
										lengths.put(inter[iii].distance(currPointCoor),inter[iii]);								
//										log.info(inter[iii].toString());
									}
//								}
								
								
								
								
								for(int iii = 0 ; iii < inter.length ; iii++ ){
									
									Coordinate ppp = lengths.get(lengths.firstKey());
				
									if(ppp == null){
										log.warn(countlines+": polyCoor is empty: no intersection found.");
										break;
									}
									
									Coordinate [] cccc = new Coordinate[]{currPointCoor, ppp};
									CoordinateSequence seq4 = new CoordinateArraySequence(cccc);
									LineString line2 = new LineString(seq4, geofac);

									if(controlPoly.contains(line2) || line2.crosses(controlPoly)  ){
										Coordinate [] p = new Coordinate[] {lengths.get(lengths.firstKey())};
										CoordinateSequence seqII = new CoordinateArraySequence(p);
										Point poi = new Point(seqII, geofac);

										if (pp == null || (currPoint.distance(cppPoint) > currPoint.distance(poi))){
											points.add(poi);
											found = true;
											break;
										}

									}else{
										lengths.remove(lengths.firstKey());
									}



								}
								if (found){break;}
							}
							
							Coordinate [] cc = new Coordinate[]{currPointCoor, pp};
							CoordinateSequence seq2 = new CoordinateArraySequence(cc);
							LineString line = new LineString(seq2, geofac);

								if(controlPoly.contains(line) || line.crosses(controlPoly)  ){ 

									
									Coordinate [] p = new Coordinate[]{pp};

									CoordinateSequence seqII = new CoordinateArraySequence(p);
									Point poi = new Point(seqII, geofac);
									points.add(poi);

									found = true;
									break;
								}else{
									polyCoor.remove(pp.x, pp.y, pp);
								}
						}	
						
					}
					
					if(points.size()<3){
						continue;
					}
					
					HashSet<Polygon> cutPolys = null;
					try {
						cutPolys = separatePoly(poly, points);
					} catch (RuntimeException e) {
						e.printStackTrace();
						continue;
					}

//					int p2409 = 5;					
					for(Polygon polygon : cutPolys){
//						if (lsId == 2409) {
//							createPolygonFeature(polygon, p2409++, lsId);
//						}
						if(currLs.crosses(polygon) || polygon.contains(currLs) ){	//
							
//							log.info(polygon.toString());
							
							if(fPoly.containsKey(lsId)){
								fPoly.remove(lsId);
							}
							fPoly.put(lsId, polygon);
							if (p1.equalsExact(po.get(0),0.5) && p2.equalsExact(po.get(1),0.5)) debugPolys.put(61, polygon);
						}						
					}
				}
			}
		log.info("done.");
		return fPoly;
//		return controlPolys;
	}
		
	private Polygon setAdditionalIntersects(Polygon poly, Point currPoint) {
		Collection<LineString> ls = this.lineStringTree.get(currPoint.getX(), currPoint.getY(),CATCH_RADIUS);
		
		for (LineString l : ls) {
			Point p = null;			
			if (l.getStartPoint().equalsExact(currPoint, CATCH_RADIUS)) {
				p = l.getPointN(1);
			} else if (l.getEndPoint().equalsExact(currPoint, CATCH_RADIUS)) {
				p = l.getPointN(l.getNumPoints()-2);
			} else {
				throw new RuntimeException("this should not happen!!!");
			}
			double x_diff = currPoint.getX() - p.getX();
			double y_diff = currPoint.getY() - p.getY();
			double length = Math.max(Math.sqrt(x_diff*x_diff + y_diff * y_diff),10.0);
			double scale = l.getLength() / length;
			x_diff *= scale;
			y_diff *= scale;
			Coordinate [] c = new Coordinate [] {currPoint.getCoordinate(), new Coordinate(currPoint.getX() + x_diff, currPoint.getY() + y_diff)};
			LineString tmp = this.geofac.createLineString(c);
			LinearRing lr = (LinearRing) poly.getExteriorRing();
			Geometry v =  lr.intersection(tmp);

			
			if (!v.isEmpty()) {
					poly = addVertex(poly,(Point) v.getGeometryN(0));	
			}
			
		}
		return poly;
		
	}

	private Polygon addVertex(Polygon poly, Point v) {
		LinearRing ls = (LinearRing) poly.getExteriorRing();
		Coordinate [] coords = new Coordinate [ls.getNumPoints()+1];
		coords[0] = ls.getStartPoint().getCoordinate();
		int n = 1;
		boolean notFound = true;
		for (int i =1; i < ls.getNumPoints() ; i ++) {
			LineString seg = this.geofac.createLineString(new Coordinate[] {ls.getPointN(i-1).getCoordinate(),ls.getPointN(i).getCoordinate() });
			if (seg.distance(v) < 0.1 && notFound) {
				notFound = false;
				coords[n++] = v.getCoordinate();
			} 
			coords[n++] = ls.getPointN(i).getCoordinate();
			
			
		}
		
		
		return this.geofac.createPolygon(this.geofac.createLinearRing(coords), null);
	}

	private HashMap<Integer, Polygon> mergePolygons(){
		
		log.info("merging polygons ...");
		
		HashMap<Integer, Polygon> returnPolys = new HashMap<Integer, Polygon>();
		
		for (Iterator<Integer> lsIt = lineStrings.keySet().iterator() ; lsIt.hasNext() ; ){
		
			Integer id = (Integer) lsIt.next();
						
			LineString ls = lineStrings.get(id);
			
			HashSet<Polygon> neighborhood = new HashSet<Polygon>();
			Collection<Polygon> polys = polygonTree.get(ls.getCentroid().getX(),ls.getCentroid().getY() ,1000);

			
			for (Polygon po : polys){
				if(ls.intersects(po)) { 
					neighborhood.add(po);
				}	
			}
			List<Polygon> extNeighborhood = new ArrayList<Polygon>();
    		extNeighborhood.addAll(neighborhood);
    		for (Polygon po : polys) {
    			if (!neighborhood.contains(po)){
//    				LineString pols = po.getExteriorRing();
//    				boolean gotOne = false;
//    				for (Polygon tmp : neighborhood) {
//    					if (po.intersects(tmp)||po.touches(tmp)){
//    						for (int i = 0; i < pols.getNumGeometries(); i++) {
//    							Point p = pols.getPointN(i);
//    							if (p.distance(ls.getStartPoint()) <= 5 ||p.distance(ls.getEndPoint()) <= 5 ) {
//    								extNeighborhood.add(po);
//    								gotOne = true;
//    								break;
//    							}
//    						}
//    					
//    						if (gotOne) {
//    							break;
//    						}
//    					}
//    				}


    				for (Polygon tmp : neighborhood) {
    					
    					if (po.intersects(tmp)||po.touches(tmp) || ls.getStartPoint().distance(po) < CATCH_RADIUS || ls.getEndPoint().distance(po) < CATCH_RADIUS ){
    						extNeighborhood.add(po);
    						break;
    					} 
    				}
    			}
    		}
			
			if(extNeighborhood.isEmpty()){	continue;}

			Geometry [] gA = new Geometry[0];
   			Geometry [] geoArray = extNeighborhood.toArray(gA);
   			GeometryCollection geoColl = new GeometryCollection(geoArray,geofac);	
   			   			 			
   			try{
   				Geometry retPoly = null;
   				for (double dbl = 0.01; dbl <= 0.52; dbl += 0.05) {
   					retPoly = (Geometry) geoColl.buffer(dbl);
	   				if (retPoly.getNumGeometries() > 1) {
	   					if (dbl >= 0.5) {
	   						log.warn("Multipolygon produced in mergePolygons() - setting radius to:" + dbl);
	   			   			for (int i = 0; i < retPoly.getNumGeometries(); i++) {
	   							Polygon polygon = (Polygon) retPoly.getGeometryN(i);
	   							createPolygonFeature(polygon,3,id);
	   			   			}	   						
	   					} else {
	   						log.info("Multipolygon produced in mergePolygons() - setting radius to:" + dbl);
	   					}
	   				} else {
	   					break;
	   				}
	   				
   				}
   				
//   				int iii = 4;
	   			for (int i = 0; i < retPoly.getNumGeometries(); i++) {
					Polygon polygon = (Polygon) retPoly.getGeometryN(i);
//					Polygon p2 = this.geofac.createPolygon((LinearRing) polygon.getExteriorRing(),null);
//					if (id == 2409) {
//						createPolygonFeature(polygon,iii++,id);	
//					}
					
					if(!polygon.isEmpty()){	
//						if (lsId == 2409) 
//						polygon = setAdditionalIntersects(polygon,ls.getStartPoint());
//						polygon = setAdditionalIntersects(polygon,ls.getEndPoint());
	
//						polygon.
						returnPolys.put( id ,polygon);
					}
				}
   			}catch(Exception e){
   				e.printStackTrace();
   			}
   		}

		log.info("done.");
		return returnPolys;
	}
		

	private void parsePolygons()throws Exception{
		
		log.info("parseing features ...");
		
		FeatureCollection collectionPolygon = this.featureSourcePolygon.getFeatures();
		Envelope o = this.featureSourcePolygon.getBounds();
		this.envelope = o;
		this.polygons = new HashSet<Polygon>();
		this.polygonTree = new QuadTree<Polygon>(o.getMinX(), o.getMinY(), o.getMaxX() + (o.getMaxX() - o.getMinX()), o.getMaxY() + (o.getMaxY()-o.getMinY()));
		log.info("\t-PolygonString");
		FeatureIterator it = collectionPolygon.features();
		while (it.hasNext()) {
			Feature feature = it.next();
			
			MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
			for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
				this.add(polygon);
			}

		}
		log.info("\t-LineString");		
		Iterator iit;
		if(!graph){
			this.lineStrings = new HashMap<Integer, LineString>();
			this.lineTree = new QuadTree<Feature>(o.getMinX(), o.getMinY(), o.getMaxX() + (o.getMaxX() - o.getMinX()), o.getMaxY() + (o.getMaxY()-o.getMinY()));
			this.lineStringTree= new QuadTree<LineString>(o.getMinX(), o.getMinY(), o.getMaxX() + (o.getMaxX() - o.getMinX()), o.getMaxY() + (o.getMaxY()-o.getMinY()));
			this.collectionLineString = this.featureSourceLineString.getFeatures();
			it = collectionLineString.features();
			while (it.hasNext()) {
				Feature feature = (Feature) it.next();
				int id = (Integer) feature.getAttribute(1);
				MultiLineString multiLineString = (MultiLineString) feature.getDefaultGeometry();
				
				if (multiLineString.getNumGeometries() > 1) {
					throw new RuntimeException("only one LineString is allowed per MultiLineString");
				}
				LineString lineString = (LineString) multiLineString.getGeometryN(0);
				if (lineString.getNumPoints() <= 1) {
					log.warn("ls consists of <= 1 point! This should not Happen!!");
					continue;
				}
				this.add(id, lineString);
				
				
//				for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
//					LineString lineString = (LineString) multiLineString.getGeometryN(i);
//					this.add(id, lineString);
//					id++;
//				}
			}
		}else{
			iit = featureList.iterator();
			this.lineStrings = new HashMap<Integer, LineString>();
			this.lineTree = new QuadTree<Feature>(o.getMinX(), o.getMinY(), o.getMaxX() + (o.getMaxX() - o.getMinX()), o.getMaxY() + (o.getMaxY()-o.getMinY()));
			this.lineStringTree= new QuadTree<LineString>(o.getMinX(), o.getMinY(), o.getMaxX() + (o.getMaxX() - o.getMinX()), o.getMaxY() + (o.getMaxY()-o.getMinY()));
			while (iit.hasNext()) {
				Feature feature = (Feature) iit.next();
				int id = (Integer) feature.getAttribute(1);
				MultiLineString multiLineString = (MultiLineString) feature.getDefaultGeometry();
				
				if (multiLineString.getNumGeometries() > 1) {
					throw new RuntimeException("only one LineString is allowed per MultiLineString");
				}
				LineString lineString = (LineString) multiLineString.getGeometryN(0);
				if (lineString.getNumPoints() <= 1) {
					log.warn("ls consists of <= 1 point! This should not Happen!!");
					continue;
				}
				this.add(id, lineString);
				
				
//				for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
//					LineString lineString = (LineString) multiLineString.getGeometryN(i);
//					this.add(id, lineString);
//					id++;
//				}
			}
		}

		
		log.info("done.");
	}

	private void add(Polygon po) {
		this.polygons.add(po);
		Point p = po.getCentroid();
		this.polygonTree.put(p.getX(), p.getY(), po);
	}
	
	private void add(Integer id,  LineString ls) throws Exception {
		this.lineStrings.put(id , ls);
		Feature ft = genLineStringFeature(id, ls,-3);
		Point ep = ls.getEndPoint();
		this.lineTree.put(ep.getX(), ep.getY(), ft);
		Point sp = ls.getStartPoint();
		this.lineTree.put(sp.getX(), sp.getY(), ft);
		this.lineStringTree.put(ep.getX(), ep.getY(), ls);
		this.lineStringTree.put(sp.getX(), sp.getY(), ls);
		
	}
	
	private void createPolygonFeature(Polygon polygon, int info, int id ) {
		Feature ft;
		try {
			ft = this.ftPolygon.create(new Object [] {new MultiPolygon(new Polygon []{ polygon  },this.geofac), id, 0.0, 0.0, info},"network");
			this.retPolygons.add(ft);
		} catch (IllegalAttributeException e) {
			e.printStackTrace();
		}

	}
	
	
	private void createPolygonFeatures(HashMap<Integer, Polygon> polygons) {

		try {
			for (Iterator<Entry<Integer, Polygon>> it = polygons.entrySet().iterator() ; it.hasNext() ; ){	
				Entry<Integer, Polygon> e = it.next();
				Feature ft = this.ftPolygon.create(new Object [] {new MultiPolygon(new Polygon []{ e.getValue()  },this.geofac), e.getKey().toString(), 0.0, 0.0, 0},"network");
				this.retPolygons.add(ft);
			}
		} catch (IllegalAttributeException e1) {
			e1.printStackTrace();
		}
	}
	
	

	private Feature genLineStringFeature(Integer iid, LineString ls, int info)throws FactoryRegistryException, SchemaException, IllegalAttributeException, Exception{
		return this.ftLineString.create(new Object [] {ls, iid,info},"lineString");
	}
	
	
	private Collection<Feature> genPointFeatureCollection(HashMap<Integer, Point> interPoints)throws FactoryRegistryException, SchemaException, IllegalAttributeException, Exception{
		
		Collection<Feature> features = new ArrayList<Feature>();
		for (Iterator<Entry<Integer, Point>> it = interPoints.entrySet().iterator() ; it.hasNext() ; ){	
			Entry<Integer, Point> e = it.next();
			Feature ft = this.ftPoint.create(new Object [] { e.getValue() , e.getKey().toString()},"network");
			features.add(ft);
		}
		return features;
	}
	
		
//	private double calcWitdh(LineString ls, List<Polygon> po){
//		
//		
//		List<Double> widths = new ArrayList<Double>();
//		Coordinate [] linkCoord = ls.getCoordinates();	
//									
//		//Breiten ueber Linienpunkte 
//		
//		for(int i=0 ; i < linkCoord.length ; i++){												
//			
//			Coordinate [] g = new Coordinate[]{linkCoord[i]};
//			
//			CoordinateSequence seq = new CoordinateArraySequence(g);
//    		Point point = new Point(seq,geofac);
//			
//    		Iterator<Polygon> polygonIterator = po.iterator();
//    		
//			
//				while(polygonIterator.hasNext()){
//						
//						Polygon polygonGeo = polygonIterator.next();
//						Coordinate coord1;
//						Coordinate coord2;
//						
//						if(i==0){
//							coord1 = new Coordinate(linkCoord[i]);
//							coord2 = new Coordinate(linkCoord[i+1]);
//						} else {
//							coord1 = new Coordinate(linkCoord[i]);
//							coord2 = new Coordinate(linkCoord[i-1]);
//						}
//						
//						Coordinate normiert12 = new Coordinate(subCoord(coord2,coord1).x/getLength(subCoord(coord2,coord1)),subCoord(coord2,coord1).y/getLength(subCoord(coord2,coord1)));
//						
//						Coordinate ortho = new Coordinate(-normiert12.y, normiert12.x);
//						Coordinate coordA = subCoord(coord1, multiCoord(ortho, 100));
//						Coordinate coordB = subCoord(coord1, multiCoord(ortho, -100));
//									
//						Coordinate [] d = new Coordinate[]{coordA, coordB};
//						CoordinateSequence seqd = new CoordinateArraySequence(d);
//						LineString orthoLine = new LineString(seqd, geofac);			
//														
//						if(polygonGeo.intersects(orthoLine)){
//							Coordinate [] inter = polygonGeo.intersection(orthoLine).getCoordinates();
//							for(int iii = 0 ; iii < inter.length-1 ; iii++ ){
//								widths.add(getLength(subCoord(inter[iii],inter[iii+1])));																			
//							}
//									
//						}																				
//					}
//				}
//			
//		//Breiten ueber Polygonpunkte
//		
//		for(Iterator<Polygon> it = po.iterator() ; it.hasNext() ; ){
//			
//			Polygon polygonGeo = it.next();
//			Coordinate [] polyCoord = polygonGeo.getCoordinates();
//
//			for(int i=0 ; i<polyCoord.length-1 ; i++){																		
//				
//				Coordinate polyCoordX = polyCoord[i];
//				
//				for(int ii=0 ; ii<linkCoord.length-1 ; ii++){
//					
//					Coordinate coord1 = new Coordinate(linkCoord[ii]);
//					Coordinate coord2 = new Coordinate(linkCoord[ii+1]);
//												
//					Coordinate normiert12 = new Coordinate(subCoord(coord2,coord1).x/getLength(subCoord(coord2,coord1)),subCoord(coord2,coord1).y/getLength(subCoord(coord2,coord1)));
//					Coordinate ortho = new Coordinate(-normiert12.y, normiert12.x);
//					
//					Coordinate coordA = subCoord(polyCoordX, multiCoord(ortho, 100));
//					Coordinate coordB = subCoord(polyCoordX, multiCoord(ortho, -100));
//					
//					Coordinate [] d = new Coordinate[]{coordA, coordB};
//					CoordinateSequence seqd = new CoordinateArraySequence(d);
//					LineString orthoLine = new LineString(seqd, geofac);
//					
//					Coordinate [] e = new Coordinate[]{coord1, coord2};
//					CoordinateSequence seqe = new CoordinateArraySequence(e);
//					LineString line = new LineString(seqe, geofac);
//													
//					if(polygonGeo.intersects(orthoLine) && orthoLine.intersects(line)){
//						try{
//							Coordinate [] inter = polygonGeo.intersection(orthoLine).getCoordinates();
//							for(int iii = 0 ; iii < inter.length-1 ; iii++ ){
//								widths.add(getLength(subCoord(inter[iii],inter[iii+1])));									
//							}
//						}catch(com.vividsolutions.jts.geom.TopologyException e1){
//							log.info("TopologyException [Geometry.intersection()]: link:");
//						}catch(Exception e1){
//							log.info(e1);
//						}
//						
//					}																					
//				}
//			}					
//		}
//		
//		//Breite
//		double minWidth = 100;
//		for(Iterator<Double> it = widths.iterator() ; it.hasNext() ; ){					
//			Double tmpWidth = (Double) it.next();
//			double tmpWidthValue = tmpWidth.doubleValue();
//			if(tmpWidthValue < minWidth)minWidth = tmpWidthValue;
//		}
//		
//	return minWidth;	
//	}
	
	public static Coordinate multiCoord(Coordinate coordI, double skalar){
		Coordinate coord = new Coordinate(coordI.x*skalar, coordI.y*skalar);
		return coord;
	}
	
	public static Coordinate subCoord(Coordinate coordI, Coordinate coordII){
		Coordinate coord = new Coordinate(coordI.x - coordII.x, coordI.y - coordII.y);
		return coord;
	}
	
	public static double getLength(Coordinate coordI){
		double length = Math.sqrt((coordI.x*coordI.x)+(coordI.y*coordI.y));
		return length;
	}
	
	public static double getAngle(Coordinate coordI, Coordinate coordII){				
		double angle = Math.acos(skalarMultiCoord(coordI,coordII)/(getLength(coordI)*getLength(coordII)));
		return angle;
	}
	
	public static Coordinate addCoord(Coordinate coordI, Coordinate coordII){
		Coordinate coord = new Coordinate(coordI.x + coordII.x, coordI.y + coordII.y);
		return coord;
	}
	
	//Only works for lineStrings with one segment
	public static double getAngle(LineString lI, LineString lII){
		Coordinate [] cI = lI.getCoordinates();
		Coordinate [] cII = lII.getCoordinates();
		Coordinate coordI = subCoord(cI[1],cI[0]);
		Coordinate coordII = subCoord(cII[1],cII[0]);
		
		double angle = Math.acos(skalarMultiCoord(coordI,coordII)/(getLength(coordI)*getLength(coordII)));
		if (Double.isNaN(angle)){//TODO ist das richtig???
			return 0.;
		}
		return Math.toDegrees(angle);
		
	}
	
	public static double skalarMultiCoord(Coordinate coordI, Coordinate coordII){
		double skalarprodukt = (coordI.x*coordII.x) + (coordI.y*coordII.y);
		return skalarprodukt;
	}
	
	//Returns the first (start == true) or last (start == false) segment of a lineString
	//The first point of the returned lineString is the start or end point of the original lineString
	private LineString separateLine(LineString ls, boolean start){
		
		LineString vec;
		Coordinate [] lineIcoor = ls.getCoordinates();
		Coordinate [] coor;
		int length = lineIcoor.length;
		
		
		if(lineIcoor[0] == lineIcoor[1]){
			 coor = new Coordinate [length-1];
			 for(int i = 0 ; i < length-1 ; i++){
				 coor[i] = lineIcoor[i+1];
 			 }
			 lineIcoor = coor;
		}
		
		else if(lineIcoor[length -1] == lineIcoor[length -2]){
			 coor = new Coordinate [length-1];
			 for(int i = 0 ; i < length-1 ; i++){
				 coor[i] = lineIcoor[i];
			 }
			 lineIcoor = coor;
		}
		
		
		if(start){
			CoordinateSequence seqd = new CoordinateArraySequence(new Coordinate[]{new Coordinate(lineIcoor[0]), new Coordinate(lineIcoor[1])});
			vec = new LineString(seqd, geofac);
		} else {
			
			CoordinateSequence seqd = new CoordinateArraySequence(new Coordinate[]{new Coordinate(lineIcoor[lineIcoor.length-1]), new Coordinate(lineIcoor[lineIcoor.length-2])});
			vec = new LineString(seqd, geofac);		
		}
		return vec;
	}
		
	//	LineStrings should have two coordinates. The first should be equal in ls1 and ls2
	private Coordinate getBisectingLine(LineString ls1, LineString ls2){
		
		Coordinate r5;
		
//		if (getAngle(ls1, ls2) == 180){
//			
//			Coordinate c1 = subCoord(ls1.getCoordinates()[1],ls1.getCoordinates()[0]);
//			
//			Coordinate c = new Coordinate( - c1.y , c1.x );
//			Coordinate c2 = multiCoord(addCoord(ls1.getCoordinates()[0], c), (1/getLength(c1)));
//			r5 = multiCoord(c2, 50);
//			
//		}else{
			Coordinate c1 = subCoord(ls1.getCoordinates()[1],ls1.getCoordinates()[0]);
			Coordinate c2 = subCoord(ls2.getCoordinates()[1],ls2.getCoordinates()[0]);
			double c1Length = getLength(c1); 
			double c2Length = getLength(c2);
			Coordinate c11 = multiCoord(c1,(1/c1Length));
			Coordinate c22 = multiCoord(c2,(1/c2Length));
			Coordinate r = 	multiCoord(subCoord(c22,c11),0.5);
			Coordinate r2 = addCoord(ls1.getCoordinates()[0], c11);
			Coordinate r3 = addCoord(r2, r);
			Coordinate r4; 
			
			
			if(getAngle(ls1, ls2) == 180){
				r4 = new Coordinate( - c1.y , c1.x);
			} else {
				r4 = subCoord(r3 , ls1.getCoordinates()[0]);
			}
		
			
//			if(getLength(r4) < 1){
				r4 = multiCoord(r4, (1/getLength(r4))*50);  //Math.min(c1Length, c2Length)
//			}
				 
			r5 = addCoord(ls1.getCoordinates()[0], r4);  //multiCoord(r4, ((c1Length + c2Length)/2))
//		}
//			if (r5 == null){
//				
//				Coordinate c = new Coordinate(0,0);
//				
//				return c;
//			}
//		log.info(r5.toString());
		return r5;
	}
	
	private SortedMap<Double, LineString> sortLines(Collection<LineString> ls, Point point){
		
		SortedMap<Double, LineString> sortedLines = new TreeMap<Double, LineString>();
		List<LineString> lines = new ArrayList<LineString>();
		Coordinate [] c = new Coordinate [] {new Coordinate(0,0),new Coordinate(0,1)};
		CoordinateSequence seq = new CoordinateArraySequence(c);
		LineString yLine = new LineString(seq, geofac);
		Coordinate pc = point.getCoordinate();
		
		for (LineString l : ls){			
			
			
			boolean start = l.getStartPoint().equalsExact(point, CATCH_RADIUS);
			LineString vec = separateLine(l, start);
			lines.add(vec);
		}
		for (LineString line : lines){
			
			Coordinate [] co = line.getCoordinates();
			Coordinate [] newCo = new Coordinate [] {new Coordinate(co[0].x - pc.x, co[0].y - pc.y), new Coordinate(co[1].x - pc.x, co[1].y - pc.y)  };
			CoordinateSequence seq2 = new CoordinateArraySequence(newCo);
			LineString li = new LineString(seq2, geofac);
				
			if((co[1].x - pc.x) < 0){
				sortedLines.put((360 - getAngle(li,yLine)), line);
			}else{
				sortedLines.put(getAngle(li,yLine), line);
			}			
		}
		return sortedLines;
	}
	
	private HashSet<Polygon> separatePoly(Polygon poly , List<Point> points){
		
		HashSet<Polygon> pp = new HashSet<Polygon>();
			
			List<Coordinate> pcoor = new ArrayList<Coordinate>();
			
			for(Point p : points){
				pcoor.add(p.getCoordinate());
			}
			
			if(!pcoor.get(0).equals(pcoor.get(pcoor.size()-1))){
				pcoor.add(pcoor.get(0));
			}
			
//			Coordinate [] cos = new Coordinate[0];
			
			Coordinate [] cos = pcoor.toArray(new Coordinate[pcoor.size()]);
			LinearRing lr = this.geofac.createLinearRing(cos);
			Polygon p = this.geofac.createPolygon(lr, null);
			Polygon b = (Polygon) p.buffer(0.5);
//			createPolygonFeature(b, -1);
			
			
//			if(!poly.contains(p)){
//				log.info("poly touches point: "+ p.toString());
//			}
			
			Geometry geo = (Geometry) poly.difference(b);
			
			
			if (geo.getNumGeometries() == 1){
				pp.add((Polygon) geo);
			}else{
				
				MultiPolygon mPoly = (MultiPolygon) geo;
				for (int i = 0; i < mPoly.getNumGeometries(); i++) {
					Polygon polygon = (Polygon) mPoly.getGeometryN(i);
					pp.add(polygon);
				}
			}
			
	
			
			return pp;		
	}
	
	

//	private HashSet<Polygon> separatePolyII(Polygon poly, List<Point> points){
//		
//		Coordinate [] coords = poly.getCoordinates();
//		HashSet<Polygon> cutPolys = new HashSet<Polygon>();
//				
//		for(int it1 = 0 ; it1 < points.size() ; it1++){        
//			
//			List<Coordinate> newPoly = new ArrayList<Coordinate>();
//			List<Coordinate> restPoly = new ArrayList<Coordinate>();
//			
//			int countPoints = 0;
//						
//			for (int i = 0 ; i < coords.length ; i++){
//				CoordinateSequence seq = new CoordinateArraySequence(new Coordinate []{coords[i]});
//				Point p = new Point(seq, geofac);
//			
//				if (countPoints == 0 || countPoints == points.size()){
//					
//					newPoly.add(coords[i]);				
//					for (Iterator<Point> it = points.iterator() ; it.hasNext() ; ){
//						Point p1 = (Point) it.next();
//						if ((p.equalsExact(p1, CATCH_RADIUS)) && (i != 0)){
//							restPoly.add(coords[i]);						
//							countPoints++;
//							break;
//						}
//					}
//				}else{
//					restPoly.add(coords[i]);
//					for (Iterator<Point> it = points.iterator() ; it.hasNext() ; ){
//						Point p1 = (Point) it.next();
//						
//						if ((countPoints == (points.size()-1)) && (p.equalsExact(p1, CATCH_RADIUS))){
//							newPoly.add(coords[i]);
//							countPoints++;
//							break;
//						}else if (p.equalsExact(p1, CATCH_RADIUS)){
//							countPoints++;
//						}
//					}
//				}
//			}
//			if (newPoly.get(0) != newPoly.get(newPoly.size()-1)){
//				log.warn("newpoly is not closed!");
//				newPoly.add(newPoly.get(0));
//			}
//			
//			if ((!restPoly.isEmpty()) && restPoly.get(0) != restPoly.get(restPoly.size()-1)){
//				log.info("restpoly is not closed!");
//				restPoly.add(restPoly.get(0));
//			}
//			
//			Coordinate [] co = new Coordinate[0];
//			Coordinate [] coNew = newPoly.toArray(co);
//			CoordinateSequence seq = new CoordinateArraySequence(coNew);
//
//			try{
//				LinearRing lr = new LinearRing(seq, geofac);
//				Polygon p = new Polygon(lr, null, geofac);
//				cutPolys.add(p);
//			}catch(Exception e){
//				
//				log.warn("linearRing" + seq);
//			}
//				
//			coords = restPoly.toArray(co);
//		}
//		
//		return cutPolys;
//	}
	
@Deprecated
	private Polygon getControlPoly(Coordinate currPointCoor, LineString [] lineArr, Double [] angleArr, int i){
		//TODO manchmal liefert die methode ein kaputtes polygon -- isValid() == false;
		LineString lineI = lineArr[i];
		double angleI = angleArr[i];
		LineString lineII;
		double angleII;
		double deltaAngle;
		if (i == ( lineArr.length -1) ) {	
			lineII = lineArr[0];
			angleII = angleArr[0];
			
			if ( (angleII - angleI) <= -180 ){
				deltaAngle = 60;
			}else{
				deltaAngle = 270;
			}

		}else {
			lineII = lineArr[i+1];
			angleII = angleArr[i+1];
			deltaAngle = angleII - angleI;
		}
		
		Coordinate bisecCoor = getBisectingLine(lineII, lineI);
		
		if (deltaAngle > 180 ){
			Coordinate co = subCoord(currPointCoor, bisecCoor); 
			bisecCoor = addCoord(currPointCoor, co);
		}
								
		Coordinate [] quadcoor = new Coordinate [] {currPointCoor, lineI.getEndPoint().getCoordinate(),
				bisecCoor, lineII.getEndPoint().getCoordinate(), currPointCoor}; 
		CoordinateSequence triseq = new CoordinateArraySequence(quadcoor);
		LinearRing quadRing = new LinearRing(triseq, geofac);
		Polygon controlPoly = new Polygon(quadRing, null, geofac);
//		log.info("controlPoly: "+controlPoly.toString());
		return controlPoly;
	}
	private Polygon getControlPolygon(Point p, double angle1, org.matsim.utils.collections.gnuclasspath.TreeMap<Double, LineString> sortedLs) {
		
		LineString ls1 = sortedLs.get(angle1);
		Entry e = sortedLs.higherEntry(angle1);
		if (e == null) {
			e = sortedLs.firstEntry();
		}
		LineString ls2 = (LineString) e.getValue();
		double angle2 = (Double) e.getKey();
		double dA = angle2 - angle1;
		dA = dA > 0  ? dA : 360 + dA;
		
		Coordinate bisecCoor = getBisectingLine(ls1, ls2);
		
		if (dA > 180 ){
			Coordinate co = subCoord(p.getCoordinate(), bisecCoor); 
			bisecCoor = addCoord(p.getCoordinate(), co);
		}
								
		Coordinate [] quadcoor = new Coordinate [] {p.getCoordinate(), ls1.getEndPoint().getCoordinate(),
				bisecCoor, ls2.getEndPoint().getCoordinate(), p.getCoordinate()}; 
		CoordinateSequence triseq = new CoordinateArraySequence(quadcoor);
		LinearRing quadRing = new LinearRing(triseq, geofac);
		Polygon controlPoly = new Polygon(quadRing, null, geofac);
//		log.info("controlPoly: "+controlPoly.toString());
		if (!controlPoly.isValid()) { //TODO das darf nicht passieren
			return null;
		}
		return controlPoly;

	}
	
	
}
