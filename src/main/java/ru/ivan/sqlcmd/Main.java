package ru.ivan.sqlcmd;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ru.ivan.sqlcmd.controller.MainController;
import ru.ivan.sqlcmd.model.PostgreSQLManager;
import ru.ivan.sqlcmd.view.Console;

public class Main {

    public static void main(String[] args) {



        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        MainController controller = new
                MainController(new Console(), new PostgreSQLManager());
        controller.run();
    }
}
