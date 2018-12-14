package Main;

import Main.MessageObjects.*;
import jbotsim.Message;
import jbotsim.Node;

import java.util.Vector;

public class BasicNode extends Node {
    //Tree we get with "Flood"
    private Node fatherT = null;

    //tree we want to build
    private Node currentFragment = null;
    private Node fatherInFragment = null;
    private int best = -1;
    private int bestID;
    private Node F;

    private States state;

    private Vector<Node> sonsInFragment;
    private Vector<Node> border;
    private Vector<Integer> readys;

    private boolean fragSent = false;
    @Override
    public void onStart() {
        if(getID() == 0)
        {
            fatherT = this;
            sendAll(new FloodMessage(getID()));
        }
        currentFragment = this;
        fatherInFragment = this;
        sonsInFragment = new Vector<>();
        border = new Vector<>();
        readys = new Vector<>();
        state = States.DONE;
    }

    @Override
    public void onClock() {
        // code to be executed by this node in each round
        if(state.equals(States.PULSE))
        {
            if(sonsInFragment.isEmpty())
            {
                System.out.println("remve This");
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        Object content = message.getContent();

        if(content.getClass() == FloodMessage.class)
            handleFlood(message);
        else if(content.getClass() == FragMessage.class)
            handleFrag(message);
        else if(content.getClass() == PulseMessage.class)
            handlePulse(message);
        else if(content.getClass() == SyncMessage.class)
            handleSync(message);
        else if(content.getClass() == MinMessage.class)
            handleMin(message);

    }

    @Override
    public void onSelection() {
        // what to do when this node is selected by the user
    }

    public boolean setF(Node n)
    {
        if(!sonsInFragment.contains(n))
            return false;
        F = n;
        return true;
    }

    private void handleFlood(Message msg)
    {
        if(fatherT != null)
        {
            System.out.println(getID());
            fatherT = msg.getSender();
            sendAll(new FloodMessage(getID()));
        }
    }

    private void handleFrag(Message msg)
    {
        if(!fragSent)
        {
            send(msg.getSender(), new FragMessage(currentFragment));
            fragSent = true;
        }
        border.add(msg.getSender());
        //TODO : calculer la distance pour calculer le BEST
        if(best < 0 /*|| dist < best*/)
        {
            bestID = msg.getSender().getID();
        }


    }

    private void handlePulse(Message msg)
    {
        //Si on recoit pulse mais qu'on est deja dans l'état pulse, on a déja fait les actions
        //donc on ne fait rien
        if(state.equals(States.PULSE))
            return;

        fragSent = false;
        for (Node n: getOutNeighbors()) {
            for (Node i : sonsInFragment)
                if (!i.equals(n) || !(fatherInFragment.equals(n)))
                    send(n, new FragMessage(currentFragment));
        }
        fragSent = true;
        for(Node sons : sonsInFragment)
            send(sons, new PulseMessage());
        state = States.PULSE;
    }

    private void handleSync(Message msg)
    {
        if(this == fatherInFragment)
        {
            for(Node sons : sonsInFragment)
                    send(sons, new PulseMessage());
        }
    }

    private void handleMin(Message msg)
    {

    }
}
