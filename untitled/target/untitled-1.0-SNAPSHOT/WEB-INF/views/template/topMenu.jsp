<%@ page import="fr.untitled2.utils.UserUtils" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<div id="topMenu">
<%
if (UserUtils.isConnected()) {
    pageContext.setAttribute("user", UserUtils.getGoogleUser()); %>
    <div class="menuItem">
        <a href="/ihm/"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="home" defaultValue="Home"/></a>
    </div>
        &nbsp;|&nbsp;
    <div class="menuItem">
        <a href="/ihm/logs"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="logs" defaultValue="Logs"/></a>
    </div>
    <div class="menuItem">
        &nbsp;|&nbsp;
    </div>
    <div class="menuItem">
        <a href="/ihm/images"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="images" defaultValue="Images"/></a>
    </div>
    <div class="menuItem">
        &nbsp;|&nbsp;
    </div>
    <div class="menuItem">
        <a href="/ihm/maps"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="maps" defaultValue="Maps"/></a>
    </div>
        &nbsp;|&nbsp;
    <div class="menuItem">
        <a href="https://sites.google.com/a/mypicturelog.com/howto"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="help" defaultValue="Help"/></a>
    </div>
    <div class="loginInfos">
        <a href="<%=UserUtils.getProfileUrl(request)%>"><%=UserUtils.getUserNickName()%>!</a> <messages:message packageName="fr.untitled2.bundle.template.topMenu" key="signout" defaultValue="(You can <a href='/ihm/logout.htm'>sign out</a>)"/>
    </div>
<%
} else { %>
    <div class="menuItem">
        <a href="/ihm/"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="home" defaultValue="Home"/></a>
    </div>
    <div class="loginInfos">
        <a href="<%=UserUtils.createLoginUrl(request)%>"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="start" defaultValue="Start using now !!"/></a>
    </div><%
} %>
</div>