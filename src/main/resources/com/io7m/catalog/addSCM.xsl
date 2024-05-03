<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:c="urn:com.io7m.catalog:1"
                exclude-result-prefixes="#all"
                xmlns="http://www.w3.org/1999/xhtml"
                version="2.0">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="c:Meta[@Name='site']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>

    <xsl:if test="../c:GroupAssignment[@Name='com.io7m.core']">
      <xsl:element name="c:Meta">
        <xsl:attribute name="Name">scm_source</xsl:attribute>
        <xsl:value-of select="concat('https://www.github.com/io7m-com/',../@Name)"/>
      </xsl:element>
    </xsl:if>

    <xsl:if test="../c:GroupAssignment[@Name='com.io7m.eol']">
      <xsl:element name="c:Meta">
        <xsl:attribute name="Name">scm_source</xsl:attribute>
        <xsl:value-of select="concat('https://www.github.com/io7m/',../@Name)"/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>