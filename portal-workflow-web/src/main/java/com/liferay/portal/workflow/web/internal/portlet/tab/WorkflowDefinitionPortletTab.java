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

import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import org.osgi.service.component.annotations.Component;

/**
 * @author Adam Brandizzi
 */
@Component(
	property = {
		"com.liferay.portal.workflow.web.portlet.tab.name=" +
			WorkflowWebKeys.WORKFLOW_TAB_DEFINITION
	}
)
public class WorkflowDefinitionPortletTab implements WorkflowPortletTab {

	@Override
	public String getLabel() {
		return "workflows";
	}

	@Override
	public String getName() {
		return WorkflowWebKeys.WORKFLOW_TAB_DEFINITION;
	}

	@Override
	public String getViewJSP() {
		return "/definition/view.jsp";
	}

}