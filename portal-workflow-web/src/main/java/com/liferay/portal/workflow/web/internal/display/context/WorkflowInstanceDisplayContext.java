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

package com.liferay.portal.workflow.web.internal.display.context;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowLogManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.kernel.workflow.comparator.WorkflowComparatorFactoryUtil;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;
import com.liferay.portal.workflow.web.internal.search.WorkflowInstanceSearch;
import com.liferay.portal.workflow.web.internal.util.WorkflowInstancePortletUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.PortletURL;

/**
 * @author Leonardo Barros
 */
public class WorkflowInstanceDisplayContext
	extends BaseWorkflowInstanceDisplayContext {

	public WorkflowInstanceDisplayContext(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse)
		throws PortalException {

		super(liferayPortletRequest, liferayPortletResponse);

		PortletURL portletURL = PortletURLUtil.getCurrent(
			liferayPortletRequest, liferayPortletResponse);

		_searchContainer = new WorkflowInstanceSearch(
			liferayPortletRequest, portletURL);

		_searchContainer.setResults(
			getSearchContainerResults(
				_searchContainer.getStart(), _searchContainer.getEnd(),
				_searchContainer.getOrderByComparator()));

		_searchContainer.setTotal(getSearchContainerTotal());

		setSearchContainerEmptyResultsMessage(_searchContainer);
	}

	public AssetEntry getAssetEntry() throws PortalException {
		AssetRenderer<?> assetRenderer = getAssetRenderer();

		if (assetRenderer == null) {
			return null;
		}

		AssetRendererFactory<?> assetRendererFactory =
			getAssetRendererFactory();

		return assetRendererFactory.getAssetEntry(
			assetRendererFactory.getClassName(), assetRenderer.getClassPK());
	}

	public String getAssetEntryVersionId() {
		long classPK = getWorkflowContextEntryClassPK();

		return String.valueOf(classPK);
	}

	public String getAssetIconCssClass(WorkflowInstance workflowInstance) {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler(
			workflowInstance);

		return workflowHandler.getIconCssClass();
	}

	public String getAssetName() {
		return HtmlUtil.escape(getWorkflowDefinitionName());
	}

	public AssetRenderer<?> getAssetRenderer() throws PortalException {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler();

		return workflowHandler.getAssetRenderer(
			getWorkflowContextEntryClassPK());
	}

	public AssetRendererFactory<?> getAssetRendererFactory() {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler();

		return workflowHandler.getAssetRendererFactory();
	}

	public String getAssetTitle(WorkflowInstance workflowInstance) {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler(
			workflowInstance);

		long classPK = getWorkflowContextEntryClassPK(
			workflowInstance.getWorkflowContext());

		return HtmlUtil.escape(
			workflowHandler.getTitle(
				classPK, workflowInstanceRequestHelper.getLocale()));
	}

	public String getAssetType(WorkflowInstance workflowInstance) {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler(
			workflowInstance);

		return workflowHandler.getType(
			workflowInstanceRequestHelper.getLocale());
	}

	public String getAssignedTheTaskMessageKey(WorkflowLog workflowLog)
		throws PortalException {

		User user = getUser(workflowLog.getUserId());

		if (user.isMale()) {
			return "x-assigned-the-task-to-himself";
		}
		else {
			return "x-assigned-the-task-to-herself";
		}
	}

	public Object getAssignedTheTaskToMessageArguments(WorkflowLog workflowLog)
		throws PortalException {

		String userName = PortalUtil.getUserName(
			workflowLog.getAuditUserId(), StringPool.BLANK);
		String actorName = getActorName(workflowLog);

		return new Object[] {
			HtmlUtil.escape(userName), HtmlUtil.escape(actorName)
		};
	}

	public String getDefinition(WorkflowInstance workflowInstance) {
		return LanguageUtil.get(
			workflowInstanceRequestHelper.getRequest(),
			HtmlUtil.escape(workflowInstance.getWorkflowDefinitionName()));
	}

	public String getDisplayStyle() {
		if (_displayStyle == null) {
			_displayStyle = WorkflowInstancePortletUtil.getDisplayStyle(
				liferayPortletRequest, getDisplayViews());
		}

		return _displayStyle;
	}

	public String[] getDisplayViews() {
		return _DISPLAY_VIEWS;
	}

	public String getEditHeaderTitle() {
		return LanguageUtil.get(
			workflowInstanceRequestHelper.getRequest(),
			getWorkflowDefinitionName());
	}

	public Date getEndDate(WorkflowInstance workflowInstance) {
		return workflowInstance.getEndDate();
	}

	public String getHeaderTitle() {
		return "workflow-submissions";
	}

	public String getIconCssClass() {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler();

		return workflowHandler.getIconCssClass();
	}

	public String getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(liferayPortletRequest, "keywords");

		return _keywords;
	}

	public Date getLastActivityDate(WorkflowInstance workflowInstance)
		throws PortalException {

		WorkflowLog workflowLog = getLatestWorkflowLog(workflowInstance);

		if (workflowLog == null) {
			return null;
		}

		return workflowLog.getCreateDate();
	}

	public String getNavigation() {
		if (_navigation != null) {
			return _navigation;
		}

		_navigation = ParamUtil.getString(request, "navigation", "all");

		return _navigation;
	}

	public String getOrderByCol() {
		if (_orderByCol != null) {
			return _orderByCol;
		}

		_orderByCol = ParamUtil.getString(request, "orderByCol");

		if (Validator.isNull(_orderByCol)) {
			_orderByCol = portalPreferences.getValue(
				WorkflowPortletKeys.USER_WORKFLOW, "instance-order-by-col",
				"last-activity-date");
		}
		else {
			boolean saveOrderBy = ParamUtil.getBoolean(request, "saveOrderBy");

			if (saveOrderBy) {
				portalPreferences.setValue(
					WorkflowPortletKeys.USER_WORKFLOW, "instance-order-by-col",
					_orderByCol);
			}
		}

		return _orderByCol;
	}

	public String getOrderByType() {
		if (_orderByType != null) {
			return _orderByType;
		}

		_orderByType = ParamUtil.getString(request, "orderByType");

		if (Validator.isNull(_orderByType)) {
			_orderByType = portalPreferences.getValue(
				WorkflowPortletKeys.USER_WORKFLOW, "instance-order-by-type",
				"asc");
		}
		else {
			boolean saveOrderBy = ParamUtil.getBoolean(request, "saveOrderBy");

			if (saveOrderBy) {
				portalPreferences.setValue(
					WorkflowPortletKeys.USER_WORKFLOW, "instance-order-by-type",
					_orderByType);
			}
		}

		return _orderByType;
	}

	public String getPanelTitle() {
		String modelResource = ResourceActionsUtil.getModelResource(
			workflowInstanceRequestHelper.getLocale(),
			getWorkflowContextEntryClassName());

		return LanguageUtil.format(
			workflowInstanceRequestHelper.getRequest(), "preview-of-x",
			modelResource, false);
	}

	public Object getPreviousAssigneeMessageArguments(WorkflowLog workflowLog) {
		return HtmlUtil.escape(
			PortalUtil.getUserName(
				workflowLog.getPreviousUserId(), StringPool.BLANK));
	}

	public List<WorkflowHandler<?>> getSearchableAssetsWorkflowHandlers() {
		List<WorkflowHandler<?>> searchableAssetsWorkflowHandlers =
			new ArrayList<>();

		List<WorkflowHandler<?>> workflowHandlers =
			WorkflowHandlerRegistryUtil.getWorkflowHandlers();

		for (WorkflowHandler<?> workflowHandler : workflowHandlers) {
			if (workflowHandler.isAssetTypeSearchable()) {
				searchableAssetsWorkflowHandlers.add(workflowHandler);
			}
		}

		return searchableAssetsWorkflowHandlers;
	}

	public WorkflowInstanceSearch getSearchContainer() {
		return _searchContainer;
	}

	public String getStatus(WorkflowInstance workflowInstance) {
		return LanguageUtil.get(
			workflowInstanceRequestHelper.getRequest(),
			HtmlUtil.escape(workflowInstance.getState()));
	}

	public String getTaskCompleted(WorkflowTask workflowTask) {
		if (workflowTask.isCompleted()) {
			return LanguageUtil.get(
				workflowInstanceRequestHelper.getRequest(), "yes");
		}

		return LanguageUtil.get(
			workflowInstanceRequestHelper.getRequest(), "no");
	}

	public Object getTaskCompletionMessageArguments(WorkflowLog workflowLog)
		throws PortalException {

		String actorName = getActorName(workflowLog);

		return new Object[] {
			HtmlUtil.escape(actorName), HtmlUtil.escape(workflowLog.getState())
		};
	}

	public String getTaskContentTitleMessage() {
		WorkflowHandler<?> workflowHandler = getWorkflowHandler();

		long classPK = getWorkflowContextEntryClassPK();

		return HtmlUtil.escape(
			workflowHandler.getTitle(
				classPK, workflowInstanceRequestHelper.getLocale()));
	}

	public String getTaskDueDate(WorkflowTask workflowTask) {
		if (workflowTask.getDueDate() == null) {
			return LanguageUtil.get(
				workflowInstanceRequestHelper.getRequest(), "never");
		}
		else {
			return dateFormatDateTime.format(workflowTask.getDueDate());
		}
	}

	public Object getTaskInitiallyAssignedMessageArguments(
			WorkflowLog workflowLog)
		throws PortalException {

		return HtmlUtil.escape(getActorName(workflowLog));
	}

	public String getTaskName(WorkflowTask workflowTask) {
		return HtmlUtil.escape(workflowTask.getName());
	}

	public Object getTaskUpdateMessageArguments(WorkflowLog workflowLog)
		throws PortalException {

		return HtmlUtil.escape(getActorName(workflowLog));
	}

	public Object getTransitionMessageArguments(WorkflowLog workflowLog)
		throws PortalException {

		String actorName = getActorName(workflowLog);

		return new Object[] {
			HtmlUtil.escape(actorName),
			HtmlUtil.escape(workflowLog.getPreviousState()),
			HtmlUtil.escape(workflowLog.getState())
		};
	}

	public String getUserFullName(WorkflowLog workflowLog)
		throws PortalException {

		User user = getUser(workflowLog.getUserId());

		return HtmlUtil.escape(user.getFullName());
	}

	public PortletURL getViewPortletURL() {
		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		portletURL.setParameter("tab", "monitoring");

		return portletURL;
	}

	public String getWorkflowInstanceEndDate() {
		WorkflowInstance workflowInstance = getWorkflowInstance();

		if (workflowInstance.getEndDate() == null) {
			return LanguageUtil.get(
				workflowInstanceRequestHelper.getRequest(), "never");
		}
		else {
			return dateFormatDateTime.format(workflowInstance.getEndDate());
		}
	}

	public String getWorkflowInstanceState() {
		WorkflowInstance workflowInstance = getWorkflowInstance();

		return LanguageUtil.get(
			workflowInstanceRequestHelper.getRequest(),
			workflowInstance.getState());
	}

	public String getWorkflowLogComment(WorkflowLog workflowLog) {
		return HtmlUtil.escape(workflowLog.getComment());
	}

	public String getWorkflowLogCreateDate(WorkflowLog workflowLog) {
		return dateFormatDateTime.format(workflowLog.getCreateDate());
	}

	public List<WorkflowLog> getWorkflowLogs() throws WorkflowException {
		if (_workflowLogs == null) {
			OrderByComparator<WorkflowLog> orderByComparator =
				WorkflowComparatorFactoryUtil.getLogCreateDateComparator(false);

			_workflowLogs =
				WorkflowLogManagerUtil.getWorkflowLogsByWorkflowInstance(
					workflowInstanceRequestHelper.getCompanyId(),
					getWorkflowInstanceId(), _logTypes, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, orderByComparator);
		}

		return _workflowLogs;
	}

	public List<WorkflowTask> getWorkflowTasks() throws PortalException {
		if (workflowTasks == null) {
			workflowTasks =
				WorkflowTaskManagerUtil.getWorkflowTasksByWorkflowInstance(
					workflowInstanceRequestHelper.getCompanyId(), null,
					getWorkflowInstanceId(), null, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);
		}

		return workflowTasks;
	}

	public boolean isAuditUser(WorkflowLog workflowLog) {
		if (workflowLog.getUserId() == 0) {
			return false;
		}

		if (workflowLog.getAuditUserId() == workflowLog.getUserId()) {
			return true;
		}

		return false;
	}

	public boolean isNavigationAll() {
		if (Objects.equals(getNavigation(), "all")) {
			return true;
		}

		return false;
	}

	public boolean isNavigationCompleted() {
		if (Objects.equals(getNavigation(), "completed")) {
			return true;
		}

		return false;
	}

	public boolean isNavigationPending() {
		if (Objects.equals(getNavigation(), "pending")) {
			return true;
		}

		return false;
	}

	public boolean isSearch() {
		if (Validator.isNotNull(getKeywords())) {
			return true;
		}

		return false;
	}

	public boolean isWorkflowTasksEmpty() throws PortalException {
		return getWorkflowTasks().isEmpty();
	}

	protected String getActorName(WorkflowLog workflowLog)
		throws PortalException {

		if (workflowLog.getRoleId() != 0) {
			Role role = getRole(workflowLog.getRoleId());

			return role.getDescriptiveName();
		}
		else if (workflowLog.getUserId() != 0) {
			User user = getUser(workflowLog.getUserId());

			return user.getFullName();
		}

		return StringPool.BLANK;
	}

	protected String getAssetType(String keywords) {
		for (WorkflowHandler<?> workflowHandler :
				getSearchableAssetsWorkflowHandlers()) {

			String assetType = workflowHandler.getType(
				workflowInstanceRequestHelper.getLocale());

			if (StringUtil.equalsIgnoreCase(keywords, assetType)) {
				return workflowHandler.getClassName();
			}
		}

		return StringPool.BLANK;
	}

	protected Boolean getCompleted() {
		if (isNavigationAll()) {
			return null;
		}

		if (isNavigationCompleted()) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	protected WorkflowLog getLatestWorkflowLog(
			WorkflowInstance workflowInstance)
		throws PortalException {

		List<WorkflowLog> workflowLogs =
			WorkflowLogManagerUtil.getWorkflowLogsByWorkflowInstance(
				workflowInstanceRequestHelper.getCompanyId(),
				workflowInstance.getWorkflowInstanceId(), null, 0, 1,
				WorkflowComparatorFactoryUtil.getLogCreateDateComparator());

		if (workflowLogs.isEmpty()) {
			return null;
		}

		return workflowLogs.get(0);
	}

	protected Role getRole(long roleId) throws PortalException {
		Role role = _roles.get(roleId);

		if (role == null) {
			role = RoleLocalServiceUtil.getRole(roleId);

			_roles.put(roleId, role);
		}

		return role;
	}

	protected List<WorkflowInstance> getSearchContainerResults(
			int start, int end, OrderByComparator<WorkflowInstance> comparator)
		throws PortalException {

		return WorkflowInstanceManagerUtil.search(
			workflowInstanceRequestHelper.getCompanyId(), null,
			getAssetType(getKeywords()), getKeywords(), getKeywords(),
			getCompleted(), start, end, comparator);
	}

	protected int getSearchContainerTotal() throws PortalException {
		return WorkflowInstanceManagerUtil.searchCount(
			workflowInstanceRequestHelper.getCompanyId(), null,
			getAssetType(getKeywords()), getKeywords(), getKeywords(),
			getCompleted());
	}

	protected User getUser(long userId) throws PortalException {
		User user = _users.get(userId);

		if (user == null) {
			user = UserLocalServiceUtil.getUser(userId);

			_users.put(userId, user);
		}

		return user;
	}

	protected Map<String, Serializable> getWorkflowContext() {
		WorkflowInstance workflowInstance = getWorkflowInstance();

		return workflowInstance.getWorkflowContext();
	}

	protected String getWorkflowContextEntryClassName() {
		Map<String, Serializable> workflowContext = getWorkflowContext();

		return (String)workflowContext.get(
			WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME);
	}

	protected String getWorkflowContextEntryClassName(
		Map<String, Serializable> workflowContext) {

		return (String)workflowContext.get(
			WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME);
	}

	protected long getWorkflowContextEntryClassPK() {
		Map<String, Serializable> workflowContext = getWorkflowContext();

		return GetterUtil.getLong(
			(String)workflowContext.get(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK));
	}

	protected long getWorkflowContextEntryClassPK(
		Map<String, Serializable> workflowContext) {

		return GetterUtil.getLong(
			(String)workflowContext.get(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK));
	}

	protected String getWorkflowDefinitionName() {
		WorkflowInstance workflowInstance = getWorkflowInstance();

		return workflowInstance.getWorkflowDefinitionName();
	}

	protected WorkflowHandler<?> getWorkflowHandler() {
		String className = getWorkflowContextEntryClassName();

		return WorkflowHandlerRegistryUtil.getWorkflowHandler(className);
	}

	protected WorkflowHandler<?> getWorkflowHandler(
		WorkflowInstance workflowInstance) {

		String className = getWorkflowContextEntryClassName(
			workflowInstance.getWorkflowContext());

		return WorkflowHandlerRegistryUtil.getWorkflowHandler(className);
	}

	protected WorkflowInstance getWorkflowInstance() {
		return (WorkflowInstance)liferayPortletRequest.getAttribute(
			WebKeys.WORKFLOW_INSTANCE);
	}

	protected long getWorkflowInstanceId() {
		WorkflowInstance workflowInstance = getWorkflowInstance();

		return workflowInstance.getWorkflowInstanceId();
	}

	protected void setSearchContainerEmptyResultsMessage(
		WorkflowInstanceSearch searchContainer) {

		DisplayTerms searchTerms = searchContainer.getDisplayTerms();

		if (isNavigationAll()) {
			searchContainer.setEmptyResultsMessage("there-are-no-instances");
		}
		else if (isNavigationPending()) {
			searchContainer.setEmptyResultsMessage(
				"there-are-no-pending-instances");
		}
		else {
			searchContainer.setEmptyResultsMessage(
				"there-are-no-completed-instances");
		}

		if (Validator.isNotNull(searchTerms.getKeywords())) {
			searchContainer.setEmptyResultsMessage(
				searchContainer.getEmptyResultsMessage() +
					"-with-the-specified-search-criteria");
		}
	}

	protected List<WorkflowTask> workflowTasks;

	private static final String[] _DISPLAY_VIEWS = {"descriptive", "list"};

	private static final List<Integer> _logTypes = Arrays.asList(
		WorkflowLog.TASK_ASSIGN, WorkflowLog.TASK_COMPLETION,
		WorkflowLog.TASK_UPDATE, WorkflowLog.TRANSITION);

	private String _displayStyle;
	private String _keywords;
	private String _navigation;
	private String _orderByCol;
	private String _orderByType;
	private final Map<Long, Role> _roles = new HashMap<>();
	private final WorkflowInstanceSearch _searchContainer;
	private final Map<Long, User> _users = new HashMap<>();
	private List<WorkflowLog> _workflowLogs;

}