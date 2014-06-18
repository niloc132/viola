package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.client.mvp.SimpleAcceptsView;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.HasLayout;
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

public class ProjectEditorViewImpl extends AbstractClientView<ProjectEditorPresenter> implements ProjectEditorView, ValueAwareEditor<Project> {

	TextField title = new TextField();
	TextArea description = new TextArea();
	private final ContentPanel example = new ContentPanel();
	private String lastCompiledId;
	private String lastUrl;


	public interface Driver extends SimpleBeanEditorDriver<Project, ProjectEditorViewImpl> {}

	private Driver driver = GWT.create(Driver.class);
	private BorderLayoutContainer blc;
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
		BorderLayoutContainer code = new BorderLayoutContainer();
		
		ContentPanel projectDetailsPanel = new ContentPanel();
		VerticalLayoutContainer container = new VerticalLayoutContainer();
		projectDetailsPanel.setHeadingText("Project Details");
		projectDetailsPanel.setWidget(container);
		container.add(new FieldLabel(title, "Name"), new VerticalLayoutData(1, -1, new Margins(0, 10, 0, 10)));
		container.add(new FieldLabel(description, "Description"), new VerticalLayoutData(1, -1, new Margins(0, 10, 0, 10)));

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
		container.add(filePanel, new VerticalLayoutData(1, 1));

		BorderLayoutData west = new BorderLayoutData(300);
		west.setMaxSize(5000);
		west.setSplit(true);
		west.setCollapsible(true);
		west.setCollapseMini(true);
		west.setMargins(new Margins(0, 8, 0, 0));
		west.setFloatable(false);
		code.setWestWidget(projectDetailsPanel, west);

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
				example.mask("Compiling...");
				getPresenter().compile();
			}
		}));
		progress.hide();
		toolBar.add(progress);
		error.setVisible(false);
		toolBar.add(error);
		center.add(toolBar);

		code.setCenterWidget(center);


		blc = new BorderLayoutContainer();
		BorderLayoutData codeLayoutData = new BorderLayoutData(.5);
		codeLayoutData.setCollapseMini(true);
		codeLayoutData.setMaxSize(10000);
		codeLayoutData.setSplit(true);
		codeLayoutData.setCollapsible(true);
		codeLayoutData.setMargins(new Margins(0, 8, 0, 0));
		codeLayoutData.setFloatable(false);
		ContentPanel codeWrap = new ContentPanel();
		codeWrap.setWidget(code);
		codeWrap.setHeaderVisible(false);
		blc.setWestWidget(codeWrap, codeLayoutData);

		example.setHeadingText("Output");
//		east.addTool(new ToolButton(ToolButton.REFRESH, new SelectHandler() {
//			@Override
//			public void onSelect(SelectEvent event) {
//				getPresenter().compile();
//			}
//		}));
		setCurrentCompiled(null, null);
		blc.setCenterWidget(example);

		initWidget(blc);

		driver.initialize(this);
	}

	@Override
	public AcceptsView getCodeEditorSlot() {
		return codeEditor;
	}

	@Override
	public SimpleBeanEditorDriver<Project, ?> getDriver() {
		return driver;
	}

	@Override
	public void setDelegate(EditorDelegate<Project> delegate) {
		//no-op
	}

	@Override
	public void flush() {
		//no-op
	}

	@Override
	public void onPropertyChange(String... paths) {
		//no-op
	}

	@Override
	public void setValue(Project value) {
		paths.clear();
		for (String file : value.getFiles()) {
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
	public void setCurrentCompiled(String compiledId, String url) {
		this.lastCompiledId = compiledId;
		this.lastUrl = url;
		if (url != null) {
			example.setWidget(new Frame(url));
		} else {
			example.setWidget(new Label("Waiting for project to load, or project not yet compiled..."));
		}
		example.forceLayout();
		example.unmask();
	}

	@Override
	public void setActiveFile(String activeFile) {
		if (files.getStore().findModel(activeFile) != null) {
			files.getSelectionModel().select(activeFile, false);
		}
	}

	List<CompiledProjectStatus> statuses = Arrays.asList(
			CompiledProjectStatus.QUEUED,
			CompiledProjectStatus.ACCEPTED,
			CompiledProjectStatus.PRECOMPILING,
			CompiledProjectStatus.COMPILING,
			CompiledProjectStatus.LINKING,
			CompiledProjectStatus.COMPLETE);
	@Override
	public void showProgress(CompiledProjectStatus status) {
		int index = statuses.indexOf(status);
		if (index == -1) {
			//failed or stuck, draw error
			error.setVisible(true);
			progress.hide();
			((HasLayout) error.getParent()).forceLayout();
			return;
		}
		this.progress.show();
		((ToolBar) this.progress.getParent()).forceLayout();
		double progress = ((double) index) / 5.0;

		this.progress.updateProgress(progress, status.name());

//		if (status == CompiledProjectStatus.COMPLETE) {
//			blc.expand(LayoutRegion.EAST);
//		}
	}
}
