<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>SQLCmd</title>
    </head>
    <body>
        <form action="insertrow?table=${tableName}" method="post">
            <table>
                <c:forEach items="${columns}" var="column">
                    <tr>
                        <td>
                            ${column}
                        </td>
                        <td><input type="text" name=${column}></td>
                    </tr>
                </c:forEach>
                <tr>
                    <td></td>
                    <td><input type="submit" value="insert row"/></td>
                </tr>

            </table>
        </form>
        <%@include file="footer.jsp" %>
    </body>
</html>