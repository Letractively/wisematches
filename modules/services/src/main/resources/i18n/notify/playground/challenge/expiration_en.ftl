<#-- @ftlvariable name="context.board" type="wisematches.playground.GameBoard" -->
<#-- @ftlvariable name="context.expirationType" type="wisematches.playground.propose.ProposalExpirationType" -->
<#import "../../utils.ftl" as util>

<p>You have unresolved challenge that will be cancelled in
    <strong><em>${messageSource.formatTimeMillis(context.expirationType.remainedTime, locale)}</em></strong>.</p>
<br>
<#include "initiated_en.ftl"/>