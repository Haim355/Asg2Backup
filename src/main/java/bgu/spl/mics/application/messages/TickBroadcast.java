package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    int currentTick;
    public TickBroadcast(int TickTime) {
        this.currentTick = TickTime;
    }
    public int getTickTime() {
        return currentTick;
    }
    public void IncrementTime(){
        currentTick++;
    }
}
