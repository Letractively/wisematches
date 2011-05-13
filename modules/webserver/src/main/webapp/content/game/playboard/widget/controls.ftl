<#-- @ftlvariable name="board" type="wisematches.playground.scribble.ScribbleBoard" -->

<#include "/core.ftl">

<div id="boardActionsToolbar" class="ui-widget-content ui-corner-all" style="border-top: 0" align="center">
    <div>
        <button id="makeTurnButton" onclick="boardActionsToolbar.makeTurn()">
        <@message code="game.play.make"/>
        </button>
        <button id="clearSelectionButton" onclick="board.clearSelection()">
        <@message code="game.play.clear"/>
        </button>
        <button id="exchangeTilesButton" onclick="boardActionsToolbar.exchangeTiles()">
        <@message code="game.play.exchange"/>
        </button>
    </div>
    <div>
        <button id="passTurnButton" onclick="boardActionsToolbar.passTurn()">
        <@message code="game.play.pass"/>
        </button>
        <button id="resignGameButton" onclick="boardActionsToolbar.resignGame()">
        <@message code="game.play.resign"/>
        </button>
    </div>
</div>

<div id="exchangeTilesPanel" style="display: none;">
    <div><@message code="game.play.exchange.description"/></div>
    <div style="height: 16px; position: relative;"></div>
</div>

<script type="text/javascript">
    var boardActionsToolbar = new wm.scribble.Controls(board, {
                "acceptedLabel": "<@message code="game.move.accepted.label"/>",
                "acceptedDescription": "<@message code="game.move.accepted.description"/>",
                "updatedLabel": "<@message code="game.move.updated.label"/>",
                "updatedYour": "<@message code="game.move.updated.you"/>",
                "updatedOther": "<@message code="game.move.updated.other"/>",
                "finishedLabel": "<@message code="game.move.finished.label"/>",
                "finishedInterrupted": "<@message code="game.move.finished.interrupted"/>",
                "finishedDrew": "<@message code="game.move.finished.drew"/>",
                "finishedWon": "<@message code="game.move.finished.won"/>",
                "clickToClose": "<@message code="game.move.clickToClose"/>",
                "passLabel": "<@message code="game.move.pass.label"/>",
                "passDescription": "<@message code="game.move.pass.description"/>",
                "resignLabel": "<@message code="game.move.resign.label"/>",
                "resignDescription": "<@message code="game.move.resign.description"/>",
                "exchange": "<@message code="game.play.exchange.label"/>",
                "cancel": "<@message code="cancel.label"/>"
            });
</script>