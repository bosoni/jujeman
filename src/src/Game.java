/**
 * @file Game.java
 * @author mjt, 2006-07
 * mixut@hotmail.com
 *
 */

package tstgame;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * peliluokka
 */
public class Game
{
    /** hiiren koordinaatit */
    public static int mx = 0, my = 0;
    /** hiiren nappi 0=ei mit‰‰n, 1=vasen .. */
    public static int mbutton = 0;
    
    /**
     * huone jossa ollaan
     */
    public static Room curRoom = null;
    
    /**
     * mode: 0=k‰vely, 1=katso, 2=ota/k‰yt‰,  tavarat
     */
    public static int mode = 0;
    
    /**
     * jos hiirikursori on huonekuvan ulkopuolella, aseta t‰m‰ true ja ruutuun piirret‰‰n ruksi
     */
    public static boolean outOfArea=false;
    
    /**
     * tavarat jota tyyppi kantaa mukanaan.
     */
    HashMap inventory = new HashMap();
    
    void init()
    {
	curRoom=new Room();
	
	// lataa animaatioiden nimet
	curRoom.loadAnimInfo("anim.cfg");
	
	// lataa scripti joka voidaan suorittaa joka huoneessa
	curRoom.loadScript("everyroom.sc", true);
	
	// lataa aloitus scripti jossa hoidetaan muut lataukset
	curRoom.loadScript("start.sc", false);
    }
    
    // max matka josta ota/k‰yt‰ toiminto toimii
    final int LEN=20;
    static boolean getUse=false; // jos klikataan k‰dell‰, tai esineell‰, aseta true:ksi
    int tmppoly=0;
    int destX, destY; // polygonin l‰himm‰n vertexin paikka ‰ij‰‰n, siihen k‰vell‰‰n
    int mouseClickedX, mouseClickedY; // paikka jossa hiirt‰ klikattiin
    ArrayList params=new ArrayList();
    
    /**
     * kutsutaan paintComponent funkkarista. suoritetaan piirrot
     */
    public void drawAll()
    {
	Main.checkTime();
	
	// jos ei ole odotus p‰‰ll‰, piirr‰ kaikki graffat
	if(Main.waitMousePressed==0 && Main.sleepTime==0)
	{
	    if(Main.canDraw()==true)
	    {
		if(Game.curRoom.zImage!=null)
		{
		    // zbufferiin syvyyskartta. zbuffia p‰ivitet‰‰n joka framella niin
		    // se pit‰‰ laittaa aina alkuper‰iseksi piirron alussa.
		    Main.main.zbuf.getGraphics().drawImage(Game.curRoom.zImage, 0,0,null);
		}
		else
		{
		    // tyhjennet‰‰n zbuffer
		    Graphics2D gr2d=Main.main.zbuf.createGraphics();
		    gr2d.setColor(Color.BLACK);
		    gr2d.fillRect(0, 0,Main.main.zbuf.getWidth(), Main.main.zbuf.getHeight());
		}
		
		Main.main.clear();
	    }
	    
	    Main.main.g2d.translate(Main.main.transX, Main.main.transY);
	    Main.main.drawRoom();
	    Main.main.drawItems();
	    
	    // jos huoneella ei ole scriptitiedostoa, p‰ivitet‰‰n animaatiot automaattisesti
	    if(Room.automaticUpdates==true)
	    {
		for(int anims=0; anims<ScriptFuncs.entity.size(); anims++)
		{
		    ((Entity)ScriptFuncs.entity.values().toArray()[anims]).update();
		    ((Entity)ScriptFuncs.entity.values().toArray()[anims]).updatePath();
		}
	    }
	    else
	    {
		// paikkojen p‰ivitykset ym
		curRoom.runScript("update", false); // huonekohtanen
		curRoom.runScript("update", true); // joka huoneen script
		
		// jos piirt‰misen aika, suorita skriptin render() funktio
		if(Main.canDraw()==true) curRoom.runScript("render", false);
	    }
	    
	    // tarkista tapahtumat
	    checkActions();
	    
	    Main.main.g2d.translate(-Main.main.transX, -Main.main.transY);
	    
	    // scripti tai checkactions on voinut muuttaa n‰it‰, ja jos odotus, ‰l‰ piirr‰ hiirt‰
	    if(Main.waitMousePressed==0 && Main.sleepTime==0)
		Main.main.drawMouseCursor(mode);
	    
	    
	    // tsekkaa rajat kunhan kuva on ladattu
	    if(curRoom.bgImage!=null)
	    {
		if(mx<Main.transX || my<Main.transY || mx>Main.transX+curRoom.bgImage.getWidth() || my>Main.transY+curRoom.bgImage.getHeight())
		{
		    outOfArea=true;
		}
		else outOfArea=false;
	    }
	    
	    
	    if(Main.canDraw()) Main.setTime();
	    
	}
    }
    
    
    /**
     * tsekkaa WALK, LOOK, TAKE toiminnot, muut ohjelmoidaan scriptiin
     *
     */
    void checkActions()
    {
	// toiminnon vaihto (hiiren oikea)
	if (mbutton == 3)
	{
	    Main.main.findCursor(false);
	    mbutton=0;
	}
	
	if(curRoom.polys.size()==0) return;
	
	// ahhaa, nyt toimitaan (hiiren vasen nappi) kunhan klikattu huoneen alueella
	if (mbutton == 1)
	{
	    
	    if(!outOfArea)
	    {
		mx-=Main.transX;
		my-=Main.transY;
		
		switch (mode)
		{
		    case 0: // k‰vely
			walkTo(mx, my);
			break;
			
		    case 1: // katso
			look(mx, my);
			break;
			
		    case 2: // ota/k‰yt‰
		    {
			// koordinaatit talteen
			destX=mx;
			destY=my;
			mouseClickedX=mx;
			mouseClickedY=my;
			
			// jos ohjattavaa ukkoa ei ole, varmaan "silmist‰p‰in" ja tsekataan heti mit‰ tehd‰
			if(ScriptFuncs.entity.containsKey("EGO")==false)
			{
			    getOrUse(destX, destY);
			    break;
			}
			
			
			// laske et‰isyys
			Entity ego= (Entity)ScriptFuncs.entity.get("EGO");
			int dx=ego.x;
			int dy=ego.y;
			double len=Math.abs(Math.sqrt( (dx-destX)*(dx-destX) + (dy-destY)*(dy-destY) ));
			
			// jos tarpeeksi l‰hell‰, tsekkaa heti mit‰ tehd‰
			if(len<LEN)
			{
			    getOrUse(destX, destY);
			    break;
			}
			else
			{
			    // liian kaukana, k‰vele l‰hemm‰ksi
			    // polyn l‰himp‰‰n vertexiin
			    int poly = getPoly(mx, my, 2);
			    if (poly!=-1)
			    {
				double minlen=9999;
				for(int q=0; q<curRoom.polys.get(poly).verts.size(); q++)
				{
				    double len2=Math.abs(Math.sqrt( (curRoom.polys.get(poly).verts.get(q).x - dx )* (curRoom.polys.get(poly).verts.get(q).x - dx) +
					 (curRoom.polys.get(poly).verts.get(q).y - dy ) * (curRoom.polys.get(poly).verts.get(q).y - dy ) ));
				    
				    if(len2<minlen)
				    {
					minlen=len2;
					destX=curRoom.polys.get(poly).verts.get(q).x;
					destY=curRoom.polys.get(poly).verts.get(q).y;
				    }
				}
			    }
			    
			    walkTo(destX, destY);
			    getUse=true;
			    
			}
			break;
		    }
		    
		    default: // joku muu eli tavaran k‰yttˆ
			useItem(mx, my, ScriptFuncs.cursor.getName(mode));
			break;
			
		}
		mbutton = 0;
		mx+=Main.transX;
		my+=Main.transY;
		
	    }
	    
	}
	
	// ota tavara, k‰yt‰ jotain?
	if(getUse==true && ScriptFuncs.entity.containsKey("EGO"))
	{
	    Entity ego= (Entity)ScriptFuncs.entity.get("EGO");
	    int dx=ego.x;
	    int dy=ego.y;
	    int xx=dx-destX;
	    int yy=dy-destY;
	    double len=Math.abs(Math.sqrt( xx*xx + yy*yy )); // pituus jaloita
	    
	    if(len>LEN)
	    {
		// tsekkaa y ylemp‰n‰
		yy-=ego.h/2;
		len=Math.abs(Math.sqrt( xx*xx + yy*yy ));
	    }
	    
	    
	    if(len<=LEN)
	    {
		getOrUse(mouseClickedX, mouseClickedY);
		getUse=false;
		
		ego.reset(dx, dy); // pys‰yt‰ hemmo asettamalla kaikki arvot sen t‰nhetkiseks arvoiks
	    }
	    
	}
    }
    
    static void MessageBox(String msg)
    {
	if (msg.equals("")) return;
	System.out.println(msg);
	JOptionPane.showMessageDialog(null, msg, Main.APPTITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void walkTo(int x, int y)
    {
	if(ScriptFuncs.entity.containsKey("EGO"))
	    ((Entity)ScriptFuncs.entity.get("EGO")).walkTo(x, y);
    }
    
    void look(int x, int y)
    {
	// etsi poly
	int poly = getPoly(x, y, 1);
	if (poly == -1)
	{
	    Main.main.write(15, -1, -1, "Ei siin‰ olo mit‰‰n ihmeellist‰.", true);
	    return;
	}
	
	String desc = curRoom.polys.get(poly).descStr;
	// jos eka kirjain on $ merkki, pit‰‰ kutsua funktiota scriptist‰
	if(desc.charAt(0)=='$') curRoom.runScript(desc.substring(1), false);
	else
	    Main.main.write(15, -1, -1, desc, true);
	
    }
    
    // palauttaa true jos osui polyyn xy kohdassa, muuten false
    void getOrUse(int x, int y)
    {
	// etsi poly
	int poly = getPoly(x, y, 2);
	
	// k‰dell‰ tyhj‰‰, ei mit‰‰n
	if (poly == -1) return;
	
	// n‰yt‰ tapahtumateksti
	String desc = curRoom.polys.get(poly).actionStr;
	if(desc.length()==0) return; // ei teksti‰
	
	// jos eka kirjain on $ merkki, pit‰‰ kutsua funktiota scriptist‰
	if(desc.charAt(0)=='$')
	{
	    curRoom.runScript(desc.substring(1), false);
	    return;
	}
	else
	    Main.main.write(15, -1, -1, desc, true);
	
	// vaatii tavaran?
	if (curRoom.polys.get(poly).needsItem > 0)
	{
	    //Main.main.write(15, -1, -1, "Ei auttanut.", true);
	    return;
	}
	
	// h‰vi‰‰kˆ poly?
	curRoom.polys.get(poly).visible = !curRoom.polys.get(poly).removePoly;
	
	// onko linkattu johonkin esineeseen
	if (curRoom.polys.get(poly).itemNum > 0)
	{
	    // siirtyykˆ omiin tavaroihin?
	    if (curRoom.polys.get(poly).toInventory)
	    {
		
		// todo
		// 1 tapa tavaroille: lis‰t‰‰n se hiirikursoriksi ja sit‰ voi k‰ytt‰‰
		// sitten k‰ym‰ll‰ hiiren kuvakkeita l‰pi ja valikoida sielt‰ oikea tavara
		// huono tapa, parempi olisi inventaarioikkuna josta valitaan tavara.
		Item2D item = Game.curRoom.objs.get(curRoom.polys.get(poly).itemNum-1);
		ScriptFuncs.cursor.add(item.name, item.pic);
		//------
		
		
		// lis‰‰ esineen nimi tavaroihin
		inventory.put(Main.itemNames[curRoom.polys.get(poly).itemNum-1], "1");
		if(Main.DEBUG) System.out.println("lis‰t‰‰n tavaroihin: "+ Main.itemNames[curRoom.polys.get(poly).itemNum-1] );
	    }
	    // h‰vitet‰‰nkˆ ruudulta?
	    curRoom.objs.get(curRoom.polys.get(poly).itemNum-1).visible=!curRoom.polys.get(poly).removeFromScreen;
	    
	}
	return;
    }
    
    void useItem(int x, int y, String itemName)
    {
	// etsi poly
	int poly = getPoly(x, y, 2);
	
	if(poly==-1 || curRoom.polys.get(poly).successUseStr.length()==0)
	{
	    //Main.main.write(15, -1, -1, "Mit‰h?", true);
	    return;
	}
	// jos eka kirjain on $ merkki, pit‰‰ kutsua funktiota scriptist‰
	if(curRoom.polys.get(poly).successUseStr.charAt(0)=='$')
	{
	    curRoom.runScript(curRoom.polys.get(poly).successUseStr.substring(1), false);
	    return;
	}
	
	// jos v‰‰r‰‰ esinett‰ v‰‰r‰‰n paikkaan
	if (curRoom.polys.get(poly).getNeededItemName().equals(itemName)==false)
	{
	    Main.main.write(15, -1, -1, "No ei.", true);
	}
	else // oikea esine oikeaan paikkaan, esim avain oveen
	{
	    // h‰vi‰‰kˆ poly?
	    curRoom.polys.get(poly).visible = !curRoom.polys.get(poly).removePoly;
	    
	    // onko linkattu johonkin esineeseen
	    if (curRoom.polys.get(poly).itemNum > 0)
	    {
		// siirtyykˆ omiin tavaroihin?
		if (curRoom.polys.get(poly).toInventory)
		{
		    // lis‰‰ esineen nimi tavaroihin
		    inventory.put(Main.itemNames[curRoom.polys.get(poly).itemNum-1], "1");
		    if(Main.DEBUG) System.out.println("lis‰t‰‰n tavaroihin: "+ Main.itemNames[curRoom.polys.get(poly).itemNum-1] );
		    
		}
		
		
		// h‰vitet‰‰nkˆ ruudulta?
		for(int qw=0; qw<curRoom.objs.size(); qw++)
		{
		    if(curRoom.objs.get(qw).name.equals(Main.itemNames[curRoom.polys.get(poly).itemNum-1]))
		    {
			curRoom.objs.get(qw).visible=!curRoom.polys.get(poly).removeFromScreen;
			
			ScriptFuncs.action.put(curRoom.objs.get(qw).name, curRoom.objs.get(qw).visible==false ? "0" : "1");
			
			if(Main.DEBUG)  System.out.println("lis‰t‰‰n action: "+		  curRoom.objs.get(qw).name);
		    }
		}
		
		
		// h‰vitet‰‰nkˆ k‰ytetty esine tavaroista
		if (curRoom.polys.get(poly).removeFromInventory)
		{
		    inventory.remove(itemName);
		    inventory.put(itemName, "2"); // 2 eli k‰ytetty
		    
		    // h‰vit‰ hiirikursori
		    ScriptFuncs.cursor.remove((String)itemName);
		    
		    if(Main.DEBUG) System.out.println("h‰vit‰ "+itemName);
		    Main.main.findCursor(true);
		    
		}
		
		// jos palautetta pelaajalle
		if (!curRoom.polys.get(poly).successUseStr.equals(""))
		{
		    Main.main.write(15, -1, -1, curRoom.polys.get(poly).successUseStr, true);
		}
		
	    }
	}
    }
    
    /**
     * etsii klikattu polygon, ja sen pit‰‰ olla visible
     * mode 1: jos polya katsotaan
     * mode 2: jos ota/k‰yt‰
     */
    int getPoly(int x, int y, int mode)
    {
	// kaikki polyt l‰pi
	// viimeisimm‰st‰ ensimm‰iseen
	for (int q = curRoom.polys.size()-1; q >= 0; q--)
	{
	    if (curRoom.polys.get(q).visible)
	    {
		// tarkista lˆytyykˆ
		if (Polygon.pointInPolygon(x, y, q) == true)
		{
		    // jos polylla ei ole selityst‰, etsi jos jollain toisella polylla on
		    // (t‰m‰ siksi koska polyja voi olla p‰‰llekk‰in)
		    if(mode==1) // katsotaan joten etsi kuvaus
		    {
			if (Game.curRoom.polys.get(q).descStr.equals("")) // v‰‰r‰ poly?
			{
			    continue;
			}
		    }
		    else
			if(mode==2) // ota/k‰yt‰ joten actionstr palaute
			{
			if (Game.curRoom.polys.get(q).actionStr.equals("")) // v‰‰r‰ poly?
			{
			    continue;
			}
			}
		    
		    // ok, taisi olla oikea poly ja sen indeksi palautetaan
		    return q;
		}
	    }
	}
	
	return -1; // ei lˆytynyt
    }
    
    
    /**
     * lataa huone ja etsi poly (PAIKKA startPlace) ja aseta juntti siihen. jos
     * startPlace=="", aseta ensinm‰iseen PAIKKA paikkaan.. jos ei lˆydy
     * sit‰k‰‰n, n‰yt‰ virhe.
     *
     * @param roomName
     * @param startPlace
     */
    public static void load(String roomName, String startPlace)
    {
	curRoom=new Room();
	
	// lataa tiedot
	curRoom.load(roomName);
	
	// jos ep‰onnistui, palaa
	if (curRoom.backGroundImage.equals(""))
	    return;
	
	if(startPlace.length()>0 && ScriptFuncs.entity.containsKey("EGO"))
	{
	    Entity ent=(Entity)ScriptFuncs.entity.get("EGO");
	    ent.reset(0, 0);
	    
	    for (int q = 0; q < curRoom.polys.size(); q++)
	    {
		// etsi PAIKKA
		String[] strs = curRoom.polys.get(q).descStr.split(" ");
		if (strs[0].equals("PAIKKA"))
		{
		    if(strs.length>2)
			Game.MessageBox("Virhe: "+curRoom.polys.get(q).descStr+"\n(oikea k‰yttˆ: PAIKKA haluttu_paikka)");
		    
		    if(strs[1].equals(startPlace)) // jos haluttu paikka
		    {
			if(ScriptFuncs.entity.containsKey("EGO"))
			    ent.reset( curRoom.polys.get(q).verts.get(0).x, curRoom.polys.get(q).verts.get(0).y );
			break;
		    }
		    
		}
	    }
	    if(ent.x==0 && ent.y==0)
	    {
		Game.MessageBox("PAIKKA "+startPlace+" puuttuu, huoneen tiedot vajaat.");
	    }
	}
	
	// lataa tausta
	if(curRoom.backGroundImage.length()>0)
	    curRoom.loadBackground(curRoom.backGroundImage);
	
	// lataa zbuffer
	if(curRoom.zBufImage.length()>0)
	{
	    curRoom.loadZBuf(curRoom.zBufImage);
	    Main.zbuf=Main.mask.createCompatibleDestImage(curRoom.zImage, null);
	}
	else
	    if(curRoom!=null && curRoom.bgImage!=null && Main.zbuf==null) Main.zbuf=Main.mask.createCompatibleDestImage(curRoom.bgImage, null);
	
	// esineet
	for (int q = 0; q < curRoom.objs.size(); q++)
	{
	    // lataa esine
	    curRoom.objs.get(q).pic = Room.loadImage(curRoom.objs.get(q).fileName);
	    
	    // otetaan nyt lev ja kor talteen ja pit‰‰ korjata x niin ett‰ 
	    // x on objektin keskell‰ (pituuksien laskeminen menee silloin oikein ym)
	    Entity ent=(Entity)ScriptFuncs.entity.get(curRoom.objs.get(q).name);
	    if(ent!=null)
	    {
		ent.w=curRoom.objs.get(q).pic.getWidth();
		ent.h=curRoom.objs.get(q).pic.getHeight();
		
		ent.y+=ent.h; // alanurkkaan y kohta
		ent.x+=ent.w/2; // keskelle (piirt‰ess‰ tietenkin ext.x-ent.w/2)
	    }
	    
	}

	// lataa scripti jos lˆytyy
	curRoom.loadScript(roomName+".sc", false);
	
	Main.main.findCursor(true);

	Main.main.clear();
    }
    
}
