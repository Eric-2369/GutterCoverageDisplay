package cx.eri.guttercoveragedisplay;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ShowMessageAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Messages.showMessageDialog("Hello from your plugin!", "Greeting", Messages.getInformationIcon());
    }
}
