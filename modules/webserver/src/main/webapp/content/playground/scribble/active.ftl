<#-- @ftlvariable name="player" type="wisematches.personality.player.Player" -->
<#-- @ftlvariable name="board" type="wisematches.playground.scribble.ScribbleBoard" -->
<#-- @ftlvariable name="activeBoards" type="java.util.Collection<wisematches.playground.scribble.ScribbleBoard>" -->
<#-- @ftlvariable name="activeProposals" type="java.util.Collection<wisematches.server.playground.propose.GameProposal<wisematches.server.playground.scribble.ScribbleSettings>" -->
<#include "/core.ftl">

<#macro gameStatus board>
    <#if board.isGameActive()>
        <#if board.getPlayerTurn().getPlayerId() == principal.getId()>
        <span class="player"><a
                href="/playground/scribble/board?b=${board.getBoardId()}"><strong><@message code="game.status.move_you"/></strong></a></span>
        <#else>
            <@message code="game.status.move_opp" args=["${playerManager.getPlayer(board.getPlayerTurn().getPlayerId()).nickname!}"]/>
        </#if>
    </#if>
</#macro>

<@wm.jstable/>

<@wm.playground id="activeGamesWidget">
    <@wm.dtHeader>
        <#if player != principal>
            <@message code="game.player"/> <@wm.player player=player showState=true showType=true/>
        <#else><@message code="game.menu.games.label"/>
        </#if>
    > <@message code="game.dashboard.label"/>
    </@wm.dtHeader>

    <@wm.dtToolbar>
        <#if player == principal>
        <a href="/playground/scribble/history"><@message code="game.past.history.label"/></a>
        <#else>
        <a href="/playground/scribble/history?p=${player.id}"><@message code="game.past.history.label"/></a>
        </#if>
    </@wm.dtToolbar>

    <@wm.dtContent>
    <table id="dashboard" width="100%" class="display">
        <thead>
        <tr>
            <th width="100%"><@message code="game.title.label"/></th>
            <th><@message code="game.language.label"/></th>
            <th><@message code="game.status.label"/></th>
            <th><@message code="game.remained.label"/></th>
            <th><@message code="game.opponents.label"/></th>
            <th><@message code="game.scores.label"/></th>
        </tr>
        </thead>
        <tbody>
            <#list activeBoards as board>
            <tr id="board${board.boardId}">
                <#assign settings=board.gameSettings/>
                <td>
                    <a href="/playground/scribble/board?b=${board.boardId}">${settings.title}</a>
                </td>
                <td><@message code="language.${settings.language}"/></td>
                <td><@gameStatus board=board/></td>
                <td class="center">
                ${gameMessageSource.formatRemainedTime(board, locale)}
                </td>
                <td>
                    <#list board.playersHands as hand>
                        <div><@wm.player player=playerManager.getPlayer(hand.getPlayerId())/></div>
                    </#list>
                </td>
                <td class="center">
                    <#list board.playersHands as hand>
                        <div>${hand.points}</div>
                    </#list>
                </td>
            </tr>
            </#list>

            <#list activeProposals as proposal>
            <tr id="proposal${proposal.id}">
                <td>${proposal.gameSettings.title}</td>
                <td><@message code="language.${proposal.gameSettings.language}"/></td>
                <td>
                            <span class="player"><span
                                    class="waiting"><@message code="game.status.waiting"/></span></span>

                    <div style="text-align: right;">
                        <a href="decline?p=${proposal.id}"
                           onclick="cancelProposal(${proposal.id}); return false;">
                            <@message code="game.proposal.cancel"/>
                        </a>
                    </div>
                </td>
                <td class="center">
                ${gameMessageSource.formatMinutes(proposal.gameSettings.daysPerMove *24 * 60, locale)}
                </td>
                <td>
                    <#list proposal.players as p>
                    <div>
                        <#if p??>
                            <@wm.player player=playerManager.getPlayer(p)/>
                        <#else>
                            <span class="player"><span
                                    class="waiting"><@message code="game.status.waiting"/></span></span>
                        </div>
                        </#if>
                    </#list>
                </td>
                <td class="center">
                    <#list proposal.players as p>
                        <div><#if p??>0<#else>-</#if></div>
                    </#list>
                    <div>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
    </@wm.dtContent>

    <@wm.dtFooter/>
</@wm.playground>

<script type="text/javascript">
    wm.ui.dataTable('#dashboard', {
        "bStateSave":true,
        "bFilter":false,
        "bSortClasses":false,
        "aaSorting":[
            [3, 'asc']
        ],
        "aoColumns":[
            null,
            null,
            null,
            null,
            { "bSortable":false },
            { "bSortable":false }
        ],
        "oLanguage":{
        <#if player == principal>
            "sEmptyTable":"<@message code="game.dashboard.empty" args=['/playground/scribble/create', '/playground/scribble/join']/>"
        </#if>
        }
    });

    $(".data-table-toolbar div").buttonset();

    function cancelProposal(id) {
        $.ajax('decline.ajax?p=' + id, {
            success:function (data, textStatus, jqXHR) {
                if (data.success) {
                    $("#proposal" + id).fadeOut();
                    wm.ui.showStatus("<@message code="game.proposal.canceled"/>", false);
                } else {
                    wm.ui.showStatus("<@message code="game.proposal.cancel.error"/>", true);
                }
            }
        });
    }
</script>
