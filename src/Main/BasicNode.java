package Main;

import Main.MessageObjects.*;
import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BasicNode extends Node {
    //Tree we get with "Flood"
    private Node fatherT = null;

    //tree we want to build
    private Node currentFragment = null;
    private Node fatherInFragment = null;
    private Node best;
    private Link bestLink;
    private double bestDistance = -1;
    private Node bestRoot;
    private Node F;

    private States state;

    private Vector<Node> sonsInFragment;
    private Map<Link, Node> border;
    private Vector<Integer> ready;


    private boolean castReceive = false;

    private boolean fragSent = false;
    @Override
    public void onStart() {
        currentFragment = this;
        fatherInFragment = this;
        sonsInFragment = new Vector<>();
        border = new HashMap<>();
        ready = new Vector<>();
        state = States.DONE;
        if(getID() == 1)
        {
            fatherT = this;
            sendAll(new FloodMessage(getID()));
        }
    }

    @Override
    public void onClock() {
        // code to be executed by this node in each round
    	//4
        if(state.equals(States.PULSE))
        {
            if(sonsInFragment.isEmpty() && currentFragment != this)
            {
                send(fatherInFragment, new MinMessage(bestRoot, best, bestDistance));
            }
        }
        
        //6
        if(state.equals(States.MIN) && fatherInFragment == this)
        {
           for(Node f : sonsInFragment)
               send(f, new ReadyMessage(bestLink, F));
        }
        
        //8
        if(state.equals(States.READY) && !castReceive)
        {
            if(best == this)
            {
                for(Map.Entry<Link, Node> entries : border.entrySet())
                {
                    Link l = entries.getKey();
                    if(l.destination == this || l.source == this)
                    {
                        Node other = l.destination;
                        if (other == this)
                            other = l.source;
                        if(getID() < other.getID())
                        {
                            for(Node n : sonsInFragment)
                                send(n, new CastMessage(currentFragment));
                            send(fatherInFragment, new CastMessage(currentFragment));
                            send(this, new CastMessage(currentFragment));
                        }
                    }
                }
            }
        }
        
        //9
        if(state.equals(States.READY) && castReceive)
        {
            if(ready.size() == border.size())
            {
                for(Map.Entry<Link, Node> entries : border.entrySet())
                {
                    if(entries.getKey().destination == this || entries.getKey().source == this)
                    {
                        Node n = entries.getValue();
                        sonsInFragment.add(n);
                    }
                }
                for(Node n : sonsInFragment)
                    send(n, new CastMessage(currentFragment));
                state = States.CAST;
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
        else if(content.getClass() == ReadyMessage.class)
            handleReady(message);
        else if(content.getClass() == CastMessage.class)
            handleCast(message);
        else if(content.getClass() == DoneMessage.class)
        	handleDone(message);

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

        if(fatherT == null)
        {
            fatherT = msg.getSender();
            sendAll(new FloodMessage(getID()));
            send(this, new PulseMessage());
        }
    }

    private void handleFrag(Message msg)
    {
        if(!fragSent)
        {
            send(msg.getSender(), new FragMessage(currentFragment));
            fragSent = true;
        }
        border.put(this.getCommonLinkWith(msg.getSender()), msg.getSender());
        if(bestDistance < 0 || distance(msg.getSender()) < bestDistance)
        {
            bestDistance = distance(msg.getSender());
            best = msg.getSender();
            bestLink = msg.getSender().getCommonLinkWith(this);
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
        ready.clear();
        border.clear();
        castReceive = false;

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
            best = msgContent.bestNode;
            F = msg.getSender();
        }
        send(fatherInFragment, new MinMessage(bestRoot, best, bestDistance));
        state = States.MIN;
    }

    private void handleReady(Message msg)
    {

        ReadyMessage content = (ReadyMessage)msg.getContent();
        if(msg.getSender().equals(fatherInFragment))
        {
            Node f = content.F;
            if(f == this)
            {
                f = F;
                fatherInFragment = F;
                sonsInFragment.remove(F);
            }

            for(Node n : sonsInFragment)
                send(n, new ReadyMessage(bestLink, f));
            for(Map.Entry<Link, Node> entries : border.entrySet())
            {
                Node n = entries.getValue();
                send(n, new ReadyMessage(bestLink, f));
            }

            // 7.3
            //currentFragment =

            state = States.READY;
        }
        else{
        	// 8
            //TODO : faire l'ajout de (ab, v) dans ready
            ready.add(3);
        }
    }

    private void handleCast(Message msg)
    {
        castReceive = true;
    }
    
    private void handleDone(Message msg) {
    	DoneMessage content = (DoneMessage)msg.getContent();

    	if(fatherT != this)
        {
            int id = currentFragment.getID();
            if(content.f1 > id)
                content.f1 = id;
            if(content.f2 < id)
                content.f2 = id;

            send(fatherT, content);
        }
        else{
            if(content.f1 != content.f2)
                sendAll(new SyncMessage());
        }

    }
}
