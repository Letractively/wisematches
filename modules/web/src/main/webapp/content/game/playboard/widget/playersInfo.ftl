<#-- @ftlvariable name="board" type="wisematches.server.gameplaying.scribble.board.ScribbleBoard" -->
<#include "/core.ftl">

<@wm.widget id="playersInfo" title="Players Info">
<table cellpadding="5">
    <#list board.playersHands as hand>
        <#assign p = playerManager.getPlayer(hand.getPlayerId())/>
        <#assign active=(board.getPlayerTurn() == hand)/>
        <#assign playerStyle="ui-state-active"/>
        <tr id="playerInfo${hand.getPlayerId()}" class="playerInfo <#if !active>passive</#if>">
            <td class="player-icon ui-corner-left ${playerStyle} ui-table-left">
                <img align="top" src="/resources/images/player/noPlayerIcon.png" width="31" height="28" alt=""/>
            </td>
            <td class="player-name ${playerStyle} ui-table-middle"><@wm.player player=p showRating=false/></td>
            <td class="player-points ${playerStyle} ui-table-middle" align="center" width="40">${hand.getPoints()}</td>
            <td class="player-time ui-corner-right ${playerStyle} ui-table-right" align="left" width="60">3d 21m</td>
        </tr>
    </#list>
</table>

<script type="text/javascript">
    board.bind('playerMoved', function(event, move) {
        var v = $("#playerInfo" + move.playerTurn + " .player-points");
        v.text(parseInt(v.text()) + move.points);
        $("#playerInfo" + move.playerTurn).addClass("passive");
        $("#playerInfo" + move.nextPlayerTurn).removeClass("passive");
    });
</script>
</@wm.widget>
