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

package com.liferay.portal.workflow.web.internal.portlet.action;

import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + WorkflowPortletKeys.CONTROL_PANEL_WORKFLOW,
		"mvc.command.name=revertWorkflowDefinition"
	},
	service = MVCActionCommand.class
)
public class RevertWorkflowDefinitionMVCActionCommand
	extends UpdateWorkflowDefinitionMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String name = ParamUtil.getString(actionRequest, "name");
		int version = ParamUtil.getInteger(actionRequest, "version");

		WorkflowDefinition workflowDefinition =
			WorkflowDefinitionManagerUtil.getWorkflowDefinition(
				themeDisplay.getCompanyId(), name, version);

		String content = workflowDefinition.getContent();

		workflowDefinition = workflowDefinitionManager.deployWorkflowDefinition(
			themeDisplay.getCompanyId(), themeDisplay.getUserId(),
			workflowDefinition.getTitle(), content.getBytes());

		setRedirectAttribute(actionRequest, actionResponse, workflowDefinition);
	}

	protected void setRedirectAttribute(
			ActionRequest actionRequest, ActionResponse actionResponse,
			WorkflowDefinition workflowDefinition)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String redirect = StringPool.BLANK;

		LiferayPortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, themeDisplay.getPpid(), PortletRequest.RENDER_PHASE);

		portletURL.setParameter(
			"mvcPath", "/definition/edit_workflow_definition.jsp");
		portletURL.setParameter("name", workflowDefinition.getName(), false);
		portletURL.setParameter(
			"version", String.valueOf(workflowDefinition.getVersion()), false);
		portletURL.setWindowState(WindowState.NORMAL);

		redirect = actionRequest.getParameter("redirect");

		String mvcPath = actionResponse.getNamespace() + "mvcPath";

		mvcPath = _http.getParameter(redirect, mvcPath, false);

		if ((mvcPath != null) && mvcPath.contains("redirect.jsp")) {
			redirect = _http.setParameter(
				redirect, actionResponse.getNamespace() + "redirect",
				portletURL.toString());
		}
		else {
			redirect = portletURL.toString();
		}

		actionRequest.setAttribute(WebKeys.REDIRECT, redirect);
	}

	@Reference
	private Http _http;

}