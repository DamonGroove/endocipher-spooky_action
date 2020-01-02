package com.spookyaction.midi;

import com.spookyaction.InputDataListBuilder;
import com.spookyaction.Main;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.util.logging.Level;

public class VirtualDevice implements Transmitter, Receiver {

    private Receiver receiver;

    // Testing purposes Only extract timestamp once every 6 messages since the ManadalaDrum
    // sends out 6 messages per hit
    // Solve this by having the user test midi device during the device detection process
    // to determine how many midi messages are sent per trigger
    private int messagesPerEvent = 6;
    private int count = 0;
    private long sumDurations;

    InputDataListBuilder inputDataListBuilder = new InputDataListBuilder();

    @Override
    public void send(MidiMessage midiMessage, long timeStamp) {

        if(count >= messagesPerEvent) {
            count = 0;
        }

        if (count == 0) {
            long duration = (timeStamp - sumDurations);
            long dynamic = 0;
            long positionX = 0;
            long positionY = 0;
            if (sumDurations == 0) {
                Main.logger.log(Level.SEVERE, "Duration is 0");
            } else {
                Main.logger.log(Level.INFO, "Output: " + duration + " : " + dynamic + " : " + positionX + " : " + positionY);

                // Add to the data list that will sent the the web service in pieces
                inputDataListBuilder.addToInputDataList(duration, dynamic, positionX, positionY);
            }
            sumDurations = timeStamp;

            if (count >= myMaxCount) {
                Batch batch = inputDataListBuilder.build();
                // sent batch to API
                // advanced version: Add batch to a queue that will be sent to the API by another thread
                inputDataListBuilder = new InputDataListBuilder();
                count = 0;
            }
        }

        // Pass the original midi message and time stamp to the default receiver
        this.getReceiver().send(midiMessage, timeStamp);
        count++;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return this.receiver;
    }

    @Override
    public void close() {

    }
}