/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2017 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.lmh.coverage.entity;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Transition;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static dk.brics.automaton.Transition.appendCharString;


public class transitionDefine implements Serializable, Cloneable {

	static final long serialVersionUID = 40001;

	/*
	 * CLASS INVARIANT: min<=max
	 */

	char min;
	char max;

	public transitionDefine() {
	}

	/**
	 * Constructs a new singleton interval transition.
	 * @param c transition character
	 */
	public transitionDefine(char c)	{
		min = max = c;

	}

	/**
	 * Constructs a new transition.
	 * Both end points are included in the interval.
	 * @param min transition interval minimum
	 * @param max transition interval maximum
	 */
	public transitionDefine(char min, char max)	{
		if (max < min) {
			char t = max;
			max = min;
			min = t;
		}
		this.min = min;
		this.max = max;
	}

	public transitionDefine(Transition transition)	{
		this.min = transition.getMin();
		this.max = transition.getMax();
	}

	/** Returns minimum of this transition interval. */
	public char getMin() {
		return min;
	}
	
	/** Returns maximum of this transition interval. */
	public char getMax() {
		return max;
	}

	public boolean including(transitionDefine define){
		if(this.max >= define.max && this.min <= define.min)
			return true;
		else
			return false;
	}

	public static Set<transitionDefine> getUnionDefine(Set<transitionDefine> set){
		Set<transitionDefine> defines = new HashSet<>();
		Iterator<transitionDefine> iterator = set.iterator();

		while (iterator.hasNext()) {
			//先将单个字符的转换直接加入defines
			transitionDefine transitionDefine= iterator.next();
			if(transitionDefine.getMax() == transitionDefine.getMin()){
				transitionDefine newDefine = new transitionDefine(transitionDefine.min);
				defines.add(newDefine);
				iterator.remove();
			}
		}
		if (!set.isEmpty()){
			transitionDefine minDefine = getMinDefine(set);
			char maxx = minDefine.getMax();

			iterator = set.iterator();
			while (iterator.hasNext()){
				transitionDefine transitionDefine= iterator.next();

				if(transitionDefine.getMax() > maxx){
					maxx = transitionDefine.getMax();
				}
			}

			//此时找到transition中min最小，max最大的minDefine，从它的最大值开始确定范围
			while (maxx >= minDefine.max){
				boolean flag = false;
				Iterator<transitionDefine> setIterator = set.iterator();
				while (setIterator.hasNext()){
					transitionDefine transitionDefine= setIterator.next();
					if(minDefine.getMax()+1 >= transitionDefine.getMin() && minDefine.getMax()+1 <= transitionDefine.getMax() && minDefine.getMin() >= transitionDefine.getMin()){
						minDefine.max = (char)(minDefine.getMax()+1);
						flag = true;
						break;
					}else if (minDefine.getMax()+1 > transitionDefine.getMax()){
						//此时的范围已包含此转换，移除
						setIterator.remove();
					}
				}
				if(!flag){
					if (minDefine.max != minDefine.min){
						//不为单点
						defines.add(minDefine);
						minDefine = new transitionDefine((char)(minDefine.getMax()+1));
					}else {
						minDefine = getMinDefine(set);
						if(minDefine == null) break;
					}
				}
			}
		}
		//defines中可能存在这样的情况<‘-’，‘.','--.'>此步骤为消除重复，消除后仅存在<'--.'>
		Iterator<transitionDefine> transitionDefineIterator = defines.iterator();
		while (transitionDefineIterator.hasNext()){
			transitionDefine transitionDefine = transitionDefineIterator.next();
			if(transitionDefine.getMin()==transitionDefine.getMax()){
				for (transitionDefine tr:defines){
					if(transitionDefine != tr && transitionDefine.getMax() <= tr.getMax() && transitionDefine.getMax() >= tr.getMin() ){
						transitionDefineIterator.remove();
						break;
					}
				}
			}
		}
		return defines;
	}

	static transitionDefine getMinDefine(Set<transitionDefine> set){
		//返回当前集合的minDefine，minDefine满足minDefine.min最小的原则
		if (set.isEmpty()) return null;
		Iterator<transitionDefine> iterator = set.iterator();
		//设minDefine为迭代器的第一个元素
		transitionDefine first = iterator.next();
		transitionDefine minDefine = new transitionDefine(first.min,first.max);
		iterator = set.iterator();
		while (iterator.hasNext()){
			transitionDefine transitionDefine= iterator.next();
			if(transitionDefine.getMin() <= minDefine.getMin()){
				minDefine = new transitionDefine(transitionDefine.getMin(),transitionDefine.getMax());
			}
		}
		return minDefine;
	}
	/**
	 * Checks for equality.
	 * @param obj object to compare with
	 * @return true if <code>obj</code> is a transition with same
	 *         character interval and destination state as this transition.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof transitionDefine) {
			transitionDefine t = (transitionDefine)obj;
			return t.min == min && t.max == max;
		} else
			return false;
	}

	/**
	 * Returns hash code.
	 * The hash code is based on the character interval (not the destination state).
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return min * 2 + max * 3;
	}

	/**
	 * Clones this transition.
	 * @return clone with same character interval and destination state
	 */
	@Override
	public transitionDefine clone() {
		try {
			return (transitionDefine)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}



	/**
	 * Returns a string describing this state. Normally invoked via
	 * {@link Automaton#toString()}.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		appendCharString(min, b);
		if (min != max) {
			b.append("-");
			appendCharString(max, b);
		}
		return b.toString();
	}
}
