package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {
    MicroService Sendermicro;

    public TerminatedBroadcast(MicroService micro) {
        this.Sendermicro=micro;
    }

    public MicroService getSendermicro() {
        return Sendermicro;
    }
}
