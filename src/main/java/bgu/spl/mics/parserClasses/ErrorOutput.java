package bgu.spl.mics.parserClasses;

import bgu.spl.mics.application.objects.ERROR;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.List;

public class ErrorOutput {
    private ERROR err;
    private StatisticalFolder stats;
    private List<LandMark> landMarks;
    public ErrorOutput(){}
    public ErrorOutput(ERROR er, StatisticalFolder stat, List<LandMark> lms){
        this.err = er;
        this.stats = stat;
        this.landMarks = lms;
    }
    private static class OutputHolder {
        private static ErrorOutput instance = new ErrorOutput();
    }
    public static ErrorOutput getInstance() {
        return ErrorOutput.OutputHolder.instance;
    }
    public ERROR getErr(){return err;}
    public StatisticalFolder getStats(){return stats;}
    public List<LandMark> getLandMarks(){return landMarks;}

}
