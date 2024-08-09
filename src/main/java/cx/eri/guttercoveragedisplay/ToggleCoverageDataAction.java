package cx.eri.guttercoveragedisplay;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ToggleCoverageDataAction extends AnAction {
    private static final String RELATIVE_COVERAGE_DATA_FILE_PATH_DEFAULT = "deploy/next-gen/config/coverage-data-aggregate.json";
    private static final String RELATIVE_COVERAGE_DATA_FILE_PATH_RELEASE = "deploy/next-gen/config/coverage-data-aggregate-release.json";
    private static final String TARGET_REPOSITORY_URL = "git@github.com:techops-e2ecs/psa-sfdx.git";
    private final Map<VirtualFile, Map<Integer, RangeHighlighter>> fileHighlighters = new HashMap<>();
    private boolean isCoverageDataDisplayed = false;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        if (!isTargetRepository(project)) {
            Messages.showMessageDialog("This plugin only works with the repository: " + TARGET_REPOSITORY_URL, "Warning", Messages.getWarningIcon());
            return;
        }

        String branchName = getCurrentBranchName(project);
        if (branchName == null) {
            Messages.showMessageDialog("Failed to get the current branch name", "Error", Messages.getErrorIcon());
            return;
        }

        String coverageDataFilePath = project.getBasePath() + File.separator + getCoverageDataFilePath(branchName);

        if (isCoverageDataDisplayed) {
            removeCoverageData();
        } else {
            displayCoverageData(project, coverageDataFilePath);
        }

        isCoverageDataDisplayed = !isCoverageDataDisplayed;
    }

    private boolean isTargetRepository(Project project) {
        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
        for (GitRepository repository : manager.getRepositories()) {
            if (repository.getRemotes().stream().anyMatch(remote -> TARGET_REPOSITORY_URL.equals(remote.getFirstUrl()))) {
                return true;
            }
        }
        return false;
    }

    private String getCurrentBranchName(Project project) {
        GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
        for (GitRepository repository : manager.getRepositories()) {
            if (repository.getRemotes().stream().anyMatch(remote -> TARGET_REPOSITORY_URL.equals(remote.getFirstUrl()))) {
                return repository.getCurrentBranchName();
            }
        }
        return null;
    }

    private String getCoverageDataFilePath(String branchName) {
        if (branchName.startsWith("release")) {
            return RELATIVE_COVERAGE_DATA_FILE_PATH_RELEASE;
        } else {
            return RELATIVE_COVERAGE_DATA_FILE_PATH_DEFAULT;
        }
    }

    private void displayCoverageData(Project project, String coverageDataFilePath) {
        Map<String, CoverageData> coverageDataMap = CoverageDataReader.readCoverageData(coverageDataFilePath);
        if (coverageDataMap == null) {
            Messages.showMessageDialog("Failed to read coverage data", "Error", Messages.getErrorIcon());
            return;
        }

        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            Messages.showMessageDialog("Failed to get project base path", "Error", Messages.getErrorIcon());
            return;
        }

        VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(projectBasePath);
        if (projectDir == null) {
            Messages.showMessageDialog("Failed to get project directory", "Error", Messages.getErrorIcon());
            return;
        }

        for (Map.Entry<String, CoverageData> entry : coverageDataMap.entrySet()) {
            String className = entry.getKey();
            CoverageData coverageData = entry.getValue();

            VirtualFile classFile = findClassFile(projectDir, className + ".cls");
            if (classFile != null) {
                displayCoverageDataForFile(classFile, coverageData);
            }
        }

        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                CoverageData coverageData = coverageDataMap.get(file.getNameWithoutExtension());
                if (coverageData != null) {
                    displayCoverageDataForFile(file, coverageData);
                }
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                removeCoverageDataForFile(file);
            }
        });
    }

    private void removeCoverageData() {
        for (Map.Entry<VirtualFile, Map<Integer, RangeHighlighter>> entry : fileHighlighters.entrySet()) {
            VirtualFile file = entry.getKey();
            Map<Integer, RangeHighlighter> highlighters = entry.getValue();

            Editor[] editors = EditorFactory.getInstance().getEditors(Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(file)));
            if (editors.length == 0) {
                continue;
            }

            Editor editor = editors[0];
            MarkupModel markupModel = editor.getMarkupModel();

            for (RangeHighlighter highlighter : highlighters.values()) {
                markupModel.removeHighlighter(highlighter);
            }
        }
        fileHighlighters.clear();
    }

    private VirtualFile findClassFile(VirtualFile dir, String fileName) {
        for (VirtualFile file : dir.getChildren()) {
            if (file.isDirectory()) {
                VirtualFile result = findClassFile(file, fileName);
                if (result != null) {
                    return result;
                }
            } else if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    private void displayCoverageDataForFile(VirtualFile classFile, CoverageData coverageData) {
        Editor[] editors = EditorFactory.getInstance().getEditors(Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(classFile)));
        if (editors.length == 0) {
            return;
        }

        Editor editor = editors[0];
        MarkupModel markupModel = editor.getMarkupModel();
        Map<Integer, RangeHighlighter> highlighters = new HashMap<>();

        for (int line : coverageData.getCoveredLines()) {
            RangeHighlighter highlighter = addLineHighlighter(markupModel, line - 1, new JBColor(new Color(0, 255, 0, 128), new Color(0, 255, 0, 128)));
            highlighters.put(line, highlighter);
        }

        for (int line : coverageData.getUncoveredLines()) {
            RangeHighlighter highlighter = addLineHighlighter(markupModel, line - 1, new JBColor(new Color(255, 0, 0, 128), new Color(255, 0, 0, 128)));
            highlighters.put(line, highlighter);
        }

        fileHighlighters.put(classFile, highlighters);
    }

    private void removeCoverageDataForFile(VirtualFile classFile) {
        Map<Integer, RangeHighlighter> highlighters = fileHighlighters.get(classFile);
        if (highlighters == null) {
            return;
        }

        Editor[] editors = EditorFactory.getInstance().getEditors(Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(classFile)));
        if (editors.length == 0) {
            return;
        }

        Editor editor = editors[0];
        MarkupModel markupModel = editor.getMarkupModel();

        for (RangeHighlighter highlighter : highlighters.values()) {
            markupModel.removeHighlighter(highlighter);
        }

        fileHighlighters.remove(classFile);
    }

    private RangeHighlighter addLineHighlighter(MarkupModel markupModel, int line, Color color) {
        RangeHighlighter highlighter = markupModel.addLineHighlighter(line, HighlighterLayer.ADDITIONAL_SYNTAX, null);
        highlighter.setErrorStripeMarkColor(color);
        highlighter.setErrorStripeTooltip("Coverage info");
        highlighter.setLineMarkerRenderer((editor, graphics, rectangle) -> {
            graphics.setColor(color);
            int gutterWidth = editor.getContentComponent().getWidth();
            graphics.fillRect(0, rectangle.y, gutterWidth, rectangle.height);
        });
        return highlighter;
    }
}
