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

package com.liferay.portal.workflow.web.internal.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.workflow.web.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Adam Brandizzi
 */
public abstract class BaseWorkflowPortlet extends MVCPortlet {

	public String getDefaultTab() {
		List<WorkflowPortletTab> portletTabs = getPortletTabs();

		WorkflowPortletTab portletTab = portletTabs.get(0);

		return portletTab.getName();
	}

	public abstract List<WorkflowPortletTab> getPortletTabs();

	public List<String> getWorkflowTabNames() {
		List<WorkflowPortletTab> portletTabs = getPortletTabs();

		Stream<WorkflowPortletTab> stream = portletTabs.stream();

		return stream.map(
			portletTab -> portletTab.getName()
		).collect(
			Collectors.toList()
		);
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		actionRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_DEFAULT_TAB, getDefaultTab());

		for (WorkflowPortletTab portletTab : getPortletTabs()) {
			portletTab.prepareProcessAction(actionRequest, actionResponse);
		}

		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		addRenderRequestAttributes(renderRequest);

		for (WorkflowPortletTab portletTab : getPortletTabs()) {
			portletTab.prepareRender(renderRequest, renderResponse);
		}

		super.render(renderRequest, renderResponse);
	}

	protected void addRenderRequestAttributes(RenderRequest renderRequest) {
		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_PORTLET_TABS, getPortletTabs());
		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_SELECTED_PORTLET_TAB,
			getSelectedPortletTab(renderRequest));
	}

	@Override
	protected void doDispatch(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		if (SessionErrors.contains(
				renderRequest, WorkflowException.class.getName())) {

			include("/instance/error.jsp", renderRequest, renderResponse);
		}
		else {
			for (WorkflowPortletTab portletTab : getPortletTabs()) {
				portletTab.prepareDispatch(renderRequest, renderResponse);
			}

			super.doDispatch(renderRequest, renderResponse);
		}
	}

	protected WorkflowPortletTab getSelectedPortletTab(
		RenderRequest renderRequest) {

		String tabName = ParamUtil.get(renderRequest, "tab", getDefaultTab());
		List<WorkflowPortletTab> portletTabs = getPortletTabs();

		Stream<WorkflowPortletTab> stream = portletTabs.stream();

		return stream.filter(
			portletTab -> portletTab.getName().equals(tabName)
		).findFirst(
		).orElse(
			portletTabs.get(0)
		);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setDynamicInclude(
		WorkflowPortletTab dynamicInclude, Map<String, Object> properties) {

		String tabsName = MapUtil.getString(
			properties, "portal.workflow.tabs.name");

		_dynamicIncludes.put(tabsName, dynamicInclude);
	}

	protected void unsetDynamicInclude(
		WorkflowPortletTab dynamicInclude, Map<String, Object> properties) {

		String tabsName = MapUtil.getString(
			properties, "portal.workflow.tabs.name");

		_dynamicIncludes.remove(tabsName);
	}

	private final Map<String, WorkflowPortletTab> _dynamicIncludes =
		new ConcurrentHashMap<>();

}