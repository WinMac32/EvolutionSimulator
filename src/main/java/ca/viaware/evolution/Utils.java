package ca.viaware.evolution;

import processing.core.PGraphics;

import java.util.ArrayList;

public class Utils {
    public static float inter(int a, int b, float offset) {
        return (float)a + ((float)b - (float)a) * offset;
    }

    public static void adjustToCenter(ArrayList<Node> nodes, int nodeNum) {
        float avx = 0;
        float lowY = -1000;
        for (int i = 0; i < nodeNum; i++) {
            Node ni = nodes.get(i);
            avx += ni.x;
            if (ni.y + ni.m / 2 > lowY) {
                lowY = ni.y + ni.m / 2;
            }
        }
        avx /= nodeNum;
        for (int i = 0; i < nodeNum; i++) {
            Node ni = nodes.get(i);
            ni.x -= avx;
            ni.y -= lowY;
        }
    }

    public static void toStableConfiguration(ArrayList<Node> nodes, ArrayList<Muscle> muscles, int nodeNum, int muscleNum) {
        for (int j = 0; j < 200; j++) {
            for (int i = 0; i < muscleNum; i++) {
                muscles.get(i).applyForce(nodes);
            }
            for (int i = 0; i < nodeNum; i++) {
                nodes.get(i).applyForces();
            }
        }
        for (int i = 0; i < nodeNum; i++) {
            Node ni = nodes.get(i);
            ni.vx = 0;
            ni.vy = 0;
        }
    }

    public static PGraphics getImage(int image) {
        switch(image) {
            case 1: return Globals.screenImage;
            case 2: return Globals.popUpImage;
            default: return Globals.screenImage;
        }
    }
}
