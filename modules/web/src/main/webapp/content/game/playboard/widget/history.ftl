<#include "/core.ftl">

<@wm.widget id="movesHistory" title="game.history.label">
<table width="100%">
    <thead>
    <tr>
        <th>#</th>
        <th><@message code="game.history.player"/></th>
        <th><@message code="game.history.word"/></th>
        <th><@message code="game.history.points"/></th>
    </tr>
    </thead>
    <tbody></tbody>
</table>

<script type="text/javascript">
    wm.scribble.history = new function() {
        var movesHistoryTable = $("#movesHistory table").dataTable({
            "bJQueryUI": true,
            "bSort": true,
            "bSortClasses": false,
            "aaSorting": [
                [0,'desc']
            ],
            "bPaginate": false,
            "sScrollY": "300px",
            "bStateSave": true,
            "sDom": 't'
        });

        this.addMoveToHistory = function(move) {
            var link = '';
            if (move.type == 'make') {
                var word = move.word;
                link = '<span class="moveMade"><a href="javascript: board.selectHistoryWord(' +
                        '{row: ' + word.position.row + ', column: ' + word.position.column +
                        ', direction: \'' + word.direction + '\', length: ' + word.tiles.length + '})">' +
                        word.text + '</a></span>';
            } else if (move.type == 'exchange') {
                link = '<span class="moveExchange"><@message code="game.history.exchange.label"/></span>';
            } else if (move.type == 'pass') {
                link = '<span class="movePassed"><@message code="game.history.passed.label"/></span>';
            }
            movesHistoryTable.fnAddData([1 + move.number, board.getPlayerInfo(move.player).nickname, link, move.points]);
        };
    };

    $.each(gameInfo.moves, function(i, move) {
        wm.scribble.history.addMoveToHistory(move)
    });
    board.bind('playerMoved', function(event, gameMove) {
        wm.scribble.history.addMoveToHistory(gameMove.move)
    });
</script>
</@wm.widget>