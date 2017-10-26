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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Adam Brandizzi
 */
public class DynamicServiceStoreTest {

	@Test
	public void testPutHigherRankingObjectFirst() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object higherRankingObject = new Object();
		Object lowerRankingObject = new Object();

		store.put("object", higherRankingObject, 2);
		store.put("object", lowerRankingObject, 1);

		Assert.assertEquals(higherRankingObject, store.get("object"));
	}

	@Test
	public void testPutHigherRankingObjectLast() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object higherRankingObject = new Object();
		Object lowerRankingObject = new Object();

		store.put("object", lowerRankingObject, 1);

		store.put("object", higherRankingObject, 2);

		Assert.assertEquals(higherRankingObject, store.get("object"));
	}

	@Test
	public void testPutObjectOnKey() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object object = new Object();

		store.put("object", object, 1);

		Assert.assertEquals(object, store.get("object"));
	}

	@Test
	public void testPutTwoObjectsOnTwoKeys() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object object1 = new Object();
		Object object2 = new Object();

		store.put("object1", object1, 1);
		store.put("object2", object2, 1);

		Assert.assertEquals(object1, store.get("object1"));
		Assert.assertEquals(object2, store.get("object2"));
	}

	@Test
	public void testRemoveHigherRankingValueFromKey() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object higherRankingObject = new Object();
		Object lowerRankingObject = new Object();

		store.put("object", lowerRankingObject, 1);

		store.put("object", higherRankingObject, 2);

		Assert.assertEquals(higherRankingObject, store.get("object"));

		store.remove("object", higherRankingObject);

		Assert.assertEquals(lowerRankingObject, store.get("object"));
	}

	@Test
	public void testRemoveLowerRankingValueFromKey() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object higherRankingObject = new Object();
		Object lowerRankingObject = new Object();

		store.put("object", lowerRankingObject, 1);

		store.put("object", higherRankingObject, 2);

		Assert.assertEquals(higherRankingObject, store.get("object"));

		store.remove("object", lowerRankingObject);

		Assert.assertEquals(higherRankingObject, store.get("object"));
	}

	@Test
	public void testRemoveValueFromKey() {
		DynamicServiceStore<Object> store = new DynamicServiceStore<>();

		Object object = new Object();

		store.put("object", object, 1);

		Assert.assertEquals(object, store.get("object"));

		store.remove("object", object);

		Assert.assertNull(store.get("object"));
	}

}