/**
 * @file FileIO.java
 * @author mjt, 2006-07
 * mixut@hotmail.com
 *
 * tiedostonkäsittely luokka.
 *
 */

package tstgame;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class FileIO
{
    File file;
    
    /**
     * avaa tiedosto
     *
     * @param fileName
     * @return
     */
    public boolean openFile(String fileName)
    {
	file = new File(fileName);
	if(file==null) return false;
	return true;
    }
    
    /**
     * luo uusi tiedosto. jos jo olemassa, kysyy että kirjoitetaanko päälle
     *
     * @param fileName
     * @return
     */
    public boolean createFile(String fileName)
    {
	file = new File(fileName);
	
	// jos tiedosto olemassa
	if (file.exists())
	{
	    int response = JOptionPane.showConfirmDialog(null, "Korvataanko tiedosto?", "Samanniminen tiedosto löytyi",
		 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	    if (response == JOptionPane.CANCEL_OPTION)
		return false;
	    
	    // poista vanha
	    file.delete();
	}
	
	return true;
    }
    
    /**
     * lue koko tiedosto Stringiin joka palautetaan
     *
     * @return true jos onnistui, muuten false
     */
    public String readFile()
    {
	StringBuffer fileBuffer;
	String fileString = null;
	String line;
	
	try
	{
	    FileReader in = new FileReader(file);
	    if(in==null) return null;
	    BufferedReader dis = new BufferedReader(in);
	    fileBuffer = new StringBuffer();
	    
	    while ((line = dis.readLine()) != null)
	    {
		fileBuffer.append(line + "\n");
	    }
	    
	    in.close();
	    fileString = fileBuffer.toString();
	    
	}
	catch (IOException e)
	{
	    System.out.println("readFile(): " + e);
	    ErrorMessage("readFile(): "+ e.getMessage());
	    return null;
	}
	return fileString;
    }
    
    /**
     * kirjoita dataString tiedostoon
     *
     * @param dataString
     *            kirjoitettava teksti
     * @return true,jos onnistui. muuten false
     */
    public boolean writeFile(String dataString)
    {
	try
	{
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	    out.print(dataString);
	    out.flush();
	    out.close();
	}
	catch (IOException e)
	{
	    return false;
	}
	return true;
    }
    
    
    
    public String openAndReadFile(String fileName)
    {
	String fileString = "";
	String line;
	
	try
	{
	    // tarkista onko tiedosto olemassa
	    file = new File(fileName);
	    if(file.exists())
	    {
		return readFile();
	    
	    }
	    else // etsi paketista
	    {
		InputStream in = this.getClass().getResourceAsStream(fileName);
		if(in==null) return null;
		int c;
		while((c = in.read()) != -1)
		{
		    if(c=='\r') continue;
		    fileString+=(char)c;
		}
	    }
	    
	}
	catch (IOException e)
	{
	    System.out.println("openAndReadFile("+file+"): " + e);
	    ErrorMessage("openAndReadFile("+file+"): "+e.getMessage());
	    return null;
	}
	return fileString;
    }
    
    static void ErrorMessage(String msg)
    {
	if (msg.equals("")) return;
	System.out.println("Virhe: "+ msg);
	JOptionPane.showMessageDialog(null, msg, "Virhe", JOptionPane.ERROR_MESSAGE);
	Main.running=false;
    }
    
    
    
}
