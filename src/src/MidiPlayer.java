/**
 * @file Room.java
 * @author mjt, 2006-07
 * mixut@hotmail.com
 *
 */
package tstgame;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

/**
 * midi-tiedostojen soittoluokka
 */
public class MidiPlayer
{
    static Sequencer sequencer=null;
    
    /**
     *	lataa file miditiedosto. jos res==true, se ladataan jar-tiedostosta
     */
    static void playMidi(String file)
    {
	URL url=null;
	File midiFile=null;
	file=Main.SOUNDDIR+file;
	try
	{
	    sequencer = MidiSystem.getSequencer();
	    if(sequencer==null) return;
	    
	    url = Main.main.getClass().getResource(file);
	    if(url==null)
	    {
		midiFile = new File(file);
		sequencer.setSequence(MidiSystem.getSequence(midiFile));
	    }
	    else
		sequencer.setSequence(MidiSystem.getSequence(url));
	    
	    sequencer.open();
	    sequencer.start();
	}
	catch(MidiUnavailableException mue)
	{
	    sequencer=null;
	    FileIO.ErrorMessage("Midi device unavailable!");
	}
	catch(InvalidMidiDataException imde)
	{
	    sequencer=null;
	    FileIO.ErrorMessage("Invalid Midi data!");
	}
	catch(IOException ioe)
	{
	    sequencer=null;
	    FileIO.ErrorMessage("Invalid Midi data!");
	}
	
    }
    static void closeMidi()
    {
	if(sequencer!=null)
	{
	    sequencer.stop();
	    sequencer.close();
	}
    }
    
    static void stopMidi()
    {
	closeMidi();
	sequencer=null;
	
	try
	{
	    Thread.sleep(300);
	}
	catch (InterruptedException e)
	{
	}
    }
    
}
