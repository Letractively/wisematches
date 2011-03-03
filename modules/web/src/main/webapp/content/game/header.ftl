<#-- @ftlvariable name="pageName" type="java.lang.String" -->
<#include "/core.ftl">

<script type="text/javascript">
    $(function() {
        $("#game-toolbar div").buttonset();
        var activeButton = $("#${pageName!}Button");
        if (activeButton != undefined) {
            var b = activeButton.button().addClass("ui-state-active");
            b.bind("mouseleave.button", function() {
                $(this).addClass("ui-state-active");
            });
            b.bind("mouseup.button", function() {
                $(this).addClass("ui-state-active");
            });
        }
    });
</script>

<table id="game-header">
    <tr>
        <td width="178px" height="72px">
            <img id="header-image" src="/resources/images/logo.png" width="178" height="72" alt="logo"/>
        </td>
        <td width="100%" height="72px">
            <table style="height: 100%; width: 100%">
                <tr>
                    <td valign="top" align="right">
                        <b><@security.authentication property="principal.nickname"/>
                            (<@security.authentication property="principal.username"/>)</b>
                        |
                        <a href="/account/logout.html">Logout</a>
                    </td>
                </tr>
                <tr>
                    <td valign="bottom">
                        <div id="game-toolbar" class="" align="right">
                            <div style="float: left;">
                                <button id="dashboardButton" onclick="wm.util.url.redirect('/game/dashboard.html')">
                                    My Games
                                </button>
                                <button id="gameboardButton" onclick="wm.util.url.redirect('/game/gameboard.html')">
                                    Join Game
                                </button>
                                <button id="createButton" onclick="wm.util.url.redirect('/game/create.html')">
                                    New Game
                                </button>
                            </div>
                            <div style="float: left;">
                                <button id="messagesButton" onclick="wm.util.url.redirect('/profile/messages.html')">
                                    Messages
                                </button>
                            </div>
                            <div style="float: left;">
                                <button id="tournamentsButton" onclick="wm.util.url.redirect('/game/tournaments.html')">
                                    Tournaments
                                </button>
                            </div>
                            <div>
                                <button id="modifyButton" onclick="wm.util.url.redirect('/account/modify.html')">
                                    Settings
                                </button>
                                <button onclick="wm.util.url.redirect('/info/general.html')">
                                    Help
                                </button>
                            </div>
                        </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>