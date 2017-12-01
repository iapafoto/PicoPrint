/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picoprint;

/**
 *
 * @author durands
 */
import java.awt.*;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import java.awt.geom.Path2D;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.*;
import javax.swing.*;

/**
 * A Java class to demonstrate how to load an image from disk with the ImageIO
 * class. Also shows how to display the image by creating an ImageIcon, placing
 * that icon an a JLabel, and placing that label on a JFrame.
 *
 * @author alvin alexander, alvinalexander.com
 */
public class ImageToDrawEvaluateLine {
    public static final double PEN_BLACKNESS = 64;
    
    static class Panel extends Container {
        List<Path2D> pathRemix = null;
        public float time=0;

        Panel(List<Path2D> pathRemix) {
            this.pathRemix = pathRemix;
        }
        
        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics;
           
            g2.setStroke(new BasicStroke(1.f));
            g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
            
        //    if (lineDrawn != null) {
        //        g2.drawImage(lineDrawn, 0,0, this);
        //    }
//           
            g2.setColor(new Color(0,0,0,(int)PEN_BLACKNESS));
       //   g2.draw(pathRemix);
            
            for (int t=0; t<pathRemix.size(); t++) {
                g2.draw(pathRemix.get(t));
            }

        }

        private void setTime(int d) {
            time = d;
        }
    };

    public static void main(String[] args) throws Exception {
  
        JFrame editorFrame = new JFrame("Image Demo");
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

       // String filename = "C:\\Users\\durands\\Desktop\\cerf.png"; //visage.png"; //cerf.png";
       String filename = "C:\\Users\\durands\\Desktop\\drawer\\1489679025811.jpg"; //noel.gif";//quest.png"; //visage.png"; //cerf.png";
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        List<Path2D> pathRemix = toCurvePath/*toLinePath*/(findDrawPath(image));

        editorFrame.setSize(image.getWidth(), image.getHeight());

        final Panel panel = new Panel(pathRemix);

        editorFrame.setContentPane(panel);
        editorFrame.setVisible(true);
   //     Thread queryThread = new Thread() {
   //         public void run() {
                
        for (int d=0; d<pathRemix.size(); d+=50) {
            final int t = d;
//               SwingUtilities.invokeLater(() -> {
                panel.setTime(t);
                panel.repaint();
//             });
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   
      //      }
      //    };
      //    queryThread.start();
     //   SwingUtilities.invokeLater(new Runnable() {
     //       public void run() {         
               
       //     }
       // });
    }
    
    public static double map(double i0, double i1, double o0, double o1, double i) {
        return o0 + (i-i0)*(o1-o0)/(i1-i0);
    }
    
    final public static double smoothstep(final double edge0, final double edge1, final double x) {
        final double t = Math.min(Math.max((x - edge0) / (edge1 - edge0), 0), 1);
        return t * t * (3 - t-t);
    }
    
    private static int[] findZoneToDraw(double[][] buffSrc, double[][] buffDrawing, int step, int r) {
        // On recherche la zones dans laquels on doit le plus dessiner
        int[] diffZone;
        int w = buffSrc.length, h = buffSrc[0].length;
        int[] diff = {w/2, h/2, -Integer.MAX_VALUE};

        for (int cx=r; cx<w-r; cx+=step) {
            for (int cy=r; cy<h-r; cy+=step) {
                diffZone = evaluateZone(buffSrc, buffDrawing, cx,cy, r);
                if (diffZone[2] > diff[2]) {
                    diff = diffZone;
                }
            }
        }
        return diff;
    }
    
    private static int[] evaluateZone(double[][] buffSrc, double[][] buffDrawing, int cx, int cy, int r) {
        // On evalue la difference dans une sous zone si on doit encore dessiner, diff est > 0
        // TODO mmoyen d'optimizer les parcours 
        double diff = 0, diffloc = 0, difflocmax = 0;
        int w = buffSrc.length, h = buffSrc[0].length;
        int x,y,xmax=cy, ymax=cx,rr = r*r;
        int cpt=0;
        for (int dx=-r; dx<r; dx++) {
            for (int dy=-r; dy<r; dy++) {
                if (dx*dx + dy*dy < rr) {
                    x = cx+dx;
                    y = cy+dy;
                    if (x>=0 && y>=0 && x<w && y<h) {
                        diffloc = buffSrc[x][y] - buffDrawing[x][y];
                        diff += diffloc;
                        cpt++;
                        if (diffloc > difflocmax) {
                            xmax = x;
                            ymax = y;
                        }
                    }
                }
            }
        }
        return new int[] {xmax, ymax, (int)(10.*diff/cpt)};
    }
    
    private static double evaluateLine(double[][] buffSrc, double[][] buffDrawing, int x1, int y1, int x2, int y2, double blackness) {
        int d = 0;
        int dx = Math.abs(x2 - x1),
            dy = Math.abs(y2 - y1);
        int dx2 = 2 * dx, // slope scaling factors to
            dy2 = 2 * dy; // avoid floating point
        int ix = x1 < x2 ? 1 : -1, // increment direction
            iy = y1 < y2 ? 1 : -1;
        int x = x1, y = y1;
        double diff = 0;

        // De combien ca nous rapproche de la realitee
        if (dx >= dy) {
            while (true) {
                if (x != x1) diff += /*Math.abs*/Math.abs((buffDrawing[x][y] + blackness) - buffSrc[x][y]) - Math.abs((buffDrawing[x][y] - buffSrc[x][y]));
                if (x == x2) break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                if (y != y1) diff += /*Math.abs*/Math.abs((buffDrawing[x][y] + blackness) - buffSrc[x][y]) - Math.abs((buffDrawing[x][y] - buffSrc[x][y]));
                if (y == y2) break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
        return diff;
    }
    
    // TODO: remplacer par Xiaolin_Wu
    // https://rosettacode.org/wiki/Xiaolin_Wu%27s_line_algorithm#Java
    private static double drawLine(double[][] buffDrawing, int x1, int y1, int x2, int y2, double blackness) {
        // delta of exact value and rounded value of the dependent variable
        int d = 0;
        int dx = Math.abs(x2 - x1),
            dy = Math.abs(y2 - y1);
        int dx2 = 2 * dx, // slope scaling factors to
            dy2 = 2 * dy; // avoid floating point
        int ix = x1 < x2 ? 1 : -1, // increment direction
            iy = y1 < y2 ? 1 : -1;
        int x = x1, y = y1;
        double diff = 0;
        
        if (dx >= dy) {
            while (true) {
                if (x != x1) buffDrawing[x][y] += blackness;
                if (x == x2) break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                if (y != y1) buffDrawing[x][y] += blackness;
                if (y == y2) break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
        return diff;
    }

    public static double getAngleDiffRad(final double source, final double target) {
        return (target - source + Math.PI) % (2.*Math.PI) - Math.PI;
    }
    
    public static List<Path2D> toLinePath(double[] pts) {
        List<Path2D> paths = new ArrayList();
        
        Path2D path = new Path2D.Double();
        double x,y;
      
        x = pts[0]; y=pts[1];
        path.moveTo(x, y);
        for (int i=2; i<pts.length-4; i+=2) {
            path.lineTo(pts[i], pts[i+1]);
           // if (i%10==0) {
//                paths.add(path);
//                path = new Path2D.Double();
             //   path.lineTo(pts[i], pts[i+1]);
        //    }
        //    x = pts[i]; y = pts[i+1];
            
        }
        paths.add(path);

        return paths;
    }
    
    public static List<Path2D> toCurvePath(double[] pts) {
        List<Path2D> paths = new ArrayList();
        double x,y,x2,y2;
        x = (pts[0]+pts[2])/2.;
        y = (pts[1]+pts[3])/2.;
        for (int i=2; i<pts.length-4; i+=2) {
            Path2D path = new Path2D.Double();
            x2 = (pts[i]+pts[i+2])/2.;
            y2 = (pts[i+1]+pts[i+3])/2.;
            path.moveTo(x, y);
            path.quadTo(pts[i], pts[i+1], x2,y2);
            if (Math.sqrt((x-x2)*(x-x2)+(y-y2)*(y-y2)) < 128)
                paths.add(path);
            x = x2; y = y2;
        }
        return paths;
    }
    
    
    public static double[] findDrawPath(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        BufferedImage img2 = new BufferedImage(w,h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img2.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        byte[] data = ((DataBufferByte)(img2.getRaster().getDataBuffer())).getData();
        
        double srcImg[][] = new double[w][h];
        double drawnImg[][] = new double[w][h];
        
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                srcImg[x][y] = 255-(data[y*w+x]&0xff); // le sombre c'est la ou l'on doit dessiner
                drawnImg[x][y] = 0;  // rien de dessine pour le moment
            }
        }
           
        double k, dx, dy;
    
        
  //      List<Line2D> path = new ArrayList();
        double x = w*Math.random()-1, y = h*Math.random()-1;
  //      path.moveTo(x,y);
        double a, dbest =1, xbest=x, ybest=y, xmem = x, ymem = y, kinit, k0, x1, y1, amem=0, abest = 0;
                
        double[] pts = new double[(int)(w*h*1.)];
        int nb = pts.length/2;
        int ptid = 0;
        
        int[] zoneToDraw = findZoneToDraw(srcImg, drawnImg, w/20, w/10);
     //   System.out.println("" + zoneToDraw[2]);
        if (zoneToDraw[2] < 0) {
            xmem = x = zoneToDraw[0];
            ymem = y = zoneToDraw[1];
        }
        
       int zoneToDrawWorst = 0;
     
        double xzone = 0, yzone = 0;
        double lenTotal = 0;
        double vkinit = 0;
        
        for (int i=0; i<nb; i++) {
            
            if (i%(nb/100) == 0) { // On test si on doit bientot arreter
                zoneToDraw = findZoneToDraw(srcImg, drawnImg, w/10, w/5);
                System.out.println("" + zoneToDraw[2]);
                if (zoneToDraw[2] < 0 /*|| zoneToDraw[2] == zoneToDrawWorst*/) {
                    // On a deja trop dessiné partout => fin du dessin
                    break;
                }
                zoneToDrawWorst = zoneToDraw[2];
              //  if (i%(nb/100) == 0) {
                    xzone = zoneToDraw[0];
                    yzone = zoneToDraw[1];
              //  }
            }
            
            
            // La longeur du segment depend de la couleur de la zone (plus c 'est sombre plus c'est cour)
            
      //      if (i>pts.lengthpts.length/8) {
      //          kinit = 32 + 128*smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
          //  if (i<nb/16) {
          //      kinit = 32 + (128./256.)*(double)(data[(int)y*w+(int)x]&0xFF); //smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
          //  } else 
          
       //     if (i<nb/4) {
               // draw = new byte[w*h];
       //         kinit = 8 + (64./256.)*(double)(data[(int)y*w+(int)x]&0xFF); //smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
        //   } else 
            {

                // todo vider draw[] a la transition
                kinit = 3 + (48./256.)*(double)(data[(int)y*w+(int)x]&0xFF);//*/ 64*smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
              //  kinit = Math.max(3,(.001*kinit + .999*vkinit));
              //  vkinit = kinit;
            }
            
         //   kinit = 4+(16./256.)*(double)(data[(int)y*w+(int)x]&0xFF);
            double bestScore = Double.MAX_VALUE;
            boolean superGood = false, allBad = true;
            // On recherche le meilleur angle pour dessiner la ligne
            for (int j=0; j<1000; j++) {
                a = ThreadLocalRandom.current().nextDouble(0, 6.285);
                k = /*kinit; */Math.max(3, .5*kinit + Math.abs(.5*ThreadLocalRandom.current().nextGaussian()*kinit));

                dx = k*Math.cos(a);
                dy = k*Math.sin(a);

                if (x+dx < 0 || x+dx >=w) dx = -dx; 
                if (y+dy < 0 || y+dy >=h) dy = -dy;

                x1 = Math.min(Math.max(0,x+dx),w-1);
                y1 = Math.min(Math.max(0,y+dy),h-1);
                
                double len = Math.max(Math.abs((int)x-(int)x1),Math.abs((int)y-(int)y1));
                double maxScore = PEN_BLACKNESS*len;
                double differenceToReality = evaluateLine(srcImg, drawnImg, (int)xmem,(int)ymem,(int)x1,(int)y1, PEN_BLACKNESS) ;
                
                if (differenceToReality >= maxScore) {
                    superGood = true;
                    allBad = false;
                  //  kinit *= 1.1;
                } else if (differenceToReality <= -maxScore+2*PEN_BLACKNESS) {
                    int bad = 1;
                } else {
                    allBad = false;
                    // normal case
                }
            //    differenceToReality = -(maxScore - differenceToReality)/len; 
                differenceToReality /= len;
                 
        //(draw[(int)(x1/sz)+(int)(y1/sz)*(int)(w/sz)] == 1) ? 5000.:1) + (double)(data[(int)(y1)*w+(int)(x1)]&0xFF);
            //    if (differenceToReality > 0) differenceToReality = differenceToReality/3+ThreadLocalRandom.current().nextDouble(0, differenceToReality*2/3); // pour permettre a tous les choix d e pouvoir gagner
                // mais en privilegiant un peu les meilleurs
             
                // On privilegie les faible changement d'angles
              //  differenceToReality -= 1.5*getAngleDiffRad(a, amem);
                
                if (differenceToReality < bestScore) {
                    bestScore = differenceToReality;
                    xbest = x+dx;
                    ybest = y+dy;
                    dbest = Math.max(dx, dy);
                    abest = a;
                }
                
                // Pas besoin de tester les autres (sutotu si on est a taille constante
                //if (superGood) break;
            }
            
      
            if (kinit>10 && allBad) {
               // x = xzone;
               // y = yzone;
           //     continue;
            }
         
       // On est trop eloigne de la realitee
         //       zoneToDraw = findZoneToDraw(srcImg, drawnImg, w/10, w/5);
                
              /*  if (zoneToDraw[2] < 0 {
                    // On a deja trop dessiné partout => fin du dessin
                    break;
                }
                x = zoneToDraw[0];
                y = zoneToDraw[1];
                zoneToDrawWorst = zoneToDraw[2];
                continue;
            } else */
            {
                x = xbest;
                y = ybest;
            }
            
            if (kinit>10 && allBad) {
               // x = xzone;
               // y = yzone;
                continue;
            }
  
            // AU cas où ca deborde quand meme
            x = Math.min(Math.max(0,x),w-1);
            y = Math.min(Math.max(0,y),h-1);
        
         //  path.add(new Line2D.Double(xmem,ymem, x,y));
            pts[ptid*2] = x;
            pts[ptid*2+1] = y;
            ptid++;
            lenTotal += Math.sqrt((xmem-x)*(xmem-x) + (ymem-y)*(ymem-y))-1;
            drawLine(drawnImg, (int)xmem, (int)ymem, (int)x, (int)y, PEN_BLACKNESS);

     //    drawLine(draw, (int)(w/sz), (int)(h/sz), (int)(xmem/sz),(int)(ymem/sz), (int)(x/sz),(int)(y/sz));
            xmem = x; ymem = y;
            amem = abest;
        }

        lineDrawn = arrayToBufferedImage(drawnImg);
        System.out.println("TotalLen = " +lenTotal);
                
        return Arrays.copyOf(pts, ptid*2);
    }
    
    public static BufferedImage lineDrawn = null;
    
    static BufferedImage arrayToBufferedImage(double[][] drawnImg) {
        int w = drawnImg.length, h = drawnImg[0].length;
        
        BufferedImage img2 = new BufferedImage(w,h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte)(img2.getRaster().getDataBuffer())).getData();
      
        for (int xx=0; xx<w; xx++) {
            for (int yy=0; yy<h; yy++) {
                data[xx+yy*w] = (byte)(Math.min(Math.max(0,255 - drawnImg[xx][yy]),255));
            }
        }
        return img2;
    }
}
