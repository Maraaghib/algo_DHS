package Main.MessageObjects;

import jbotsim.Node;

public class ReadyMessage {
    public Node F;
    public Node bestNode;

    public ReadyMessage(Node best,Node F)
    {
        this.F = F;
        bestNode = best;
    }
}
