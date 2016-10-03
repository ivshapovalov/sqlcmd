package ru.ivan.sqlcmd.controller.command;

import ru.ivan.sqlcmd.model.DatabaseManager;
import ru.ivan.sqlcmd.view.View;

import java.util.LinkedHashMap;
import java.util.Map;


public class UpdateRow extends Command {

    private DatabaseManager manager;
    private View view;

    public UpdateRow() {
    }

    public UpdateRow(DatabaseManager manager, View view) {
        this.manager=manager;

        this.view=view;
    }
    @Override
    public String description() {
        return "update the entry in the table using the ID";
    }

    @Override
    public String format() {
        return "updateRow|tableName|ID";
    }
    @Override
    public boolean canProcess(String command) {
        return command.startsWith("insertRow|");
    }

    @Override
    public void process(String command) {
        String[] data=command.split("[|]");
        if (data.length%2==0) {
            throw new IllegalArgumentException("Должно быть нечетное количество параметров " +
                    "в формате updateRow|tableName|ID|column1|value1|column2|value2|...|columnN|valueN");

        }
        String tableName=data[1];
        int id=Integer.parseInt(data[2]);

        Map<String, Object> tableData = new LinkedHashMap<>();
        for (int i = 1; i < data.length/2-1; i++) {
            String column=data[i*2+1];
            String value=data[i*2+2];
            tableData.put(column,value);
        }
        try {
            manager.updateRow(tableName, id,tableData);
        } catch (IllegalArgumentException e) {
            String originalMessage = e.getMessage();
            String newMessage="";
            if (originalMessage.contains("отношение")) {
                newMessage=originalMessage.replace("отношение","таблица");
            } else if (originalMessage.contains("столбец")) {
                newMessage=originalMessage.replace("столбец","поле");
            }
            throw new IllegalArgumentException(newMessage);
        }

        view.write(String.format("В таблице '%s' успешно создана запись %s",tableName,tableData ));



    }
}
