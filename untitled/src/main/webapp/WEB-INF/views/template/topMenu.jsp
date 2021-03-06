<%@ page import="fr.untitled2.utils.UserUtils" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@taglib prefix="servers" uri="/WEB-INF/servers.tld"%>
<div id="topMenu">
<%
if (UserUtils.isConnected()) {
    pageContext.setAttribute("user", UserUtils.getGoogleUser()); %>
    <div class="menuItem">
        <a href="/ihm/"><img src="/images/icons/black/house.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="home" defaultValue="Home"/>"/></a>
    </div>
    <div class="menuItem">
        <a href="/ihm/logs"><img src="/images/icons/black/logs.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="logs" defaultValue="Logs"/>"/></a>
    </div>
    <div class="menuItem">
        <a href="/ihm/journeys/list"><img src="/images/icons/black/logs.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="logs" defaultValue="Logs"/>"/></a>
    </div>
    <div class="menuItem">
        <a href="/ihm/images"><img src="/images/icons/black/photos.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="images" defaultValue="Images"/>"/></a>
    </div>
    <div class="menuItem">
        <a href="/ihm/maps"><img src="/images/icons/black/share.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="maps" defaultValue="Maps"/>"/></a>
    </div>
    <servers:servers href="/ihm/servers/list" imgSrc="/images/icons/black/server.png" messageResourceBundleKey="servers" packageName="fr.untitled2.bundle.template.topMenu" defaultValue="Servers"/>
    <div class="menuItem">
        <a href="https://sites.google.com/a/mypicturelog.com/howto"><img src="/images/icons/black/help.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="help" defaultValue="Help"/>"/></a>
    </div>
    <div class="loginInfos">
        <a href="<%=UserUtils.getProfileUrl(request)%>"><%=UserUtils.getUserNickName()%>!</a> <messages:message packageName="fr.untitled2.bundle.template.topMenu" key="signout" defaultValue="(You can <a href='/ihm/logout.htm'>sign out</a>)"/>
    </div>
<%
} else { %>
    <div class="menuItem">
        <a href="/ihm/"><img src="/images/icons/black/house.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.template.topMenu" key="home" defaultValue="Home"/>"/></a>
    </div>
    <div class="loginInfos">
        <a href="<%=UserUtils.createLoginUrl(request)%>"><messages:message packageName="fr.untitled2.bundle.template.topMenu" key="start" defaultValue="Start using now !!"/></a>
    </div><%
} %>
</div>