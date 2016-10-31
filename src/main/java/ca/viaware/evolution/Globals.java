package ca.viaware.evolution;

import processing.core.PGraphics;

import java.util.ArrayList;

public class Globals {
    public static final float GRAVITY = 0.005f;
    public static final float AIR_FRICTION = 0.95f;
    public static final int FRAMES = 60;
    public static final float FRICTION = 4;
    public static final float HAZEL_STAIRS = -1;
    public static final float PRESSURE_UNIT = 500.0f / 2.37f;
    public static final float NAUSEA_UNIT = 5;

    public static final float WINDOW_SIZE_MULTIPLIER = 1.0f;
    public static final int SEED = 0;
    public static final float ENERGY_UNIT = 20;
    public static final int MIN_BAR = -10;
    public static final int MAX_BAR = 100;
    public static final int BAR_LEN = MAX_BAR - MIN_BAR;
    public static final float BASELINE_ENERGY = 0.0f;
    public static final int ENERGY_DIRECTION = 1; // if 1, it'll count up how much energy is used.  if -1, it'll count down from the baseline energy, and when energy hits 0, the creature dies.
    public static final float BIG_MUTATION_CHANCE = 0.06f;
    public static final boolean SAVE_FRAMES_PER_GENERATION = true;
    public static final String FITNESS_NAME = "Distance";
    public static final String FITNESS_UNIT = "metres";
    public static final int OPERATION_COUNT = 12;
    public static final String[] OPERATION_NAMES = {"#", "time", "px", "py", "+", "-", "*", "÷", "%", "sin", "sig", "pres"};
    public static final int[] OPERATION_AXONS = {0, 0, 0, 0, 2, 2, 2, 2, 2, 1, 1, 0};
    public static final boolean HAVE_GROUND = true;
    public static final int HIST_BARS_PER_METER = 5;
    public static final float POST_FONT_SIZE = 0.96f;
    public static final float SCALE_TO_FIX_BUG = 1000;
    public static final float LINE_Y_1 = -0.08f; // These are for the lines of text on each node.
    public static final float LINE_Y_2 = 0.35f;
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int[] FONT_SIZES = {
            50, 36, 25, 20, 16, 14, 11, 9
    };
    public static final int[] CREATURES_IN_POSITION = new int[1000];
    public static final int[] PERCENTILE_POSITIONS = {
            0, 10, 20, 30, 40, 50, 60, 70, 80, 90,
            100, 200, 300, 400, 500, 600, 700, 800, 900, 910, 920, 930, 940, 950, 960, 970, 980, 990, 999
    };

    public static final ArrayList<Float[]> PERCENTILE = new ArrayList<Float[]>(0);
    public static final ArrayList<Integer[]> BAR_COUNTS = new ArrayList<Integer[]>(0);
    public static final ArrayList<Integer[]> SPECIES_COUNTS = new ArrayList<Integer[]>(0);
    public static final ArrayList<Integer> TOP_SPECIES_COUNTS = new ArrayList<Integer>(0);
    public static final ArrayList<Creature> CREATURE_DATABASE = new ArrayList<Creature>(0);
    public static final ArrayList<Rectangle> RECTS = new ArrayList<Rectangle>(0);

    public static int lastImageSaved = -1;
    public static int gensToDo = 0;
    public static int timer = 0;
    public static float camX = 0;
    public static float camY = 0;
    public static int menu = 0;
    public static int gen = -1;
    public static float sliderX = 1170;
    public static int genSelected = 0;
    public static boolean drag = false;
    public static boolean justGotBack = false;
    public static int creatures = 0;
    public static int creaturesTested = 0;
    public static int fontSize = 0;
    public static int statusWindow = -4;
    public static int prevStatusWindow = -4;
    public static int overallTimer = 0;
    public static boolean miniSimulation = false;
    public static int creatureWatching = 0;
    public static float camZoom = 0.015f;
    public static boolean stepByStep;
    public static boolean stepByStepSlow;
    public static boolean slowDies;
    public static int timeShow;
    public static PGraphics graphImage;
    public static PGraphics screenImage;
    public static PGraphics popUpImage;
    public static PGraphics segBarImage;

    public static Simulator simulator;
    public static final int THREAD_COUNT = 8;
}
