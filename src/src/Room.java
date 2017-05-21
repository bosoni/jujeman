/**
 * @file Room.java
 * @author mjt, 2006-07
 * mixut@hotmail.com
 *
 *
 */
package tstgame;

import java.net.URL;
import java.util.HashMap;
import murlen.util.fscript.*;
import java.util.ArrayList;

import java.io.*;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

/**
 * Room luokka t‰nne asetukset eli taustakuva, syvyysbufferi, huoneessa olevat
 * objektit ym..
 *
 */
public class Room
{
    // scriptiin liittyv‰t muuttujat
    public static FScript fscript=null; // huonekohtainen
    public static FScript globalScript=null; // kaikissa huoneissa, pysyy muistissa
    
    boolean entered=true; // juuri tullut huoneeseen
    ArrayList params=new ArrayList();
    
    public Room()
    {
	fscript=null;
	entered=true;
    }
    
    /**
     * jos str=="", huoneeseen tulo ja scriptin tsekkaus
     * muuten suorita str-niminen funktio (samassa parametrit jos on)
     *
     */
    void runScript(String str, boolean global)
    {
	if(fscript==null) return; // jos ei scripti‰ ole, poistu
	
	try
	{
	    if(str.equals(""))
	    {
		if(entered==true) // jos juuri tullut huoneeseen
		{
		    entered=false;
		    fscript.callScriptFunction("entered", params);
		}
		return;
	    }
	    
	    // aseta parametrit
	    String[] parmsStr= str.split(" ");
	    for(int q=1; q<parmsStr.length; q++)
	    {
		params.add(parmsStr[q]);
	    }
	    
	    if(global==false)
		// suoritetaan parms[0]:ssa oleva funktionimi
		fscript.callScriptFunction(parmsStr[0], params);
	    else
		if(globalScript!=null)
		    globalScript.callScriptFunction(parmsStr[0], params);
	    
	}
	catch(IOException e)
	{
	    System.out.println(e.getMessage());
	    //FileIO.ErrorMessage(e.getMessage());
	    Main.running=false;
	}
	catch(FSException e)
	{
	    System.out.println(e.getMessage());
	    //FileIO.ErrorMessage(e.getMessage());
	    Main.running=false;
	}
	
    }
    
    
    static HashMap animInfo=new HashMap();
    /**
     * lataa animaatioiden nimet muistiin
     *
     * formaaatti:
     *   animaation_nimi
     *   kuvien_lkm
     *   kuva_1
     *   kuva_
     *   kuva_n
     * ja t‰m‰ joka animaatiolle.
     */
    public void loadAnimInfo(String fileName)
    {
	fileName=Main.ROOMDIR+fileName;
	
	FileIO file=new FileIO();
	String str=file.openAndReadFile(fileName);
	if(str==null) return;
	
	String[] strs = str.split("\n"); // palasiksi
	
	for(int q=0; q<strs.length; )
	{
	    String name=strs[q++];
	    int num=Integer.parseInt(strs[q++]);
	    String[] fnames=new String [num];
	    for(int w=0; w<num; w++)
	    {
		fnames[w]=strs[q];
		q++;
	    }
	    
	    animInfo.put(name, fnames);
	}
	
    }
    
    // jos true, javassa p‰ivitet‰‰n animaatiot.
    // jos scriptitiedosto lˆytyy, siell‰ pit‰‰ hoitaa p‰ivitykset
    static boolean automaticUpdates=true;
    /**
     * lataa huoneen scriptitiedosto, jos sellainen lˆytyy.
     * siihen voi asettaa toimintoja joita speitorilla ei saa tehty‰.
     *
     */
    public void loadScript(String fileName, boolean global)
    {
	automaticUpdates=true;
	ScriptFuncs.reset(); // luodaan ja tyhjennet‰‰n action taulukko (vain kerran)
	
	fileName=Main.ROOMDIR+fileName;
	
	// tsekkaa lˆytyykˆ tiedosto file
	URL url = Main.main.getClass().getResource(fileName);
	if(url==null)
	{
	    File file = new File(fileName);
	    
	    if(file.exists()==false)
	    {
		//System.out.println("Warning: "+fileName+" script.");
		return;
	    }
	}

	if(global==false) 
	{
	    automaticUpdates=false;
	}
	
	try
	{
	    if(global==false)
	    {
		fscript=new FScript();
		fscript.registerExtension(new ScriptFuncs());
	    }
	    else
	    {
		globalScript=new FScript();
		globalScript.registerExtension(new ScriptFuncs());
	    }
	    
	    
	    FileReader f=null;
	    
	    if(url==null)
	    {
		f=new FileReader(fileName);
		
		if(global==false) fscript.load(f);
		else globalScript.load(f);
	    }
	    else
	    {
		InputStream in = Main.main.getClass().getResourceAsStream(fileName);
		InputStreamReader inR = new InputStreamReader(  in  ) ;
		
		if(global==false) fscript.load(inR);
		else globalScript.load(inR);
	    }
	    if(global==false) fscript.run();
	    else globalScript.run();
	}
	catch(IOException e)
	{
	    FileIO.ErrorMessage(e.getMessage());
	}
	catch(FSException e)
	{
	    FileIO.ErrorMessage(e.getMessage());
	}
    }
    
    /**
     * lataa huoneen kaikki tiedot, kuvat, polyt, tekstit
     */
    public void load(String fileName)
    {
	fileName = Main.ROOMDIR + fileName;
	
	objs.clear();
	polys.clear();
	zBufImage = "";
	backGroundImage = "";
	name = "";
	
	FileIO file=new FileIO();
	
	/** t‰h‰n huoneen tiedot */
	String dataString = "";
	dataString = file.openAndReadFile(fileName); // koko tiedosto dataStringiin
	
	if (dataString == null)
	{
	    FileIO.ErrorMessage(fileName+" lataaminen ep‰onnistui!");
	    return;
	}
	String[] strs = dataString.split("\n"); // palasiksi
	
	// dataString+=name+"\n"+backGroundImage+"\n"+zBufImage+"\n"+polys.size();
	int pos = 0;
	name = strs[pos++];
	backGroundImage = strs[pos++];
	zBufImage = strs[pos++];
	
	int polys = Integer.parseInt(strs[pos++]);
	
	// aseta polygonin/reitin tiedot ja lis‰‰ huoneeseen
	for (int q = 0; q < polys; q++)
	{
	    Polygon tmppoly = new Polygon();
	    tmppoly.descStr = strs[pos++];
	    tmppoly.actionStr = strs[pos++];
	    tmppoly.removePoly = (strs[pos++].equals("false") ? false : true);
	    tmppoly.block = (strs[pos++].equals("false") ? false : true);
	    tmppoly.itemNum = Integer.parseInt(strs[pos++]);
	    tmppoly.removeFromScreen = (strs[pos++].equals("false") ? false : true);
	    tmppoly.toInventory = (strs[pos++].equals("false") ? false : true);
	    tmppoly.needsItem = Integer.parseInt(strs[pos++]);
	    int verts = Integer.parseInt(strs[pos++]);
	    
	    tmppoly.newRoom = strs[pos++];
	    tmppoly.removeFromInventory = (strs[pos++].equals("false") ? false : true);
	    tmppoly.successUseStr = strs[pos++];
	    
	    // aseta polyn verteksit
	    for (int w = 0; w < verts; w++)
	    {
		Vector2i v = new Vector2i();
		v.x = Integer.parseInt(strs[pos++]);
		v.y = Integer.parseInt(strs[pos++]);
		
		// lis‰‰ verteksit
		tmppoly.verts.add(v);
	    }
	    
	    
	    if(tmppoly.itemNum!=0)
	    {
		// jos tavara otettu jo, ‰l‰ lis‰‰ huoneeseen
		if(Main.main.game.inventory.containsKey( Main.itemNames[tmppoly.itemNum-1])==true) continue;
		// jos tehty joku teht‰v‰ ja tavara on h‰vinnyt, ‰l‰ aseta uudelleen (esim avattu ovi)
		if(ScriptFuncs.action.containsKey(Main.itemNames[tmppoly.itemNum-1]) && ScriptFuncs.action.get(Main.itemNames[tmppoly.itemNum-1]).equals("0")) continue; // 0 on visible==false
	    }
	    
	    // ja aseteltu poly/reitti huoneeseen
	    if(tmppoly.descStr.length()>=4 && tmppoly.descStr.substring(0, 4).equals("PATH"))
	    {
		// reitti talteen
		String[] _strs=tmppoly.descStr.split(" ");
		ScriptFuncs.path.put(_strs[1], tmppoly);
		
		if(Main.DEBUG) System.out.println("Path found: "+_strs[1]);
		
	    }
	    else
		Game.curRoom.polys.add(tmppoly); // polygoni talteen
	}
	
	// aseta objektin tiedot ja lis‰‰ huoneeseen
	int size = Integer.parseInt(strs[pos++]);
	for (int q = 0; q < size; q++)
	{
	    Item2D item = new Item2D();
	    item.name = strs[pos++];
	    
	    item.fileName = strs[pos++];
	    item.x = Integer.parseInt(strs[pos++]);
	    item.y = Integer.parseInt(strs[pos++]);
	    item.visible = (strs[pos++].equals("false") ? false : true);
	    
	    
	    // jos tavara otettu jo, ‰l‰ lis‰‰ huoneeseen
	    if(Main.main.game.inventory.containsKey(item.name)==true) continue;
	    // jos tehty joku teht‰v‰ ja tavara on h‰vinnyt, ‰l‰ aseta uudelleen (esim avattu ovi)
	    if(ScriptFuncs.action.containsKey(item.name) && ScriptFuncs.action.get(item.name).equals("0")) continue; // 0 on visible==false

	    // laita tiedot talteen myˆs entity hashmappiin. itemin lev ja kor otetaan samalla kun ladataan ne kuvat
	    Entity ent=new Entity();
	    ent.reset(item.x, item.y);
	    ent.name=item.name;
	    ScriptFuncs.entity.put(item.name, ent);
	    
	    objs.add(item);
	}
	
    }
    /** huoneen nimi */
    String name = "";
    /** taustakuva */
    String backGroundImage = "";
    /** syvyyskartta */
    String zBufImage = "";
    
    /** esteet ja esineiden alueet */
    Vector<Polygon> polys = new Vector<Polygon>();
    /** esineiden tiedot */
    Vector<Item2D> objs = new Vector<Item2D>();
    
    BufferedImage bgImage = null;
    BufferedImage zImage = null;
    
    public void setName(String name)
    {
	this.name = name;
    }
    public void loadBackground(String name)
    {
	bgImage = loadImage(name);
	backGroundImage = name;
    }
    public void loadZBuf(String name)
    {
	zImage = loadImage(name);
	zBufImage = name;
    }
    
    /**
     * lataa kuva
     *
     * @param file
     *            tiedostonimi
     */
    static BufferedImage loadImage(String file)
    {
	if (file.equals("")) return null;
	if(file.contains("/")==false) file=Main.PICSDIR+file;
	
	if(Main.DEBUG) System.out.println("loadImage(): "+file);
	
	BufferedImage bufimage = null;
	BufferedImage image = null;
	
	try
	{
	    URL url = Main.main.getClass().getResource(file);
	    
	    if (url != null)
	    {
		bufimage = ImageIO.read(url);
	    }
	    else
	    {
		bufimage = ImageIO.read(new File(file));
	    }
	    
	    // luo kuva
	    image = new BufferedImage(bufimage.getWidth(), bufimage.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    
	    // piirr‰ sinne bufimage
	    Graphics g = image.createGraphics();
	    g.drawImage(bufimage, 0, 0, null);
	    
	}
	catch (IOException err)
	{
	    FileIO.ErrorMessage("loadImage("+file+"): " + err);
	}
	
	return image;
    }
    
}

