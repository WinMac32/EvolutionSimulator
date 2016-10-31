package ca.viaware.evolution;

import processing.core.PGraphics;

import java.util.ArrayList;

public class Simulator implements Runnable {

    private ArrayList<Muscle> muscles;
    private ArrayList<Node> nodes;

    private Creature creature;

    private int simulationTimer;

    private int averageNodeNausea;
    private int totalNodeNausea;

    private float averageX;
    private float averageY;

    private int speed;
    private boolean renderable;

    public Simulator(Creature creature, int speed, boolean renderable) {
        this.creature = creature;
        this.speed = speed;
        this.renderable = renderable;

        this.muscles = new ArrayList<Muscle>();
        this.nodes = new ArrayList<Node>();

        this.averageNodeNausea = 0;
        this.totalNodeNausea = 0;

        for (int i = 0; i < creature.getNodes().size(); i++) {
            nodes.add(creature.getNodes().get(i).copyNode());
        }
        for (int i = 0; i < creature.getMuscles().size(); i++) {
            muscles.add(creature.getMuscles().get(i).copyMuscle(creature));
        }

        //Log.info("Created a new simulator for creature %0", creature.getId());
    }

    public void run() {
        for (int i = 0; i < muscles.size(); i++) {
            muscles.get(i).applyForce(nodes);
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.applyGravity();
            this.totalNodeNausea += node.applyForces();
            node.hitWalls();
            node.doMath(nodes, simulationTimer);
        }
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).realizeMathValues(i);
        }
        this.averageNodeNausea = this.totalNodeNausea / nodes.size();

        this.simulationTimer++;
        if (renderable) Globals.timer++;
    }

    public void setFitness() {
        creature.setDistance(this.averageX * 0.2f); // Multiply by 0.2 because a meter is 5 units for some weird reason.
    }

    public void setAverages() {
        this.averageX = 0;
        this.averageY = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node ni = nodes.get(i);
            this.averageX += ni.x;
            this.averageY += ni.y;
        }
        this.averageX = this.averageX / nodes.size();
        this.averageY = this.averageY / nodes.size();
    }

    public void render(Evolution evolution) {
        if (!renderable) return;
        if (Globals.timer <= 900) {
            evolution.background(120, 200, 255);
            for (int s = 0; s < this.speed; s++) {
                if (Globals.timer < 900) {
                    Globals.simulator.run();
                }
            }
            Globals.simulator.setAverages();
            if (this.speed < 30) {
                for (int s = 0; s < this.speed; s++) {
                    Globals.camX += (this.averageX - Globals.camX) * 0.06;
                    Globals.camY += (this.averageY - Globals.camY) * 0.06;
                }
            } else {
                Globals.camX = this.averageX;
                Globals.camY = this.averageY;
            }
            evolution.pushMatrix();
            evolution.translate(evolution.width / 2.0f, evolution.height / 2.0f);
            evolution.scale(1.0f / Globals.camZoom / Globals.SCALE_TO_FIX_BUG);
            evolution.translate(-Globals.camX * Globals.SCALE_TO_FIX_BUG, -Globals.camY * Globals.SCALE_TO_FIX_BUG);

            this.drawPosts(evolution, 0);
            this.drawGround(evolution, 0);
            evolution.drawCreaturePieces(nodes, muscles, 0, 0, 0);
            evolution.drawArrow(Globals.simulator.getAverageX());
            evolution.popMatrix();
            this.renderStats(evolution, Globals.WINDOW_WIDTH - 10, 0, 0.7f);
            evolution.drawSkipButton();
            this.drawOtherButtons(evolution);
        }
        if (Globals.timer == 900) {
            if (this.speed < 30) {
                evolution.noStroke();
                evolution.fill(0, 0, 0, 130);
                evolution.rect(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
                evolution.fill(0, 0, 0, 255);
                evolution.rect(Globals.WINDOW_WIDTH / 2 - 500, 200, 1000, 240);
                evolution.fill(255, 0, 0);
                evolution.textAlign(Evolution.CENTER);
                evolution.textFont(Evolution.font, 96);
                evolution.text("Creature's " + Globals.FITNESS_NAME + ":", Globals.WINDOW_WIDTH / 2, 300);
                evolution.text(Evolution.nf(Globals.simulator.getAverageX() * 0.2f, 0, 2) + " " + Globals.FITNESS_UNIT, Globals.WINDOW_WIDTH / 2, 400);
            } else {
                Globals.timer = 1020;
            }
            Globals.simulator.setFitness();
        }
        if (Globals.timer >= 1020) {
            evolution.setMenu(4);
            Globals.creaturesTested++;
            if (Globals.creaturesTested == 1000) {
                evolution.setMenu(6);
            }
            Globals.camX = 0;
        }
        if (Globals.timer >= 900) {
            Globals.timer += this.speed;
        }
    }

    public void renderPopUpImage(Evolution evolution) {
        if (!renderable) return;
        Globals.camZoom = 0.009f;
        Globals.simulator.setAverages();
        Globals.camX += (this.averageX - Globals.camX) * 0.1;
        Globals.camY += (this.averageY - Globals.camY) * 0.1;
        PGraphics popUp = Globals.popUpImage;
        popUp.beginDraw();
        popUp.smooth();

        popUp.pushMatrix();
        popUp.translate(225, 225);
        popUp.scale(1.0f / Globals.camZoom / Globals.SCALE_TO_FIX_BUG);
        popUp.translate(-Globals.camX * Globals.SCALE_TO_FIX_BUG, -Globals.camY * Globals.SCALE_TO_FIX_BUG);

        if (Globals.simulator.getSimulationTimer() < 900) {
            popUp.background(120, 200, 255);
        } else {
            popUp.background(60, 100, 128);
        }
        this.drawPosts(evolution, 2);
        this.drawGround(evolution, 2);
        evolution.drawCreaturePieces(nodes, muscles, 0, 0, 2);
        popUp.noStroke();
        popUp.endDraw();
        popUp.popMatrix();
    }

    private void drawPosts(Evolution evolution, int toImage) {
        int startPostY = Evolution.min(-8, (int) (this.averageY / 4) * 4 - 4);
        float scale = Globals.SCALE_TO_FIX_BUG;
        if (toImage == 0) {
            evolution.noStroke();
            evolution.textAlign(Evolution.CENTER);
            evolution.textFont(Evolution.font, Globals.POST_FONT_SIZE * scale);
            for (int postY = startPostY; postY <= startPostY + 8; postY += 4) {
                for (int i = (int) (this.averageX / 5 - 5); i <= (int) (this.averageX / 5 + 5); i++) {
                    evolution.fill(255);
                    evolution.rect((i * 5.0f - 0.1f) * scale, (-3.0f + postY) * scale, 0.2f * scale, 3.0f * scale);
                    evolution.rect((i * 5.0f - 1f) * scale, (-3.0f + postY) * scale, 2.0f * scale, 1.0f * scale);
                    evolution.fill(120);
                    evolution.textAlign(Evolution.CENTER);
                    evolution.text(i + " m", i * 5.0f * scale, (-2.17f + postY) * scale);
                }
            }
        } else if (toImage == 2) {
            PGraphics popUp = Globals.popUpImage;
            popUp.textAlign(Evolution.CENTER);
            popUp.textFont(Evolution.font, Globals.POST_FONT_SIZE * scale);
            popUp.noStroke();
            for (int postY = startPostY; postY <= startPostY + 8; postY += 4) {
                for (int i = (int) (this.averageX / 5 - 5); i <= (int) (this.averageX / 5 + 5); i++) {
                    popUp.fill(255);
                    popUp.rect((i * 5 - 0.1f) * scale, (-3.0f + postY) * scale, 0.2f * scale, 3 * scale);
                    popUp.rect((i * 5 - 1) * scale, (-3.0f + postY) * scale, 2 * scale, 1 * scale);
                    popUp.fill(120);
                    popUp.text(i + " m", i * 5 * scale, (-2.17f + postY) * scale);
                }
            }
        }
    }

    private void drawGround(Evolution evolution, int toImage) {
        float stairs = Globals.HAZEL_STAIRS;
        int stairDrawStart = Evolution.max(1, (int) (-this.averageY / stairs) - 10);
        float scale = Globals.SCALE_TO_FIX_BUG;

        if (toImage == 0) {
            evolution.noStroke();
            evolution.fill(0, 130, 0);
            if (Globals.HAVE_GROUND)
                evolution.rect((Globals.camX - Globals.camZoom * 800.0f) * scale, 0 * scale, (Globals.camZoom * 1600.0f) * scale, (Globals.camZoom * 900.0f) * scale);
            for (int i = 0; i < Globals.RECTS.size(); i++) {
                Rectangle r = Globals.RECTS.get(i);
                evolution.rect(r.x1 * scale, r.y1 * scale, (r.x2 - r.x1) * scale, (r.y2 - r.y1) * scale);
            }
            if (stairs > 0) {
                for (int i = stairDrawStart; i < stairDrawStart + 20; i++) {
                    evolution.fill(255, 255, 255, 128);
                    evolution.rect((this.averageX - 20) * scale, -stairs * i * scale, 40 * scale, stairs * 0.3f * scale);
                    evolution.fill(255, 255, 255, 255);
                    evolution.rect((this.averageX - 20) * scale, -stairs * i * scale, 40 * scale, stairs * 0.15f * scale);
                }
            }
        } else if (toImage == 2) {
            PGraphics popUp = Globals.popUpImage;
            popUp.noStroke();
            popUp.fill(0, 130, 0);
            if (Globals.HAVE_GROUND)
                popUp.rect((Globals.camX - Globals.camZoom * 300.0f) * scale, 0 * scale, (Globals.camZoom * 600.0f) * scale, (Globals.camZoom * 600.0f) * scale);
            for (int i = 0; i < Globals.RECTS.size(); i++) {
                Rectangle r = Globals.RECTS.get(i);
                popUp.rect(r.x1 * scale, r.y1 * scale, (r.x2 - r.x1) * scale, (r.y2 - r.y1) * scale);
            }
            if (stairs > 0) {
                for (int i = stairDrawStart; i < stairDrawStart + 20; i++) {
                    popUp.fill(255, 255, 255, 128);
                    popUp.rect((this.averageX - 20) * scale, -stairs * i * scale, 40 * scale, stairs * 0.3f * scale);
                    popUp.fill(255, 255, 255, 255);
                    popUp.rect((this.averageX - 20) * scale, -stairs * i * scale, 40 * scale, stairs * 0.15f * scale);
                }
            }
        }
    }

    private void drawOtherButtons(Evolution evolution) {
        evolution.fill(0);
        evolution.rect(120, Globals.WINDOW_HEIGHT - 40, 240, 40);
        evolution.fill(255);
        evolution.textAlign(Evolution.CENTER);
        evolution.textFont(Evolution.font, 32);
        evolution.text("PB speed: x" + this.speed, 240, Globals.WINDOW_HEIGHT - 8);
        evolution.fill(0);
        evolution.rect(Globals.WINDOW_WIDTH - 120, Globals.WINDOW_HEIGHT - 40, 120, 40);
        evolution.fill(255);
        evolution.textAlign(Evolution.CENTER);
        evolution.textFont(Evolution.font, 32);
        evolution.text("FINISH", Globals.WINDOW_WIDTH - 60, Globals.WINDOW_HEIGHT - 8);
    }

    public void renderStats(Evolution evolution, float x, float y, float size) {
        if (!renderable) return;
        evolution.textAlign(Evolution.RIGHT);
        evolution.textFont(Evolution.font, 32);
        evolution.fill(0);
        evolution.pushMatrix();
        evolution.translate(x, y);
        evolution.scale(size);
        evolution.text("Creature ID: " + this.creature.getId(), 0, 32);
        if (this.speed > 60) {
            Globals.timeShow = (int)((Globals.timer + Globals.creaturesTested * 37f) / 60f) % 15;
        } else {
            Globals.timeShow = (Globals.timer / 60);
        }
        evolution.text("Time: " + Evolution.nf(Globals.timeShow, 0, 2) + " / 15 sec.", 0, 64);
        evolution.text("Playback Speed: x" + Evolution.max(1, this.speed), 0, 96);
        String extraWord = "used";
        if (Globals.ENERGY_DIRECTION == -1) {
            extraWord = "left";
        }
        evolution.text("X: " + Evolution.nf(Globals.simulator.getAverageX() / 5.0f, 0, 2) + "", 0, 128);
        evolution.text("Y: " + Evolution.nf(-Globals.simulator.getAverageY() / 5.0f, 0, 2) + "", 0, 160);
        evolution.text("Energy " + extraWord + ": " + Evolution.nf(this.creature.getEnergy(), 0, 2) + " yums", 0, 192);
        evolution.text("A.N.Nausea: " + Evolution.nf(Globals.simulator.getAverageNodeNausea(), 0, 2) + " blehs", 0, 224);

        evolution.popMatrix();
    }

    public float getAverageX() {
        return averageX;
    }

    public float getAverageY() {
        return averageY;
    }

    public int getSimulationTimer() {
        return simulationTimer;
    }

    public void setSimulationTimer(int simulationTimer) {
        this.simulationTimer = simulationTimer;
    }

    public int getAverageNodeNausea() {
        return averageNodeNausea;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
