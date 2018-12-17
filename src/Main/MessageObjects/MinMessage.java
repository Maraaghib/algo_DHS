package Main.MessageObjects;

import jbotsim.Node;

public class MinMessage {

    public Node bestFragment;
    public double distance;

    public MinMessage (Node best, double dist)
    {
        bestFragment = best;
        distance = dist;

    }
}
