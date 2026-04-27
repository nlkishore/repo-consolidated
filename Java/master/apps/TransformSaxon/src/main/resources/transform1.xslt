<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs">

    <!-- Declare the parameter -->
    <xsl:param name="upperCaseText" as="xs:string"/>

    <!-- Declare a template to match the root element -->
    <xsl:template match="/">
        <output>
            <xsl:apply-templates/>
        </output>
    </xsl:template>

    <!-- Template to process the elements -->
    <xsl:template match="text">
        <result>
            <!-- Use the parameter value -->
            <xsl:value-of select="$upperCaseText"/>
        </result>
    </xsl:template>
</xsl:stylesheet>
