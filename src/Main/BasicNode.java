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
    private Node best;
    private double bestDistance = -1;
    private Node bestRoot;
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
            if(sonsInFragment.isEmpty() && currentFragment != this)
            {
                send(fatherInFragment, new MinMessage(bestRoot, bestDistance));
            }
        }
        if(state.equals(States.MIN) && fatherInFragment == this)
        {
           for(Node f : sonsInFragment)
               send(f, new ReadyMessage());
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
        else if(content.getClass() == ReadyMessage.class)
            handleReady(message);

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
        if(bestDistance < 0 || distance(msg.getSender()) < bestDistance)
        {
            bestDistance = distance(msg.getSender());
            best = msg.getSender();
            bestRoot = ((FragMessage)msg.getContent()).fragmentRoot;
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
        F = null;
        best = null;
        bestRoot = null;
        bestDistance = -1;
        if(this == fatherInFragment)
        {
            for(Node sons : sonsInFragment)
                    send(sons, new PulseMessage());
        }
    }

    private void handleMin(Message msg)
    {
        MinMessage msgContent = (MinMessage)msg.getContent();
        if(msgContent.distance < bestDistance)
        {
            bestRoot = msgContent.bestFragment;
            bestDistance = msgContent.distance;
            best = null;
            F = msg.getSender();
        }
        send(fatherInFragment, new MinMessage(bestRoot, bestDistance));
        state = States.MIN;
    }

    private void handleReady(Message msg)
    {

    }
}
