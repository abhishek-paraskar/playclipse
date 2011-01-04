package org.playframework.playclipse.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class ClassWizardPage extends PlayWizardPage {

	private Button packageBrowse;

	protected boolean useJapid = true;
	
	protected Text packageText;

	protected abstract String defaultPackage();

	protected abstract String nameLabel();

	public ClassWizardPage(ISelection selection) {
		super(selection);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		Label label = new Label(container, SWT.NULL);
		label.setText("Source fol&der:");
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("Package name:");
		packageText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		packageText.setLayoutData(gd);
		packageText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		packageBrowse = new Button(container, SWT.PUSH);
		packageBrowse.setText("Browse...");
		packageBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handlePackageBrowse();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(nameLabel());
		name = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		name.setLayoutData(gd);
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		final Button useJapidButton = new Button(container, SWT.CHECK);
		useJapidButton.setText("Use Japid");
		useJapidButton.setSelection(true);
		useJapidButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				useJapid = useJapidButton.getSelection();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	@Override
	protected void initialize() {
		super.initialize();
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof ICompilationUnit) {
				obj = ((ICompilationUnit)obj).getParent();
			}
			if (obj instanceof IJavaElement) {
				packageText.setText(((IJavaElement)obj).getElementName());
			}
		}
		if (packageText.getText().isEmpty()) {
			packageText.setText(defaultPackage());
		}
		if (project == null) {
			packageBrowse.setEnabled(false);
		}
	}

	private void handlePackageBrowse() {
		IJavaProject javaProject = JavaCore.create(project);
		SelectionDialog dialog;
		try {
			dialog = JavaUI.createPackageDialog(getShell(), javaProject, 0);
			if (dialog.open() == SelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length == 1) {
					packageText.setText(((IPackageFragment) result[0]).getElementName());
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name.getText());
		result.put("container", containerText.getText());
		result.put("package", packageText.getText());
		return result;
	}

}
