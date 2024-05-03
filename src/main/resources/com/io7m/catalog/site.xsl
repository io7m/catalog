<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:si="urn:com.io7m.site:1.0"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:c="urn:com.io7m.catalog:1"
                xmlns:ext="urn:com.io7m.catalog:extensions"
                exclude-result-prefixes="#all"
                xmlns="http://www.w3.org/1999/xhtml"
                version="2.0">

  <xsl:template match="si:Software">
    <xsl:for-each select="si:Package">
      <xsl:sort select="@name"/>
      <xsl:element name="c:Work">
        <xsl:attribute name="ID">
          <xsl:value-of select="ext:uuidHashOf(@name)"/>
        </xsl:attribute>
        <xsl:attribute name="Name">
          <xsl:value-of select="@name"/>
        </xsl:attribute>

        <xsl:if test="@status='ACTIVE'">
          <xsl:element name="c:GroupAssignment">
            <xsl:attribute name="Name">com.io7m.core</xsl:attribute>
          </xsl:element>
        </xsl:if>
        <xsl:if test="@status='INACTIVE'">
          <xsl:element name="c:GroupAssignment">
            <xsl:attribute name="Name">com.io7m.eol</xsl:attribute>
          </xsl:element>
        </xsl:if>

        <xsl:element name="dc:title">
          <xsl:value-of select="@name"/>
        </xsl:element>
        <xsl:element name="dc:description">
          <xsl:value-of select="@description"/>
        </xsl:element>
        <dc:rights>ISC License</dc:rights>
        <dc:type>Software</dc:type>
        <xsl:element name="c:Meta">
          <xsl:attribute name="Name">site</xsl:attribute>
          <xsl:value-of select="@site"/>
        </xsl:element>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="si:Site">
    <xsl:result-document href="out.xml"
                         indent="no"
                         exclude-result-prefixes="#all"
                         method="xml">
      <c:Works>
        <xsl:apply-templates select="si:Software"/>
      </c:Works>
    </xsl:result-document>
  </xsl:template>

</xsl:stylesheet>