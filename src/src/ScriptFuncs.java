/**
 * @file ScriptFuncs.java
 * @author mjt, 2007
 * mixut@hotmail.com
 *
 */

package tstgame;

import java.nio.Buffer;
import java.util.HashMap;
import murlen.util.fscript.*;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.image.BufferedImage;

class Cursor
{
    Vector name=new Vector();
    Vector pic=new Vector();
    
    void add(String pname, BufferedImage ppic)
    {
	name.add(pname);
	pic.add(ppic);
    }
    String getName(int index)
    {
	return (String)name.get(index);
    }
    BufferedImage getPic(int index)
    {
	return (BufferedImage)pic.get(index);
    }
    BufferedImage get(String pname)
    {
	for(int q=0; q<name.size(); q++)
	{
	    if(name.equals(pname)) return (BufferedImage)pic.get(q);
	}
	return null;
    }
    int getIndex(String pname)
    {
	for(int q=0; q<name.size(); q++)
	{
	    if(name.get(q).equals(pname)) return q;
	}
	return -1;
    }
    
    void clear()
    {
	name.clear();
	pic.clear();
    }
    
    void remove(String cur)
    {
	int c=getIndex(cur);
	
	if(c!=-1)
	{
	    name.remove(c);
	    pic.remove(c);
	}
    }
    
    void add(int i, String nm, BufferedImage ppic)
    {
	name.add(i, nm);
	pic.add(i, ppic);
    }
    
    int size()
    {
	return name.size();
    }
    
}

/**
 * luokan metodeita voidaan kutsua scriptitiedostosta.
 */
public class ScriptFuncs  extends BasicExtension
{
    static HashMap action=null; // käytetään tallentamaan scriptin muuttujien arvot
    static Cursor cursor=null; // hiirikursoreiden kuvat (valmiiks ohjelmoituja on WALK, LOOK, TAKE)
    static HashMap picture=null; // muut kuvat
    static HashMap animation; // animaatioinfot
    static HashMap entity; // muut henkilöt, animoidut oliot
    static HashMap path; // reitit
    
    static public void reset()
    {
	if(action==null)
	{
	    action=new HashMap();
	    picture=new HashMap();
	    animation=new HashMap();
	    entity=new HashMap();
	    path=new HashMap();
	    
	    cursor=new Cursor();
	    cursor.add("WALK", null); // tempit
	    cursor.add("LOOK", null); // varaa vain paikat ettei muut kursorit mene
	    cursor.add("TAKE", null); // näitten paikalle
	}
	
    }
    /**
     * tuhoa kaikki!
     */
    public void freeAll()
    {
	action.clear();
	picture.clear();
	animation.clear();
	entity.clear();
	path.clear();
	cursor.clear();
    }
    
    public Object callFunction(String name, ArrayList params) throws FSException
    {
	
	if(name.equals("MessageBox"))
	{
	    String str=(String)params.get(0);
	    Game.MessageBox(str);
	    return null;
	}
	if(name.equals("freeAll"))
	{
	    freeAll();
	    return null;
	}
	if(name.equals("delay"))
	{
	    Main.setSleep(((Integer)params.get(0)).intValue());
	    return null;
	}
	if(name.equals("waitMousePressed"))
	{
	    Main.waitMousePressed=1;
	    return null;
	}
	if(name.equals("endGame"))
	{
	    Main.running=false;
	    return null;
	}
	if(name.equals("loadRoom")) // fileName position
	{
	    Game.load((String)params.get(0), (String)params.get(1));
	    return null;
	}
	if(name.equals("loadImage")) // fileName, name
	{
	    String filename=(String)params.get(0);
	    String objname=(String)params.get(1);
	    
	    // poistetaanko kuva käytöstä?
	    if(filename.equals("null"))
	    {
		picture.remove(params.get(1));
		return null;
	    }
	    
	    BufferedImage pic=Room.loadImage(filename);
	    picture.put( objname, (Object)pic);
	    
	    // turha täällä? todo
	    Entity ent=(Entity)entity.get(objname);
	    if(ent!=null)
	    {
		ent.w=pic.getWidth();
		ent.h=pic.getHeight();
	    }
	    
	    return null;
	}
	if(name.equals("loadCursor")) // fileName, name
	{
	    String cname=(String)params.get(1);
	    if(cname.equals("null"))
	    {
		cursor.remove(cname);
		return null;
	    }
	    
	    BufferedImage pic=Room.loadImage((String)params.get(0));
	    
	    // tarkista onko walk/look/take
	    if(params.get(1).equals("WALK"))
	    {
		cursor.remove("WALK");
		cursor.add(0, "WALK", pic);
		return null;
	    }
	    if(params.get(1).equals("LOOK"))
	    {
		cursor.remove("LOOK");
		cursor.add(1, "LOOK", pic);
		return null;
	    }
	    if(params.get(1).equals("TAKE"))
	    {
		cursor.remove("TAKE");
		cursor.add(2, "TAKE", pic);
		return null;
	    }
	    cursor.add(cname, pic);
	    
	    return null;
	}
	if(name.equals("drawImage")) // x, y, name, yp
	{
	    int x=((Integer)params.get(0)).intValue();
	    int y=((Integer)params.get(1)).intValue();
	    String str=(String)params.get(2);
	    int yp=0; // hienosäätö y:hyn lisää tämän verran
	    if(params.size()==4) yp=((Integer)params.get(3)).intValue();
	    Main.main.drawImage(x, y, str, yp);
	    return null;
	}
	
	if(name.equals("playMidi")) // midifile
	{
	    String str=(String)params.get(0);
	    
	    MidiPlayer.stopMidi();
	    MidiPlayer.playMidi(str);
	    return null;
	}
	if(name.equals("loadAnimation")) // animName
	{
	    String animName=(String)params.get(0);
	    
	    // tarkista löytyykö animName nimistä animaatiota
	    if(Room.animInfo.containsKey(animName)==true)
	    {
		// tiedostonimet
		String[] files=(String[])Room.animInfo.get(animName);
		
		Entity ent=(Entity)entity.get(animName);
		
		Animation anim=new Animation();
		// lataa kuvat
		for(int qq=0; qq<files.length; qq++)
		{
		    BufferedImage tmppic=Room.loadImage(Main.ANIMDIR+files[qq]);
		    if(ent!=null)
		    {
			ent.w=tmppic.getWidth();
			ent.h=tmppic.getHeight();
		    }
		    
		    anim.add(tmppic); // kuva talteen
		}
		
		// animaatio talteen
		animation.put(animName, anim);
		
	    }
	    else
		FileIO.ErrorMessage(animName+" animaatiota ei löytynyt!");
	    
	    return null;
	}
	if(name.equals("setAnimation")) // objname, up_anim, down_anim, left_anim, right_anim
	{
	    String objname=(String)params.get(0);
	    String up=(String)params.get(1);
	    String down=(String)params.get(2);
	    String left=(String)params.get(3);
	    String right=(String)params.get(4);
	    
	    // jos vanha animaatio, poista se
	    if(entity.containsKey(objname)) entity.remove(objname);
	    
	    Entity ent=new Entity();
	    ent.name=objname;
	    
	    // animaatioiden nimet talteen
	    ent.animNames[0]=up;
	    ent.animNames[1]=down;
	    ent.animNames[2]=left;
	    ent.animNames[3]=right;
	    
	    entity.put(objname, ent);
	    
	    return null;
	}
	if(name.equals("updateAnimation")) // objname
	{
	    String objname=(String)params.get(0);
	    
	    // päivitä animaatio jos objekti löytyy
	    Entity tmpent=(Entity)entity.get(objname);
	    if(tmpent==null) return null;
	    
	    tmpent.update();
	    tmpent.updatePath();
	    
	    return null;
	}
	if(name.equals("removeAnimation")) // objname
	{
	    String objname=(String)params.get(0);
	    entity.remove(objname);
	    animation.remove(objname);
	    if(Main.DEBUG) System.out.println("remove "+objname);
	    
	    return null;
	}
	if(name.equals("drawAnimImage")) // objname, yp
	{
	    String objname=(String)params.get(0);
	    Entity tmpent=(Entity)entity.get(objname);
	    if(tmpent==null) return null;
	    
	    int yp=0; // hienosäätö y:hyn lisää tämän verran
	    if(params.size()==2) yp=((Integer)params.get(1)).intValue();
	    
	    if(tmpent.walkMode!=-1)
	    {
		Main.main.drawAnimImage(objname, yp);
	    }
	    return null;
	}
	
	if(name.equals("setPath")) // objectName, pathName, walk_mode
	{
	    String objname=(String)params.get(0);
	    
	    if(entity.containsKey(objname))
	    {
		String pathname=(String)params.get(1);
		
		// aseta reitti
		Polygon pa=(Polygon)path.get(pathname);
		Entity ent=(Entity)entity.get(objname);
		ent.path=pa;
		
		ent.reset(ent.path.verts.get(0).x, ent.path.verts.get(0).y);
		ent.setWalkMode((Integer)params.get(2));
	    }
	    return null;
	}
	if(name.equals("message")) // font_size, x, y, text
	{
	    int size=((Integer)params.get(0)).intValue();
	    int x=((Integer)params.get(1)).intValue();
	    int y=((Integer)params.get(2)).intValue();
	    String text=(String)params.get(3);
	    
	    int waitmouse=1;
	    if(params.size()==5) waitmouse=((Integer)params.get(4)).intValue();
	    
	    Main.main.write(size, x, y, text, waitmouse==1 ? true : false);
	    
	    return null;
	}
	if(name.equals("getItem")) // objname
	{
	    // ottaa tavaran
	    String item=(String)params.get(0);
	    
	    //void setItem(String item, boolean toInventory, boolean toCursor, boolean removePoly, boolean removeItem)
	    setItem(item, true, true, true, true);
	    
	    return null;
	}
	if(name.equals("setItem")) // (String itemname, boolean toInventory, boolean toCursor, boolean removePoly, boolean removeItem)
	{
	    // ottaa tavaran
	    String item=(String)params.get(0);
	    int p1=((Integer)params.get(1)).intValue();
	    int p2=((Integer)params.get(2)).intValue();
	    int p3=((Integer)params.get(3)).intValue();
	    int p4=((Integer)params.get(4)).intValue();
	    
	    //void setItem(String item, boolean toInventory, boolean toCursor, boolean removePoly, boolean removeItem)
	    setItem(item, p1==1 ? true : false, p2==1 ? true : false, p3==1 ? true : false, p4==1 ? true : false);
	    
	    return null;
	}
	
	if(name.equals("setPos")) // x, y, object
	{
	    Entity ent=(Entity)entity.get((String)params.get(2));
	    if(ent!=null)
	    {
		ent.x=((Integer)params.get(0)).intValue();
		ent.y=((Integer)params.get(1)).intValue();
	    }
	    
	    return null;
	}
	if(name.equals("setYP")) // object, yp
	{
	    Entity ent=(Entity)entity.get((String)params.get(0));
	    if(ent!=null)
	    {
		ent.yp=((Integer)params.get(1)).intValue();
	    }
	    
	    return null;
	}
	
	if(name.equals("using")) // hiirikursori, objekti, max_etäisyys
	{
	    if(Game.mbutton!=1) return "0"; // eli kun hiirtä klikataan, suoritetaan tämä funktio
	    String cur=(String)params.get(0);
	    
	    // jos eri hiirikursori, poistu
	    if(cursor.getName(Game.mode).equals(cur)==false) return "0";
	    
	    String objname=(String)params.get(1);
	    Entity ent=(Entity)entity.get(objname);
	    if(ent==null) return "0";
	    
	    // lev ja kor
	    int w=ent.w;
	    int h=ent.h;
	    
	    // pitää tarkistaa että hiirellä klikattiin objektin alueella
	    if(Game.mx-Main.transX>=ent.x-ent.h/2 && Game.mx-Main.transX<=ent.x+w &&
		 Game.my-Main.transY>=ent.y-h && Game.my-Main.transY<=ent.y)
	    {
		// jos ei ole ohjattavaa ukkoa, ok, palauta 1 koska on kumminkin painettu objektin alueella
		if(entity.containsKey("EGO")==false) return "1";
		
		Entity ego=(Entity)entity.get("EGO");
		int xx=ego.x-ent.x;
		int yy=ego.y-ent.y;
		
		// laske etäisyys
		double len=Math.abs(Math.sqrt(xx*xx + yy*yy));
		
		if(Main.DEBUG) System.out.println("arvot "+len+" "+xx+" "+yy);
		
		if(len > ((Integer)params.get(2)).intValue()) return "0";
		
		return "1";
	    }
	    
	    return "0";
	}
	
	
	throw new FSUnsupportedException(name);
    }
    
    int getIndex(Object index)
    {
	Integer i=(Integer)index;
	return i.intValue();
    }
    
    // taulukot ---
    public Object getVar(String name,Object index)
    {
	if(name.equals("action"))
	{
	    if(action.get(index)!=null) return action.get(index);
	}
	if(name.equals("inventory"))
	{
	    if(Main.main.game.inventory.get(index)!=null) return Main.main.game.inventory.get(index);
	}
	if(name.equals("roomName"))
	{
	    if(Main.main.game.curRoom.name!=null) return (String)Main.main.game.curRoom.name;
	}
	
	if(name.equals("positionX"))
	{
	    if(entity.get(index)!=null) return ((Entity)entity.get(index)).x;
	}
	if(name.equals("positionY"))
	{
	    if(entity.get(index)!=null) return ((Entity)entity.get(index)).y;
	}
	
	//if(Main.DEBUG) System.out.println(" *Warning: maybe error in script: "+name);
	return new Integer(0);
    }
    
    public void setVar(String name,Object index,Object value)
    {
	if(name.equals("action"))
	{
	    action.put(index, value);
	    return;
	}
	if(name.equals("inventory"))
	{
	    Main.main.game.inventory.remove(index);
	    Main.main.game.inventory.put(index, value);
	    
	    // hävitä hiirikursori
	    cursor.remove((String)index);
	    Main.main.findCursor(true);
	    
	    return;
	}
	
	System.out.println("error in script: "+name);
    }
    
    
    void setItem(String item, boolean toInventory, boolean toCursor, boolean removePoly, boolean removeItem)
    {
	action.put(item, "0");
	
	if(toInventory) Main.main.game.inventory.put(item, "1");
	
	// todo ehkä toinen toteuttamistapa
	// tavara hiiripointteriksi
	BufferedImage img=null;
	
	for(int qqq=0; qqq < Main.main.game.curRoom.polys.size(); qqq++)
	{
	    if(Main.main.game.curRoom.polys.get(qqq).getItemName().equals(item))
	    {
		if(removePoly)
		{
		    Main.main.game.curRoom.polys.get(qqq).visible=false;
		}
		break;
	    }
	}
	
	for(int qqq=0; qqq < Main.main.game.curRoom.objs.size(); qqq++)
	{
	    if(Main.main.game.curRoom.objs.get(qqq).name.equals(item))
	    {
		
		if(toCursor) img=Main.main.game.curRoom.objs.get(qqq).pic;
		if(removeItem) Main.main.game.curRoom.objs.get(qqq).visible=false;
	    }
	}
	
	if(toCursor)
	{
	    ScriptFuncs.cursor.add(item, img);
	}
	
    }
    
}


class Animation
{
    // animaation kuvat
    Vector pics=new Vector();
    
    public void add(BufferedImage picname)
    {
	pics.add(picname);
    }
    
    public BufferedImage get(int i)
    {
	return (BufferedImage)pics.get(i);
    }
}
