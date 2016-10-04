package ru.ivan.sqlcmd.controller.command;

import ru.ivan.sqlcmd.controller.command.Command;
import ru.ivan.sqlcmd.controller.command.Exit;
import ru.ivan.sqlcmd.controller.command.ExitException;
import org.junit.Test;
import org.mockito.Mockito;
import ru.ivan.sqlcmd.view.View;

import static org.junit.Assert.*;


public class ExitWithMockitoTest {

    private View view= Mockito.mock(View.class);

    @Test
    public void testCanProcessExitString() {
        //given
        Command command =new Exit(view);

        //whrn
        Boolean canProcess=command.canProcess("exit");

        assertTrue(canProcess);
    }

    @Test
    public void testCanProcessQweString() {
        //given
        Command command =new Exit(view);

        //whrn
        Boolean canProcess=command.canProcess("qwe");

        assertFalse(canProcess);
    }


    @Test
    public void testCanProcessExitCommand_ThrowsExitException() {
        //given
        Command command =new Exit(view);

        //whrn
        try {
            command.process("exit");
            fail("Expected ExitException");
        } catch (ExitException e) {

        }

        Mockito.verify(view).write("До скорой встречи!");

    }
}
