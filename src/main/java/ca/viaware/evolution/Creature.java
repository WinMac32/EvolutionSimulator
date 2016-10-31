package ca.viaware.evolution;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;

class Creature {
    private Evolution evolution;
    private ArrayList<Node> nodes;
    private ArrayList<Muscle> muscles;
    private float distance;
    private int id;
    private boolean alive;
    private float creatureTimer;
    private float mutability;
    private float energy;

    Creature(Evolution evolution, float energy, int id, ArrayList<Node> nodes, ArrayList<Muscle> muscles, float distance, boolean alive, float creatureTimer, float mutability) {
        this.evolution = evolution;
        this.id = id;
        this.muscles = muscles;
        this.nodes = nodes;
        this.distance = distance;
        this.alive = alive;
        this.creatureTimer = creatureTimer;
        this.mutability = mutability;
        this.energy = energy;
    }

    Creature modified(int id) {
        Creature modifiedCreature = new Creature(evolution, energy, id,
                new ArrayList<Node>(0), new ArrayList<Muscle>(0), 0, true, creatureTimer + evolution.r() * 16 * mutability, PApplet.min(mutability * evolution.random(0.8f, 1.25f), 2));
        for (int i = 0; i < nodes.size(); i++) {
            modifiedCreature.nodes.add(nodes.get(i).modifyNode(mutability, nodes.size()));
        }
        for (int i = 0; i < muscles.size(); i++) {
            modifiedCreature.muscles.add(muscles.get(i).modifyMuscle(modifiedCreature, nodes.size(), mutability));
        }
        float mutationChance = Globals.BIG_MUTATION_CHANCE * mutability;
        if (evolution.random(0, 1) < mutationChance || nodes.size() <= 2) { //Add a node
            modifiedCreature.addRandomNode();
        }
        if (evolution.random(0, 1) < mutationChance) { //Add a muscle
            modifiedCreature.addRandomMuscle(-1, -1);
        }
        if (evolution.random(0, 1) < mutationChance && modifiedCreature.nodes.size() >= 4) { //Remove a node
            modifiedCreature.removeRandomNode();
        }
        if (evolution.random(0, 1) < mutationChance && modifiedCreature.muscles.size() >= 2) { //Remove a muscle
            modifiedCreature.removeRandomMuscle();
        }
        modifiedCreature.checkForOverlap();
        modifiedCreature.checkForLoneNodes();
        modifiedCreature.checkForBadAxons();
        return modifiedCreature;
    }

    void checkForOverlap() {
        ArrayList<Integer> bads = new ArrayList<Integer>();
        for (int i = 0; i < muscles.size(); i++) {
            for (int j = i + 1; j < muscles.size(); j++) {
                if (muscles.get(i).c1 == muscles.get(j).c1 && muscles.get(i).c2 == muscles.get(j).c2) {
                    bads.add(i);
                } else if (muscles.get(i).c1 == muscles.get(j).c2 && muscles.get(i).c2 == muscles.get(j).c1) {
                    bads.add(i);
                } else if (muscles.get(i).c1 == muscles.get(i).c2) {
                    bads.add(i);
                }
            }
        }
        for (int i = bads.size() - 1; i >= 0; i--) {
            int b = bads.get(i) + 0;
            if (b < muscles.size()) {
                muscles.remove(b);
            }
        }
    }

    void checkForLoneNodes() {
        if (nodes.size() >= 3) {
            for (int i = 0; i < nodes.size(); i++) {
                int connections = 0;
                int connectedTo = -1;
                for (int j = 0; j < muscles.size(); j++) {
                    if (muscles.get(j).c1 == i || muscles.get(j).c2 == i) {
                        connections++;
                        connectedTo = j;
                    }
                }
                if (connections <= 1) {
                    int newConnectionNode = PApplet.floor(evolution.random(0, nodes.size()));
                    while (newConnectionNode == i || newConnectionNode == connectedTo) {
                        newConnectionNode = PApplet.floor(evolution.random(0, nodes.size()));
                    }
                    addRandomMuscle(i, newConnectionNode);
                }
            }
        }
    }

    void checkForBadAxons() {
        for (int i = 0; i < nodes.size(); i++) {
            Node ni = nodes.get(i);
            if (ni.axon1 >= nodes.size()) {
                ni.axon1 = (int) evolution.random(0, nodes.size());
            }
            if (ni.axon2 >= nodes.size()) {
                ni.axon2 = (int) evolution.random(0, nodes.size());
            }
        }
        for (int i = 0; i < muscles.size(); i++) {
            Muscle mi = muscles.get(i);
            if (mi.axon >= nodes.size()) {
                mi.axon = evolution.getNewMuscleAxon(nodes.size());
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node ni = nodes.get(i);
            ni.safeInput = (Globals.OPERATION_AXONS[ni.operation] == 0);
        }
        int iterations = 0;
        boolean didSomething = false;

        while (iterations < 1000) {
            didSomething = false;
            for (int i = 0; i < nodes.size(); i++) {
                Node ni = nodes.get(i);
                if (!ni.safeInput) {
                    if ((Globals.OPERATION_AXONS[ni.operation] == 1 && nodes.get(ni.axon1).safeInput) ||
                            (Globals.OPERATION_AXONS[ni.operation] == 2 && nodes.get(ni.axon1).safeInput && nodes.get(ni.axon2).safeInput)) {
                        ni.safeInput = true;
                        didSomething = true;
                    }
                }
            }
            if (!didSomething) {
                iterations = 10000;
            }
        }

        for (Node ni : nodes) {
            if (!ni.safeInput) { // This node doesn't get its input from a safe place.  CLEANSE IT.
                ni.operation = 0;
                ni.value = evolution.random(0, 1);
            }
        }
    }

    private void addRandomNode() {
        int parentNode = PApplet.floor(evolution.random(0, nodes.size()));
        float ang1 = evolution.random(0, 2 * PConstants.PI);
        float distance = PApplet.sqrt(evolution.random(0, 1));
        float x = nodes.get(parentNode).x + PApplet.cos(ang1) * 0.5f * distance;
        float y = nodes.get(parentNode).y + PApplet.sin(ang1) * 0.5f * distance;

        int newNodeCount = nodes.size() + 1;

        nodes.add(new Node(evolution, x, y, 0, 0, 0.4f, evolution.random(0, 1), evolution.random(0, 1), PApplet.floor(evolution.random(0, Globals.OPERATION_COUNT)),
                PApplet.floor(evolution.random(0, newNodeCount)), PApplet.floor(evolution.random(0, newNodeCount)))); //random(0.1,1),random(0,1)
        int nextClosestNode = 0;
        float record = 100000;
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (i != parentNode) {
                float dx = nodes.get(i).x - x;
                float dy = nodes.get(i).y - y;
                if (PApplet.sqrt(dx * dx + dy * dy) < record) {
                    record = PApplet.sqrt(dx * dx + dy * dy);
                    nextClosestNode = i;
                }
            }
        }
        addRandomMuscle(parentNode, nodes.size() - 1);
        addRandomMuscle(nextClosestNode, nodes.size() - 1);
    }

    private void addRandomMuscle(int tc1, int tc2) {
        int axon = evolution.getNewMuscleAxon(nodes.size());
        if (tc1 == -1) {
            tc1 = (int) evolution.random(0, nodes.size());
            tc2 = tc1;
            while (tc2 == tc1 && nodes.size() >= 2) {
                tc2 = (int) evolution.random(0, nodes.size());
            }
        }
        float len = evolution.random(0.5f, 1.5f);
        if (tc1 != -1) {
            len = PApplet.dist(nodes.get(tc1).x, nodes.get(tc1).y, nodes.get(tc2).x, nodes.get(tc2).y);
        }
        muscles.add(new Muscle(evolution, this, axon, tc1, tc2, len, evolution.random(0.02f, 0.08f)));
    }

    private void removeRandomNode() {
        int choice = PApplet.floor(evolution.random(0, nodes.size()));
        nodes.remove(choice);
        int i = 0;
        while (i < muscles.size()) {
            if (muscles.get(i).c1 == choice || muscles.get(i).c2 == choice) {
                muscles.remove(i);
            } else {
                i++;
            }
        }
        for (int j = 0; j < muscles.size(); j++) {
            if (muscles.get(j).c1 >= choice) {
                muscles.get(j).c1--;
            }
            if (muscles.get(j).c2 >= choice) {
                muscles.get(j).c2--;
            }
        }
    }

    private void removeRandomMuscle() {
        int choice = PApplet.floor(evolution.random(0, muscles.size()));
        muscles.remove(choice);
    }

    Creature copyCreature(int newID) {
        if (newID == -1) {
            newID = id;
        }

        Creature creature = new Creature(evolution, energy, newID, new ArrayList<Node>(), new ArrayList<Muscle>(), distance, alive, creatureTimer, mutability);

        for (int i = 0; i < nodes.size(); i++) {
            creature.getNodes().add(this.nodes.get(i).copyNode());
        }
        for (int i = 0; i < muscles.size(); i++) {
            creature.getMuscles().add(this.muscles.get(i).copyMuscle(creature));
        }
        return creature;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Muscle> getMuscles() {
        return muscles;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public float getCreatureTimer() {
        return creatureTimer;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }
}
