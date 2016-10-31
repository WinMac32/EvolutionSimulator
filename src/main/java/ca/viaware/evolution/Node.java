package ca.viaware.evolution;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;

public class Node {
    private Evolution evolution;
    float x, y, vx, vy, prevX, prevY, pvx, pvy, m, f, value, valueToBe;
    int operation, axon1, axon2;
    boolean safeInput;
    float pressure;

    Node(Evolution evolution, float tx, float ty, float tvx, float tvy, float tm, float tf, float val, int op, int a1, int a2) {
        this.evolution = evolution;
        prevX = x = tx;
        prevY = y = ty;
        pvx = vx = tvx;
        pvy = vy = tvy;
        m = tm;
        f = tf;
        value = valueToBe = val;
        operation = op;
        axon1 = a1;
        axon2 = a2;
        pressure = 0;
    }

    float applyForces() {
        vx *= Globals.AIR_FRICTION;
        vy *= Globals.AIR_FRICTION;
        y += vy;
        x += vx;
        float acc = PApplet.dist(vx, vy, pvx, pvy);

        float nausea = acc * acc * Globals.NAUSEA_UNIT;
        pvx = vx;
        pvy = vy;
        return nausea;
    }

    void applyGravity() {
        vy += Globals.GRAVITY;
    }

    void pressAgainstGround(float groundY) {
        float dif = y - (groundY - m / 2);
        pressure += dif * Globals.PRESSURE_UNIT;
        y = (groundY - m / 2);
        vy = 0;
        x -= vx * f;
        if (vx > 0) {
            vx -= f * dif * Globals.FRICTION;
            if (vx < 0) {
                vx = 0;
            }
        } else {
            vx += f * dif * Globals.FRICTION;
            if (vx > 0) {
                vx = 0;
            }
        }
    }

    void hitWalls() {
        pressure = 0;
        float dif = y + m / 2;
        if (dif >= 0 && Globals.HAVE_GROUND) {
            pressAgainstGround(0);
        }
        if (y > prevY && Globals.HAZEL_STAIRS >= 0) {
            float bottomPointNow = y + m / 2;
            float bottomPointPrev = prevY + m / 2;
            int levelNow = (int) (PApplet.ceil(bottomPointNow / Globals.HAZEL_STAIRS));
            int levelPrev = (int) (PApplet.ceil(bottomPointPrev / Globals.HAZEL_STAIRS));
            if (levelNow > levelPrev) {
                float groundLevel = levelPrev * Globals.HAZEL_STAIRS;
                pressAgainstGround(groundLevel);
            }
        }
        for (int i = 0; i < Globals.RECTS.size(); i++) {
            Rectangle r = Globals.RECTS.get(i);
            boolean flip = false;
            float px, py;
            if (PApplet.abs(x - (r.x1 + r.x2) / 2) <= (r.x2 - r.x1 + m) / 2 && PApplet.abs(y - (r.y1 + r.y2) / 2) <= (r.y2 - r.y1 + m) / 2) {
                if (x >= r.x1 && x < r.x2 && y >= r.y1 && y < r.y2) {
                    float d1 = x - r.x1;
                    float d2 = r.x2 - x;
                    float d3 = y - r.y1;
                    float d4 = r.y2 - y;
                    if (d1 < d2 && d1 < d3 && d1 < d4) {
                        px = r.x1;
                        py = y;
                    } else if (d2 < d3 && d2 < d4) {
                        px = r.x2;
                        py = y;
                    } else if (d3 < d4) {
                        px = x;
                        py = r.y1;
                    } else {
                        px = x;
                        py = r.y2;
                    }
                    flip = true;
                } else {
                    if (x < r.x1) {
                        px = r.x1;
                    } else if (x < r.x2) {
                        px = x;
                    } else {
                        px = r.x2;
                    }
                    if (y < r.y1) {
                        py = r.y1;
                    } else if (y < r.y2) {
                        py = y;
                    } else {
                        py = r.y2;
                    }
                }
                float distance = PApplet.dist(x, y, px, py);
                float rad = m / 2;
                float wallAngle = PApplet.atan2(py - y, px - x);
                if (flip) {
                    wallAngle += PConstants.PI;
                }
                if (distance < rad || flip) {
                    dif = rad - distance;
                    pressure += dif * Globals.PRESSURE_UNIT;
                    float multi = rad / distance;
                    if (flip) {
                        multi = -multi;
                    }
                    x = (x - px) * multi + px;
                    y = (y - py) * multi + py;
                    float veloAngle = PApplet.atan2(vy, vx);
                    float veloMag = PApplet.dist(0, 0, vx, vy);
                    float relAngle = veloAngle - wallAngle;
                    float relY = PApplet.sin(relAngle) * veloMag * dif * Globals.FRICTION;
                    vx = -PApplet.sin(relAngle) * relY;
                    vy = PApplet.cos(relAngle) * relY;
                }
            }
        }
        prevY = y;
        prevX = x;
    }

    void doMath(ArrayList<Node> n, int simulationTimer) {
        float axonValue1 = n.get(axon1).value;
        float axonValue2 = n.get(axon2).value;
        if (operation == 0) { // constant
        } else if (operation == 1) { // time
            valueToBe = simulationTimer / 60.0f;
        } else if (operation == 2) { // x - coordinate
            valueToBe = x * 0.2f;
        } else if (operation == 3) { // y - coordinate
            valueToBe = -y * 0.2f;
        } else if (operation == 4) { // plus
            valueToBe = axonValue1 + axonValue2;
        } else if (operation == 5) { // minus
            valueToBe = axonValue1 - axonValue2;
        } else if (operation == 6) { // times
            valueToBe = axonValue1 * axonValue2;
        } else if (operation == 7) { // divide
            valueToBe = axonValue1 / axonValue2;
        } else if (operation == 8) { // modulus
            valueToBe = axonValue1 % axonValue2;
        } else if (operation == 9) { // sin
            valueToBe = PApplet.sin(axonValue1);
        } else if (operation == 10) { // sig
            valueToBe = 1 / (1 + PApplet.pow(2.71828182846f, -axonValue1));
        } else if (operation == 11) { // pressure
            valueToBe = pressure;
        }
    }

    void realizeMathValues(int i) {
        value = valueToBe;
    }

    Node copyNode() {
        return (new Node(evolution, x, y, 0, 0, m, f, value, operation, axon1, axon2));
    }

    Node modifyNode(float mutability, int nodeNum) {
        float newX = x + evolution.r() * 0.5f * mutability;
        float newY = y + evolution.r() * 0.5f * mutability;
        //float newM = m + evolution.r() * 0.1f * mutability;
        //newM = PApplet.min(PApplet.max(newM, 0.3f), 0.5f);
        float newM = 0.4f;

        float newV = value * (1 + evolution.r() * 0.2f * mutability);
        int newOperation = operation;
        int newAxon1 = axon1;
        int newAxon2 = axon2;
        if (evolution.random(0, 1) < Globals.BIG_MUTATION_CHANCE * mutability) {
            newOperation = (int) evolution.random(0, Globals.OPERATION_COUNT);
        }
        if (evolution.random(0, 1) < Globals.BIG_MUTATION_CHANCE * mutability) {
            newAxon1 = (int) evolution.random(0, nodeNum);
        }
        if (evolution.random(0, 1) < Globals.BIG_MUTATION_CHANCE * mutability) {
            newAxon2 = (int) evolution.random(0, nodeNum);
        }

        if (newOperation == 1) { // time
            newV = 0;
        } else if (newOperation == 2) { // x - coordinate
            newV = newX * 0.2f;
        } else if (newOperation == 3) { // y - coordinate
            newV = -newY * 0.2f;
        }

        Node newNode = new Node(evolution, newX, newY, 0, 0, newM, PApplet.min(PApplet.max(f + evolution.r() * 0.1f * mutability, 0), 1), newV, newOperation, newAxon1, newAxon2);
        return newNode;//max(m+r()*0.1,0.2),min(max(f+r()*0.1,0),1)
    }
}
