package ru.ivan.sqlcmd.controller.command;

import ru.ivan.sqlcmd.model.DatabaseManager;
import ru.ivan.sqlcmd.view.View;

/**
 * Created by Ivan on 21.09.2016.
 */
public class Disconnect extends Command {

    private DatabaseManager manager;
    private View view;

    @Override
    public String description() {
        return "disconnect from current database";
    }

    @Override
    public String format() {
        return "disconnect";
    }

    public Disconnect() {
    }

    public Disconnect(DatabaseManager manager, View view) {
        this.manager = manager;
        this.view = view;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith("disconnect");
    }

    @Override
    public void process(String command) {
        manager.disconnect();
        view.write("Отключение успешно");
    }

}
