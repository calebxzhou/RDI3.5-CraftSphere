package calebxzhou.test

import javax.sound.midi.Instrument
import javax.sound.midi.MidiSystem

/**
 * Created  on 2023-02-24,23:46.
 */
fun main() {
    val midiSynth = MidiSystem.getSynthesizer()
    midiSynth.open()
    //get and load default instrument and channel lists
    val instr: Array<Instrument> = midiSynth.defaultSoundbank.instruments
    val mChannels = midiSynth.channels
    midiSynth.loadInstrument(instr[0]) //load an instrument
    for(i in 60..80){
        mChannels[0].noteOn(i, 50)
    }
}
