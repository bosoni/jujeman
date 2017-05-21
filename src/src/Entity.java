/**
 * @file Entity.java
 * @author mjt, 2006-07
 * mixut@hotmail.com
 *
 */

package tstgame;

import java.io.*;
import murlen.util.fscript.*;
import java.awt.image.Raster;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import javax.swing.*;

/**
 * henkilöluokka
 */
public class Entity
{
    // objektin nimi
    String name;
    
    int w=0, h=0; // leveys ja korkeus jos ei animaatio
    int yp=0;
    
    /** juntin koordinaatit */
    int x = 0, y = 0;
    /** juntin koordinaatit mihin se liikkuu */
    int tx = 0, ty = 0;
    float fx = 0, fy = 0;
    float addx = 0, addy = 0;
    int curInd=0;
    
    Polygon path=null; // reitti
    int curVert=0; // mihin pisteeseen mennään reitissä
    int suunta=1;
    
    /** jos
     * 0, häviää ruudulta
     * 1, palaa samaa reittiä takas
     * 2, reitin alkuun.
     * 3, jää viimeiseen vertexin kohtaan
     */
    int walkMode=0;
    String[] animNames=new String [4]; // animaatioiden nimet joka suuntaan
    
    Entity()
    {
	animNames[0]=animNames[1]=animNames[2]=animNames[3]="";
    }
    void setYP(int y)
    {
	yp=y;
    }
    
    void setWalkMode(int mode)
    {
	suunta=1;
	curVert=0;
	
	walkMode=mode;
    }
    
    // flagi joka ilmoittaa jos menty viimeseen pisteeseen
    boolean lastPoint=false;
    
    // tarkistaa onko ukko jo curVert kohdassa. jos on, seuraavaan pisteeseen
    void updatePath()
    {
	if(path==null) return;
	
	if(x==path.verts.get(curVert).x && y==path.verts.get(curVert).y)
	{
	    curVert+=suunta;
	    
	    if(curVert==-1 || curVert==path.verts.size()) // yli pisteiden, käännä suunta
	    {
		curVert+=-suunta;
		if(walkMode==0 || walkMode==3) suunta=0; // pysähdy
		if(walkMode==1) suunta=-suunta; // käännä suunta
		if(walkMode==2) // alkuun
		{
		    curVert=0;
		    reset(path.verts.get(curVert).x, path.verts.get(curVert).y);
		}
		
		if(walkMode==0) walkMode=-1; // hävitä
		
		lastPoint=true;
	    }
	    walkTo(path.verts.get(curVert).x, path.verts.get(curVert).y);
	    
	}
    }
    
    void reset(int x, int y)
    {
	this.x = x;
	this.y = y;
	this.tx = x;
	this.ty = y;
	fx = x;
	fy = y;
    }
    
    void setSpeed(int sp)
    {
	SPD=sp;
    }
    
    int SPD=6; // monta framea menee kunnes asento päivitetään
    int direction=1; // suunta
    public BufferedImage getPic()
    {
	if(ScriptFuncs.animation.containsKey(animNames[direction]))
	    return ((Animation)ScriptFuncs.animation.get(animNames[direction])).get(curInd/SPD);
	
	return null;
    }
    
    /**
     * liikuta junttia kohti päämääräänsä huonosti toteutettu, ei reitinhakua
     */
    public void update()
    {
	if (tx == x && ty == y)
	    return;
	
	float lx = (float) (tx - x), ly = (float) (ty - y);
	
	float ux = Math.abs(lx);
	float uy = Math.abs(ly);
	
	if (ux == uy)
	    addx = addy = 1;
	else if (ux > uy)
	{
	    addx = 1;
	    addy = uy / ux;
	}
	else
	{
	    addy = 1;
	    addx = ux / uy;
	}
	
	if (lx < 0)
	    addx = -addx;
	if (ly < 0)
	    addy = -addy;
	
	if(path==null)
	{
	    if (!canMove((int) (fx + addx), (int) fy) ||
		 !canMove((int) (fx + addx), (int) fy))
		addx=0;
	    if (!canMove((int) (fx), (int) (fy + addy)) ||
		 !canMove((int) (fx), (int) (fy + addy)))
		addy=0;
	}
	if(addx==0 && addy==0) return;
	
	fx+=addx;
	fy+=addy;
	
	x = (int) fx;
	y = (int) fy;
	
	// laske kulma ja aseta suunta sen mukaan
	double ang=Math.toDegrees(Math.atan2(addy, addx));
	if(ang<0) ang=360+ang;
	if(ang>=270-45 && ang<360-45) direction=0; // up
	if(ang>=360-45 || ang<90-45) direction=3; // down
	if(ang>=90-45 && ang<180-45) direction=1; // left
	if(ang>=180-45 && ang<270-45) direction=2; // right
	
	curInd++;
	Animation tmpan=(Animation)ScriptFuncs.animation.get(animNames[direction]);
	if(curInd==tmpan.pics.size()*SPD) curInd=0;
	
    }
    
    public void walkTo(int mx, int my)
    {
	tx = mx;
	ty = my;
    }
    
    /**
     * tarkistaa voiko x,y kohtaan liikkua (ei esteitä tiellä). palauttaa true
     * jos voi.
     */
    boolean canMove(int x, int y)
    {
	// tarkista ensin ruudun reunoihin
	if(x<0 || y<0 || x>Game.curRoom.bgImage.getWidth() || y>Game.curRoom.bgImage.getHeight()) return false;
	
	
	for (int q = 0; q < Game.curRoom.polys.size(); q++)
	{
	    // jos osuu polygoniin
	    if (Polygon.pointInPolygon(x, y, q))
	    {
		// jos poly on huoneenvaihto tai ukon paikan merkkaaja poly
		if (!Game.curRoom.polys.get(q).newRoom.equals(""))
		{
		    // pitääkö kutsua scriptiä
		    if(Game.curRoom.polys.get(q).newRoom.charAt(0)=='$')
		    {
			Game.curRoom.runScript(Game.curRoom.polys.get(q).newRoom.substring(1), false);
			return false;
		    }
		    
		    String[] strs = Game.curRoom.polys.get(q).newRoom.split(" ");
		    
		    // tarkista onko paikan merkkaaja (PAIKKA tai PATH)
		    if (strs[0].equals("PAIKKA") || strs[0].equals("PATH"))
			return true; // paikkamerkki tai reitti, eli ei välitetä siitä.
		    
		    if (strs.length == 1)
			Game.load(strs[0], ""); // lataa paikka, pistä ukko ekaan PAIKKA:aan mikä löytyy
		    else
			Game.load(strs[0], strs[1]); // huone ja paikka
		    return false;
		}
		
		// jos estepolygoni, palauta false
		if (Game.curRoom.polys.get(q).block)
		    return false;
	    }
	}
	return true; // ei ongelmia, voi liikkua
    }
    
}
