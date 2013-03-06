<%@ page language="java" contentType="text/html;charset=UTF-8" info="TestMVC"%>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/transitional.dtd"><%
    String returnPath = request.getParameter(MVCConstants.return_path_parameter); %>
<html>
<%
    request.setAttribute("pageTitle", "Register");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container"><%
if ("true".equals(request.getAttribute(MVCConstants.registration_form_mode_attribute))) { %>
                <h2><messages:message packageName="fr.untitled2.bundle.views.profile" key="register.welcome" defaultValue="Welcome to the MyPictureLog project"/></h2>
                <p><messages:message packageName="fr.untitled2.bundle.views.profile" key="register.header" defaultValue="Before enjoying this wonderfull app you need to select the date time zone that is configured on all your photo cameras"/></p><%
} else { %>
                <h2><messages:message packageName="fr.untitled2.bundle.views.profile" key="profile.welcome" defaultValue="Hey !!! It seems to be your profile !! What's new ??"/></h2>
                <p><messages:message packageName="fr.untitled2.bundle.views.profile" key="profile.header" defaultValue="Look !!! It is so simple to update !! I'm sure you have moved so you change time zone of your camera ;)"/></p><%
} %>
                <div class="item rounded dark">
                    <div class="white" style="text-align: right;">
                        <form:form modelAttribute="<%=fr.untitled2.mvc.MVCConstants.registration_form_bean%>" method="POST" action="${pageContext.request.contextPath}/ihm/profile/validation" style="margin: 0 155px 0;">
                            <table style="border: 0;">
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.profile" key="camera.timezone" defaultValue="Select the time zone of your camera"/> : </td>
                                    <td style="border: 0;"><form:select path="dateTimeZone" items="${timeZoneList}" /></td>
                                </tr>
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.profile" key="dateformat" defaultValue="Select the date format you currently use"/> : </td>
                                    <td style="border: 0;"><form:select path="dateFormat" items="${dateFormatList}" /></td>
                                </tr>
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.profile" key="language" defaultValue="Select your prefered locale"/> : </td>
                                    <td style="border: 0;"><form:select path="userLocale" items="${supportedLocales}" /></td>
                                </tr>
                                <tr><%
    if ("true".equals(MVCConstants.registration_form_mode_attribute)) { %>
                                    <td colspan="2" style="border: 0;" align="center"><input type="submit" value="<messages:message packageName="fr.untitled2.bundle.views.profile" key="register.button" defaultValue="Register !!"/>" /></td><%
    } else { %>
                                    <td colspan="2" style="border: 0;" align="center"><input type="submit" value="<messages:message packageName="fr.untitled2.bundle.views.profile" key="profile.button" defaultValue="Update !!"/>" /></td><%
    } %>
                                </tr>
                            </table><%
    if (returnPath != null) { %>
                            <input type="hidden" name="<%=MVCConstants.return_path_parameter%>" value="<%=returnPath%>"/><%
    } %>
                        </form:form>
                    </div>
                </div>
                <p><messages:message packageName="fr.untitled2.bundle.views.profile" key="camera.datetimezone.why" defaultValue="Why time zone of your camera ???? One of the features of this application is to set a correct date time for your pictures when you are on holidays even if you forgot to change the date and time of your camera"/></p>
                <p><messages:message packageName="fr.untitled2.bundle.views.profile" key="localeanddateformat.why" defaultValue="Locale and date format are used to make the user interface as user friendly as possible"/></p>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
