package Main.MessageObjects;

import jbotsim.Node;

public class MinMessage {

    public Node bestFragment;
    public Node bestNode;
    public double distance;

    public MinMessage (Node bestFragment, Node bestNode, double dist)
    {
        this.bestFragment = bestFragment;
        this.bestNode = bestNode;
        distance = dist;

    }
}
