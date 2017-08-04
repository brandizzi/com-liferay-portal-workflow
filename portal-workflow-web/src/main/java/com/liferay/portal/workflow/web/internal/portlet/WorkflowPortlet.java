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

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.workflow.definition.link.web.internal.display.context.WorkflowDefinitionLinkDisplayContext;
import com.liferay.portal.workflow.definition.web.internal.display.context.WorkflowDefinitionDisplayContext;
import com.liferay.portal.workflow.instance.web.configuration.WorkflowInstanceWebConfiguration;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;
import com.liferay.portal.workflow.web.internal.constants.WorkflowWebKeys;

import java.io.IOException;

import java.util.Map;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
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
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-workflow-definition-link",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.header-portlet-css=/definition_link/css/main.css",
		"com.liferay.portlet.icon=/icons/workflow_definition_link.png",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Workflow Definition Link",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + WorkflowPortletKeys.WORKFLOW,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class WorkflowPortlet extends MVCPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		prepareWorkflowInstanceProcessAction(actionRequest);

		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		prepareWorkflowDefinitionRender(renderRequest, renderResponse);
		prepareWorkflowDefinitionLinkRender(renderRequest, renderResponse);
		prepareWorkflowInstanceRender(renderRequest);

		super.render(renderRequest, renderResponse);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		workflowInstanceWebConfiguration = ConfigurableUtil.createConfigurable(
			WorkflowInstanceWebConfiguration.class, properties);
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
			renderRequest.setAttribute(
				WorkflowInstanceWebConfiguration.class.getName(),
				workflowInstanceWebConfiguration);

			super.doDispatch(renderRequest, renderResponse);
		}
	}

	protected void prepareWorkflowDefinitionRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		try {
			String path = getPath(renderRequest, renderResponse);

			WorkflowDefinitionDisplayContext displayContext =
				new WorkflowDefinitionDisplayContext(renderRequest);

			renderRequest.setAttribute(
				WorkflowWebKeys.WORKFLOW_DEFINITION_DISPLAY_CONTEXT,
				displayContext);

			if (Objects.equals(
					path, "/definition/edit_workflow_definition.jsp") ||
				Objects.equals(
					path, "/definition/view_workflow_definition.jsp")) {

				setWorkflowDefinitionRenderRequestAttribute(renderRequest);
			}
		}
		catch (Exception e) {
			if (isSessionErrorException(e)) {
				hideDefaultErrorMessage(renderRequest);

				SessionErrors.add(renderRequest, e.getClass());
			}
			else {
				throw new PortletException(e);
			}
		}
	}

	protected void prepareWorkflowDefinitionLinkRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		try {
			WorkflowDefinitionLinkDisplayContext displayContext =
				new WorkflowDefinitionLinkDisplayContext(
					renderRequest, renderResponse,
					_workflowDefinitionLinkLocalService);

			renderRequest.setAttribute(
				WorkflowWebKeys.WORKFLOW_DEFINITION_LINK_DISPLAY_CONTEXT,
				displayContext);
		}
		catch (PortalException pe) {
			throw new PortletException(pe);
		}
	}

	protected void prepareWorkflowInstanceProcessAction(
		ActionRequest actionRequest) {

		String actionName = ParamUtil.getString(
			actionRequest, ActionRequest.ACTION_NAME);

		if (StringUtil.equalsIgnoreCase(actionName, "invokeTaglibDiscussion")) {
			hideDefaultSuccessMessage(actionRequest);
		}
	}

	protected void prepareWorkflowInstanceRender(RenderRequest renderRequest)
		throws PortletException {

		try {
			setWorkflowInstanceRenderRequestAttribute(renderRequest);
		}
		catch (Exception e) {
			if (isSessionErrorException(e)) {
				if (_log.isWarnEnabled()) {
					_log.warn(e, e);
				}

				hideDefaultErrorMessage(renderRequest);

				SessionErrors.add(renderRequest, e.getClass());
			}
			else {
				throw new PortletException(e);
			}
		}
	}

	@Reference(unbind = "-")
	protected void setWorkflowDefinitionLinkLocalService(
		WorkflowDefinitionLinkLocalService workflowDefinitionLinkLocalService) {

		_workflowDefinitionLinkLocalService =
			workflowDefinitionLinkLocalService;
	}

	protected void setWorkflowDefinitionRenderRequestAttribute(
			RenderRequest renderRequest)
		throws PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String name = ParamUtil.getString(renderRequest, "name");
		int version = ParamUtil.getInteger(renderRequest, "version");

		if (Validator.isNull(name)) {
			return;
		}

		WorkflowDefinition workflowDefinition =
			WorkflowDefinitionManagerUtil.getWorkflowDefinition(
				themeDisplay.getCompanyId(), name, version);

		renderRequest.setAttribute(
			WebKeys.WORKFLOW_DEFINITION, workflowDefinition);
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

	protected volatile WorkflowInstanceWebConfiguration
		workflowInstanceWebConfiguration;

	private static final Log _log = LogFactoryUtil.getLog(
		WorkflowPortlet.class);

	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

}