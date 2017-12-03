/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package bezier;

import java.awt.geom.Path2D;

/**
 * A handler class that generates an array of shorts and an array doubles from
 * parsing path data.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id: PathArrayProducer.java 475685 2006-11-16 11:16:05Z cam $
 */
public class BezierListProducer {

    Path2D path = new Path2D.Double();

    double 
        startPointX, startPointY,
        lastPointX, lastPointY,
        lastKnotX, lastKnotY;

    void setLastPoint(double x, double y) {
    	lastPointX = x;
        lastPointY = y; 
    }
    
    void setLastKnot(double x, double y) {
    	lastKnotX = x;
        lastKnotY = y;
    }
    
    public void startPath()  {
        path = new Path2D.Double();
    }

    public void movetoRel(double x, double y) {
        movetoAbs(lastPointX + x, lastPointY + y);;
    }
    
    public void movetoAbs(double x, double y)  {
      //  startPointX = x;
      //  startPointY = y;
    	lastPointX = x;
        lastPointY = y;
        path.moveTo(x,y);
    }

    public void closePath()  {
        path.closePath();
    }

    public void linetoRel(double x, double y) {
        linetoAbs(lastPointX + x, lastPointY + y);
    }
   
    public void linetoAbs(double x, double y)  {
        path.lineTo(x,y);
        setLastPoint(x, y);
        setLastKnot(x, y);
    }

   
    public void linetoHorizontalRel(double x)  {
    	linetoAbs(x + lastPointX, lastPointY);
    }

   
    public void linetoHorizontalAbs(double x)  {
    	linetoAbs(x, lastPointY);
    }

   
    public void linetoVerticalRel(double y)  {
    	linetoAbs(lastPointX, y + lastPointY);
    }

    public void linetoVerticalAbs(double y)  {
    	linetoAbs(lastPointX, y);
    }
   
    public void curvetoCubicRel(double x1, double y1, 
                                double x2, double y2, 
                                double x, double y)  {
        curvetoCubicAbs(x1 + lastPointX, y1 + lastPointY,
                x2 + lastPointX, y2 + lastPointY, 
                x + lastPointX, y + lastPointY);
    }

   
    public void curvetoCubicAbs(double x1, double y1, 
                                double x2, double y2, 
                                double x, double y)  {
        path.curveTo(x1, y1, x2, y2, x, y);
        setLastPoint(x, y);
        setLastKnot(x2, y2);
    }
   
    public void curvetoCubicSmoothRel(double x2, double y2, 
                                      double x, double y)  {
        curvetoCubicSmoothAbs(x2 + lastPointX, y2 + lastPointY, x + lastPointX, y + lastPointY);
    }

   
    public void curvetoCubicSmoothAbs(double x2, double y2, 
                                      double x, double y)  {
        path.curveTo(lastPointX * 2f - lastKnotX, lastPointY * 2f - lastKnotY, x2, y2, x, y);
        setLastPoint(x, y);
        setLastKnot(x2, y2);
    }

   
    public void curvetoQuadraticRel(double x1, double y1, 
                                    double x, double y)  {
          curvetoQuadraticAbs(x1 + lastPointX, y1 + lastPointY, x + lastPointX, y + lastPointY);
    }

   
    public void curvetoQuadraticAbs(double x1, double y1, 
                                    double x, double y)  {
        path.quadTo(x1, y1, x, y);
    	setLastPoint(x, y);
        setLastKnot(x1, y1);
    }

   
    public void curvetoQuadraticSmoothRel(double x, double y) {
	curvetoQuadraticSmoothAbs(x + lastPointX, y + lastPointY);
    }

    public void curvetoQuadraticSmoothAbs(double x, double y) {
    	curvetoQuadraticAbs(lastKnotX, lastKnotY, x, y);
    }
   
    public void arcRel(double rx, double ry, 
                       double xAxisRotation, 
                       double largeArcFlag, double sweepFlag, 
                       double x, double y)  {
        arcAbs(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x+lastPointX, y+lastPointY);
    }
   
    public void arcAbs(double rx, double ry, 
                       double xAxisRotation, 
                       double largeArcFlag, double sweepFlag, 
                       double x, double y)  {
        // TODO
    }

   
    public void endPath()  {
    	setLastPoint(startPointX, startPointY);
        setLastKnot(startPointX, startPointY);
    }
    
}
