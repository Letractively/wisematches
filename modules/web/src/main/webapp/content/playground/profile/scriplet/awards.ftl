<#-- @ftlvariable name="player" type="wisematches.core.Member" -->
<#-- @ftlvariable name="awardsSummary" type="wisematches.server.services.award.AwardsSummary" -->

<#include "/core.ftl">


<#assign totalCount=0/>
<div class="awards">
<#assign medals=awardsSummary.getAwards(AwardType.MEDAL)!""/>
<#assign badges=awardsSummary.getAwards(AwardType.BADGE)!""/>
<#assign ribbons=awardsSummary.getAwards(AwardType.RIBBON)!""/>

<#if medals?has_content>
    <div class="awards-section">
        <div class="ui-state-hover awards-type">
            <@message code="awards.medals.label"/>
        </div>
        <div class="awards-set">
            <#list medals as d>
                <#list awardsSummary.getAwardWeights(d.name) as w>
                    <#assign totalCount=totalCount + 1/>
                    <div class="award medal">
                        <@wm.award.image d w/>

                        <p>${awardsSummary.getAwardsCount(d.name, w)}</p>
                    </div>
                </#list>
            </#list>
        </div>
    </div>
</#if>

<#if badges?has_content>
    <div class="awards-section">
        <div class="ui-state-hover awards-type">
            <@message code="awards.badges.label"/>
        </div>
        <div class="awards-set">
            <#list badges as d>
                <#assign totalCount=totalCount + 1/>
                <div class="award badge">
                    <@wm.award.image d awardsSummary.getHighestWeight(d.name)/>
                </div>
            </#list>
        </div>
    </div>
</#if>

<#if ribbons?has_content>
    <div class="awards-section">
        <div class="ui-state-hover awards-type">
            <@message code="awards.ribbons.label"/>
        </div>
        <div class="awards-set">
            <#list ribbons as d>
                <#assign totalCount=totalCount + 1/>
                <div class="award ribbon">
                    <@wm.award.image d ""/>
                </div>
            </#list>
        </div>
    </div>
</#if>

<#if totalCount != 0>
    <div style="text-align: right">
        <a href="/playground/profile/awards?p=${player.id?string}"><@message code="profile.awards.label"/></a>
    </div>
</#if>
</div>
