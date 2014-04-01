package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.client.mvp.SimpleAcceptsView;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import de.barop.gwt.client.ui.HyperlinkPushState;

import java.util.Arrays;
import java.util.List;

public class ProjectEditorViewImpl extends AbstractClientView<ProjectEditorPresenter> implements ProjectEditorView {

	private final TreeStore<String> paths = new TreeStore<>(new ModelKeyProvider<String>() {
		@Override
		public String getKey(String s) {
			return s;
		}
	});
	private final Tree<String, String> files;

	private final SimpleAcceptsView codeEditor = new SimpleAcceptsView();
//	private final SimpleAcceptsView runningExample = new SimpleAcceptsView();

	private ProgressBar progress = new ProgressBar();
	private Label error = new Label("An error occurred compiling");// TODO link to error msg
	private HyperlinkPushState compiledLink = new HyperlinkPushState();

	public ProjectEditorViewImpl() {
		BorderLayoutContainer blc = new BorderLayoutContainer();

		VerticalLayoutContainer westContainer = new VerticalLayoutContainer();
		ContentPanel projectDetailsPanel = new ContentPanel();
		projectDetailsPanel.setHeadingText("Project Details");
		CssFloatLayoutContainer container = new CssFloatLayoutContainer();
		projectDetailsPanel.setWidget(container);
		container.add(new FieldLabel(new TextField(), "Name"));
		container.add(new FieldLabel(new TextArea(), "Description"));
		westContainer.add(projectDetailsPanel, new VerticalLayoutData(1, -1));

		ContentPanel filePanel = new ContentPanel();
		filePanel.setHeadingText("Project Files");

		paths.addSortInfo(new StoreSortInfo<>(new IdentityValueProvider<String>(), SortDir.DESC));

		files = new Tree<>(paths, new IdentityValueProvider<String>());
		files.setCell(new AbstractCell<String>() {
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				sb.appendEscaped(value.substring(value.lastIndexOf("/") + 1));
			}
		});
		files.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		files.getSelectionModel().addBeforeSelectionHandler(new BeforeSelectionHandler<String>() {
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<String> event) {
				if (!paths.hasChildren(event.getItem())) {
					if (!getPresenter().tryLoadFile(event.getItem())) {
						event.cancel();
					}
				} else {
					event.cancel();
				}
			}
		});

		filePanel.setWidget(files);
		westContainer.add(filePanel, new VerticalLayoutData(1, 1));

		BorderLayoutData west = new BorderLayoutData(300);
		west.setSplit(true);
//		west.setCollapsible(true);
		blc.setWestWidget(westContainer, west);

		VerticalLayoutContainer center = new VerticalLayoutContainer();

		center.add(codeEditor, new VerticalLayoutData(1, 1));
		ToolBar toolBar = new ToolBar();
		toolBar.add(new TextButton("Save", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				getPresenter().save();
			}
		}));
		toolBar.add(new TextButton("Compile", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				getPresenter().compile();
			}
		}));
		progress.hide();
		toolBar.add(progress);
		error.setVisible(false);
		toolBar.add(error);
		center.add(toolBar);

		blc.setCenterWidget(center);

		initWidget(blc);
	}

	@Override
	public AcceptsView getCodeEditorSlot() {
		return codeEditor;
	}

	@Override
	public void setFileList(List<String> fileList) {
		paths.clear();
		for (String file : fileList) {
			addPath(file);
		}
		files.expandAll();
	}

	private void addPath(String file) {
		String last = null;
		String current = "";
		for (String part : file.split("/")) {
			current += part;
			if (paths.findModel(current) == null) {
				if (last == null) {
					paths.add(current);
				} else {
					paths.add(last, current);
				}
			}
			last = current;
			current += "/";
		}
	}

	@Override
	public void setActiveFile(String activeFile) {
		files.getSelectionModel().select(activeFile, false);
	}

	@Override
	public void showProgress(CompiledProjectStatus status) {
		List<CompiledProjectStatus> statuses = Arrays.asList(
				CompiledProjectStatus.QUEUED,
				CompiledProjectStatus.ACCEPTED,
				CompiledProjectStatus.PRECOMPILING,
				CompiledProjectStatus.COMPILING,
				CompiledProjectStatus.LINKING,
				CompiledProjectStatus.COMPLETE);
		int index = statuses.indexOf(status);
		if (index == -1) {
			//failed or stuck, draw error
			error.setVisible(true);
			progress.hide();
			return;
		}
		this.progress.show();
		((ToolBar) this.progress.getParent()).forceLayout();
		double progress = ((double) index) / 5.0;

		this.progress.updateProgress(progress, status.name());
	}
}
