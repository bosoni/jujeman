/**
 * @file Mask.java
 * @author mjt, 2007 
 * mixut@hotmail.com
 * 
 * joka pixelille:
 *  piirr‰ pixeli ruudulle jos piirrett‰v‰n objektin y suurempi kuin siin‰ kohdassa
 *  oleva pixelin v‰riarvo zbuf kuvassa.
 *  eli jos zbuf kuvaan piirretty joku 255,255,255 (valkoinen), sen eteen ei voi menn‰,
 *  objekti piirtyy aina sen taakse (alpha 0).
 *
 */
// Custom Filters tutorial:
// http://www.particle.kth.se/~lindsey/JavaCourse/Book/Part1/Supplements/Chapter11/customFilter.html
package tstgame;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

public class Mask
{
    public final BufferedImage filter(BufferedImage source_img, BufferedImage zbuf, int xx, int yy, int lowy)
    {
	if(source_img==null || zbuf==null) return source_img;
	
	BufferedImage dest_img = createCompatibleDestImage(source_img, null);
	
	int width = source_img.getWidth();
	int height= source_img.getHeight();
	
	// bx ja by muuttujat pit‰‰ huolta ettei piirret‰ yli ruudun
	
	int by=yy;
	for (int y=0; y < height; y++)
	{
	    if(by<0) { y+=-by; by=0; }
	    if(by>=Main.WINHEIGHT) break;
	    
	    int bx=xx;
	    for (int x=0; x < width; x++)
	    {
		if(bx<0) { x+=-bx; bx=0; }
		if(bx>=Main.WINWIDTH) break;
		
		int pixel=0;
		pixel=source_img.getRGB(x, y);
		if((pixel&0xFF000000)!=0) 
		{
		    int z=zbuf.getRGB(bx, by)&0xFFFFFF;

		    if( lowy < z) 
		    {
			dest_img.setRGB(x, y, 0); // ei n‰y
		    }
		    else 
		    {
			dest_img.setRGB(x, y, pixel); // n‰kyy

			// p‰ivit‰ zbufferi
			zbuf.setRGB(bx, by, lowy-1);
		    }
		}
		
		bx++;
	    }
	    
	    by++;
	}
	return dest_img;
    }
    
    
    /**
     *  Create a destination image if needed. Must be same width as source
     *  and will by default use the same color model. Otherwise, it will use
     *  the one passed to it.
     **/
    public BufferedImage createCompatibleDestImage(BufferedImage source_img,
	 ColorModel dest_color_model)
    {
	// If no color model passed, use the same as in source
	if (dest_color_model == null)
	    dest_color_model = source_img.getColorModel();
	
	int width = source_img.getWidth();
	int height= source_img.getHeight();
	
	// Create a new image with this color model & raster. Check if the
	// color components already multiplied by the alpha factor.
	return new BufferedImage(
	     dest_color_model,
	     dest_color_model.createCompatibleWritableRaster(width,height),
	     dest_color_model.isAlphaPremultiplied(),
	     null);
    }
    
}

