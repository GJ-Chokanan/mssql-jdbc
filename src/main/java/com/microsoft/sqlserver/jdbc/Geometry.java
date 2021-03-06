/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */

package com.microsoft.sqlserver.jdbc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Geometry datatype represents data in a Euclidean (flat) coordinate system.
 */

public class Geometry extends SQLServerSpatialDatatype {

    /**
     * Private constructor used for creating a Geometry object from WKT and Spatial Reference Identifier.
     * 
     * @param wkt
     *        Well-Known Text (WKT) provided by the user.
     * @param srid
     *        Spatial Reference Identifier (SRID) provided by the user.
     * @throws SQLServerException
     *         if an exception occurs
     */
    private Geometry(String wkt, int srid) throws SQLServerException {
        if (null == wkt || wkt.length() <= 0) {
            throwIllegalWKT();
        }

        this.wkt = wkt;
        this.srid = srid;

        parseWKTForSerialization(this, currentWktPos, -1, false);

        serializeToWkb(false, this);
        isNull = false;
    }

    /**
     * Private constructor used for creating a Geometry object from WKB.
     * 
     * @param wkb
     *        Well-Known Binary (WKB) provided by the user.
     * @throws SQLServerException
     *         if an exception occurs
     */
    private Geometry(byte[] wkb) throws SQLServerException {
        if (null == wkb || wkb.length <= 0) {
            throwIllegalWKB();
        }

        this.wkb = wkb;
        buffer = ByteBuffer.wrap(wkb);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        parseWkb(this);

        WKTsb = new StringBuffer();
        WKTsbNoZM = new StringBuffer();

        constructWKT(this, internalType, numberOfPoints, numberOfFigures, numberOfSegments, numberOfShapes);

        wkt = WKTsb.toString();
        wktNoZM = WKTsbNoZM.toString();
        isNull = false;
    }

    /**
     * Constructor for a Geometry instance from an Open Geospatial Consortium (OGC) Well-Known Text (WKT) representation
     * augmented with any Z (elevation) and M (measure) values carried by the instance.
     * 
     * @param wkt
     *        Well-Known Text (WKT) provided by the user.
     * @param srid
     *        Spatial Reference Identifier (SRID) provided by the user.
     * @return Geometry Geometry instance created from WKT and SRID
     * @throws SQLServerException
     *         if an exception occurs
     */
    public static Geometry STGeomFromText(String wkt, int srid) throws SQLServerException {
        return new Geometry(wkt, srid);
    }

    /**
     * Constructor for a Geometry instance from an Open Geospatial Consortium (OGC) Well-Known Binary (WKB)
     * representation.
     * 
     * @param wkb
     *        Well-Known Binary (WKB) provided by the user.
     * @return Geometry Geometry instance created from WKB
     * @throws SQLServerException
     *         if an exception occurs
     */
    public static Geometry STGeomFromWKB(byte[] wkb) throws SQLServerException {
        return new Geometry(wkb);
    }

    /**
     * Constructor for a Geometry instance from an internal SQL Server format for spatial data.
     * 
     * @param wkb
     *        Well-Known Binary (WKB) provided by the user.
     * @return Geometry Geometry instance created from WKB
     * @throws SQLServerException
     *         if an exception occurs
     */
    public static Geometry deserialize(byte[] wkb) throws SQLServerException {
        return new Geometry(wkb);
    }

    /**
     * Constructor for a Geometry instance from an Open Geospatial Consortium (OGC) Well-Known Text (WKT)
     * representation. Spatial Reference Identifier is defaulted to 0.
     * 
     * @param wkt
     *        Well-Known Text (WKT) provided by the user.
     * @return Geometry Geometry instance created from WKT
     * @throws SQLServerException
     *         if an exception occurs
     */
    public static Geometry parse(String wkt) throws SQLServerException {
        return new Geometry(wkt, 0);
    }

    /**
     * Constructor for a Geometry instance that represents a Point instance from its X and Y values and an Spatial
     * Reference Identifier.
     * 
     * @param x
     *        x coordinate
     * @param y
     *        y coordinate
     * @param srid
     *        Spatial Reference Identifier value
     * @return Geometry Geography instance
     * @throws SQLServerException
     *         if an exception occurs
     */
    public static Geometry point(double x, double y, int srid) throws SQLServerException {
        return new Geometry("POINT (" + x + " " + y + ")", srid);
    }

    /**
     * Returns the Open Geospatial Consortium (OGC) Well-Known Text (WKT) representation of a Geometry instance. This
     * text will not contain any Z (elevation) or M (measure) values carried by the instance.
     * 
     * @return the WKT representation without the Z and M values.
     * @throws SQLServerException
     *         if an exception occurs
     */
    public String STAsText() throws SQLServerException {
        if (null == wktNoZM) {
            buffer = ByteBuffer.wrap(wkb);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            parseWkb(this);

            WKTsb = new StringBuffer();
            WKTsbNoZM = new StringBuffer();
            constructWKT(this, internalType, numberOfPoints, numberOfFigures, numberOfSegments, numberOfShapes);
            wktNoZM = WKTsbNoZM.toString();
        }
        return wktNoZM;
    }

    /**
     * Returns the Open Geospatial Consortium (OGC) Well-Known Binary (WKB) representation of a Geometry instance. This
     * value will not contain any Z or M values carried by the instance.
     * 
     * @return byte array representation of the Geometry object.
     */
    public byte[] STAsBinary() {
        if (null == wkbNoZM) {
            serializeToWkb(true, this);
        }
        return wkbNoZM;
    }

    /**
     * Returns the bytes that represent an internal SQL Server format of Geometry type.
     * 
     * @return byte array representation of the Geometry object.
     */
    public byte[] serialize() {
        return wkb;
    }

    /**
     * Returns if the object contains a M (measure) value.
     * 
     * @return boolean that indicates if the object contains M value.
     */
    public boolean hasM() {
        return hasMvalues;
    }

    /**
     * Returns if the object contains a Z (elevation) value.
     * 
     * @return boolean that indicates if the object contains Z value.
     */
    public boolean hasZ() {
        return hasZvalues;
    }

    /**
     * Returns the X coordinate value.
     * 
     * @return double value that represents the X coordinate.
     */
    public Double getX() {
        if (null != internalType && internalType == InternalSpatialDatatype.POINT && xValues.length == 1) {
            return xValues[0];
        }
        return null;
    }

    /**
     * Returns the Y coordinate value.
     * 
     * @return double value that represents the Y coordinate.
     */
    public Double getY() {
        if (null != internalType && internalType == InternalSpatialDatatype.POINT && yValues.length == 1) {
            return yValues[0];
        }
        return null;
    }

    /**
     * Returns the M (measure) value of the object.
     * 
     * @return double value that represents the M value.
     */
    public Double getM() {
        if (null != internalType && internalType == InternalSpatialDatatype.POINT && hasM()) {
            return mValues[0];
        }
        return null;
    }

    /**
     * Returns the Z (elevation) value of the object.
     * 
     * @return double value that represents the Z value.
     */
    public Double getZ() {
        if (null != internalType && internalType == InternalSpatialDatatype.POINT && hasZ()) {
            return zValues[0];
        }
        return null;
    }

    /**
     * Returns the Spatial Reference Identifier (SRID) value.
     * 
     * @return int value of SRID.
     */
    public int getSrid() {
        return srid;
    }

    /**
     * Returns if the Geometry object is null.
     * 
     * @return boolean that indicates if the object is null.
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Returns the number of points in the Geometry object.
     * 
     * @return int that indicates the number of points in the Geometry object.
     */
    public int STNumPoints() {
        return numberOfPoints;
    }

    /**
     * Returns the Open Geospatial Consortium (OGC) type name represented by a geometry instance.
     * 
     * @return String that contains the Geometry object's type name
     */
    public String STGeometryType() {
        if (null != internalType) {
            return internalType.getTypeName();
        }
        return null;
    }

    /**
     * Returns the Well-Known Text (WKT) representation of the Geometry object.
     * 
     * @return String that contains the WKT representation of the Geometry object.
     */
    public String asTextZM() {
        return wkt;
    }

    /**
     * Returns the String representation of the Geometry object.
     * 
     * @return String that contains the WKT representation of the Geometry object.
     */
    @Override
    public String toString() {
        return wkt;
    }
}
