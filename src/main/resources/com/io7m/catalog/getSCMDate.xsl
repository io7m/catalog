<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:c="urn:com.io7m.catalog:1"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:ext="urn:com.io7m.catalog:extensions"
                exclude-result-prefixes="#all"
                xmlns="http://www.w3.org/1999/xhtml"
                version="2.0">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="c:Meta[@Name='scm_source']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>

    <xsl:element name="dc:date">
      <xsl:value-of select="ext:scmDateOf(.)"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>