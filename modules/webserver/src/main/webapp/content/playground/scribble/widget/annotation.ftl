<#-- @ftlvariable name="board" type="wisematches.playground.scribble.ScribbleBoard" -->

<#include "/core.ftl">

<div class="annotation ui-widget">
    <div class="header ui-widget-header ui-corner-all shadow">
        <div class="controls">
            <div class="ui-helper-hidden">
                <a href="#" onclick="comments.create(); return false;"><@message code="game.comment.new"/></a>
            </div>
            <div class="loading-image" style="width: 150px">&nbsp;</div>
        </div>
        <div class="label"><b><@message code="game.comment.label"/></b></div>
    </div>

    <div class="editor ui-widget-content ui-corner-all shadow ui-helper-hidden">
        <div>
            <label><textarea style="width:  100%; height: 100px;"></textarea></label>
        </div>

        <table width="100%">
            <tr>
                <td width="100%" align="left" valign="top">
                    <div class="ui-state-error-text"></div>
                </td>
                <td nowrap="nowrap" align="right" valign="top">
                    <button onclick="comments.save(); return false;"><@message code="game.comment.submit"/></button>
                    <button onclick="comments.cancel(); return false;"><@message code="game.comment.cancel"/></button>
                </td>
            </tr>
        </table>
    </div>

    <div class="content ui-helper-hidden">
        <div class="items ui-widget-content ui-corner-all shadow"></div>

        <div class="status ui-widget-content ui-corner-all ui-state-default shadow">
            <div class="controls">
                <a href='#' onclick="comments.load(); return false;">&raquo; load older <span>0</span></a>
            </div>
            <div class="progress">qwe</div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var comments = new wm.scribble.Comments(board, {
        "ago": "<@message code='game.comment.ago'/>",
        "of": "<@message code='separator.of'/>"
    });
</script>