#!/bin/sh -ex

sed -i 's@schemaLocation="http://www.w3.org/2009/01/xml.xsd"@schemaLocation="xml_2009_01.xsd"@' *.xsd
sed -i 's@schemaLocation="[^"]\+/\([^"]\+\)"@schemaLocation="\1"@' *.xsd
