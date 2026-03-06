package com.lmh.coverage.entity;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.HashMap;
import java.util.HashSet;

public class Edge {

    private Transition transition;

    private Boolean isVisited;

    public Edge(Transition transition, boolean b) {
        this.transition = transition;
        this.isVisited = b;
    }

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    public Boolean getVisited() {
        return isVisited;
    }

    public void setVisited(Boolean visited) {
        isVisited = visited;
    }
}
