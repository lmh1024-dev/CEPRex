package com.lmh.coverage.entity;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import lombok.Data;

import java.io.*;
import java.util.LinkedList;

import static dk.brics.automaton.Transition.appendCharString;


@Data
public class Path implements Serializable {
    private Integer initial;

    private LinkedList<Transition> transitions;

    public Path(){
        this.initial = null;
        this.transitions = new LinkedList<>();
    }
    public Path(Integer initial){
        this.initial = initial;
        this.transitions = new LinkedList<>();
    }
    public boolean isEmpty(){
        return this.initial == null && this.transitions.isEmpty();
    }
    public void pop(){
        LinkedList<Transition> transitions = this.transitions;
        if(!transitions.isEmpty())
            transitions.removeLast();
    }
    public void push(Transition t){
        LinkedList<Transition> transitions = this.transitions;
        transitions.add(t);
    }
    public void add(Path path){
        LinkedList<Transition> transitions = this.transitions;
        if(!transitions.addAll(path.getTransitions())){
            throw new RuntimeException("添加路径错误！");
        }
    }
    public State getLastState(){
        LinkedList<Transition> transitions = this.transitions;
        if(!transitions.isEmpty()){
            return transitions.getLast().getDest();
        }
        return null;
    }

    public Path deepClone() {
        try {
            // 将对象写入流中
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);

            // 从流中读取对象
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Path) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clear(){
        this.initial = null;
        this.transitions = new LinkedList<>();
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.initial);
        if(this.transitions.isEmpty())
            return "";
        for(Transition t:this.transitions){
            stringBuilder.append(',');
            appendCharString(t.getMin(), stringBuilder);
            if (t.getMin()!= t.getMax()) {
                stringBuilder.append("-");
                appendCharString(t.getMax(), stringBuilder);
            }
            stringBuilder.append(',').append(t.getDest().getNumber());
        }
        return String.valueOf(stringBuilder);
    }
}
