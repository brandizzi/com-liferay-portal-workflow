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

<%@ include file="/instance/init.jsp" %>

<%
WorkflowInstanceViewDisplayContext displayTerms = new WorkflowInstanceViewDisplayContext((LiferayPortletRequest)renderRequest, (LiferayPortletResponse)renderResponse);
%>

<liferay-ui:search-toggle
	autoFocus="<%= windowState.equals(WindowState.MAXIMIZED) %>"
	buttonLabel="search"
	displayTerms="<%= displayTerms %>"
	id="toggle_id_workflow_instance_search"
	markupView="lexicon"
>
	<aui:fieldset>
		<aui:input inlineField="<%= true %>" name="keywords" size="20" type="text" value="<%= displayTerms.getKeywords() %>" />
	</aui:fieldset>
</liferay-ui:search-toggle>