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

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.workflow.web.internal.configuration.WorkflowInstanceWebConfiguration;
import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;
import com.liferay.portal.workflow.web.internal.display.context.WorkflowInstanceDisplayContext;
import com.liferay.portal.workflow.web.internal.request.prepocessor.WorkflowPreprocessorHelper;
import com.liferay.portal.workflow.web.portlet.tab.WorkflowPortletTab;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adam Brandizzi
 */
@Component(
	property = {
		"com.liferay.portal.workflow.web.portlet.tab.name=" +
			WorkflowWebKeys.WORKFLOW_TAB_INSTANCE
	}
)
public class WorkflowInstancePortletTab implements WorkflowPortletTab {

	@Override
	public String getLabel() {
		return "monitoring";
	}

	@Override
	public String getName() {
		return WorkflowWebKeys.WORKFLOW_TAB_INSTANCE;
	}

	@Override
	public String getSearchJSP() {
		return "/instance/workflow_instance_search.jsp";
	}

	@Override
	public String getSearchURL(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		WorkflowInstanceDisplayContext workflowInstanceDisplayContext =
			(WorkflowInstanceDisplayContext)renderRequest.getAttribute(
				WorkflowWebKeys.WORKFLOW_INSTANCE_DISPLAY_CONTEXT);

		PortletURL portletURL =
			workflowInstanceDisplayContext.getViewPortletURL();

		return portletURL.toString();
	}

	@Override
	public String getViewJSP() {
		return "/instance/view.jsp";
	}

	@Override
	public void prepareDispatch(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		renderRequest.setAttribute(
			WorkflowInstanceWebConfiguration.class.getName(),
			workflowInstanceWebConfiguration);
	}

	@Override
	public void prepareProcessAction(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		String actionName = ParamUtil.getString(
			actionRequest, ActionRequest.ACTION_NAME);

		if (StringUtil.equalsIgnoreCase(actionName, "invokeTaglibDiscussion")) {
			workflowPreprocessorHelper.hideDefaultSuccessMessage(actionRequest);
		}
	}

	@Override
	public void prepareRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		try {
			setWorkflowInstanceRenderRequestAttribute(renderRequest);

			setWorkflowInstanceDisplayContextRequestAttribute(
				renderRequest, renderResponse);
		}
		catch (Exception e) {
			if (workflowPreprocessorHelper.isSessionErrorException(e)) {
				if (_log.isWarnEnabled()) {
					_log.warn(e, e);
				}

				workflowPreprocessorHelper.hideDefaultErrorMessage(
					renderRequest);

				SessionErrors.add(renderRequest, e.getClass());
			}
			else {
				throw new PortletException(e);
			}
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		workflowInstanceWebConfiguration = ConfigurableUtil.createConfigurable(
			WorkflowInstanceWebConfiguration.class, properties);
	}

	protected WorkflowInstanceDisplayContext getWorkflowInstanceDisplayContext(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse)
		throws PortalException {

		WorkflowInstanceDisplayContext workflowInstanceDisplayContext =
			new WorkflowInstanceDisplayContext(
				liferayPortletRequest, liferayPortletResponse);

		return workflowInstanceDisplayContext;
	}

	protected void setWorkflowInstanceDisplayContextRequestAttribute(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortalException {

		LiferayPortletRequest liferayPortletRequest =
			portal.getLiferayPortletRequest(renderRequest);
		LiferayPortletResponse liferayPortletResponse =
			portal.getLiferayPortletResponse(renderResponse);

		WorkflowInstanceDisplayContext workflowInstanceDisplayContext =
			getWorkflowInstanceDisplayContext(
				liferayPortletRequest, liferayPortletResponse);

		renderRequest.setAttribute(
			WorkflowWebKeys.WORKFLOW_INSTANCE_DISPLAY_CONTEXT,
			workflowInstanceDisplayContext);
	}

	protected void setWorkflowInstanceRenderRequestAttribute(
			RenderRequest renderRequest)
		throws PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long workflowInstanceId = ParamUtil.getLong(
			renderRequest, "workflowInstanceId");

		WorkflowInstance workflowInstance = null;

		if (workflowInstanceId != 0) {
			workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(
				themeDisplay.getCompanyId(), workflowInstanceId);
		}

		renderRequest.setAttribute(WebKeys.WORKFLOW_INSTANCE, workflowInstance);
	}

	@Reference
	protected Portal portal;

	protected volatile WorkflowInstanceWebConfiguration
		workflowInstanceWebConfiguration;

	@Reference
	protected WorkflowPreprocessorHelper workflowPreprocessorHelper;

	private static final Log _log = LogFactoryUtil.getLog(
		WorkflowInstancePortletTab.class);

}