package com.lmh.coverage.entity;

import dk.brics.automaton.Transition;

public class edgePair {
    /*
    邻接对边：<S,T1,P,T2,E>
     */
    private Integer number;
    private Transition transition1;
    private Transition transition2;
    private boolean visited;
    public edgePair(Integer number, Transition transition1, Transition transition2) {
        this.number = number;
        this.transition1 = transition1;
        this.transition2 = transition2;
        this.visited = false;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Transition getTransition1() {
        return transition1;
    }

    public void setTransition1(Transition transition1) {
        this.transition1 = transition1;
    }

    public Transition getTransition2() {
        return transition2;
    }

    public void setTransition2(Transition transition2) {
        this.transition2 = transition2;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(this.number).append(',');
        Transition[] transitions = new Transition[]{transition1,transition2};
        for(int i = 0;i<transitions.length;i++){
            b.append(transitions[i].getMin());
            if (transitions[i].getMin() != transitions[i].getMax()){
                b.append("-").append(transitions[i].getMax());
            }
            b.append(',').append(transitions[i].getDest().getNumber());
            if(i == 0) b.append(',');
        }
        return String.valueOf(b);
    }


}
