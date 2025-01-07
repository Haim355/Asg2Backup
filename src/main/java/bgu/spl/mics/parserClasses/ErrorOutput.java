package bgu.spl.mics.parserClasses;

import bgu.spl.mics.application.objects.ERROR;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.List;
import java.util.Map;

public class ErrorOutput {
    private ERROR Error;
    private StatisticalFolder stats;
    private Map< String , LandMark > landMarks;
    public ErrorOutput(){}
    public ErrorOutput(ERROR er, StatisticalFolder stat, Map< String , LandMark > lms){
        this.Error = er;
        this.stats = stat;
        this.landMarks = lms;
    }
    public ERROR getError(){return Error;}
    public StatisticalFolder getStats(){return stats;}
    public Map<String , LandMark> getLandMarks(){return landMarks;}

    public void setError(ERROR error){
        this.Error = error;
    }
    public void setStats(StatisticalFolder stat){
        this.stats = stat;
    }
    public void setLandMarks(Map< String , LandMark > land){
        this.landMarks = land;
    }
    private static class OutputHolder {
        private static ErrorOutput instance = new ErrorOutput();
    }
    public static ErrorOutput getInstance() {
        return ErrorOutput.OutputHolder.instance;
    }

}
