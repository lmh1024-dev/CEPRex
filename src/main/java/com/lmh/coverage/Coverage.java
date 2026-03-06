package com.lmh.coverage;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

import java.util.LinkedList;

import static com.lmh.coverage.utils.myBasicOperations.myComplement;

public class Coverage {

    private String goldenExample;

    private Automaton automaton;

    private Automaton attemptAutomaton;

    private String positiveStrings;

    private String negativeStrings;

    private int poTR;

    private int neTR;

    private long poTime;

    public long getPoTime() {
        return poTime;
    }

    public void setPoTime(long poTime) {
        this.poTime = poTime;
    }

    public long getNeTime() {
        return neTime;
    }

    public void setNeTime(long neTime) {
        this.neTime = neTime;
    }

    private long neTime;

    public String getGoldenExample() {
        return goldenExample;
    }

    public void setGoldenExample(String goldenExample) {
        this.goldenExample = goldenExample;
    }
    public Automaton getAutomaton() {
        return automaton;
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public Automaton getAttemptAutomaton() {
        return attemptAutomaton;
    }

    public void setAttemptAutomaton(Automaton attemptAutomaton) {
        this.attemptAutomaton = attemptAutomaton;
    }

    public String getPositiveStrings() {
        return positiveStrings;
    }

    public void setPositiveStrings(String positiveStrings) {
        this.positiveStrings = positiveStrings;
    }

    public String getNegativeStrings() {
        return negativeStrings;
    }

    public void setNegativeStrings(String negativeStrings) {
        this.negativeStrings = negativeStrings;
    }

    public int getPoTR() {
        return poTR;
    }

    public void setPoTR(int poTR) {
        this.poTR = poTR;
    }

    public int getNeTR() {
        return neTR;
    }

    public void setNeTR(int neTR) {
        this.neTR = neTR;
    }

    public void generate(){
        //run positive
        run(true);

        //run negative
        run(false);
    }
    private void run(boolean isPositive){
    }
}
