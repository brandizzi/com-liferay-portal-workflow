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

import com.liferay.portal.workflow.web.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import java.util.Arrays;
import java.util.List;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Adam Brandizzi
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-workflow",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.footer-portlet-javascript=/js/main.js",
		"com.liferay.portlet.friendly-url-mapping=workflow",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.icon=/icons/workflow.png",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Workflow",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + WorkflowPortletKeys.CONTROL_PANEL_WORKFLOW,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class ControlPanelWorkflowPortlet extends BaseWorkflowPortlet {

	@Override
	public List<WorkflowPortletTab> getPortletTabs() {
		return Arrays.asList(
			_definitionPortletTab, _definitionLinkPortletTab,
			_instancePortletTab);
	}

	@Reference(
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(portal.workflow.tabs.name=" + WorkflowWebKeys.WORKFLOW_TAB_DEFINITION_LINK + ")",
		unbind = "-"
	)
	public void setDefinitionLinkPortletTab(
		WorkflowPortletTab definitionLinkPortletTab) {

		_definitionLinkPortletTab = definitionLinkPortletTab;
	}

	@Reference(
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(portal.workflow.tabs.name=" + WorkflowWebKeys.WORKFLOW_TAB_DEFINITION + ")",
		unbind = "-"
	)
	public void setDefinitionPortletTab(
		WorkflowPortletTab definitionPortletTab) {

		_definitionPortletTab = definitionPortletTab;
	}

	@Reference(
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(portal.workflow.tabs.name=" + WorkflowWebKeys.WORKFLOW_TAB_INSTANCE + ")",
		unbind = "-"
	)
	public void setInstancePortletTab(WorkflowPortletTab instancePortletTab) {
		_instancePortletTab = instancePortletTab;
	}

	private WorkflowPortletTab _definitionLinkPortletTab;
	private WorkflowPortletTab _definitionPortletTab;
	private WorkflowPortletTab _instancePortletTab;

}