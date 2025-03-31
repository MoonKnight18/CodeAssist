package org.gradle.internal.logging.console;

import org.gradle.internal.logging.events.OutputEventListener;
import org.gradle.internal.logging.events.PromptOutputEvent;

public class UserInputConsoleRenderer extends AbstractUserInputRenderer {
    private final Console console;

    public UserInputConsoleRenderer(OutputEventListener delegate, Console console) {
        super(delegate);
        this.console = console;
    }

    @Override
    void startInput() {
        toggleBuildProgressAreaVisibility(false);
        flushConsole();
    }

    @Override
    void handlePrompt(PromptOutputEvent event) {
        event.render(console.getBuildOutputArea());
        flushConsole();
    }

    @Override
    void finishInput() {
        toggleBuildProgressAreaVisibility(true);
        flushConsole();
    }

    private void toggleBuildProgressAreaVisibility(boolean visible) {
        console.getBuildProgressArea().setVisible(visible);
    }

    private void flushConsole() {
        console.flush();
    }
}
