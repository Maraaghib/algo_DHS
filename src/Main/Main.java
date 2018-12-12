package Main;

import jbotsim.Topology;
import jbotsimx.ui.JViewer;

public class Main {

    public static void main(String[] args) {
        Topology t = new Topology();
        t.setDefaultNodeModel(BasicNode.class);
        t.addNode(new BasicNode());
        new JViewer(t);
        t.start();
    }
}
