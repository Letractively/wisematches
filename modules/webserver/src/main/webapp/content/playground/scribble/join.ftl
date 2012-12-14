<#-- @ftlvariable name="waitingGames" type="wisematches.server.web.controllers.playground.scribble.view.WaitingGamesView" -->

<#include "/core.ftl">

<@wm.ui.table.dtinit/>

<div id="join-game">
<@wm.ui.playground id="waitingGamesWidget">
    <@wm.ui.table.header>
        <@message code="game.menu.games.label"/> > <@message code="game.join.label"/>
    </@wm.ui.table.header>

    <@wm.ui.table.toolbar>
        <div>
            <a href="/playground/scribble/create"><@message code="game.create.label"/></a>
        </div>
    </@wm.ui.table.toolbar>

    <@wm.ui.table.content>
        <table id="gameboard" width="100%" class="display">
            <thead>
            <tr>
                <th><@message code="game.title.label"/></th>
                <th><@message code="game.language.label"/></th>
                <th><@message code="game.time.label"/></th>
                <th><@message code="game.opponents.label"/></th>
                <th><@message code="game.join.action.label"/></th>
            </tr>
            </thead>
            <tbody>
                <#list waitingGames.proposalViews as view>
                    <#assign settings=view.proposal.settings/>
                <tr>
                    <td>${settings.title}</td>
                    <td><@message code="language.${settings.language}"/></td>
                    <td align="center">${gameMessageSource.formatTimeMinutes(settings.daysPerMove*24*60,locale)}</td>
                    <td>
                        <#list view.proposal.players as p>
                        <div>
                            <#if p??>
                                <@wm.player.name player=playerManager.getPlayer(p)/>
                            <#else>
                                <span class="player"><span
                                        class="waiting"><@message code="game.status.waiting"/></span></span>
                            </div>
                            </#if>
                        </#list>
                    </td>
                    <td class="center">
                        <#if view.violations?? && !view.violations.empty>
                            <#list view.violations as violation>
                                <div class="game-join-error">${gameMessageSource.formatViolation(violation, locale, true)}</div>
                            </#list>
                        <#else>
                            <#if view.proposal.proposalType == "CHALLENGE">
                                <a href="#accept"
                                   onclick="join.accept(${view.proposal.id}); return false;">&raquo; <@message code="game.join.accept.label"/></a>
                                <br>
                                <a href="#decline"
                                   onclick="join.decline(${view.proposal.id}); return false;">&raquo; <@message code="game.join.decline.label"/></a>
                            <#else>
                                <a href="#join"
                                   onclick="join.accept(${view.proposal.id}); return false;">&raquo; <@message code="game.join.join.label"/></a>
                            </#if>
                        </#if>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
    </@wm.ui.table.content>

    <@wm.ui.table.footer>
        <#if waitingGames.globalViolation??>
            <div class="ui-state-error-text">
            ${gameMessageSource.formatViolation(waitingGames.globalViolation, locale, false)}
            </div>
        </#if>
    </@wm.ui.table.footer>
</@wm.ui.playground>

    <script type="text/javascript">
        var join = new wm.game.Join({
            "accepting": "<@message code="game.join.accepting"/>",
            "declining": "<@message code="game.join.declining"/>",
            "sEmptyTable": "<@message code="game.gameboard.empty" args=["/playground/scribble/create"]/>"
        });
    </script>
</div>