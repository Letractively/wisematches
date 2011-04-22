<#-- @ftlvariable name="activeBoards" type="java.util.Collection<wisematches.server.gameplaying.scribble.board.ScribbleBoard>" -->
<#-- @ftlvariable name="activeProposals" type="java.util.Collection<wisematches.server.gameplaying.scribble.proposal.ScribbleProposal>" -->
<#include "/core.ftl">

<script type="text/javascript">
    $(document).ready(function() {
        $("#refreshGameboard").button({icons: {primary: 'ui-icon-refresh'}});

        $('#gameboard').dataTable({
                    "bJQueryUI": true,
                    "bStateSave": true,
                    "bFilter": false,
                    "bSort": false,
                    "bSortClasses": false,
                    "sDom": '<"H"lCr>t<"F"ip>',
                    "sPaginationType": "full_numbers",
                    "oLanguage": {
                        "sEmptyTable": "<@message code="game.gameboard.empty" args=["/game/create.html"]/>"
                    }
                });
    });
</script>

<table width="100%">
    <tr>
        <td width="160px" valign="top">
        <#include "/content/ops/advertisement.ftl">
        </td>
        <td valign="top">
            <div style="float: right;">
                <button id="refreshGameboard" onclick="window.location.reload()">
                <@message code="refresh.label"/>
                </button>
            </div>

            <table id="gameboard" width="100%">
                <thead>
                <tr>
                    <th width="100%"><@message code="game.title.label"/></th>
                    <th><@message code="game.language.label"/></th>
                    <th><@message code="game.time.label"/></th>
                    <th><@message code="game.opponents.label"/></th>
                    <th><@message code="game.rating.label"/></th>
                    <th><@message code="game.join.label"/></th>
                </tr>
                </thead>
                <tbody>
                <#list activeProposals as proposal>
                <tr>
                    <td>${proposal.title}</td>
                    <td><@message code="language.${proposal.language}"/></td>
                    <td align="center">${gameMessageSource.formatMinutes(proposal.timeLimits*24*60,locale)}</td>
                    <td>
                        <#list proposal.allPlayers as pId>
                            <div>
                                <#if pId??>
                                    <#assign playerProposed=playerManager.getPlayer(pId)/>
                                <@wm.player player=playerProposed showRating=false/>
                                    <#else>
                                        <span class="player">
                                            <span class="waiting"><@message code="game.status.waiting"/></span>
                                        </span>
                                </#if>
                            </div>
                        </#list>
                    </td>
                    <td>
                        <#list proposal.allPlayers as pId>
                            <div>
                                <#if pId??><#assign playerProposed=playerManager.getPlayer(pId)/>${playerProposed.rating?string.computer}<#else>
                                    -</#if>
                            </div>
                        </#list>
                    </td>
                    <td class="center">
                        <#assign msg=proposal.getUnsuitableMessage(player)!""/>
                        <#if msg?has_content>
                            <#assign index=msg?index_of(" ")/>
                            <#if (index > 0)>
                            <@message code="game.join.err.${msg?substring(0, index)}" args=[msg?substring(index)]/>
                                <#else>
                                <@message code="game.join.err.${msg}"/>
                            </#if>
                            <#else>
                                <a href="/game/gameboard.html?join=${proposal.id}">&raquo; <@message code="game.join.label"/></a>
                        </#if>
                    </td>
                </tr>
                </#list>
                </tbody>
            </table>
        </td>
        <td width="160px" valign="top"></td>
    </tr>
</table>