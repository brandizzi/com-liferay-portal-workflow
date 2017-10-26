/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.web.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Adam Brandizzi
 */
public class DynamicServiceStore<T> {

	public T get(String key) {
		PriorityQueue<RankedValue<T>> priorityQueue = getPriorityQueue(key);

		RankedValue<T> rankedValue = priorityQueue.peek();

		if (rankedValue == null) {
			return null;
		}

		return rankedValue.getValue();
	}

	public void put(String key, T value, int ranking) {
		PriorityQueue<RankedValue<T>> priorityQueue = getPriorityQueue(key);

		priorityQueue.add(new RankedValue<T>(value, ranking));
	}

	public void remove(String key, T value) {
		PriorityQueue<RankedValue<T>> priorityQueue = getPriorityQueue(key);

		priorityQueue.removeIf(
			rankedValue -> value.equals(rankedValue.getValue()));
	}

	protected PriorityQueue<RankedValue<T>> getPriorityQueue(String key) {
		PriorityQueue<RankedValue<T>> priorityQueue = _value.get(key);

		if (priorityQueue == null) {
			priorityQueue = new PriorityQueue<>();

			_value.put(key, priorityQueue);
		}

		return priorityQueue;
	}

	private Map<String, PriorityQueue<RankedValue<T>>> _value = new HashMap<>();

	private static class RankedValue<T> implements Comparable<RankedValue<T>> {

		public RankedValue(T value, int ranking) {
			_value = value;
			_ranking = ranking;
		}

		@Override
		public int compareTo(RankedValue<T> rankedValue) {
			return rankedValue.getRanking() - getRanking();
		}

		public int getRanking() {
			return _ranking;
		}

		public T getValue() {
			return _value;
		}

		private final int _ranking;
		private final T _value;

	}

}