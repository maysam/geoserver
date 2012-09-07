/* Copyright (c) 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.csw.records;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.ComplexFeatureBuilder;
import org.geotools.feature.LenientFeatureFactoryImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A helper that builds CSW Dublin core records as GeoTools features
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class CSWRecordBuilder {

    ComplexFeatureBuilder fb = new ComplexFeatureBuilder(CSWRecordTypes.RECORD);

    AttributeBuilder ab = new AttributeBuilder(new LenientFeatureFactoryImpl());

    List<ReferencedEnvelope> boxes = new ArrayList<ReferencedEnvelope>();

    /**
     * Adds an element to the current record
     * 
     * @param name
     * @param values
     */
    public void addElement(String name, String... values) {
        for (String value : values) {
            AttributeDescriptor descriptor = CSWRecordTypes.getDescriptor(name);
            ab.setDescriptor(descriptor);
            ab.add(null, value, CSWRecordTypes.SIMPLE_LITERAL_VALUE);
            Attribute element = ab.build();

            fb.append(CSWRecordTypes.DC_ELEMENT_NAME, element);
        }
    }

    /**
     * Adds a bounding box to the record. The envelope must be in WGS84
     * 
     * @param env
     */
    public void addBoundingBox(ReferencedEnvelope env) {
        boxes.add(env);
        ab.setDescriptor(CSWRecordTypes.RECORD_BBOX_DESCRIPTOR);
        Attribute element = ab.buildSimple(null, env);

        fb.append(CSWRecordTypes.RECORD_BBOX_NAME, element);
    }

    /**
     * Builds a record and sets up to work on the next one
     * 
     * @param id
     * @return
     */
    public Feature build(String id) {
        // gather all the bounding boxes in a single geometry
        Geometry geom = null;
        for (ReferencedEnvelope env : boxes) {
            try {
                env = env.transform(DefaultGeographicCRS.WGS84, true);

                Polygon poly = JTS.toGeometry(env);
                poly.setUserData(DefaultGeographicCRS.WGS84);
                if (geom == null) {
                    geom = poly;
                } else {
                    geom = geom.union(poly);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Failed to reproject one of the bounding boxes to WGS84, "
                                + "this should never happen with valid coordinates", e);
            }
        }
        if(geom instanceof Polygon) {
            geom = geom.getFactory().createMultiPolygon(new Polygon[] {(Polygon) geom}); 
        }

        ab.setDescriptor(CSWRecordTypes.RECORD_GEOMETRY_DESCRIPTOR);
        Attribute element = ab.buildSimple(null, geom);
        fb.append(CSWRecordTypes.RECORD_GEOMETRY_NAME, element);

        boxes.clear();
        return fb.buildFeature(id);
    }

}
