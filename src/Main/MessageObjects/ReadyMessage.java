package Main.MessageObjects;

import jbotsim.Link;
import jbotsim.Node;

public class ReadyMessage {
    public Node F;
    public Link edge;

    public ReadyMessage(Link edge,Node F)
    {
        this.F = F;
        this.edge = edge;
    }
}
