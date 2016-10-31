package ca.viaware.evolution;

import processing.core.*;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Evolution extends PApplet {

    public static void main(String[] args) {
        PApplet.main(Evolution.class.getName());
    }

    public static PFont font;
    private final int AXON_COLOUR = color(255, 255, 0);

    private ExecutorService threadPool;

    float r() {
        return pow(random(-1, 1), 19);
    }

    int getNewMuscleAxon(int nodeNum) {
        if (random(0, 1) < 0.5) {
            return (int) random(0, nodeNum);
        } else {
            return -1;
        }
    }

    private void drawNode(Node ni, float x, float y, int toImage) {
        int c = color(512 -(int) (ni.f * 512), 0, 0);
        if (ni.f <= 0.5) {
            c = color(255, 255 -(int) (ni.f * 512), 255 -(int) (ni.f * 512));
        }
        float scale = Globals.SCALE_TO_FIX_BUG;
        if (toImage == 0) {
            fill(c);
            noStroke();
            ellipse((ni.x + x) * scale, (ni.y + y) * scale, ni.m * scale, ni.m * scale);
            if (ni.f >= 0.5) {
                fill(255);
            } else {
                fill(0);
            }
            textAlign(CENTER);
            textFont(font, 0.4f * ni.m * scale);
            text(nf(ni.value, 0, 2), (ni.x + x) * scale, (ni.y + ni.m * Globals.LINE_Y_2 + y) * scale);
            text(Globals.OPERATION_NAMES[ni.operation], (ni.x + x) * scale, (ni.y + ni.m * Globals.LINE_Y_1 + y) * scale);
        } else {
            PGraphics image = Utils.getImage(toImage);

            image.fill(c);
            image.noStroke();
            image.ellipse((ni.x + x) * scale, (ni.y + y) * scale, ni.m * scale, ni.m * scale);
            if (ni.f >= 0.5) {
                image.fill(255);
            } else {
                image.fill(0);
            }
            image.textAlign(CENTER);
            image.textFont(font, 0.4f * ni.m * scale);
            image.text(nf(ni.value, 0, 2), (ni.x + x) * scale, (ni.y + ni.m * Globals.LINE_Y_2 + y) * scale);
            image.text(Globals.OPERATION_NAMES[ni.operation], (ni.x + x) * scale, (ni.y + ni.m * Globals.LINE_Y_1 + y) * scale);
        }
    }

    private void drawNodeAxons(ArrayList<Node> nodes, Node node, float x, float y, int toImage) {
        if (Globals.OPERATION_AXONS[node.operation] >= 1) {
            Node axonSource = nodes.get(node.axon1);
            float point1x = node.x - node.m * 0.3f + x;
            float point1y = node.y - node.m * 0.3f + y;
            float point2x = axonSource.x + x;
            float point2y = axonSource.y + axonSource.m * 0.5f + y;
            drawSingleAxon(point1x, point1y, point2x, point2y, toImage);
        }
        if (Globals.OPERATION_AXONS[node.operation] == 2) {
            Node axonSource = nodes.get(node.axon2);
            float point1x = node.x + node.m * 0.3f + x;
            float point1y = node.y - node.m * 0.3f + y;
            float point2x = axonSource.x + x;
            float point2y = axonSource.y + axonSource.m * 0.5f + y;
            drawSingleAxon(point1x, point1y, point2x, point2y, toImage);
        }
    }

    private void drawSingleAxon(float x1, float y1, float x2, float y2, int toImage) {
        float arrowHeadSize = 0.1f;
        float angle = atan2(y2 - y1, x2 - x1);
        float scale = Globals.SCALE_TO_FIX_BUG;
        if (toImage == 0) {
            stroke(AXON_COLOUR);
            strokeWeight(0.03f * scale);
            line(x1 * scale, y1 * scale, x2 * scale, y2 * scale);
            line(x1 * scale, y1 * scale, (x1 + cos(angle + PI * 0.25f) * arrowHeadSize) * scale, (y1 + sin(angle + PI * 0.25f) * arrowHeadSize) * scale);
            line(x1 * scale, y1 * scale, (x1 + cos(angle + PI * 1.75f) * arrowHeadSize) * scale, (y1 + sin(angle + PI * 1.75f) * arrowHeadSize) * scale);
            noStroke();
        } else {
            PGraphics popUp = Globals.popUpImage;
            PGraphics image = Utils.getImage(toImage);

            image.stroke(AXON_COLOUR);
            image.strokeWeight(0.03f * scale);
            image.line(x1 * scale, y1 * scale, x2 * scale, y2 * scale);
            image.line(x1 * scale, y1 * scale, (x1 + cos(angle + PI * 0.25f) * arrowHeadSize) * scale, (y1 + sin(angle + PI * 0.25f) * arrowHeadSize) * scale);
            image.line(x1 * scale, y1 * scale, (x1 + cos(angle + PI * 1.75f) * arrowHeadSize) * scale, (y1 + sin(angle + PI * 1.75f) * arrowHeadSize) * scale);
            popUp.noStroke();
        }
    }

    private void drawMuscle(Muscle mi, ArrayList<Node> n, float x, float y, int toImage) {
        Node ni1 = n.get(mi.c1);
        Node ni2 = n.get(mi.c2);
        float w = 0.15f;
        if (mi.axon >= 0 && mi.axon < n.size()) {
            w = toMuscleUsable(n.get(mi.axon).value) * 0.15f;
        }
        float scale = Globals.SCALE_TO_FIX_BUG;
        if (toImage == 0) {
            strokeWeight(w * scale);
            stroke(70, 35, 0, mi.rigidity * 3000);
            line((ni1.x + x) * scale, (ni1.y + y) * scale, (ni2.x + x) * scale, (ni2.y + y) * scale);
        } else {
            PGraphics image = Utils.getImage(toImage);
            image.strokeWeight(w * scale);
            image.stroke(70, 35, 0, mi.rigidity * 3000);
            image.line((ni1.x + x) * scale, (ni1.y + y) * scale, (ni2.x + x) * scale, (ni2.y + y) * scale);
        }
    }

    private void drawMuscleAxons(Muscle mi, ArrayList<Node> n, float x, float y, int toImage) {
        Node ni1 = n.get(mi.c1);
        Node ni2 = n.get(mi.c2);
        if (mi.axon >= 0 && mi.axon < n.size()) {
            Node axonSource = n.get(mi.axon);
            float muscleMidX = (ni1.x + ni2.x) * 0.5f + x;
            float muscleMidY = (ni1.y + ni2.y) * 0.5f + y;
            drawSingleAxon(muscleMidX, muscleMidY, axonSource.x + x, axonSource.y + axonSource.m * 0.5f + y, toImage);
            float averageMass = (ni1.m + ni2.m) * 0.5f;
            float scale = Globals.SCALE_TO_FIX_BUG;
            if (toImage == 0) {
                fill(AXON_COLOUR);
                textAlign(CENTER);
                textFont(font, 0.4f * averageMass * scale);
                text(nf(toMuscleUsable(n.get(mi.axon).value), 0, 2), muscleMidX * scale, muscleMidY * scale);
            } else {
                PGraphics image = Utils.getImage(toImage);
                image.fill(AXON_COLOUR);
                image.textAlign(CENTER);
                image.textFont(font, 0.4f * averageMass * scale);
                image.text(nf(toMuscleUsable(n.get(mi.axon).value), 0, 2), muscleMidX * scale, muscleMidY * scale);
            }
        }
    }

    float toMuscleUsable(float f) {
        return min(max(f, 0.5f), 1.5f);
    }

    public void drawArrow(float x) {
        textAlign(CENTER);
        float scale = Globals.SCALE_TO_FIX_BUG;
        textFont(font, Globals.POST_FONT_SIZE * scale);
        noStroke();
        fill(120, 0, 255);
        rect((x - 1.7f) * scale, -4.8f * scale, 3.4f * scale, 1.1f * scale);
        beginShape();
        vertex(x * scale, -3.2f * scale);
        vertex((x - 0.5f) * scale, -3.7f * scale);
        vertex((x + 0.5f) * scale, -3.7f * scale);
        endShape(CLOSE);
        fill(255);
        text(((float)round(x * 2) / 10f)+" m", x * scale, -3.91f * scale);
    }

    private void drawGraphImage() {
        image(Globals.graphImage, 50, 180, 650, 380);
        image(Globals.segBarImage, 50, 580, 650, 100);
        if (Globals.gen >= 1) {
            stroke(0, 160, 0, 255);
            strokeWeight(3);
            float genWidth = 590.0f / Globals.gen;
            float lineX = 110 + Globals.genSelected * genWidth;
            line(lineX, 180, lineX, 500 + 180);
            Integer[] s = Globals.SPECIES_COUNTS.get(Globals.genSelected);
            textAlign(LEFT);
            textFont(font, 12);
            noStroke();
            for (int i = 1; i < 101; i++) {
                int c = s[i] - s[i - 1];
                if (c >= 25) {
                    float y = ((s[i] + s[i - 1]) / 2) / 1000.0f * 100 + 573;
                    if (i - 1 == Globals.TOP_SPECIES_COUNTS.get(Globals.genSelected)) {
                        stroke(0);
                        strokeWeight(2);
                    } else {
                        noStroke();
                    }
                    fill(255, 255, 255);
                    rect(lineX + 3, y, 56, 14);
                    colorMode(HSB, 1.0f);
                    fill(getColor(i - 1, true));
                    text("S" + floor((i - 1) / 10) + "" + ((i - 1) % 10) + ": " + c, lineX + 5, y + 11);
                    colorMode(RGB, 255);
                }
            }
            noStroke();
        }
    }

    private int getColor(int i, boolean adjust) {
        colorMode(HSB, 1.0f);
        float col = (i * 1.618034f) % 1;
        if (i == 46) {
            col = 0.083333f;
        }
        float light = 1.0f;
        if (abs(col - 0.333f) <= 0.18 && adjust) {
            light = 0.7f;
        }
        return color(col, 1.0f, light);
    }

    private void drawGraph(int graphWidth, int graphHeight) {
        Globals.graphImage.beginDraw();
        Globals.graphImage.smooth();
        Globals.graphImage.background(220);
        if (Globals.gen >= 1) {
            drawLines(90, (int)(graphHeight * 0.05), graphWidth - 90, (int)(graphHeight * 0.9));
            drawSegBars(90, 0, graphWidth - 90, 150);
        }
        Globals.graphImage.endDraw();
    }

    private void drawLines(int x, int y, int graphWidth, int graphHeight) {
        float gh = (float)graphHeight;
        float genWidth = (float)graphWidth / Globals.gen;
        float best = extreme(1);
        float worst = extreme(-1);
        float meterHeight = (float)graphHeight / (best - worst);
        float zero = (best / (best - worst)) * gh;
        float unit = setUnit(best, worst);
        Globals.graphImage.stroke(150);
        Globals.graphImage.strokeWeight(2);
        Globals.graphImage.fill(150);
        Globals.graphImage.textFont(font, 18);
        Globals.graphImage.textAlign(RIGHT);
        for (float i = ceil((worst - (best - worst) / 18.0f) / unit) * unit; i < best + (best - worst) / 18.0; i += unit) {
            float lineY = y - i * meterHeight + zero;
            Globals.graphImage.line(x, lineY, graphWidth + x, lineY);
            Globals.graphImage.text(showUnit(i, unit) + " " + Globals.FITNESS_UNIT, x - 5, lineY + 4);
        }
        Globals.graphImage.stroke(0);
        for (int i = 0; i < 29; i++) {
            int k;
            if (i == 28) {
                k = 14;
            } else if (i < 14) {
                k = i;
            } else {
                k = i + 1;
            }
            if (k == 14) {
                Globals.graphImage.stroke(255, 0, 0, 255);
                Globals.graphImage.strokeWeight(5);
            } else {
                stroke(0);
                if (k == 0 || k == 28 || (k >= 10 && k <= 18)) {
                    Globals.graphImage.strokeWeight(3);
                } else {
                    Globals.graphImage.strokeWeight(1);
                }
            }
            for (int j = 0; j < Globals.gen; j++) {
                Globals.graphImage.line(x + j * genWidth, (-Globals.PERCENTILE.get(j)[k]) * meterHeight + zero + y,
                        x + (j + 1) * genWidth, (-Globals.PERCENTILE.get(j + 1)[k]) * meterHeight + zero + y);
            }
        }
    }

    private void drawSegBars(int x, int y, int graphWidth, int graphHeight) {
        PGraphics segBar = Globals.segBarImage;
        segBar.beginDraw();
        segBar.smooth();
        segBar.noStroke();
        segBar.colorMode(HSB, 1);
        segBar.background(0, 0, 0.5f);
        float genWidth = (float)graphWidth / Globals.gen;
        int gensPerBar = floor(Globals.gen / 500) + 1;
        for (int i = 0; i < Globals.gen; i += gensPerBar) {
            int i2 = min(i + gensPerBar, Globals.gen);
            float barX1 = x + i * genWidth;
            float barX2 = x + i2 * genWidth;
            for (int j = 0; j < 100; j++) {
                segBar.fill(getColor(j, false));
                segBar.beginShape();
                segBar.vertex(barX1, y + Globals.SPECIES_COUNTS.get(i)[j] / 1000.0f * graphHeight);
                segBar.vertex(barX1, y + Globals.SPECIES_COUNTS.get(i)[j + 1] / 1000.0f * graphHeight);
                segBar.vertex(barX2, y + Globals.SPECIES_COUNTS.get(i2)[j + 1] / 1000.0f * graphHeight);
                segBar.vertex(barX2, y + Globals.SPECIES_COUNTS.get(i2)[j] / 1000.0f * graphHeight);
                segBar.endShape();
            }
        }
        segBar.endDraw();
        colorMode(RGB, 255);
    }

    private float extreme(float sign) {
        float record = -sign;
        for (int i = 0; i < Globals.gen; i++) {
            float toTest = Globals.PERCENTILE.get(i + 1)[(int)(14 - sign * 14)];
            if (toTest * sign > record * sign) {
                record = toTest;
            }
        }
        return record;
    }

    private float setUnit(float best, float worst) {
        float unit2 = 3 * log(best - worst) / log(10) - 2;
        if ((unit2 + 90) % 3 < 1) {
            return pow(10, floor(unit2 / 3));
        } else if ((unit2 + 90) % 3 < 2) {
            return pow(10, floor((unit2 - 1) / 3)) * 2;
        } else {
            return pow(10, floor((unit2 - 2) / 3)) * 5;
        }
    }

    private String showUnit(float i, float unit) {
        if (unit < 1) {
            return nf(i, 0, 2) + "";
        } else {
            return (int)i + "";
        }
    }

    private Creature[] creatures = new Creature[1000];
    private ArrayList<Creature> creaturesList = new ArrayList<Creature>();

    public void mouseWheel(MouseEvent event) {
        float delta = event.getCount();
        if (Globals.menu == 5) {
            if (delta == -1) {
                Globals.camZoom *= 0.9090909;
                if (Globals.camZoom < 0.002) {
                    Globals.camZoom = 0.002f;
                }
                textFont(font, Globals.POST_FONT_SIZE);
            } else if (delta == 1) {
                Globals.camZoom *= 1.1;
                if (Globals.camZoom > 0.1) {
                    Globals.camZoom = 0.1f;
                }
                textFont(font, Globals.POST_FONT_SIZE);
            }
        }
    }

    public void mousePressed() {
        if (Globals.gensToDo >= 1) {
            Globals.gensToDo = 0;
        }
        float mX = mouseX / Globals.WINDOW_SIZE_MULTIPLIER;
        float mY = mouseY / Globals.WINDOW_SIZE_MULTIPLIER;
        if (Globals.menu == 1 && Globals.gen >= 1 && abs(mY - 365) <= 25 && abs(mX - Globals.sliderX - 25) <= 25) {
            Globals.drag = true;
        }
    }

    private void openMiniSimulation() {
        Globals.simulator.setSimulationTimer(0);
        if (Globals.gensToDo == 0) {
            Globals.miniSimulation = true;
            int id;
            Creature cj;
            if (Globals.statusWindow <= -1) {
                cj = Globals.CREATURE_DATABASE.get((Globals.genSelected - 1) * 3 + Globals.statusWindow + 3);
                id = cj.getId();
            } else {
                id = Globals.statusWindow;
                cj = creaturesList.get(id);
            }
            setGlobalVariables(cj);
            Globals.creatureWatching = id;
        }
    }

    public void setMenu(int m) {
        Globals.menu = m;
        if (m == 1) {
            drawGraph(975, 570);
        }
    }

    private String zeros(int n, int zeros) {
        String s = n + "";
        for (int i = s.length(); i < zeros; i++) {
            s = "0" + s;
        }
        return s;
    }

    private void startASAP() {
        setMenu(4);
        Globals.creaturesTested = 0;
        Globals.stepByStep = false;
        Globals.stepByStepSlow = false;
    }

    public void mouseReleased() {
        Globals.drag = false;
        Globals.miniSimulation = false;
        float mX = mouseX / Globals.WINDOW_SIZE_MULTIPLIER;
        float mY = mouseY / Globals.WINDOW_SIZE_MULTIPLIER;
        if (Globals.menu == 0 && abs(mX - Globals.WINDOW_WIDTH / 2) <= 200 && abs(mY - 400) <= 100) {
            setMenu(1);
        } else if (Globals.menu == 1 && Globals.gen == -1 && abs(mX - 120) <= 100 && abs(mY - 300) <= 50) {
            setMenu(2);
        } else if (Globals.menu == 1 && Globals.gen >= 0 && abs(mX - 990) <= 230) {
            if (abs(mY - 40) <= 20) {
                setMenu(4);
                Globals.creaturesTested = 0;
                Globals.stepByStep = true;
                Globals.stepByStepSlow = true;
            }
            if (abs(mY - 90) <= 20) {
                setMenu(4);
                Globals.creaturesTested = 0;
                Globals.stepByStep = true;
                Globals.stepByStepSlow = false;
            }
            if (abs(mY - 140) <= 20) {
                if (mX < 990) {
                    Globals.gensToDo = 1;
                } else {
                    Globals.gensToDo = 1000000000;
                }
                startASAP();
            }
        } else if (Globals.menu == 3 && abs(mX - 1030) <= 130 && abs(mY - 684) <= 20) {
            Globals.gen = 0;
            setMenu(1);
        } else if (Globals.menu == 7 && abs(mX - 1030) <= 130 && abs(mY - 684) <= 20) {
            setMenu(8);
        } else if ((Globals.menu == 5 || Globals.menu == 4) && mY >= Globals.WINDOW_HEIGHT - 40) {
            if (mX < 90) {
                for (int s = Globals.timer; s < 900; s++) {
                    Globals.simulator.run();
                }
                Globals.timer = 1021;
            } else if (mX >= 120 && mX < 360) {
                Globals.simulator.setSpeed(Globals.simulator.getSpeed() * 2);
                if (Globals.simulator.getSpeed() == 1024) Globals.simulator.setSpeed(900);
                if (Globals.simulator.getSpeed() >= 1800) Globals.simulator.setSpeed(1);
            } else if (mX >= Globals.WINDOW_WIDTH - 120) {
                for (int s = Globals.timer; s < 900; s++) {
                    Globals.simulator.run();
                }
                Globals.timer = 0;
                Globals.creaturesTested++;
                for (int i = Globals.creaturesTested; i < 1000; i++) {
                    setGlobalVariables(creatures[i]);
                    for (int s = 0; s < 900; s++) {
                        Globals.simulator.run();
                    }
                    Globals.simulator.setAverages();
                    Globals.simulator.setFitness();
                }
                setMenu(6);
            }
        } else if (Globals.menu == 8 && mX < 90 && mY >= Globals.WINDOW_HEIGHT - 40) {
            Globals.timer = 100000;
        } else if (Globals.menu == 9 && abs(mX - 1030) <= 130 && abs(mY - 690) <= 20) {
            setMenu(10);
        } else if (Globals.menu == 11 && abs(mX - 1130) <= 80 && abs(mY - 690) <= 20) {
            setMenu(12);
        } else if (Globals.menu == 13 && abs(mX - 1130) <= 80 && abs(mY - 690) <= 20) {
            setMenu(1);
        }
    }

    private void drawScreenImage(int stage) {
        PGraphics screen = Globals.screenImage;
        screen.beginDraw();
        screen.pushMatrix();
        screen.scale(15.0f / Globals.SCALE_TO_FIX_BUG);
        screen.smooth();
        screen.background(220, 253, 102);
        screen.noStroke();
        for (int j = 0; j < 1000; j++) {
            Creature cj = creaturesList.get(j);
            if (stage == 3) cj = creatures[cj.getId() - (Globals.gen * 1000) - 1001];
            int j2 = j;
            if (stage == 0) {
                j2 = cj.getId() - (Globals.gen * 1000) - 1;
                Globals.CREATURES_IN_POSITION[j2] = j;
            }
            int x = j2 % 40;
            int y = floor(j2 / 40);
            if (stage >= 1) y++;
            drawCreature(cj, x * 3 + 5.5f, y * 2.5f + 4, 1);
        }
        Globals.timer = 0;
        screen.popMatrix();
        screen.pushMatrix();
        screen.scale(1.5f);

        screen.textAlign(CENTER);
        screen.textFont(font, 24);
        screen.fill(100, 100, 200);
        screen.noStroke();

        int windowWidth = Globals.WINDOW_WIDTH;
        if (stage == 0) {
            screen.rect(900, 664, 260, 40);
            screen.fill(0);
            screen.text("All 1,000 creatures have been tested.  Now let's sort them!", windowWidth / 2 - 200, 690);
            screen.text("Sort", windowWidth - 250, 690);
        } else if (stage == 1) {
            screen.rect(900, 670, 260, 40);
            screen.fill(0);
            screen.text("Fastest creatures at the top!", windowWidth / 2, 30);
            screen.text("Slowest creatures at the bottom. (Going backward = slow)", windowWidth / 2 - 200, 700);
            screen.text("Kill 500", windowWidth - 250, 700);
        } else if (stage == 2) {
            screen.rect(1050, 670, 160, 40);
            screen.fill(0);
            screen.text("Faster creatures are more likely to survive because they can outrun their predators.  Slow creatures get eaten.", windowWidth / 2, 30);
            screen.text("Because of random chance, a few fast ones get eaten, while a few slow ones survive.", windowWidth / 2 - 130, 700);
            screen.text("Reproduce", windowWidth - 150, 700);
            for (int j = 0; j < 1000; j++) {
                Creature cj = creaturesList.get(j);
                int x = j % 40;
                int y = floor(j / 40) + 1;
                if (cj.isAlive()) {
                    drawCreature(cj, x * 30 + 55, y * 25 + 40, 0);
                } else {
                    screen.rect(x * 30 + 40, y * 25 + 17, 30, 25);
                }
            }
        } else if (stage == 3) {
            screen.rect(1050, 670, 160, 40);
            screen.fill(0);
            screen.text("These are the 1000 creatures of generation #" + (Globals.gen + 2) + ".", windowWidth / 2, 30);
            screen.text("What perils will they face?  Find out next time!", windowWidth / 2 - 130, 700);
            screen.text("Back", windowWidth - 150, 700);
        }
        screen.endDraw();
        screen.popMatrix();
    }

    private void drawCreature(Creature creature, float x, float y, int toImage) {
        for (Muscle muscle : creature.getMuscles()) {
            drawMuscle(muscle, creature.getNodes(), x, y, toImage);
        }
        for (Node node : creature.getNodes()) {
            drawNode(node, x, y, toImage);
        }
        for (Muscle muscle : creature.getMuscles()) {
            drawMuscleAxons(muscle, creature.getNodes(), x, y, toImage);
        }
        for (Node node : creature.getNodes()) {
            drawNodeAxons(creature.getNodes(), node, x, y, toImage);
        }
    }

    public void drawCreaturePieces(ArrayList<Node> nodes, ArrayList<Muscle> muscles, float x, float y, int toImage) {
        for (Muscle muscle : muscles) {
            drawMuscle(muscle, nodes, x, y, toImage);
        }
        for (Node node : nodes) {
            drawNode(node, x, y, toImage);
        }
        for (Muscle muscle : muscles) {
            drawMuscleAxons(muscle, nodes, x, y, toImage);
        }
        for (Node node : nodes) {
            drawNodeAxons(nodes, node, x, y, toImage);
        }
    }

    private void drawHistogram(int x, int y, int hw, int hh) {
        int maxH = 1;
        for (int i = 0; i < Globals.BAR_LEN; i++) {
            if (Globals.BAR_COUNTS.get(Globals.genSelected)[i] > maxH) {
                maxH = Globals.BAR_COUNTS.get(Globals.genSelected)[i];
            }
        }
        fill(200);
        noStroke();
        rect(x, y, hw, hh);
        fill(0, 0, 0);
        float barW = (float) hw / Globals.BAR_LEN;
        float multiplier = (float) hh / maxH * 0.9f;
        textAlign(LEFT);
        textFont(font, 16);
        stroke(128);
        strokeWeight(2);
        int unit = 100;
        if (maxH < 300) unit = 50;
        if (maxH < 100) unit = 20;
        if (maxH < 50) unit = 10;
        for (int i = 0; i < hh / multiplier; i += unit) {
            float theY = y + hh - i * multiplier;
            line(x, theY, x + hw, theY);
            if (i == 0) theY -= 5;
            text(i, x + hw + 5, theY + 7);
        }
        textAlign(CENTER);
        for (int i = Globals.MIN_BAR; i <= Globals.MAX_BAR; i += 10) {
            if (i == 0) {
                stroke(0, 0, 255);
            } else {
                stroke(128);
            }
            float theX = x + (i - Globals.MIN_BAR) * barW;
            text(nf((float) i / Globals.HIST_BARS_PER_METER, 0, 1), theX, y + hh + 14);
            line(theX, y, theX, y + hh);
        }
        noStroke();
        for (int i = 0; i < Globals.BAR_LEN; i++) {
            float h = min(Globals.BAR_COUNTS.get(Globals.genSelected)[i] * multiplier, hh);
            if (i + Globals.MIN_BAR == floor(Globals.PERCENTILE.get(min(Globals.genSelected, Globals.PERCENTILE.size() - 1))[14] * Globals.HIST_BARS_PER_METER)) {
                fill(255, 0, 0);
            } else {
                fill(0, 0, 0);
            }
            rect(x + i * barW, y + hh - h, barW, h);
        }
    }

    private void drawStatusWindow(boolean isFirstFrame) {
        int x, y, px, py;
        int rank = (Globals.statusWindow + 1);
        Creature cj;
        stroke(abs(Globals.overallTimer % 30 - 15) * 17);
        strokeWeight(3);
        noFill();
        if (Globals.statusWindow >= 0) {
            cj = creaturesList.get(Globals.statusWindow);
            if (Globals.menu == 7) {
                int id = ((cj.getId() - 1) % 1000);
                x = id % 40;
                y = floor(id / 40);
            } else {
                x = Globals.statusWindow % 40;
                y = floor(Globals.statusWindow / 40) + 1;
            }
            px = x * 30 + 55;
            py = y * 25 + 10;
            if (px <= 1140) {
                px += 80;
            } else {
                px -= 80;
            }
            rect(x * 30 + 40, y * 25 + 17, 30, 25);
        } else {
            cj = Globals.CREATURE_DATABASE.get((Globals.genSelected - 1) * 3 + Globals.statusWindow + 3);
            x = 760 + (Globals.statusWindow + 3) * 160;
            y = 180;
            px = x;
            py = y;
            rect(x, y, 140, 140);
            int[] ranks = {
                    1000, 500, 1
            };
            rank = ranks[Globals.statusWindow + 3];
        }
        noStroke();
        fill(255);
        rect(px - 60, py, 120, 52);
        fill(0);
        textFont(font, 12);
        textAlign(CENTER);
        text("#" + rank, px, py + 12);
        text("ID: " + cj.getId(), px, py + 24);
        text("Fitness: " + nf(cj.getDistance(), 0, 3), px, py + 36);
        colorMode(HSB, 1);
        int sp = (cj.getNodes().size() % 10) * 10 + (cj.getMuscles().size() % 10);
        fill(getColor(sp, true));
        text("Species: S" + (cj.getNodes().size() % 10) + "" + (cj.getMuscles().size() % 10), px, py + 48);
        colorMode(RGB, 255);
        if (Globals.miniSimulation) {
            int py2 = py - 125;
            if (py >= 360) {
                py2 -= 180;
            } else {
                py2 += 180;
            }
            int px2 = min(max(px - 90, 10), 970);
            Globals.simulator.renderPopUpImage(this);
            image(Globals.popUpImage, px2, py2, 300, 300);

            Globals.simulator.renderStats(this, px2 + 295, py2, 0.45f);

            Globals.simulator.run();
            int shouldBeWatching = Globals.statusWindow;
            if (Globals.statusWindow <= -1) {
                cj = Globals.CREATURE_DATABASE.get((Globals.genSelected - 1) * 3 + Globals.statusWindow + 3);
                shouldBeWatching = cj.getId();
            }
            if (Globals.creatureWatching != shouldBeWatching || isFirstFrame) {
                openMiniSimulation();
            }
        }
    }

    public void setup() {
        frameRate(60);
        randomSeed(Globals.SEED);
        noSmooth();
        size((int) (Globals.WINDOW_WIDTH * Globals.WINDOW_SIZE_MULTIPLIER), (int) (Globals.WINDOW_HEIGHT * Globals.WINDOW_SIZE_MULTIPLIER));
        ellipseMode(CENTER);
        Float[] beginPercentile = new Float[29];
        Integer[] beginBar = new Integer[Globals.BAR_LEN];
        Integer[] beginSpecies = new Integer[101];
        for (int i = 0; i < 29; i++) {
            beginPercentile[i] = 0.0f;
        }
        for (int i = 0; i < Globals.BAR_LEN; i++) {
            beginBar[i] = 0;
        }
        for (int i = 0; i < 101; i++) {
            beginSpecies[i] = 500;
        }

        Globals.PERCENTILE.add(beginPercentile);
        Globals.BAR_COUNTS.add(beginBar);
        Globals.SPECIES_COUNTS.add(beginSpecies);
        Globals.TOP_SPECIES_COUNTS.add(0);

        Globals.graphImage = createGraphics(975, 570);
        Globals.screenImage = createGraphics(1920, 1080);
        Globals.popUpImage = createGraphics(450, 450);
        Globals.segBarImage = createGraphics(975, 150);

        Globals.segBarImage.beginDraw();
        Globals.segBarImage.smooth();
        Globals.segBarImage.background(220);
        Globals.segBarImage.endDraw();

        Globals.popUpImage.beginDraw();
        Globals.popUpImage.smooth();
        Globals.popUpImage.background(220);
        Globals.popUpImage.endDraw();

        font = loadFont("Helvetica-Bold-96.vlw");
        textFont(font, 96);
        textAlign(CENTER);

        threadPool = newFixedThreadPool(Globals.THREAD_COUNT);
    }

    public void draw() {
        scale(Globals.WINDOW_SIZE_MULTIPLIER);
        if (Globals.menu == 0) {
            background(255);
            fill(100, 200, 100);
            noStroke();
            rect(Globals.WINDOW_WIDTH / 2 - 200, 300, 400, 200);
            fill(0);
            text("EVOLUTION!", Globals.WINDOW_WIDTH / 2, 200);
            text("START", Globals.WINDOW_WIDTH / 2, 430);
        } else if (Globals.menu == 1) {
            noStroke();
            fill(0);
            background(255, 200, 130);
            textFont(font, 32);
            textAlign(LEFT);
            textFont(font, 96);
            text("Generation " + max(Globals.genSelected, 0), 20, 100);
            textFont(font, 28);
            if (Globals.gen == -1) {
                fill(100, 200, 100);
                rect(20, 250, 200, 100);
                fill(0);
                text("Since there are no creatures yet, create 1000 creatures!", 20, 160);
                text("They will be randomly created, and also very simple.", 20, 200);
                text("CREATE", 56, 312);
            } else {
                fill(100, 200, 100);
                rect(760, 20, 460, 40);
                rect(760, 70, 460, 40);
                rect(760, 120, 230, 40);
                if (Globals.gensToDo >= 2) {
                    fill(128, 255, 128);
                } else {
                    fill(70, 140, 70);
                }
                rect(990, 120, 230, 40);
                fill(0);
                text("Do 1 step-by-step generation.", 770, 50);
                text("Do 1 quick generation.", 770, 100);
                text("Do 1 gen ASAP.", 770, 150);
                text("Do gens ALAP.", 1000, 150);
                text("Median " + Globals.FITNESS_NAME, 50, 160);
                textAlign(CENTER);
                textAlign(RIGHT);
                text((float) (round(Globals.PERCENTILE.get(min(Globals.genSelected, Globals.PERCENTILE.size() - 1))[14] * 1000)) / 1000 + " " + Globals.FITNESS_UNIT, 700, 160)
                ;
                drawHistogram(760, 410, 460, 280);
                drawGraphImage();
                if (Globals.SAVE_FRAMES_PER_GENERATION && Globals.gen > Globals.lastImageSaved) {
                    saveFrame("imgs//" + zeros(Globals.gen, 5) + ".png");
                    Globals.lastImageSaved = Globals.gen;
                }
            }
            if (Globals.gensToDo >= 1) {
                Globals.gensToDo--;
                if (Globals.gensToDo >= 1) {
                    startASAP();
                }
            }
        } else if (Globals.menu == 2) {
            Globals.creatures = 0;
            background(220, 253, 102);
            pushMatrix();
            scale(10.0f / Globals.SCALE_TO_FIX_BUG);
            for (int y = 0; y < 25; y++) {
                for (int x = 0; x < 40; x++) {
                    float heartbeat = random(40, 80);
                    Creature creature = new Creature(this, Globals.BASELINE_ENERGY, y * 40 + x + 1, new ArrayList<Node>(), new ArrayList<Muscle>(), 0, true, heartbeat, 1.0f);

                    int nodeNum = (int) random(3, 6);
                    int muscleNum = (int) random(nodeNum - 1, nodeNum * 3 - 6);
                    for (int i = 0; i < nodeNum; i++) {
                        creature.getNodes().add(new Node(this, random(-1, 1), random(-1, 1), 0, 0, 0.4f, random(0, 1), random(0, 1),
                                floor(random(0, Globals.OPERATION_COUNT)), floor(random(0, nodeNum)), floor(random(0, nodeNum)))); //replaced all nodes' sizes with 0.4, used to be random(0.1,1), random(0,1)
                    }
                    for (int i = 0; i < muscleNum; i++) {
                        int tc1 = 0;
                        int tc2 = 0;
                        int taxon = getNewMuscleAxon(nodeNum);
                        if (i < nodeNum - 1) {
                            tc1 = i;
                            tc2 = i + 1;
                        } else {
                            tc1 = (int) random(0, nodeNum);
                            tc2 = tc1;
                            while (tc2 == tc1) {
                                tc2 = (int) random(0, nodeNum);
                            }
                        }
                        float s = 0.8f;
                        if (i >= 10) {
                            s *= 1.414;
                        }
                        float len = random(0.5f, 1.5f);
                        creature.getMuscles().add(new Muscle(this, creature, taxon, tc1, tc2, len, random(0.02f, 0.08f)));
                    }

                    Utils.toStableConfiguration(creature.getNodes(), creature.getMuscles(), nodeNum, muscleNum);
                    Utils.adjustToCenter(creature.getNodes(), nodeNum);
                    creatures[y * 40 + x] = creature;
                    drawCreature(creatures[y * 40 + x], x * 3 + 5.5f, y * 2.5f + 3, 0);
                    creatures[y * 40 + x].checkForOverlap();
                    creatures[y * 40 + x].checkForLoneNodes();
                    creatures[y * 40 + x].checkForBadAxons();
                }
            }
            setMenu(3);
            popMatrix();
            noStroke();
            fill(100, 100, 200);
            rect(900, 664, 260, 40);
            fill(0);
            textAlign(CENTER);
            textFont(font, 24);
            text("Here are your 1000 randomly generated creatures!!!", Globals.WINDOW_WIDTH / 2 - 200, 690);
            text("Back", Globals.WINDOW_WIDTH - 250, 690);
        } else if (Globals.menu == 4) {
            setGlobalVariables(creatures[Globals.creaturesTested]);
            Globals.camZoom = 0.01f;
            setMenu(5);
            if (!Globals.stepByStepSlow) {
                ArrayList<Callable<Object>> simulations = new ArrayList<Callable<Object>>(1000);
                for (int i = 0; i < 1000; i++) {
                    //setGlobalVariables(creatures[i]);
                    creatures[i].setEnergy(Globals.BASELINE_ENERGY);
                    simulations.add(new SimulationRunner(new Simulator(creatures[i], 900, false)));
                }
                try {
                    threadPool.invokeAll(simulations);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setMenu(6);
            }
        }
        if (Globals.menu == 5) { //simulate running
            Globals.simulator.render(this);
        }
        if (Globals.menu == 6) {
            //sort
            creaturesList = new ArrayList<Creature>(0);
            creaturesList.addAll(Arrays.asList(creatures).subList(0, 1000));

            Collections.sort(creaturesList, new Comparator<Creature>() {
                public int compare(Creature o1, Creature o2) {
                    float d1 = o1.getDistance();
                    float d2 = o2.getDistance();
                    if (d1 < d2) return 1;
                    else if (d1 > d2) return -1;
                    else return 0;
                }
            });
            Globals.PERCENTILE.add(new Float[29]);
            for (int i = 0; i < 29; i++) {
                Globals.PERCENTILE.get(Globals.gen + 1)[i] = creaturesList.get(Globals.PERCENTILE_POSITIONS[i]).getDistance();
            }
            Globals.CREATURE_DATABASE.add(creaturesList.get(999).copyCreature(-1));
            Globals.CREATURE_DATABASE.add(creaturesList.get(499).copyCreature(-1));
            Globals.CREATURE_DATABASE.add(creaturesList.get(0).copyCreature(-1));

            Integer[] beginBar = new Integer[Globals.BAR_LEN];
            for (int i = 0; i < Globals.BAR_LEN; i++) {
                beginBar[i] = 0;
            }
            Globals.BAR_COUNTS.add(beginBar);
            Integer[] beginSpecies = new Integer[101];
            for (int i = 0; i < 101; i++) {
                beginSpecies[i] = 0;
            }
            for (int i = 0; i < 1000; i++) {
                int bar = floor(creaturesList.get(i).getDistance() * Globals.HIST_BARS_PER_METER - Globals.MIN_BAR);
                if (bar >= 0 && bar < Globals.BAR_LEN) {
                    Globals.BAR_COUNTS.get(Globals.gen + 1)[bar]++;
                }
                int species = (creaturesList.get(i).getNodes().size() % 10) * 10 + creaturesList.get(i).getMuscles().size() % 10;
                beginSpecies[species]++;
            }
            Globals.SPECIES_COUNTS.add(new Integer[101]);
            Globals.SPECIES_COUNTS.get(Globals.gen + 1)[0] = 0;
            int cum = 0;
            int record = 0;
            int holder = 0;
            for (int i = 0; i < 100; i++) {
                cum += beginSpecies[i];
                Globals.SPECIES_COUNTS.get(Globals.gen + 1)[i + 1] = cum;
                if (beginSpecies[i] > record) {
                    record = beginSpecies[i];
                    holder = i;
                }
            }
            Globals.TOP_SPECIES_COUNTS.add(holder);
            if (Globals.stepByStep) {
                drawScreenImage(0);
                setMenu(7);
            } else {
                setMenu(10);
            }
        }
        if (Globals.menu == 8) {
            //cool sorting animation
            /*
            background(220, 253, 102);
            pushMatrix();
            scale(10.0f / Globals.SCALE_TO_FIX_BUG);
            float transition = 0.5f - 0.5f * cos(min((float)(Globals.timer) / 60f, PI));
            for (int j = 0; j < 1000; j++) {
                Creature cj = creaturesList.get(j);
                int j2 = cj.getId() - (Globals.gen * 1000) - 1;
                int x1 = j2 % 40;
                int y1 = floor(j2 / 40);
                int x2 = j % 40;
                int y2 = floor(j / 40) + 1;
                float x3 = Utils.inter(x1, x2, transition);
                float y3 = Utils.inter(y1, y2, transition);
                drawCreature(cj, x3 * 3 + 5.5f, y3 * 2.5f + 4, 0);
            }
            popMatrix();
            if (Globals.stepByStepSlow) {
                Globals.timer += 2;
            } else {
                Globals.timer += 10;
            }
            drawSkipButton();
            if (Globals.timer > 60 * PI) {
                drawScreenImage(1);
                setMenu(9);
            }
            */
            drawScreenImage(1);
            setMenu(9);
        }
        float mX = mouseX / Globals.WINDOW_SIZE_MULTIPLIER;
        float mY = mouseY / Globals.WINDOW_SIZE_MULTIPLIER;
        Globals.prevStatusWindow = Globals.statusWindow;
        if (abs(Globals.menu - 9) <= 2 && Globals.gensToDo == 0 && !Globals.drag) {
            if (abs(mX - 639.5f) <= 599.5) {
                if (Globals.menu == 7 && abs(mY - 329) <= 312) {
                    Globals.statusWindow = Globals.CREATURES_IN_POSITION[floor((mX - 40) / 30) + floor((mY - 17) / 25) * 40];
                } else if (Globals.menu >= 9 && abs(mY - 354) <= 312) {
                    Globals.statusWindow = floor((mX - 40) / 30) + floor((mY - 42) / 25) * 40;
                } else {
                    Globals.statusWindow = -4;
                }
            } else {
                Globals.statusWindow = -4;
            }
        } else if (Globals.menu == 1 && Globals.genSelected >= 1 && Globals.gensToDo == 0 && !Globals.drag) {
            Globals.statusWindow = -4;
            if (abs(mY - 250) <= 70) {
                if (abs(mX - 990) <= 230) {
                    float modX = (mX - 760) % 160;
                    if (modX < 140) {
                        Globals.statusWindow = floor((mX - 760) / 160) - 3;
                    }
                }
            }
        } else {
            Globals.statusWindow = -4;
        }
        if (Globals.menu == 10) {
            //Kill!
            for (int j = 0; j < 500; j++) {
                float f = (float)j / 1000;
                float rand = (pow(random(-1, 1), 3) + 1) / 2; //cube function
                Globals.slowDies = (f <= rand);
                int j2;
                int j3;
                if (Globals.slowDies) {
                    j2 = j;
                    j3 = 999 - j;
                } else {
                    j2 = 999 - j;
                    j3 = j;
                }
                Creature cj = creaturesList.get(j2);
                cj.setAlive(true);
                Creature ck = creaturesList.get(j3);
                ck.setAlive(false);
            }
            if (Globals.stepByStep) {
                drawScreenImage(2);
                setMenu(11);
            } else {
                setMenu(12);
            }
        }
        if (Globals.menu == 12) { //Reproduce and mutate
            Globals.justGotBack = true;
            for (int j = 0; j < 500; j++) {
                int j2 = j;
                if (!creaturesList.get(j).isAlive()) j2 = 999 - j;
                Creature cj = creaturesList.get(j2);
                Creature cj2 = creaturesList.get(999 - j2);

                creaturesList.set(j2, cj.copyCreature(cj.getId() + 1000));        //duplicate

                creaturesList.set(999 - j2, cj.modified(cj2.getId() + 1000));   //mutated offspring 1
                ArrayList<Node> nodes = creaturesList.get(999 - j2).getNodes();
                ArrayList<Muscle> muscles = creaturesList.get(999 - j2).getMuscles();
                Utils.toStableConfiguration(nodes, muscles, nodes.size(), muscles.size());
                Utils.adjustToCenter(nodes, nodes.size());
            }
            for (int j = 0; j < 1000; j++) {
                Creature cj = creaturesList.get(j);
                creatures[cj.getId() - (Globals.gen * 1000) - 1001] = cj.copyCreature(-1);
            }
            drawScreenImage(3);
            Globals.gen++;
            if (Globals.stepByStep) {
                setMenu(13);
            } else {
                setMenu(1);
            }
        }
        if (Globals.menu % 2 == 1 && abs(Globals.menu - 10) <= 3) {
            image(Globals.screenImage, 0, 0, 1280, 720);
        }
        if (Globals.menu == 1 || Globals.gensToDo >= 1) {
            mX = mouseX / Globals.WINDOW_SIZE_MULTIPLIER;
            ;
            mY = mouseY / Globals.WINDOW_SIZE_MULTIPLIER;
            ;
            noStroke();
            if (Globals.gen >= 1) {
                textAlign(CENTER);
                if (Globals.gen >= 5) {
                    Globals.genSelected = round((Globals.sliderX - 760) * (Globals.gen - 1) / 410) + 1;
                } else {
                    Globals.genSelected = round((Globals.sliderX - 760) * Globals.gen / 410);
                }
                if (Globals.drag) Globals.sliderX = min(max(Globals.sliderX + (mX - 25 - Globals.sliderX) * 0.2f, 760f), 1170);
                fill(100);
                rect(760, 340, 460, 50);
                fill(220);
                rect(Globals.sliderX, 340, 50, 50);
                int fs = 0;
                if (Globals.genSelected >= 1) {
                    fs = floor(log(Globals.genSelected) / log(10));
                }
                Globals.fontSize = Globals.FONT_SIZES[fs];
                textFont(font, Globals.fontSize);
                fill(0);
                text(Globals.genSelected, Globals.sliderX + 25, 366 + Globals.fontSize * 0.3333f);
            }
            if (Globals.genSelected >= 1) {
                textAlign(CENTER);
                for (int k = 0; k < 3; k++) {
                    fill(220);
                    rect(760 + k * 160, 180, 140, 140);
                    pushMatrix();
                    translate(830 + 160 * k, 290);
                    scale(60.0f / Globals.SCALE_TO_FIX_BUG);
                    drawCreature(Globals.CREATURE_DATABASE.get((Globals.genSelected - 1) * 3 + k), 0, 0, 0);
                    popMatrix();
                }
                fill(0);
                textFont(font, 16);
                text("Worst Creature", 830, 310);
                text("Median Creature", 990, 310);
                text("Best Creature", 1150, 310);
            }
            if (Globals.justGotBack) Globals.justGotBack = false;
        }
        if (Globals.statusWindow >= -3) {
            drawStatusWindow(Globals.prevStatusWindow == -4);
            if (Globals.statusWindow >= -3 && !Globals.miniSimulation) {
                openMiniSimulation();
            }
        }
        Globals.overallTimer++;
    }



    public void drawSkipButton() {
        fill(0);
        rect(0, Globals.WINDOW_HEIGHT - 40, 90, 40);
        fill(255);
        textAlign(CENTER);
        textFont(font, 32);
        text("SKIP", 45, Globals.WINDOW_HEIGHT - 8);
    }

    private void setGlobalVariables(Creature creature) {
        int lastSpeed = (Globals.simulator == null ? 1 : Globals.simulator.getSpeed());
        Globals.simulator = new Simulator(creature, lastSpeed, true);
        creature.setEnergy(Globals.BASELINE_ENERGY);

        Globals.timer = 0;
        Globals.camZoom = 0.01f;
        Globals.camX = 0;
        Globals.camY = 0;
    }

}