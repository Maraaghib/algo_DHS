package Main;

import Main.MessageObjects.*;
import jbotsim.Message;
import jbotsim.Node;

import java.util.Vector;

public class BasicNode extends Node {
    //Tree we get with "Flood"
    private int fatherT = -1;

    //tree we want to build
    private int currentFragment = -1;
    private int fatherInFragment = -1;
    private int best = -1;
    private int F = -1;

    private States state;

    private Vector<Integer> sonsInFragment;
    private Vector<Integer> border;
    private Vector<Integer> readys;

    private boolean fragSent = false;
    @Override
    public void onStart() {
        if(getID() == 0)
        {
            fatherT = getID();
            sendAll(new FloodMessage(getID()));
        }
        currentFragment = getID();
        fatherInFragment = getID();
        sonsInFragment = new Vector<>();
        border = new Vector<>();
        readys = new Vector<>();
        state = States.DONE;
    }

    @Override
    public void onClock() {
        // code to be executed by this node in each round
    }

    @Override
    public void onMessage(Message message) {
        Object content = message.getContent();

        if(content.getClass() == FloodMessage.class)
            handleFlood((FloodMessage) content);
        else if(content.getClass() == FragMessage.class)
            handleFrag((FragMessage) content, message.getSender());
        else if(content.getClass() == PulseMessage.class)
            handlePulse((PulseMessage) content);
        else if(content.getClass() == SyncMessage.class)
            handleSync((SyncMessage) content);
        else if(content.getClass() == MinMessage.class)
            handleMin((MinMessage) content);

    }

    @Override
    public void onSelection() {
        // what to do when this node is selected by the user
    }

    public boolean setF(int id)
    {
        Integer ID = id;
        if(!sonsInFragment.contains(ID))
            return false;
        F = id;
        return true;
    }

    private void handleFlood(FloodMessage msg)
    {
        if(fatherT < 0)
        {
            System.out.println(getID());
            fatherT = msg.father;
            sendAll(new FloodMessage(getID()));
        }
    }

    private void handleFrag(FragMessage msg, Node sender)
    {
        if(!fragSent)
        {
            send(sender, new FragMessage(currentFragment));
            fragSent = true;
        }
        border.add(msg.fragmentRoot);
        //TODO : calculer la distance pour calculer le BEST
        if(best < 0 /*|| dist < best*/)
        {}
    }

    private void handlePulse(PulseMessage msg)
    {
        fragSent = false;
        for (Node n: getOutNeighbors()) {
            for (Integer i : sonsInFragment)
                if (!i.equals(n.getID()) || !(fatherInFragment == n.getID()))
                    send(n, new FragMessage(currentFragment));
        }
        fragSent = true;
    }

    private void handleSync(SyncMessage msg)
    {
        if(fatherInFragment == getID())
        {
            for (Node n: getOutNeighbors())
                for(Integer i : sonsInFragment)
                    if(i.equals(n.getID()))
                        send(n, new PulseMessage());
        }
    }

    private void handleMin(MinMessage msg)
    {

    }
}
