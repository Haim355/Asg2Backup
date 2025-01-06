package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.EventImp;

public class ExampleEvent extends EventImp<String> implements Event<String> {

    private String senderName;

    public ExampleEvent(String senderName) {
        super(senderName);
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}