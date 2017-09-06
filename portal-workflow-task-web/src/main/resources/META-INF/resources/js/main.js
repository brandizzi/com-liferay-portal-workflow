AUI.add(
	'liferay-workflow-tasks',
	function(A) {
		var WorkflowTasks = {
			onTaskClick: function(event, randomId) {
				var instance = this;

				var icon = event.currentTarget;
				var li = icon.get('parentNode');

				event.preventDefault();

				var content = null;

				var height = 310;

				if (li.hasClass('task-due-date-link')) {
					content = '#' + randomId + 'updateDueDate';

					height = 410;
				}

				var title = icon.text();

				WorkflowTasks._showPopup(icon.attr('href'), A.one(content), title, randomId, height);
			},

			_getComments: function(randomId) {
				var instance = this;

				var comments = A.one('#' + randomId + 'updateComments');

				if (comments && !instance._comments[randomId]) {
					instance._comments[randomId] = comments;
				}
				else if (!comments && instance._comments[randomId]) {
					comments = instance._comments[randomId];
				}

				return comments;
			},

			_getContent: function(content, title, randomId) {
				var instance = this;

				if (content && !instance._content[randomId]) {
					instance._content[randomId] = content;
				}
				else if (!content && title && title.trim().indexOf('Update Due Date') !== -1) {
					content = instance._content[randomId];
				}

				return content;
			},

			_getForm: function(url, content, comments) {
				var instance = this;

				var form = A.Node.create('<form />');

				form.setAttribute('action', url);
				form.setAttribute('method', 'POST');

				var comments = instance._getComments(randomId);

				content = instance._getContent(content, title, randomId);

				if (content) {
					form.append(content);
					content.show();
				}

				if (comments) {
					form.append(comments);
					comments.show();
				}

				return form;
			},

			_openDialog: function(form, title, height) {
				var instance = this;

				var dialog = Liferay.Util.Window.getWindow(
					{
						dialog: {
							bodyContent: form,
							destroyOnHide: true,
							height: height,
							toolbars: {
								footer: [
									{
										cssClass: 'btn-lg btn-primary',
										label: Liferay.Language.get('done'),
										on: {
											click: function() {
												submitForm(form);
											}
										}
									},
									{
										cssClass: 'btn-cancel btn-lg btn-link',
										label: Liferay.Language.get('cancel'),
										on: {
											click: function() {
												dialog.hide();
											}
										}
									}
								],
								header: [
									{
										cssClass: 'close',
										discardDefaultButtonCssClasses: true,
										labelHTML: '<span> \u00D7 </span>',
										on: {
											click: function(event) {
												dialog.hide();
											}
										}
									}
								]
							},
							width: 720
						},
						title: A.Lang.String.escapeHTML(title)
					}
				);
			},

			_showPopup: function(url, content, title, randomId, height) {
				var instance = this;

				var comments = instance._getComments(randomId);

				var content = instance._getContent(content, title, randomId);

				var form = instance._getForm(url, content, comments);

				instance._openDialog(form, title, height);
			},

			_comments: {},
			_content: {}
		};
		Liferay.WorkflowTasks = WorkflowTasks;
	},
	'',
	{
		requires: ['liferay-util-window']
	}
);