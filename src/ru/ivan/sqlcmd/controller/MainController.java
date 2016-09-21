package ru.ivan.sqlcmd.controller;

import ru.ivan.sqlcmd.controller.command.Command;
import ru.ivan.sqlcmd.controller.command.Exit;
import ru.ivan.sqlcmd.controller.command.Help;
import ru.ivan.sqlcmd.controller.command.List;
import ru.ivan.sqlcmd.model.DataSet;
import ru.ivan.sqlcmd.model.DatabaseManager;
import ru.ivan.sqlcmd.view.View;

import java.util.Arrays;


/**
 * Created by Ivan on 20.09.2016.
 */
public class MainController {

    private final View view;
    private final DatabaseManager manager;
    private final java.util.List<Command> commands;

    public MainController(View view, DatabaseManager manager) {
        this.manager = manager;
        this.view = view;
        this.commands = Arrays.asList(new Exit(view), new Help(view), new List(manager, view));
    }

    public void main(String[] args) {
        connectToDB();
    }

    public void run() {
        connectToDB();

        while (true) {
            String command = readCommand();
            if (commands.get(2).canProcess(command)) {
                commands.get(2).process(command);
            } else if (commands.get(1).canProcess(command)) {
                commands.get(1).process(command);
            } else if (command.startsWith("find")) {
                doFind(command);
            } else if (commands.get(0).canProcess(command)) {
                commands.get(0).process(command);
            } else {
                view.write("Такая команда отсутствует=" + command);
            }

        }

    }

    private void doFind(String command) {
        String[] data = command.split("[|]");
        String table = data[1];
        java.util.List tableData = manager.getTableData(table);
        java.util.List tableHeaders = manager.getTableColumns(table);
        printHeader(tableHeaders);
        printTable(tableData);

    }

    private void printTable(java.util.List<DataSet> tableData) {
        for (DataSet row : tableData
                ) {
            printRow(row);
        }


    }

    private void printRow(DataSet row) {
        java.util.List values = row.getValues();
        String string = "";
        for (Object column : values
                ) {
            string += column + "\t" + "|";
        }
        view.write(string);
    }

    private void printHeader(java.util.List<String> tableHeaders) {
        String header = "";
        for (String column : tableHeaders
                ) {
            header += column + "\t" + "|";
        }
        view.write(header);
    }


    private String readCommand() {
        view.write("Введите команду или help для помощи");
        return view.read();
    }

    private void connectToDB() {
        view.write("Привет, юзер");
        while (true) {
            view.write("Введи имя базы данных, имя пользователя и пароль в формате" +
                    " database|user|password");
            try {
                String string = view.read();
                string = "sqlcmd|postgres|postgres";
                String[] data = string.split("[|]");
                if (data.length != 3) {
                    throw new IllegalArgumentException("Количество параметров - " + data.length + ". Ожидается - 3");
                }
                String database = data[0];
                String user = data[1];
                String password = data[2];
                manager.connect(database, user, password);
                view.write("Подключение успешно");
                break;
            } catch (Exception e) {
                printError(e);

            }

        }
    }

    private void printError(Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null) {
            message = message + " " + e.getCause().getMessage();
        }
        view.write("Неудача по причине: " + message);
        view.write("Повтори попытку");
    }
}
