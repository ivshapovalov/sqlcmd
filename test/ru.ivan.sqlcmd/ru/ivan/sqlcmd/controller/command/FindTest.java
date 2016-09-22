package ru.ivan.sqlcmd.controller.command;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import ru.ivan.sqlcmd.model.DataSet;
import ru.ivan.sqlcmd.model.DatabaseManager;
import ru.ivan.sqlcmd.view.View;

import java.util.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;

/**
 * Created by Ivan on 22.09.2016.
 */
public class FindTest {

    private DatabaseManager manager;
    private View view;

    @Before
    public void setup() {
        manager=Mockito.mock(DatabaseManager.class);
        view=Mockito.mock(View.class);
    }

    @Test
    public void testPrintTableData() {
        //given
        Command command =new Find(manager,view);
        Mockito.when(manager.getTableColumns("users"))
                .thenReturn(Arrays.asList("id","name","password"));

        DataSet user1=new DataSet();
        user1.put("id",12);
        user1.put("name","Eva");
        user1.put("password","*****");

        DataSet user2=new DataSet();
        user2.put("id",16);
        user2.put("name","Steve");
        user2.put("password","+++++");

        List<DataSet> data=Arrays.asList(user1,user2);
        Mockito.when(manager.getTableData("users"))
                .thenReturn(data);
        //when
        command.process("find|users");

        //then
        ArgumentCaptor<String> captor=ArgumentCaptor.forClass(String.class);
        Mockito.verify(view,atLeastOnce()).write(captor.capture());

        assertEquals("[id\t|name\t|password\t|," +
                " 12\t|Eva\t|*****\t|," +
                " 16\t|Steve\t|+++++\t|]",captor.getAllValues().toString());
    }

    @Test
    public void testCanProcessFindWithParametersString() {
        //given
        Command command =new Find(manager,view);

        //whrn
        Boolean canProcess=command.canProcess("find|users");

        assertTrue(canProcess);
    }

    @Test
    public void testCanProcessFindWithoutParametersString() {
        //given
        Command command =new Find(manager,view);

        //whrn
        Boolean canProcess=command.canProcess("find");

        assertFalse(canProcess);
    }



    @Test
    public void testCanProcessFindWithIllegalParametersString() {
        //given
        Command command =new Find(manager,view);

        //whrn
        Boolean canProcess=command.canProcess("qwe|users");

        assertFalse(canProcess);
    }

    @Test
    public void testPrintEmptyTableData() {
        //given
        Command command =new Find(manager,view);
        Mockito.when(manager.getTableColumns("users"))
                .thenReturn(Arrays.asList("id","name","password"));

        List<DataSet> data=new ArrayList<>();
        Mockito.when(manager.getTableData("users"))
                .thenReturn(data);
        //when
        command.process("find|users");

        //then
        ArgumentCaptor<String> captor=ArgumentCaptor.forClass(String.class);
        Mockito.verify(view,atLeastOnce()).write(captor.capture());

        assertEquals("[id\t|name\t|password\t|]",captor.getAllValues().toString());
    }

}
