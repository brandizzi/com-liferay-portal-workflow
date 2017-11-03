<%--
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
--%>

<%@ include file="/definition/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

WorkflowDefinition currentWorkflowDefinition = (WorkflowDefinition)request.getAttribute(WebKeys.WORKFLOW_DEFINITION);

ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

WorkflowDefinition workflowDefinition = (WorkflowDefinition)row.getObject();
%>

<portlet:renderURL var="viewURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
	<portlet:param name="mvcPath" value="/definition/view_workflow_definition.jsp" />
	<portlet:param name="redirect" value="<%= currentURL %>" />
	<portlet:param name="name" value="<%= workflowDefinition.getName() %>" />
	<portlet:param name="version" value="<%= String.valueOf(workflowDefinition.getVersion()) %>" />
	<portlet:param name="<%= WorkflowWebKeys.WORKFLOW_JSP_STATE %>" value="previewBeforeRevert" />
</portlet:renderURL>

<liferay-portlet:actionURL name="revertWorkflowDefinition" var="revertURL">
	<portlet:param name="redirect" value="<%= redirect %>" />
	<portlet:param name="name" value="<%= workflowDefinition.getName() %>" />
	<portlet:param name="version" value="<%= String.valueOf(workflowDefinition.getVersion()) %>" />
</liferay-portlet:actionURL>

<c:if test="<%= currentWorkflowDefinition.getVersion() != workflowDefinition.getVersion() %>">
	<liferay-ui:icon-menu direction="left-side" icon="<%= StringPool.BLANK %>" id='<%= "iconMenu_" + String.valueOf(workflowDefinition.getVersion()) %>' markupView="lexicon" message="<%= StringPool.BLANK %>" showWhenSingleIcon="<%= true %>">
		<liferay-ui:icon
			id='<%= "previewBeforeRevert_" + String.valueOf(workflowDefinition.getVersion()) %>'
			message='<%= LanguageUtil.get(request, "preview") %>'
			url="javascript:;"
		/>

		<liferay-ui:icon
			message='<%= LanguageUtil.get(request, "restore") %>'
			url="<%= revertURL %>"
		/>
	</liferay-ui:icon-menu>
</c:if>

<aui:script use="liferay-workflow-web">

	var title = "<liferay-ui:message arguments="<%= new String[] {dateFormatTime.format(workflowDefinition.getModifiedDate()), workflowDefinitionDisplayContext.getUserName(workflowDefinition)} %>" key="preview" translateArguments="<%= false %>"/>"

	var previewBeforeRevert = A.rbind('previewBeforeRevert', Liferay.WorkflowWeb, "<%= viewURL %>", "<%= revertURL %>" , title);

	Liferay.delegateClick('<portlet:namespace />previewBeforeRevert_<%= String.valueOf(workflowDefinition.getVersion()) %>', previewBeforeRevert);
</aui:script>