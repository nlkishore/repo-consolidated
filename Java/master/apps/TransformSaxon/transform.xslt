<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:str="java:org.example.StringUtils">

    <!-- Declare a template to match the root element -->
    <xsl:template match="/">
        <output>
            <xsl:apply-templates/>
        </output>
    </xsl:template>

    <!-- Template to process the elements -->
    <xsl:template match="text">
        <result>
            <xsl:value-of select="str:toUpperCase(.)"/>
        </result>
    </xsl:template>
</xsl:stylesheet>
