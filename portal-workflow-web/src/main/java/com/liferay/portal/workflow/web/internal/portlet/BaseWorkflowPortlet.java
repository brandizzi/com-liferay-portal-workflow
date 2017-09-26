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
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowDefinitionLinkRenderPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowDefinitionRenderPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceDispatchPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceProcessActionPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceRenderPreprocessor;
import com.liferay.portal.workflow.web.internal.servlet.taglib.WorkflowDynamicInclude;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
		List<String> tabNames = getWorkflowTabNames();

		return tabNames.get(0);
	}

	public abstract List<String> getWorkflowTabNames();

	public boolean isWorkflowDefinitionLinkTabVisible() {
		List<String> tabNames = getWorkflowTabNames();

		return tabNames.contains(WorkflowWebKeys.WORKFLOW_TAB_DEFINITION_LINK);
	}

	public boolean isWorkflowDefinitionTabVisible() {
		List<String> tabNames = getWorkflowTabNames();

		return tabNames.contains(WorkflowWebKeys.WORKFLOW_TAB_DEFINITION);
	}

	public boolean isWorkflowInstanceTabVisible() {
		List<String> tabNames = getWorkflowTabNames();

		if (tabNames.contains(WorkflowWebKeys.WORKFLOW_TAB_INSTANCE) ||
			tabNames.contains(WorkflowWebKeys.WORKFLOW_TAB_MY_SUBMISSIONS)) {

			return true;
		}

		return false;
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		actionRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_DEFAULT_TAB, getDefaultTab());

		if (isWorkflowDefinitionTabVisible()) {
			workflowInstanceProcessActionPreprocessor.prepareProcessAction(
				actionRequest, actionResponse);
		}

		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_TAB_DYNAMIC_INCLUDES, _dynamicIncludes);

		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_TAB_NAMES, getWorkflowTabNames());

		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_DEFAULT_TAB, getDefaultTab());

		if (isWorkflowDefinitionLinkTabVisible()) {
			workflowDefinitionLinkRenderPreprocessor.prepareRender(
				renderRequest, renderResponse);
		}

		if (isWorkflowDefinitionTabVisible()) {
			workflowDefinitionRenderPreprocessor.prepareRender(
				renderRequest, renderResponse);
		}

		if (isWorkflowInstanceTabVisible()) {
			workflowInstanceRenderPreprocessor.prepareRender(
				renderRequest, renderResponse);
		}

		super.render(renderRequest, renderResponse);
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
			workflowInstanceDispatchPreprocessor.prepareDispatch(
				renderRequest, renderResponse);

			super.doDispatch(renderRequest, renderResponse);
		}
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setDynamicInclude(
		WorkflowDynamicInclude dynamicInclude, Map<String, Object> properties) {

		String tabsName = MapUtil.getString(
			properties, "portal.workflow.tabs.name");

		_dynamicIncludes.put(tabsName, dynamicInclude);
	}

	protected void unsetDynamicInclude(
		WorkflowDynamicInclude dynamicInclude, Map<String, Object> properties) {

		String tabsName = MapUtil.getString(
			properties, "portal.workflow.tabs.name");

		_dynamicIncludes.remove(tabsName);
	}

	@Reference(unbind = "-")
	protected WorkflowDefinitionLinkRenderPreprocessor
		workflowDefinitionLinkRenderPreprocessor;

	@Reference(unbind = "-")
	protected WorkflowDefinitionRenderPreprocessor
		workflowDefinitionRenderPreprocessor;

	@Reference(unbind = "-")
	protected WorkflowInstanceDispatchPreprocessor
		workflowInstanceDispatchPreprocessor;

	@Reference(unbind = "-")
	protected WorkflowInstanceProcessActionPreprocessor
		workflowInstanceProcessActionPreprocessor;

	@Reference(unbind = "-")
	protected WorkflowInstanceRenderPreprocessor
		workflowInstanceRenderPreprocessor;

	private final Map<String, WorkflowDynamicInclude> _dynamicIncludes =
		new ConcurrentHashMap<>();

}