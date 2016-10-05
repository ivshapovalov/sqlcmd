package ru.ivan.sqlcmd.controller.command;

import ru.ivan.sqlcmd.model.DatabaseManager;
import ru.ivan.sqlcmd.view.View;

public abstract class Command {
    protected DatabaseManager manager;
    protected View view;

    public abstract boolean canProcess(String command);

    public abstract void process(String command);

    public abstract String description();

    public abstract String format();

    public Command(View view) {
        this.view = view;
    }

    public Command() {
    }
}
