<#include "/core.ftl">

<table class="settings-board ui-widget-content ui-state-default shadow ui-corner-all" style="background-image: none;"
       width="100%">
    <tr>
        <td style="padding-top: 4px; width: 10px;">
        <@wm.ui.input fieldType='checkbox' path="settings.cleanMemory"/>
        </td>
        <td>
        <#--@declare id="cleanMemory"-->
            <label for="cleanMemory"><@message code="game.settings.board.memory.label"/></label>

            <div class="sample"><@message code="game.settings.board.memory.description"/></div>
        </td>
    </tr>

    <tr>
        <td style="padding-top: 4px; width: 10px;">
        <@wm.ui.input fieldType='checkbox' path="settings.checkWords"/>
        </td>
        <td>
        <#--@declare id="checkWords"-->
            <label for="checkWords"><@message code="game.settings.board.words.label"/></label>

            <div class="sample"><@message code="game.settings.board.words.description"/></div>
        </td>
    </tr>

    <tr>
        <td style="padding-top: 4px; width: 10px;">
        <@wm.ui.input fieldType='checkbox' path="settings.clearByClick"/>
        </td>
        <td>
        <#--@declare id="clearByClick"-->
            <label for="clearByClick"><@message code="game.settings.board.clear.label"/></label>

            <div class="sample"><@message code="game.settings.board.clear.description"/></div>
        </td>
    </tr>

    <tr>
        <td style="padding-top: 4px; width: 10px;">
        <@wm.ui.input fieldType='checkbox' path="settings.showCaptions"/>
        </td>
        <td>
        <#--@declare id="showCaptions"-->
            <label for="showCaptions"><@message code="game.settings.board.captions.label"/></label>

            <div class="sample"><@message code="game.settings.board.captions.description"/></div>
        </td>
    </tr>


    <tr>
        <td style="padding-top: 4px; width: 10px;">
        <@wm.ui.input fieldType='checkbox' path="settings.enableShare"/>
        </td>
        <td>
        <#--@declare id="enableShare"-->
            <label for="enableShare"><@message code="game.settings.board.share.label"/></label>

            <div class="sample"><@message code="game.settings.board.share.description"/></div>
        </td>
    </tr>

    <tr>
        <td colspan="2">
        <#--@declare id="tilesClass"-->
            <label for="tilesClass"><@message code="game.settings.board.tiles.label"/>:</label>

        <@wm.ui.input fieldType='hidden' path="settings.tilesClass">
            <div class="tiles-set-container" style="padding-left: 10px">
                <div class="tiles-set-nav tiles-set-prev ui-state-default">
                    <div class="ui-icon ui-icon-arrow-1-w"></div>
                </div>

                <div class="tiles-set-view ui-state-default ${wm.ui.statusValue!"tiles-set-classic"}"
                     style="width: 242px; height: 44px; position: relative;">

                    <#list 0..10 as i>
                        <div class="tile"
                             style="top: 3px; left: ${3+i*22}px; background-position: -${22*i}px 0;"></div>
                        <div class="tile"
                             style="top: 25px; left: ${3+i*22}px; background-position: -${22*i}px -22px;"></div>
                    </#list>
                </div>
                <div class="tiles-set-nav tiles-set-next ui-state-default">
                    <div class="ui-icon ui-icon-arrow-1-e"></div>
                </div>
            </div>
        </@wm.ui.input>
        </td>
    </tr>
</table>

<script type="text/javascript">
    new wm.game.settings.Board();
</script>