package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private int counter;
    private List<Pose> poseList;
    public GPSIMU(int currentTick, List<Pose> poseList) {
        this.currentTick = currentTick;
        status = STATUS.UP;
        this.poseList = poseList;
        this.counter=0;
    }
    public int getCurrentTick() {return currentTick;}
    public STATUS getStatus() {return status;}
    public void setStatus(STATUS status) {this.status = status;}
    public List<Pose> getPoseList() {return poseList;}
    public void addPose(Pose pose){poseList.add(pose);}
    /**Function giving time returns the pose position (x,y,yaw)**/
    public Pose getPoseByTime(int time) {
        if (status != STATUS.DOWN) {
            for (Pose pose : poseList) {
                if (pose.getTime() == time) {
                    counter++;
                    return pose;
                }
                if (counter == poseList.size()) {
                    status = STATUS.DOWN;
                }

            }
        }
        return null;
    }
}
