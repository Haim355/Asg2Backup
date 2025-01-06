package bgu.spl.mics.application.objects;

import java.util.List;

public class Output {
    private ERROR err;
    private StatisticalFolder stats;
    private List<LandMark> landMarks;
    public Output(){}
    public Output(ERROR er, StatisticalFolder stat, List<LandMark> lms){
        this.err = er;
        this.stats = stat;
        this.landMarks = lms;
    }
    private static class OutputHolder {
        private static Output instance = new Output();
    }
    public static Output getInstance() {
        return Output.OutputHolder.instance;
    }
    public ERROR getErr(){return err;}
    public StatisticalFolder getStats(){return stats;}
    public List<LandMark> getLandMarks(){return landMarks;}

}
