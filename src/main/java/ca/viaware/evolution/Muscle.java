package ca.viaware.evolution;

import processing.core.PApplet;

import java.util.ArrayList;

class Muscle {
    private Evolution evolution;
    int axon, c1, c2;
    float len;
    float rigidity;
    float previousTarget;
    private Creature parent;

    Muscle(Evolution evolution, Creature parent, int axon, int c1, int c2, float len, float rigidity) {
        this.evolution = evolution;
        this.parent = parent;
        this.axon = axon;
        previousTarget = this.len = len;
        this.c1 = c1;
        this.c2 = c2;
        this.rigidity = rigidity;
    }

    void applyForce(ArrayList<Node> others) {
        float target;
        if (Globals.ENERGY_DIRECTION == 1 || parent.getEnergy() >= 0.0001) {
            if (axon >= 0 && axon < others.size()) {
                target = len * evolution.toMuscleUsable(others.get(axon).value);
            } else {
                target = len;
            }
        }
        Node node1 = others.get(c1);
        Node node2 = others.get(c2);
        float distance = PApplet.dist(node1.x, node1.y, node2.x, node2.y);
        float angle = PApplet.atan2(node1.y - node2.y, node1.x - node2.x);
        float force = PApplet.min(PApplet.max(1 - (distance / target), -0.4f), 0.4f);
        float cos = PApplet.cos(angle) * force * rigidity;
        float sin = PApplet.sin(angle) * force * rigidity;
        node1.vx += cos / node1.m;
        node1.vy += sin / node1.m;
        node2.vx -= cos / node2.m;
        node2.vy -= sin / node2.m;
        parent.setEnergy(PApplet.max(parent.getEnergy() + Globals.ENERGY_DIRECTION * PApplet.abs(previousTarget - target) * rigidity * Globals.ENERGY_UNIT, 0));
        previousTarget = target;
    }

    Muscle copyMuscle(Creature parent) {
        return new Muscle(evolution, parent, axon, c1, c2, len, rigidity);
    }

    Muscle modifyMuscle(Creature parent, int nodeNum, float mutability) {
        int newc1 = c1;
        int newc2 = c2;
        int newAxon = axon;
        float mutationChance = Globals.BIG_MUTATION_CHANCE * mutability;
        if (evolution.random(0, 1) < mutationChance) {
            newc1 = (int) evolution.random(0, nodeNum);
        }
        if (evolution.random(0, 1) < mutationChance) {
            newc2 = (int) evolution.random(0, nodeNum);
        }
        if (evolution.random(0, 1) < mutationChance) {
            newAxon = evolution.getNewMuscleAxon(nodeNum);
        }
        float newR = PApplet.min(PApplet.max(rigidity * (1 + evolution.r() * 0.9f * mutability), 0.01f), 0.08f);
        float newLen = PApplet.min(PApplet.max(len + evolution.r() * mutability, 0.4f), 1.25f);

        return new Muscle(evolution, parent, newAxon, newc1, newc2, newLen, newR);
    }
}
