<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.utils.StatisticsUtils" %>
<%@ page import="fr.untitled2.entities.Log" %>
<%@ page import="fr.untitled2.business.beans.LogList" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="fr.untitled2.entities.TrackPoint" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="userdate" uri="/WEB-INF/dateTimeLib.tld"%>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%
    request.setAttribute("pageTitle", "Server Connection");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <a href="<%=request.getAttribute("url")%>" target="blank">Click here to obtain your validation code</a>
                <form action="#" id="pushVerificationCode">
                    <input type="hidden" name="serverId" value="<%=request.getAttribute("serverId")%>" id="serverId"/>
                    Enter your verification code : <input type="text" name="verificationCode" id="verificationCode"/><input type="submit" value="Ok" id="verificationCodeInputSubmit"/>
                </form>
                <div id="status"></div>
                <script>
                    $("#verificationCodeInputSubmit").click(function() {

                        var url = "/api/server/pushVerificationCode";

                        var data = '{"serverId":"' + $("#serverId").val() + '", "verificationCode":"' + $("#verificationCode").val() + '"}';

                        $.ajax({
                               type: "POST",
                               url: url,
                               data: data,
                               success: function(data)
                               {
                                   if (data.state) {
                                        $("#status").append("Verification post has been submited");
                                   } else {
                                        $("#status").append("An error has occured");
                                   }
                               }
                             });

                        return false; // avoid to execute the actual submit of the form.
                    });
                </script>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
