<?xml version="1.0"?>
<!--
  ~  Copyright (c) 2018 European Molecular Biology Laboratory
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  -->
<!--
************************************************************
    XSL Stylesheet to transform URML to an HTML page.
************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:urml="http://uniprot.org/urml/rules">
    <xsl:output method="html" indent="yes" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*"/>
    <xsl:key name="kRuleByGroup" match="urml:rule" use="@group"/>

    <xsl:template name="all" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="urml:rules">
        <html>
            <head>
                <title>URML Viz</title>
                <link rel="stylesheet" href="../css/kickstart.css"/>
                <style type="text/css">
                    body {
                        font-family: Helvetica, Arial, sans-serif;
                    }

                    table {
                        color: #333;
                        table-layout:fixed;
                    }

                    td, th { border: 1px solid #CCC; height: 30px; word-wrap: break-word; }

                    ul, ol {
                        padding:0;
                        margin:0 0 20px 35px;
                    }

                    li {
                        padding:0px 0;
                        margin:0;
                    }
                </style>
            </head>
            <body>
                <xsl:apply-templates select="urml:rule[generate-id(.)=generate-id(key('kRuleByGroup',@group)[1])] | urml:rule[not(@group)]">
                    <xsl:sort select="@group" />
                    <xsl:sort select="@extends"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="urml:rule">
        <xsl:choose>
            <xsl:when test="@group">
                <fieldset>
                    <legend><h4><xsl:value-of select="@group"/></h4></legend>
                    <xsl:for-each select="key('kRuleByGroup', @group)">
                        <xsl:call-template name="displayRule">
                            <xsl:with-param name="rule" select="."/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fieldset>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="displayRule">
                    <xsl:with-param name="rule" select="."/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="displayRule">
        <xsl:param name="rule"/>
        <fieldset>
            <a name="{$rule/@id}"/>
            <legend>
                <h5><xsl:value-of select="$rule/@id"/></h5>
            </legend>
            <xsl:if test="$rule/@extends">
                <div>
                <h6>Extends:</h6>
                <a href="#{$rule/@extends}">
                    <xsl:value-of select="$rule/@extends"/>
                </a>
                </div>
            </xsl:if>
            <xsl:apply-templates/>
        </fieldset>
    </xsl:template>

    <xsl:template match="urml:meta">
        <div class="col_12 grid">
            <h6>Informations:</h6>
            <ul>
            <xsl:for-each select="urml:information">
                <li><xsl:value-of select="@type"/>: <xsl:value-of select="."/></li>
            </xsl:for-each>
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="urml:conditions">
        <div class="col_6 grid">
            <h6>Conditions:</h6>
            <table border="1">
                <tbody>
                    <xsl:apply-templates/>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="urml:actions">
        <div class="col_6 grid">
            <h6>Actions:</h6>
            <table border="1">
                <tbody>
                    <xsl:apply-templates/>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="urml:annotations">
        <div class="col_6 grid">
            <h6>Annotations:</h6>
            On: <xsl:value-of select="concat('$', @on)"/>
            <table border="1">
                <thead>
                    <th>Type</th><th>Value</th>
                </thead>
                <tbody>
                    <xsl:apply-templates/>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="urml:condition">
        <xsl:choose>
            <xsl:when test="not(preceding-sibling::*)">If </xsl:when>
            <xsl:otherwise>and </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="@bind">
            $<xsl:value-of select="@bind"/>:
        </xsl:if>
        <xsl:call-template name="displayFactClass">
            <xsl:with-param name="factClass" select="@on"/>
        </xsl:call-template>
        <xsl:if test="@of">
            of
            <xsl:call-template name="tokenize_fact_references">
                <xsl:with-param name="text" select="@of"/>
                <xsl:with-param name="newSeparator" select="' and'"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="@with">
            with
            <xsl:call-template name="tokenize_fact_references">
                <xsl:with-param name="text" select="@with"/>
                <xsl:with-param name="newSeparator" select="' and'"/>
            </xsl:call-template>
        </xsl:if>
        having:
        <ul>
            <xsl:apply-templates/>
        </ul>
    </xsl:template>

    <xsl:template match="urml:filter">
        <li>
            <xsl:if test="@negative = true()">
                NOT
            </xsl:if>
            <xsl:value-of select="@on"/>
            <xsl:choose>
                <xsl:when test="urml:field">
                    <ul>
                        <xsl:apply-templates select="urml:field"/>
                    </ul>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template match="urml:matches">
        matches <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="urml:range">
        in <xsl:value-of select="concat('[', @start, ',', @end, ']')"/>
    </xsl:template>

    <xsl:template match="urml:value" name="value">
        <xsl:if test="parent::urml:filter">
            =
        </xsl:if>
        <xsl:value-of select="."/>
        <xsl:if test="@description"> (<xsl:value-of select="@description"/>)</xsl:if>
    </xsl:template>

    <xsl:template match="urml:field">
        <li><xsl:value-of select="@attribute"/> = <xsl:value-of select="."/></li>
    </xsl:template>

    <xsl:template match="urml:contains">
        containing <xsl:value-of select="@operator"/> of:
            <ul>
                <xsl:for-each select="urml:value">
                    <li>
                        <xsl:call-template name="value"/>
                    </li>
                </xsl:for-each>
            </ul>
    </xsl:template>

    <xsl:template match="urml:AND">
        <tr>
            <td>
                <xsl:apply-templates/>
            </td>
        </tr>
        <xsl:if test="following-sibling::*">
        <tr>
            <td>OR</td>
        </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="urml:action">
        <tr>
            <td>
                <xsl:value-of select="@type"/> with
                <xsl:call-template name="tokenize_fact_references">
                    <xsl:with-param name="text" select="@with"/>
                    <xsl:with-param name="newSeparator" select="' and'"/>
                </xsl:call-template>:
                <ul>
                    <xsl:apply-templates/>
                </ul>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="urml:call">
        with: <br/>
        <a href="javadoc-url-of:{@uri}"><xsl:value-of select="@uri"/></a>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="urml:procedure">
        <a href="{.}"><xsl:value-of select="concat('#', .)"/></a>
    </xsl:template>

    <xsl:template match="urml:arguments/urml:argument">
        <xsl:if test="not(preceding-sibling::*)">(</xsl:if>
        <xsl:if test="@isReference">$</xsl:if>
        <xsl:value-of select="."/>
        <xsl:choose>
            <xsl:when test="following-sibling::*">, </xsl:when>
            <xsl:otherwise>)</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="urml:annotation">
        <tr>
            <td><xsl:value-of select="@type"/></td><td><xsl:value-of select="."/></td>
        </tr>
    </xsl:template>

    <xsl:template match="urml:fact">
        <li>
            <xsl:if test="@id">
                $<xsl:value-of select="@id"/>:
            </xsl:if>
            <xsl:call-template name="displayFactClass">
                <xsl:with-param name="factClass" select="@type"/>
            </xsl:call-template>
            <xsl:if test="@with">
                (
                <xsl:call-template name="tokenize_fact_references">
                    <xsl:with-param name="text" select="@with"/>
                    <xsl:with-param name="newSeparator" select="' and '"/>
                </xsl:call-template>
                )
            </xsl:if>
            <xsl:choose>
                <xsl:when test="urml:field">
                    <ul>
                        <xsl:apply-templates/>
                    </ul>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template name="displayFactClass">
        <xsl:param name="factClass"/>
        <xsl:variable name="fact">
            <xsl:value-of select="substring-after($factClass, ':')"/>
        </xsl:variable>
        <a href="../web/unirule-fact-model-doc.php#type_{$fact}">
            <xsl:value-of select="$fact"/>
        </a>
    </xsl:template>

    <xsl:template name="tokenize_fact_references">
        <xsl:param name="text" select="."/>
        <xsl:param name="separator" select="' '"/>
        <xsl:param name="newSeparator" select="' , '"/>
        <xsl:choose>
            <xsl:when test="not(contains($text, $separator))">
                <xsl:call-template name="normalizeToken">
                    <xsl:with-param name="text" select="$text"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="val" select="normalize-space(substring-before($text, $separator))"/>
                <xsl:call-template name="normalizeToken">
                    <xsl:with-param name="text" select="$val"/>
                </xsl:call-template>
                <xsl:value-of select="$newSeparator"/>
                <xsl:call-template name="tokenize_fact_references">
                    <xsl:with-param name="text" select="substring-after($text, $separator)"/>
                    <xsl:with-param name="newSeparator" select="$newSeparator"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="normalizeToken">
        <xsl:param name="text" select="."/>
        <xsl:choose>
            <xsl:when test="contains($text, ':')">
                <xsl:variable name="apos">'</xsl:variable>
                <xsl:variable name="value" select="normalize-space(substring-after($text, ':'))"/>
                <xsl:value-of select="concat(' ', normalize-space(substring-before($text, ':')), ':')"/>
                <xsl:if test="not(contains($value, $apos))">
                    <xsl:value-of select="'$'"/>
                </xsl:if>
                <xsl:value-of select="normalize-space(substring-after($text, ':'))"/>
            </xsl:when>
            <xsl:otherwise>
                $<xsl:value-of select="normalize-space($text)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


</xsl:stylesheet>