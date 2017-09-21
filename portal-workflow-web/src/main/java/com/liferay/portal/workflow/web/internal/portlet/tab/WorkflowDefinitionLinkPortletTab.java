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

package com.liferay.portal.workflow.web.internal.portlet.tab;

import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.internal.display.context.WorkflowDefinitionLinkDisplayContext;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adam Brandizzi
 */
@Component(
	property = {
		"com.liferay.portal.workflow.web.portlet.tab.name=" +
			WorkflowWebKeys.WORKFLOW_TAB_DEFINITION_LINK
	}
)
public class WorkflowDefinitionLinkPortletTab implements WorkflowPortletTab {

	@Override
	public String getLabel() {
		return "schemes";
	}

	@Override
	public String getName() {
		return WorkflowWebKeys.WORKFLOW_TAB_DEFINITION_LINK;
	}

	@Override
	public String getSearchJSP() {
		return "/definition_link/workflow_definition_link_search.jsp";
	}

	@Override
	public String getSearchURL(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
		PortletURL searchURL = renderResponse.createRenderURL();

		searchURL.setParameter(
			"groupId", String.valueOf(themeDisplay.getScopeGroupId()));
		searchURL.setParameter("mvcPath", "/definition_link/view.jsp");
		searchURL.setParameter("tab", "schemes");

		return searchURL.toString();
	}

	@Override
	public String getViewJSP() {
		return "/definition_link/view.jsp";
	}

	@Override
	public void prepareRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		WorkflowDefinitionLinkDisplayContext displayContext =
			new WorkflowDefinitionLinkDisplayContext(
				renderRequest, renderResponse,
				workflowDefinitionLinkLocalService);

		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_DEFINITION_LINK_DISPLAY_CONTEXT,
			displayContext);
	}

	@Reference(unbind = "-")
	protected WorkflowDefinitionLinkLocalService
		workflowDefinitionLinkLocalService;

}