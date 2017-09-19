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
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowDefinitionLinkRenderPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowDefinitionRenderPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceDispatchPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceProcessActionPreprocessor;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowInstanceRenderPreprocessor;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import java.io.IOException;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Adam Brandizzi
 */
public abstract class BaseWorkflowPortlet extends MVCPortlet {

	public WorkflowPortletTab getDefaultWorkflowPortletTab() {
		List<WorkflowPortletTab> workflowPortletTabs = getWorkflowPortletTabs();

		return workflowPortletTabs.get(0);
	}

	public abstract List<WorkflowPortletTab> getWorkflowPortletTabs();

	public boolean isWorkflowDefinitionLinkTabVisible() {
		for (WorkflowPortletTab workflowPortletTab : getWorkflowPortletTabs()) {
			String name = workflowPortletTab.getName();

			if (name.equals(WorkflowWebKeys.WORKFLOW_TAB_DEFINITION_LINK)) {
				return true;
			}
		}

		return false;
	}

	public boolean isWorkflowDefinitionTabVisible() {
		for (WorkflowPortletTab workflowPortletTab : getWorkflowPortletTabs()) {
			String name = workflowPortletTab.getName();

			if (name.equals(WorkflowWebKeys.WORKFLOW_TAB_DEFINITION)) {
				return true;
			}
		}

		return false;
	}

	public boolean isWorkflowInstanceTabVisible() {
		for (WorkflowPortletTab workflowPortletTab : getWorkflowPortletTabs()) {
			String name = workflowPortletTab.getName();

			if (name.equals(WorkflowWebKeys.WORKFLOW_TAB_INSTANCE)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		setPortletTabsRequestAttributes(actionRequest);

		for (WorkflowPortletTab workflowPortletTab : getWorkflowPortletTabs()) {
			workflowPortletTab.prepareProcessAction(
				actionRequest, actionResponse);
		}

		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		setPortletTabsRequestAttributes(renderRequest);

		for (WorkflowPortletTab workflowPortletTab : getWorkflowPortletTabs()) {
			workflowPortletTab.prepareRender(renderRequest, renderResponse);
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
			for (WorkflowPortletTab workflowPortletTab :
					getWorkflowPortletTabs()) {

				workflowPortletTab.prepareDispatch(
					renderRequest, renderResponse);
			}

			super.doDispatch(renderRequest, renderResponse);
		}
	}

	protected WorkflowPortletTab getSelectedWorkflowPortletTab(
		PortletRequest portletRequest) {

		String tab = portletRequest.getParameter("tab");

		List<WorkflowPortletTab> workflowPortletTabs = getWorkflowPortletTabs();

		for (WorkflowPortletTab workflowPortletTab : workflowPortletTabs) {
			if (workflowPortletTab.getName().equals(tab)) {
				return workflowPortletTab;
			}
		}

		return getDefaultWorkflowPortletTab();
	}

	protected void setPortletTabsRequestAttributes(
		PortletRequest portletRequest) {

		portletRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_SELECTED_WORKFLOW_PORTLET_TAB,
			getSelectedWorkflowPortletTab(portletRequest));
		portletRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_PORTLET_TABS, getWorkflowPortletTabs());
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

}