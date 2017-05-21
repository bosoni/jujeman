/**
 * @file Main.java
 * @author mjt, 2006-07 
 * mixut@hotmail.com
 *
 * pelin runko.
 * lataa SPEditorilla tehdyt pelin tiedot.
 */
package tstgame;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.io.*;
import java.awt.Graphics;
import java.util.Vector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import murlen.util.fscript.*;

public class Main extends JPanel implements MouseListener, MouseMotionListener
{
    static final boolean DEBUG=false;
    static final boolean DEBUGPOLYS=false;
    
    static long FRAMESKIP=10; // mit‰ suurempi, sit‰ enemm‰n j‰tet‰‰n frameja piirt‰m‰tt‰
    
    static String[] itemNames; // tavaroiden nimet
    
    public static boolean running=true;
    
    public static int waitMousePressed=0;
    
    public static String FONTNAME = "Times";
    
    public static String APPTITLE = "";
    public static String ROOMDIR = "";
    public static String PICSDIR = "";
    public static String ANIMDIR = "";
    public static String SOUNDDIR = "";
    public static int WINWIDTH=640, WINHEIGHT=480;
    public static boolean initOK=false;
    
    static JFrame frame = null;
    
    static int transX=0, transY=0;
    
    static Main main=null;
    Game game=null;
    Graphics2D g2d=null;
    
    static Mask mask=null;
    static BufferedImage zbuf=null;
    
    public static void main(String[] args)
    {
	main = new Main();
	main.run();
    }
    
    
    static long nextDraw=0;
    static boolean _canDraw=false;
    static void setTime()
    {
	nextDraw=System.currentTimeMillis()+FRAMESKIP;
    }
    static void checkTime()
    {
	if(System.currentTimeMillis()>=nextDraw) _canDraw=true;
	else _canDraw=false;
    }
    static boolean canDraw()
    {
	return _canDraw;
    }
    
    
    BasicIO loadBasicScript(String scr)
    {
	BasicIO fscript = new BasicIO();
	try
	{
	    URL url=null;
	    FileReader f=null;
	    // tsekkaa lˆytyykˆ tiedosto file
	    if(Main.main!=null)
	    {
		url = Main.main.getClass().getResource(scr);
		if(url==null)
		{
		    File file = new File(scr);
		    if(!file.exists()) return null;
		}
	    }
	    
	    if(url==null)
	    {
		f=new FileReader(scr);
		fscript.load(f);
	    }
	    else
	    {
		InputStream in = Main.main.getClass().getResourceAsStream(scr);
		InputStreamReader inR = new InputStreamReader(  in  ) ;
		fscript.load(inR);
	    }
	    fscript.run();
	    
	}
	catch(IOException e)
	{
	    FileIO.ErrorMessage(e.getMessage());
	}
	catch(FSException e)
	{
	    FileIO.ErrorMessage(e.getMessage());
	}
	
	return fscript;
    }
    
    static long sleepTime=0;
    static void setSleep(int _sleepTime)
    {
	sleepTime=System.currentTimeMillis()+_sleepTime;
    }
    
    public Main()
    {
	// lataa config.cfg:st‰ hakemistot, ohjelman nimi ja resoluutio
	try
	{
	    BasicIO fscript=loadBasicScript("config.cfg"); // lataa asetukset
	    PICSDIR=(String)fscript.getScriptVar("pictureDir");
	    ROOMDIR=(String)fscript.getScriptVar("roomDir");
	    ANIMDIR=(String)fscript.getScriptVar("animDir");
	    SOUNDDIR=(String)fscript.getScriptVar("soundDir");
	    
	    APPTITLE=(String)fscript.getScriptVar("appName");
	    WINWIDTH=(Integer)fscript.getScriptVar("width");
	    WINHEIGHT=(Integer)fscript.getScriptVar("height");
	    
	    if(fscript.getScriptVar("frameskip")!=null)
		FRAMESKIP=(Integer)fscript.getScriptVar("frameskip");
	    
	    // lataa tavaroiden nimet
	    FileIO itf=new FileIO();
	    String items=itf.openAndReadFile(ROOMDIR+"items.lst");
	    itemNames=items.split("\n");
	}
	catch(FSException e)
	{
	    FileIO.ErrorMessage(e.getMessage());
	    running=false;
	    return;
	}
	// luo ikkuna ja pist‰ kuuntelijat
	frame = new JFrame(APPTITLE);
	
	Color col = new Color(0, 0, 0);
	frame.setBackground(col);
	frame.setSize(WINWIDTH, WINHEIGHT+30); // todo fix
	
	// frame.addKeyListener(this);
	frame.setContentPane(this);
	frame.setResizable(false);
	frame.addMouseListener(this);
	frame.addMouseMotionListener(this);
	frame.addWindowListener(new WindowAdapter()
	{
	    public void windowClosing(WindowEvent e)
	    {
		System.exit(0);
	    }
	});
	
	frame.setVisible(true);
	
	// piilota k‰yttiksen hiirikursori
	setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
    }
    
    /*
     * etsi hiirikursori. ellei lˆydy, peliss‰ ei voi tehd‰ mit‰‰n, silloin se on vain demo.
     */
    void findCursor(boolean fromStart)
    {
	if(fromStart==true) game.mode=0;
	else 
	{
	    game.mode++;
	    if(game.mode>=ScriptFuncs.cursor.size()) game.mode=0;
	}
	
	if(ScriptFuncs.cursor.size()>0)
	{
	    while(true)
	    {
		if(ScriptFuncs.cursor.getName(game.mode)!=null) break;
		game.mode++;
		if(game.mode>=ScriptFuncs.cursor.size())
		{
		    game.mode=-1; // ei lˆytynyt hiirikursoria
		    break;
		}
	    }
	}
    }
    
    void run()
    {
	mask=new Mask();
	
	// luo game ja lataa samalla start.sc scriptin
	game = new Game();
	game.init();
	initOK=true;
	
	findCursor(true);
	
	while (running)
	{
	    repaint();
	    
	    try
	    {
		Thread.sleep(1);
	    }
	    catch (InterruptedException e)
	    {
	    }
	    
	}
	
	// jos midi oli p‰‰ll‰, sulje se
	MidiPlayer.closeMidi();
	System.exit(0);
    }
    
    public void drawRoom()
    {
	if(canDraw()==false) return;
	
	// piirr‰ tausta jos ladattu
	if(ScriptFuncs.picture.containsKey("BACKGROUND"))
	{
	    g2d.drawImage( (BufferedImage)ScriptFuncs.picture.get("BACKGROUND"), 0, 0, null);
	}
	
	// jos huone ladattu, piirr‰ se kuva keskelle ruutua
	if(Game.curRoom.bgImage!=null)
	{
	    transX=(WINWIDTH-Game.curRoom.bgImage.getWidth())/2;
	    transY=(WINHEIGHT-Game.curRoom.bgImage.getHeight())/2;
	    
	    // huoneen kuva
	    g2d.drawImage(Game.curRoom.bgImage, 0, 0, null);
	}
	
    }
    
    
    // graffa piirret‰‰n x,y kohtaan mutta ‰ij‰n jalat on kohdassa x,lowy
    // yp -> piirt‰‰ alemmas, ei vaikuta lowy:hyn
    void drawSprite(int x, int y, int yp, BufferedImage img)
    {
	if(canDraw()==false) return;
	
	int lowy=y;
	y=lowy-img.getHeight()+yp;
	x-=img.getWidth()/2;     
	
	BufferedImage dstImage=mask.filter(img, zbuf, x, y, lowy);
	g2d.drawImage(dstImage, x, y, null);
    }
    
    
    public void drawItems()
    {
	if(canDraw()==false) return;
	
	// piirr‰ esineet
	for (int q = 0; q < Game.curRoom.objs.size(); q++)
	{
	    Item2D item = Game.curRoom.objs.get(q);
	    if (item.visible)
	    {
		Entity ent=(Entity)ScriptFuncs.entity.get(item.name);
		if(ent!=null)
		{
		    drawSprite(ent.x, ent.y-ent.yp, ent.yp, item.pic);
		}
	    }
	}
    }
    
    /**
     * piirr‰ hiirikursori
     */
    public void drawMouseCursor(int cur)
    {
	if(canDraw()==false) return;
	if(cur==-1) return;
	
	if(Game.outOfArea==true) // tsekkaa jos yli huonekuvan ja ruksi ladattu (NOT)
	{
	    if(ScriptFuncs.picture.containsKey("NOT"))
	    {
		// raksi
		g2d.drawImage( (BufferedImage)ScriptFuncs.picture.get("NOT"), Game.mx, Game.my, null);
	    }
	    else
	    {
		// raksikuvaa ei ole, piirr‰ hiirikursori
		g2d.drawImage( (BufferedImage)ScriptFuncs.cursor.getPic(cur), Game.mx, Game.my, null);
		
		// ja t‰m‰ flagi pit‰‰ checkActionssia varten laittaa falseksi
		Game.outOfArea=false;
	    }
	}
	else
	{
	    g2d.drawImage( ScriptFuncs.cursor.getPic(cur), Game.mx, Game.my, null);
	}
	
	// piirr‰ pieni merkki hiiren 0,0 kohtaan
	int[] xs={Game.mx, Game.mx+10, Game.mx+4};
	int[] ys={Game.my, Game.my+4, Game.my+10};
	g2d.fillPolygon(xs, ys, 3);
	
    }
    
    // yp hienos‰‰tˆˆn
    public void drawImage(int x, int y, String name, int yp)
    {
	if(canDraw()==false) return;
	
	BufferedImage picture=(BufferedImage)ScriptFuncs.picture.get(name);
	if(picture==null) return;
	
	drawSprite(x, y, yp, picture);
    }
    
    // yp hienos‰‰tˆˆn
    public void drawAnimImage(String name, int yp)
    {
	if(canDraw()==false) return;
	
	Entity ent=(Entity)ScriptFuncs.entity.get(name);
	int x=ent.x;
	int y=ent.y;
	BufferedImage pic=ent.getPic();
	drawSprite(x, y, yp, pic);
    }
    
    /**
     * kirjoita text x,y kohtaan size-kokoisena.
     * valkoiseen taustaan musta teksti.
     * \n toimii rivinvaihtona.
     *
     * ellei ole k‰ytt‰nyt \n merkkej‰ ja teksti on pitk‰, automaattinen rivitys.
     *
     * jos wait==true, j‰‰ odottamaan hiiren napin painallusta.
     * jos x==-1, keskit‰ x suunnassa
     * jos y==-1, keskit‰ y suunnassa
     *
     */
    public void write(int size, int x, int y, String text, boolean wait)
    {
	if(wait==true) waitMousePressed=1;
	
	Main.main.g2d.translate(-Main.main.transX, -Main.main.transY);
	
	Font font = new Font("sanserif", Font.PLAIN, size);
	g2d.setFont(font);
	FontRenderContext frc = g2d.getFontRenderContext();
	
	// splittaa teksti \n merkkeihin
	// k‰y text taulukko l‰pi, ota pisin teksti, se on maxlen
	// maxkor = taulukkosize * font korkeus
	String[] strs=null;
	strs=text.split("\n");
	if(strs.length==1)
	{
	    text=text.replace("\\n", "¥");
	    strs=text.split("¥");
	}
	
	// jos scriptiss‰ message "" k‰skyss‰ eka kirjain on " ", ohjelma kaatuu.
	if(text.charAt(0)==' ') text=text.substring(1); // fix 1 // poista eka merkki
	
	TextLayout layout = new TextLayout(text, font, frc);
	int sx=(int)layout.getBounds().getX(), sy=(int)layout.getBounds().getY();
	
	// jos ei k‰ytetty \n merkkej‰, automaattinen rivitys jos tarpeen
	if(strs.length==1)
	{
	    String tmptext="";
	    String[] words=text.split(" "); // pilko sanoiksi
	    
	    int len=0, oldlen=0;
	    // joka sana
	    for(int w=0; w<words.length; w++)
	    {
		tmptext+=words[w];
		
		// tarkista pituus
		layout = new TextLayout(tmptext, font, frc);
		len=(int)layout.getBounds().getWidth();
		
		if(len-oldlen>WINWIDTH-200)
		{
		    oldlen=len;
		    tmptext+="¥"; // rivinvaihto
		    len=0;
		}
		else tmptext+=" ";
	    }
	    
	    strs=tmptext.split("¥"); // nyt jaetaan eri riveille
	    
	}
	
	int maxlen=0, height=0;
	int[] ys=new int[100];
	ys[0]=0;
	for(int q=0; q<strs.length; q++)
	{
	    layout = new TextLayout(strs[q], font, frc);
	    if(layout.getBounds().getWidth()>maxlen) maxlen=(int)layout.getBounds().getWidth();
	    height+=layout.getBounds().getHeight()+2;
	    ys[q+1]=height;
	}
	
	g2d.setColor(Color.WHITE);
	
	if(x==-1) x=WINWIDTH/2-maxlen/2;
	if(y==-1) y=WINHEIGHT/2-height/2;
	
	g2d.fill3DRect(sx+x-5, sy+y-5, maxlen+10, height+10, true);
	
	g2d.setColor(Color.BLACK);
	for(int q=0;q<strs.length; q++) g2d.drawString(strs[q], x, y+ys[q]);
	
	
	Main.main.g2d.translate(Main.main.transX, Main.main.transY);
	
    }
    
    // tyhjenn‰ ruutu
    void clear()
    {
	_canDraw=true;
	super.setBackground(Color.BLACK);
	if(g2d!=null) super.paintComponent(g2d);
    }
    
    public void paintComponent(Graphics g)
    {
	if(!initOK) return;
	g2d = (Graphics2D) g;
	
	game.drawAll();
	
	if(DEBUGPOLYS)
	{
	    // DEBUG: piirr‰ kaikki huoneen polyt ---
	    for (int q = 0; q < Game.curRoom.polys.size(); q++)
	    {
		if (Polygon.pointInPolygon(Game.mx, Game.my, q))
		    g.setColor(Color.red);
		else
		    g.setColor(Color.green);
		
		for (int w = 0; w < Game.curRoom.polys.get(q).verts.size() - 1; w++)
		{
		    if( Game.curRoom.polys.get(q).visible )
			g.drawLine(Game.curRoom.polys.get(q).verts.get(w).x, Game.curRoom.polys.get(q).verts.get(w).y,
			     Game.curRoom.polys.get(q).verts.get(w + 1).x, Game.curRoom.polys.get(q).verts.get(w + 1).y);
		}
	    } // DEBUG ---
	}
	
    }
    
    public void mouseClicked(MouseEvent me)
    {
    }
    public void mousePressed(MouseEvent me)
    {
	if(waitMousePressed>0) waitMousePressed++;
	
	Game.getUse=false;
	
	Game.mx = me.getX();
	Game.my = me.getY();
	
	if (me.getButton() == MouseEvent.BUTTON1)
	    Game.mbutton = 1;
	if (me.getButton() == MouseEvent.BUTTON2)
	    Game.mbutton = 2;
	if (me.getButton() == MouseEvent.BUTTON3)
	    Game.mbutton = 3;
    }
    public void mouseDragged(MouseEvent me)
    {
	Game.mx = me.getX();
	Game.my = me.getY();
    }
    public void mouseMoved(MouseEvent me)
    {
	Game.mx = me.getX();
	Game.my = me.getY();
    }
    public void mouseReleased(MouseEvent me)
    {
	if(waitMousePressed==2) waitMousePressed=0;
	
	Game.mbutton = 0;
    }
    public void mouseEntered(MouseEvent me)
    {
    }
    public void mouseExited(MouseEvent me)
    {
    }
    
}

/**
 * esineiden luokka
 *
 */
class Item2D
{
    /** esineen nimi */
    String name = "";
    /** kuvatiedoston nimi */
    String fileName = "";
    
    /** esineen paikka huoneessa */
    int x = 0, y = 0;
    /** true niin piirret‰‰n */
    boolean visible = true;
    /** kuva */
    BufferedImage pic = null;
}

class Vector2i
{
    public int x = 0, y = 0;
}

class Polygon
{
    /** polygonin verteksit */
    Vector<Vector2i> verts = new Vector<Vector2i>();
    
    /** selitys jos sit‰ katsoo */
    String descStr = "";
    /** jos ota/k‰yt‰ niin pit‰‰kˆ kirjoittaa jotain */
    String actionStr = "";
    
    /** h‰vitet‰‰nkˆ poly jos ota/k‰yt‰ */
    boolean removePoly = false;
    /** jos true, poly toimii esteen‰ */
    boolean block = true;
    
    /** esine tiedot */
    /** jos vaikuttaa esineeseen, sen index */
    int itemNum = -1;
    /** jos ota/k‰yt‰, h‰vitet‰‰nkˆ esine ruudulta */
    boolean removeFromScreen = false;
    /** tuleeko esine omiin tavaroihin */
    boolean toInventory = false;
    
    /**
     * mink‰ tavaran tarvitsee ett‰ seuraa haluttu tapahtuma (polyn poisto tj)
     */
    int needsItem = -1;
    
    
    String getItemName()
    {
	if(itemNum>0)
	{
	    return Main.itemNames[itemNum-1];
	}
	return "";
    }
    String getNeededItemName()
    {
	if(needsItem>0) return Main.itemNames[needsItem-1];
	return "";
    }
    
    /**
     * onko polygoni viel‰ ruudulla.
     * jos false niin polya ei huomioida
     */
    boolean visible = true;
    
    /**
     * tarkista onko xy kohta polygonin sis‰ll‰.
     *
     * http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
     */
    public static boolean pointInPolygon(int x, int y, int polynum)
    {
	int i, j;
	boolean c = false;
	
	Polygon poly = Game.curRoom.polys.get(polynum);
	if(poly.visible==false) return false;
	
	for (i = 0, j = poly.verts.size() - 1; i < poly.verts.size(); j = i++)
	{
	    Vector2i v1 = poly.verts.get(i);
	    Vector2i v2 = poly.verts.get(j);
	    
	    if ((((v1.y <= y) && (y < v2.y)) || ((v2.y <= y) && (y < v1.y)))
	    && (x < (v2.x - v1.x) * (y - v1.y) / (v2.y - v1.y) + v1.x))
		c = !c;
	    
	}
	return c;
    }
    
    /**
     * jos polygon on linkki uuteen huoneeseen, t‰ss‰ sen nimi.
     */
    String newRoom = "";
    
    /**
     * poistetaanko esine k‰ytˆn j‰lkeen tavaroista
     */
    boolean removeFromInventory = false;
    
    /**
     * onnistuneen esineen k‰ytˆn teksti
     */
    String successUseStr = "";
    
}

