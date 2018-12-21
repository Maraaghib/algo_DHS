package Main;

import jbotsim.Topology;
import jbotsimx.topology.TopologyGenerator;
import jbotsimx.ui.JViewer;

public class Main {

    public static void main(String[] args) {
        Topology t = new Topology();
        t.setDefaultNodeModel(BasicNode.class);
        TopologyGenerator.generateRing(t, 6);
        new JViewer(t);
        t.start();
    }
}
