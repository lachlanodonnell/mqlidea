package ru.investflow.mql.runconfig.ui;

import com.intellij.execution.configuration.BrowseModuleValueActionListener;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mql.MQL4FileType;
import ru.investflow.mql.runconfig.MQL4RunCompilerConfiguration;
import ru.investflow.mql.sdk.MQL4SdkType;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MQL4CompilerRunnerEditor extends SettingsEditor<MQL4RunCompilerConfiguration> {
    private JPanel rootPanel;
    private TextFieldWithBrowseButton fileField;
    private MQL4SDKComboBox sdkComboBox;
    private JTextField buildEncodingField;
    private TextFieldWithBrowseButton buildDirField;

    public MQL4CompilerRunnerEditor(@NotNull Project project) {
        new MQL4FileSelector(project).setField(fileField);
    }

    @Override
    protected void resetEditorFrom(@NotNull MQL4RunCompilerConfiguration configuration) {
        Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
        List<Sdk> mql4Sdks = Arrays.stream(sdks).filter(s -> s.getSdkType() instanceof MQL4SdkType).collect(Collectors.toList());
        sdkComboBox.addItem(null);
        mql4Sdks.forEach(s -> sdkComboBox.addItem(s));

        Sdk selectedSdk;
        if (configuration.sdkName == null) {
            Project project = configuration.getProject();
            selectedSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        } else {
            selectedSdk = mql4Sdks.stream().filter(s -> s.getName().equals(configuration.sdkName)).findFirst().orElse(null);
        }
        sdkComboBox.setSelectedSdk(selectedSdk);
        fileField.setText(configuration.fileToCompile);
    }

    @Override
    protected void applyEditorTo(@NotNull MQL4RunCompilerConfiguration configuration) throws ConfigurationException {
        String selectedSdkName = sdkComboBox.getSelectedSdkName();
        configuration.sdkName = selectedSdkName == null ? "" : selectedSdkName;
        configuration.fileToCompile = fileField.getText();
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return rootPanel;
    }

    private static class MQL4FileSelector extends BrowseModuleValueActionListener<JTextField> {
        private final Project project;

        public MQL4FileSelector(Project project) {
            super(project);
            this.project = project;
        }

        @Nullable
        @Override
        protected String showDialog() {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(MQL4FileType.INSTANCE);
            descriptor.setTitle("Select MQL4 File…");
            descriptor.setRoots(project.getBaseDir());

            FileChooserDialog chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, project, null);
            VirtualFile[] selectedFiles = chooser.choose(project);
            if (selectedFiles.length == 0) {
                return null;
            }
            VirtualFile result = selectedFiles[0];
            String projectPath = project.getPresentableUrl();
            String resultPath = result.getPresentableUrl();
            if (projectPath != null && resultPath.startsWith(projectPath)) {
                return resultPath.substring(projectPath.length() + 1);
            }
            return resultPath;
        }
    }
}